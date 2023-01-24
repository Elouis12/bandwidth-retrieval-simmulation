import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

// Server class
class Server implements Runnable {

    Semaphore cacheSemaphore = new Semaphore(1);
    Semaphore clientSemaphore = new Semaphore(10);
    BandwidthCache bandwidthCache = new BandwidthCache();
    ClientHandler clientSocket;
    Color color = new Color();
    List<ClientHandler> clients = new ArrayList<>();

    ServerSocket server = null;

    @Override
    public void run() {

        try {

            // server is listening on port 1234
            server = new ServerSocket(1234);
            server.setReuseAddress(true);

            // running infinite loop for getting client request
            while (true) {

                // socket object to receive incoming client requests
                Socket client = server.accept();

                // create a new thread object
                clientSocket = new ClientHandler(client, bandwidthCache, cacheSemaphore, color);
                clients.add(clientSocket);

                // This thread will handle the client and determine availability of the cache separately
                Thread clientHandlerThread = new Thread(clientSocket);
                clientHandlerThread.start();

            }

        } catch (IOException e) {

            e.printStackTrace();

        } finally {

            if (server != null) {

                try {
                    server.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {

        System.out.println( "\u001B[33m" + "PRESS ENTER TO EXIT" + "\u001B[0m" + "\n\n");

        Server server = new Server();
        Thread serverThread = new Thread(server);
        serverThread.start();

        Scanner input = new Scanner(System.in);
        String exit = input.nextLine();

        while( exit.isEmpty() ){

            System.out.println("SERVER CLOSED");
            try {

                // iterate through all client connections
                for (ClientHandler client : server.clients){

                    if( client.clientSocket.isConnected() ){

                        // send message to client to exit
                        client.clientOutput.println( exit );

                        // close the connection
                        client.clientSocket.close();
                    }

                }
                // close server connection
                server.server.close();

            }catch (IOException e){

                System.exit(0);

            }finally {

                System.exit(0);

            }
        }
    }
}