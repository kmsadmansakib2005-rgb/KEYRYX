package client;

import java.net.*;
import java.io.*;
import java.util.*;

public class Client {
    public static void main(String[] args)
    {
        try
        {
            Socket socket= new Socket("localhost", 3000);

            ClientReceiver receiver= new ClientReceiver(socket);
            Thread receiverThread= new Thread(receiver);
            receiverThread.start();

            PrintWriter writer= new PrintWriter(socket.getOutputStream(),
                    true);

            Scanner sc= new Scanner(System.in);
            System.out.println("Enter message: ");

            while(true)
            {
                String message= sc.nextLine();

                writer.println(message);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}