package server;

import security.AuthenticationService;
import security.Session;

import java.io.*;
import java.net.Socket;

public class FileTransferHandler implements Runnable {

    private final Socket socket;
    private final FileTransferServer transferServer;
    private String userName;
    private String sessionToken;
    private DataOutputStream output;

    public FileTransferHandler(Socket socket, FileTransferServer transferServer) {
        this.socket = socket;
        this.transferServer = transferServer;
    }

    @Override
    public void run() {
        try (DataInputStream input = new DataInputStream(socket.getInputStream())) {
            output = new DataOutputStream(socket.getOutputStream());
            String command = input.readUTF();

            if ("REGISTER".equals(command)) {
                String token = input.readUTF();
                String requestedUsername = input.readUTF();
                Session session = AuthenticationService.authenticate(token);

                if (!validSession(session, requestedUsername)) {
                    output.writeUTF("AUTH_FAILED");
                    output.flush();
                    return;
                }

                userName = session.getUsername();
                sessionToken = token;
                transferServer.registerUser(userName, this);
                output.writeUTF("OK");
                output.flush();
                System.out.println(userName + " registered for file transfer.");

                // Keep this authenticated receiving connection open.
                while (input.read() != -1) {
                    // Wait until the client closes the connection.
                }
            } else if ("SEND".equals(command)) {
                String token = input.readUTF();
                String senderUsername = input.readUTF();
                Session session = AuthenticationService.authenticate(token);

                if (!validSession(session, senderUsername)) {
                    output.writeUTF("AUTH_FAILED");
                    output.flush();
                    return;
                }

                String recipient = input.readUTF();
                String fileName = input.readUTF();
                long fileSize = input.readLong();

                if (fileSize < 0) {
                    output.writeUTF("INVALID_FILE");
                    output.flush();
                    return;
                }

                FileTransferHandler recipientHandler =
                        transferServer.findUser(recipient);

                if (recipientHandler == null || recipientHandler.output == null
                        || !recipientHandler.isSessionActive()) {
                    output.writeUTF("RECIPIENT_OFFLINE");
                    output.flush();
                    return;
                }

                output.writeUTF("OK");
                output.flush();

                synchronized (recipientHandler.output) {
                    recipientHandler.output.writeUTF("FILE");
                    recipientHandler.output.writeUTF(
                            new java.io.File(fileName).getName());
                    recipientHandler.output.writeLong(fileSize);

                    byte[] buffer = new byte[8192];
                    long remaining = fileSize;

                    while (remaining > 0) {
                        int bytesToRead = (int) Math.min(buffer.length, remaining);
                        int bytesRead = input.read(buffer, 0, bytesToRead);

                        if (bytesRead == -1) {
                            throw new EOFException("File transfer interrupted!");
                        }

                        recipientHandler.output.write(buffer, 0, bytesRead);
                        remaining -= bytesRead;
                    }
                    recipientHandler.output.flush();
                }

                System.out.println("File forwarded from " + senderUsername +
                        " to " + recipient);
            } else {
                output.writeUTF("INVALID_COMMAND");
                output.flush();
            }
        } catch (IOException e) {
            System.out.println("File transfer failed: " + e.getMessage());
        } finally {
            if (userName != null) {
                transferServer.removeUser(userName, this);
            }
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }

    private boolean isSessionActive() {
        Session session = AuthenticationService.authenticate(sessionToken);
        return validSession(session, userName);
    }

    private boolean validSession(Session session, String requestedUsername) {
        return session != null && requestedUsername != null
                && session.getUsername().equals(requestedUsername)
                && session.getRole() != null;
    }
}
