package client;
import java.io.*;
import java.net.*;

public class ClientConnection {
    private Socket socket;
    private PrintWriter writer;
    public ClientConnection() throws IOException
    {
        socket= new Socket("localHost", 1000);
        writer= new PrintWriter(socket.getOutputStream(), true);
    }

    public Socket getSocket()
    {
        return socket;
    }

    public void sendMessage(String message)
    {
        writer.println(message);
    }

}
