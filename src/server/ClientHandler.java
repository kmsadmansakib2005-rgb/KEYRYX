package server;
import model.Message;
import model.User;
import java.net.*;
import java.io.*;

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

            String username= reader.readLine();
            user= new User(username, true);
            writer= new PrintWriter(socket.getOutputStream(),
                    true);

            writer.println("Welcome- "+ user.getUserName());
            while(true)
            {
                String message= reader.readLine();

                String[] parts= message.split("\\|", 2);
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

                if(msg.getReceiver().equals("ALL"))
                {
                    Server.broadcastMessage(msg.getTimeStamp()+" | \n"+msg.getSender()+
                            " : "+ msg.getContent());
                }
                else
                {
                    Server.sendMessage(msg.getReceiver(),msg.getTimeStamp()+" | \n"+
                            msg.getSender()+ ": "+msg.getContent());
                }
            }

        }
        catch(IOException e)
        {
            if(user!=null)
            {
                user.setOnline(false);
                Server.removeClient(this);
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