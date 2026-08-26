package server;


import java.net.*;
import java.io.*;

public class ClientHandler implements Runnable {

    private Socket socket;
    private int clientCount;

    public ClientHandler(Socket socket, int clientCount)
    {
        this.socket= socket;
        this.clientCount= clientCount;
    }
    public void run()
    {
        System.out.println("Client Handler for cleint-"+ clientCount+
                " has started.");
        try
        {
            BufferedReader reader= new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            PrintWriter writer= new PrintWriter(socket.getOutputStream(),
                    true);

            writer.println("Welcome Cleint- "+ clientCount);
            while(true)
            {
                String message= reader.readLine();
                System.out.println("Client-"+ clientCount+ " says: "+ message);
            }

        }
        catch(IOException e)
        {
            System.out.println("Client- "+ clientCount+ " disconnected!");
        }
    }
}