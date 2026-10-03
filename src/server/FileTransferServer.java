package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FileTransferServer implements Runnable {

    public static final int PORT= 1001;
    private final Map<String, FileTransferHandler> connectedUsers =
            new ConcurrentHashMap<>();

    public void registerUser(String username, FileTransferHandler handler) {
        connectedUsers.put(username, handler);
    }

    public void removeUser(String username) {
        if (username != null) {
            connectedUsers.remove(username);
        }
    }

    public FileTransferHandler findUser(String username) {
        return connectedUsers.get(username);
    }
    @Override
    public void run() {
        try(ServerSocket serverSocket= new ServerSocket(PORT))
        {
            System.out.println("KEYRYX File Transfer Server started at port "+PORT);

            while(true)
            {
                Socket socket= serverSocket.accept();
                System.out.println("New File Transfer connection has started!");
                Thread transferThread= new Thread(
                        new FileTransferHandler(socket,this));
                transferThread.start();
                //socket.close();
            }
        }
        catch(IOException e)
        {
            System.out.println("File Transfer Server could not start: "
                    + e.getMessage());
        }
    }
}