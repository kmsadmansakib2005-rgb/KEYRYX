package client;

import java.net.*;
import java.io.*;
import java.util.*;

public class Client {
    public static void main(String[] args)
    {
        try
        {
            Scanner sc= new Scanner(System.in);
            System.out.println("Enter your username: ");
            String username= sc.nextLine();


            //Socket socket= new Socket("localhost", 1000);
            ClientConnection clientConnection= new ClientConnection();
            Socket socket= clientConnection.getSocket();

            ClientReceiver receiver= new ClientReceiver(socket);
            Thread receiverThread= new Thread(receiver);
            receiverThread.start();

            clientConnection.sendMessage(username);

         //   PrintWriter writer= new PrintWriter(socket.getOutputStream(),true);

            System.out.println("Enter message: ");

            while(true)
            {
                String message= sc.nextLine();

                clientConnection.sendMessage(message);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}