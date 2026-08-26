package client;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

public class ClientReceiver implements Runnable {

    private Socket socket;

    public ClientReceiver(Socket socket)
    {
        this.socket= socket;
    }

    public void run()
    {
        try
        {
            BufferedReader reader= new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            while(true)
            {
                String message= reader.readLine();
                System.out.println("Server: "+message);
            }
        }
        catch(IOException e)
        {
            System.out.println("Disconnected from Server!");
        }
    }
}