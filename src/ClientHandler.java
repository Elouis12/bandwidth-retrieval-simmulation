import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.Semaphore;


// DecisionThread class
public class ClientHandler implements Runnable {

    private final Semaphore cacheSemaphore;
    private final BandwidthCache bandwidthCache;
    final Socket clientSocket;
    private final Random random;
    private final int clientId;
    private final Color color;
    private final String clientColor;
    PrintWriter clientOutput = null; // to send to the client
    BufferedReader clientInput = null; // to read from the client

    public ClientHandler(Socket socket, BandwidthCache bandwidthCache, Semaphore cacheSemaphore, Color color) {

        this.clientSocket = socket;
        this.cacheSemaphore = cacheSemaphore;
        this.bandwidthCache = bandwidthCache;
        this.color = color;
        this.clientColor = this.color.getColor();

        this.random = new Random();
        this.clientId = random.nextInt(20000-10000)+10000;


        // Displaying that new client is connected to server
        System.out.println( this.clientColor + "CLIENT @" + this.clientId + this.color.resetColor() + " CONNECTED");

    }

    public void run() {


        StringBuilder clientMessage;
        String messageFromClient;

        while( true ){

            try {

                // get permit to access the cache to make a destination look up
                cacheSemaphore.acquire();

                // get the output stream of client
                clientOutput = new PrintWriter( clientSocket.getOutputStream(), true );

                // get the input stream of client
                clientInput = new BufferedReader( new InputStreamReader( clientSocket.getInputStream() ) );

                // appends the messages to send to the client about the destination in the cache
                clientMessage = new StringBuilder();


                // get the message form the client
                messageFromClient = clientInput.readLine();

                // if it is the case that the user closed the connection
                if( messageFromClient.isEmpty() ){

                    throw new Exception();
                }
                int address = Integer.parseInt( messageFromClient.split(" ")[0] );
                int timeForRequest = Integer.parseInt( messageFromClient.split(" ")[1] );

                String threadInfo = " [ @ " + timeForRequest/1000d + "s ";

                System.out.println( this.clientColor + "\tCLIENT @" + this.clientId + this.color.resetColor() + " : Requesting destination " + address );


// CASE WHERE THE CACHE HOLDS THE DESTINATION
                if( !bandwidthCache.getCache().containsKey( address ) ){

                    // release the semaphore so it make a bandwidth calculation
                    // so another thread can access it
                    cacheSemaphore.release();

                    // calculate the bandwidth
                    int bandwidth = random.nextInt(10) + 1;

                    // simulates the time it took to ping the destination
                    Thread.sleep( bandwidth );

                    // once it simulates the ping, get permit to add entry to cache
                    cacheSemaphore.acquire();

                    clientMessage.append("\u001B[45m" + "  " + "\u001B[0m" + this.clientColor + " CLIENT " + this.clientId + this.color.resetColor() ).append(threadInfo).append("X ").append(address).append(" ] ");

                    StringBuilder cacheActionMessage = bandwidthCache.addToCache(address, bandwidth);

                    clientMessage.append(cacheActionMessage);
                    clientMessage.append( "\n" + this.bandwidthCache.getCache() + "\n"   );

                    // send message to client about requested destination's access to the cache
                    clientOutput.println( clientMessage );

                    // release the permit so other threads can access it
                    cacheSemaphore.release();


// THE CASE WHERE THE DESTINATION WAS IN THE CACHE AND THUS IT CAN RETURN TO IT'S DESTINATION PROCESS
                }else{

                    clientMessage.append( "\u001B[42m" + "  " + "\u001B[0m" + this.clientColor + " CLIENT " + this.clientId + this.color.resetColor() + threadInfo + "@ " + address + " ] " );
                    clientMessage.append( "\n" + this.bandwidthCache.getCache() + "\n" );

                    // send message to client about requested destination's access to the cache
                    clientOutput.println( clientMessage );

                    // release the permit so other threads can access it
                    cacheSemaphore.release();

                }



            } catch (Exception e) {

                System.out.println( this.clientColor + "CLIENT @" + this.clientId + this.color.resetColor() + " DISCONNECTED");
                clientOutput.println(this.clientColor + "CLIENT @" + this.clientId + this.color.resetColor() + " CLOSED CONNECTION");
                // release lock on cache when closing connection
                cacheSemaphore.release();
                try {
                    clientSocket.close();
                } catch (IOException ioException) {
                }
                break;
            }

        }

    }
}
