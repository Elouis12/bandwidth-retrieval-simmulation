import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;

// Client class
class Client implements Runnable {

    private final Random random = new Random();
    private final Socket clientSocket;
    PrintWriter writeMessage;
    BufferedReader getMessage;

    Client(Socket socket) throws IOException {

        this.clientSocket = socket;
    }


    @Override
    public void run() {

        // establish a connection by providing host and port number
        try {

            // writing to server
            writeMessage = new PrintWriter( this.clientSocket.getOutputStream(), true);

            // reading from server
            getMessage  = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));

            int destination;

            System.out.println( "\u001B[33m" + "PRESS ENTER TO EXIT" + "\u001B[0m" + "\n\n");

            System.out.println("\u001B[42m" + "       " + "\u001B[0m" + " - DESTINATION IN CACHE");
            System.out.println("\u001B[45m" + "       " + "\u001B[0m" + " - DESTINATION NOT IN CACHE \n\n\n");

            while( true ){

                // how long before the client makes another request for a destination
                int timeBeforeRequest = random.nextInt(2000-1500) + 500;
                Thread.sleep( timeBeforeRequest );

                destination = random.nextInt(100) + 1;

                String message = destination + " " + timeBeforeRequest;

                writeMessage.println( message ); // send destination and time it took to get to server

                try{

                    String messageFromServer = getMessage.readLine();
                    System.out.println( messageFromServer );

                }catch (Exception e){

                    System.out.println( "CLIENT(s) CLOSED BY SERVER");

                    System.exit(0);
                }

            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) throws IOException {

        Socket socket = new Socket("localhost", 1234);

        Client client = new Client(socket);
        Thread clientThread = new Thread(client);
        clientThread.start();

        Scanner input = new Scanner(System.in);
        String exit = input.nextLine();

        while( exit.isEmpty() || client.getMessage.readLine().isEmpty() ){

            client.writeMessage.println(exit);
//            System.out.println( client.getMessage.readLine() );

                System.out.println( "CLIENT CLOSED");
                try {
                    socket.close();
                }catch (IOException e){

                }finally {

                    System.exit(0);

                }

        }
    }

}