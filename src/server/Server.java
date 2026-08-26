package server;
import java.net.*;
import java.io.*;
import java.util.*;

public class Server {

    public static void main(String[] args)
    {
        try
        {
            ServerSocket serverSocket= new ServerSocket(3000);
            System.out.println("Server has Started!");
            System.out.println("Waiting for the client....");

            int clientCount=0;

            while(true)
            {
                Socket socket= serverSocket.accept();
                clientCount++;
                System.out.println("Client- "+ clientCount+ " has joined!");

                ClientHandler clientHandler= new ClientHandler(socket, clientCount);

                Thread thread= new Thread(clientHandler);
                thread.start();
            }

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

}