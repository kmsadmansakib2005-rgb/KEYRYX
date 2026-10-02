package server;
import model.Message;
import java.io.*;
import java.util.ArrayList;

public class ChatHistory {
    private static final String FILE_NAME= "chat_history.txt";

    public static void saveMessage(Message msg)
    {
        try {
            FileWriter writer= new FileWriter(FILE_NAME, true);

            writer.write(
                    msg.getTimeStamp()+" |"+ msg.getSender()+ " ->"+
                            msg.getReceiver()+" :"+ msg.getContent()+"\n"
            );
            writer.close();
        }

        catch (IOException e) {
            System.out.println("Could not save chat history");
        }

    }
    public static void readHistory()
    {
        try{
            BufferedReader reader= new BufferedReader(new FileReader(FILE_NAME));

            String line= reader.readLine();

            while(line != null)
            {
                System.out.println(line);
                line= reader.readLine();
            }
            reader.close();
        }
        catch (IOException e) {

            System.out.println("Could not read chat history");
        }
    }

    public static ArrayList<String> getUserHistory(String username)
    {
        ArrayList<String> history= new ArrayList<>();

        try {
            BufferedReader reader= new BufferedReader(new FileReader(FILE_NAME));
            String line;

            while((line=reader.readLine())!=null)
            {
                if(line.contains("|"+username+" ->")||
                        line.contains("->"+ username+ " :"))
                {
                    history.add(line);
                }
            }
            reader.close();
        }

        catch (IOException e) {
            System.out.println("Could not retreive user history");
        }

        return history;
    }
}