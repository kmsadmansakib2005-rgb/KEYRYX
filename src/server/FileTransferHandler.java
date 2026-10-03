package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;

public class FileTransferHandler implements Runnable {

    private final Socket socket;
    private final FileTransferServer transferServer;
    private String userName;
    private DataOutputStream output;

   public FileTransferHandler(Socket socket, FileTransferServer transferServer) {
       this.socket = socket;
       this.transferServer = transferServer;
   }

   @Override
    public void run() {
        try (DataInputStream input = new DataInputStream(
                socket.getInputStream())) {

            String command = input.readUTF();

            if (command.equals("REGISTER")) {
                userName = input.readUTF();

                output = new DataOutputStream(socket.getOutputStream());
                transferServer.registerUser(userName, this);

                System.out.println(userName + " registered for file transfer.");

                // Keep this connection open until the client disconnects.
                while (input.read() != -1) {
                    // Waiting for the connection to close.
                }
            }
            else if (command.equals("SEND")) {
                String recipient = input.readUTF();
                String fileName = input.readUTF();
                long fileSize = input.readLong();

                FileTransferHandler recipientHandler =
                        transferServer.findUser(recipient);

                if (recipientHandler == null) {
                    System.out.println("Recipient is not connected: " + recipient);
                    return;
                }

                synchronized (recipientHandler.output) {
                    recipientHandler.output.writeUTF("FILE");
                    recipientHandler.output.writeUTF(fileName);
                    recipientHandler.output.writeLong(fileSize);

                    byte[] buffer = new byte[8192];
                    long remaining = fileSize;

                    while (remaining > 0) {
                        int bytesToRead = (int) Math.min(
                                buffer.length, remaining);

                        int bytesRead = input.read(buffer, 0, bytesToRead);

                        if (bytesRead == -1) {
                            throw new EOFException("File transfer interrupted!");
                        }

                        recipientHandler.output.write(buffer, 0, bytesRead);
                        remaining -= bytesRead;
                    }

                    recipientHandler.output.flush();
                }

                System.out.println("File forwarded to " + recipient);
            }
        }
        catch (IOException e) {
            System.out.println("File transfer failed: " + e.getMessage());
        }
        finally {
            if (userName != null) {
                transferServer.removeUser(userName);
            }

            try {
                socket.close();
            }
            catch (IOException e) {
                System.out.println("Cannot close file connection!");
            }
        }
    }
}