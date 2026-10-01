package server;
import java.net.*;
import java.io.*;
import java.util.*;

public class Server {
    private static ArrayList<ClientHandler> clients= new ArrayList<>();

    public static void main(String[] args)
    {
        try
        {
            ServerSocket serverSocket= new ServerSocket(1000);
            System.out.println("Server has Started!");
            ChatHistory.readHistory();
            System.out.println("Waiting for the client....");

            int clientCount=0;

            while(true)
            {
                Socket socket= serverSocket.accept();
                clientCount++;
                System.out.println("Client- "+clientCount+ " has joined!");

                ClientHandler clientHandler= new ClientHandler(socket, clientCount);
                clients.add(clientHandler);

                Thread thread= new Thread(clientHandler);
                thread.start();

            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void broadcastMessage(String message)
    {
        for(ClientHandler client: clients)
        {
            client.sendMessage(message);
        }
    }

    public static void broadcastUserList()
    {
        StringBuilder userList= new StringBuilder("USERS|");

        for(ClientHandler client: clients )
        {
            if(client.getUser()!=null &&client.getUser().isOnline())
            {
                userList.append(client.getUser().getUserName()).append(",");
            }
        }

        for(ClientHandler client: clients)
        {
            client.sendMessage(userList.toString());
        }
    }

    public static void removeClient(ClientHandler client)
    {
        clients.remove(client);
    }

    public static ClientHandler findCleint(String username)
    {
        for(ClientHandler client: clients)
        {
            if(client.getUser()!=null && client.getUser().getUserName().equals(username))
            {
                return client;
            }
        }
        return null;
    }

    public static void sendMessage(String username, String message)
    {
        ClientHandler client = findCleint(username);
        if(client!=null)
        {
            client.sendMessage(message);
        }
    }
}