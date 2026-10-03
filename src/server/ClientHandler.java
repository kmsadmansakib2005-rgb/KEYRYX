package server;
import model.Message;
import model.User;
import java.net.*;
import java.io.*;
import java.util.ArrayList;

public class ClientHandler implements Runnable {

    private Socket socket;
    private int clientCount;
    private PrintWriter writer;
    private User user;

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

            writer= new PrintWriter(socket.getOutputStream(),
                    true);

            String username= reader.readLine();

            if(username==null || username.trim().isEmpty()||
            Server.findCleint(username.trim())!=null)
            {
                writer.println("Login Failed");
                return;
            }

            //log in section
            username= username.trim();

            user= new User(username, true);
            writer.println("Welcome- "+ user.getUserName());

            ArrayList<String> history=ChatHistory.getUserHistory(username);
            for(String oldMessage: history)
            {
                writer.println("HISTORY|"+oldMessage);
            }

            Server.broadcastUserList();

            String message;
            while ((message = reader.readLine()) != null)
            {
                String[] parts= message.split("\\|", 2);

                if(parts.length<2 || parts[0].trim().isEmpty()||
                        parts[1].trim().isEmpty()) {
                    continue;
                }

                String reciepent= parts[0];
                String content= parts[1];

                Message msg=new Message(
                        user.getUserName(),
                        reciepent,
                        content,
                        new java.util.Date().toString() //time stamp
                );
                ChatHistory.saveMessage(msg);

                System.out.println(msg.getTimeStamp()+" | "+msg.getSender()+ " says: "+
                        msg.getContent());

                if (msg.getReceiver().equals("ALL"))
                {
                    String groupMessage = "GROUP|" +
                            msg.getTimeStamp() + " | \n" +
                            msg.getSender() + ": " + msg.getContent();

                    Server.broadcastMessage(groupMessage);
                }
                else
                {
                    String privateMessage = "PRIVATE|" +
                            msg.getSender() + "|" +
                            msg.getTimeStamp() + " | \n" +
                            msg.getSender() + ": " + msg.getContent();

                    Server.sendMessage(msg.getReceiver(), privateMessage);

                    if (!msg.getSender().equals(msg.getReceiver()))
                    {
                        String senderCopy = "PRIVATE|" +
                                msg.getReceiver() + "|" +
                                msg.getTimeStamp() + " | \n" +
                                msg.getSender() + ": " + msg.getContent();

                        Server.sendMessage(msg.getSender(), senderCopy);
                    }
                }
            }

        }
        catch(IOException e)
        {
            System.out.println("Connection error with client.");
        }
        finally
        {
            if(user != null)
            {
                user.setOnline(false);
                Server.removeClient(this);
                Server.broadcastUserList();

                System.out.println(user.getUserName() + " disconnected!");
            }
        }
    }
    public void sendMessage(String message)
    {
        writer.println(message);
    }

    public User getUser()
    {
        return user;
    }
}