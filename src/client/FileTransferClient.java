package client;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

public class FileTransferClient {

    public void connectAndListen(
            String serverAddress,
            String username,
            String sessionToken,
            Consumer<String> fileReceivedCallback) {
        try (Socket socket = new Socket(serverAddress, 1001);
             DataInputStream input = new DataInputStream(socket.getInputStream());
             DataOutputStream output = new DataOutputStream(socket.getOutputStream())) {

            output.writeUTF("REGISTER");
            output.writeUTF(sessionToken);
            output.writeUTF(username);
            output.flush();

            String response = input.readUTF();
            if (!"OK".equals(response)) {
                System.out.println("File receiver authentication failed.");
                return;
            }

            System.out.println(username + " is ready to receive files.");

            while (true) {
                String command = input.readUTF();

                if ("FILE".equals(command)) {
                    String fileName = input.readUTF();
                    long fileSize = input.readLong();
                    if (fileSize < 0) {
                        throw new IOException("Invalid file size.");
                    }

                    Path directory = Paths.get(
                            System.getProperty("user.home"),
                            "Documents",
                            "KEYRYX",
                            "received_files"
                    );
                    Files.createDirectories(directory);
                    Path filePath = directory.resolve(
                            Paths.get(fileName).getFileName().toString());

                    try (OutputStream fileOutput = Files.newOutputStream(filePath)) {
                        byte[] buffer = new byte[8192];
                        long remaining = fileSize;

                        while (remaining > 0) {
                            int bytesToRead = (int) Math.min(buffer.length, remaining);
                            int bytesRead = input.read(buffer, 0, bytesToRead);
                            if (bytesRead == -1) {
                                throw new EOFException("File reception interrupted!");
                            }
                            fileOutput.write(buffer, 0, bytesRead);
                            remaining -= bytesRead;
                        }
                    }

                    System.out.println("File received: " + fileName);
                    fileReceivedCallback.accept("File received: " + fileName);
                }
            }
        } catch (IOException e) {
            System.out.println("File receiver disconnected: " + e.getMessage());
        }
    }
}
