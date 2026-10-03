package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;
import java.awt.Desktop;
import java.io.File;

public class FileTransferClient {

    public void connectAndListen(
            String serverAddress,
            String username,
            Consumer<String> fileReceivedCallback) {
        try (Socket socket = new Socket(serverAddress, 1001);
             DataInputStream input = new DataInputStream(
                     socket.getInputStream());
             DataOutputStream output = new DataOutputStream(
                     socket.getOutputStream())) {

            // Register this user with the file-transfer server.
            output.writeUTF("REGISTER");
            output.writeUTF(username);
            output.flush();

            System.out.println(username + " is ready to receive files.");

            // Listen for incoming file transfers.
            while (true) {
                String command = input.readUTF();

                if (command.equals("FILE")) {
                    String fileName = input.readUTF();
                    long fileSize = input.readLong();

                    Path directory = Paths.get("received_files");
                    Files.createDirectories(directory);

                    Path filePath = directory.resolve(
                            Paths.get(fileName).getFileName().toString());

                    try (OutputStream fileOutput =
                                 Files.newOutputStream(filePath)) {

                        byte[] buffer = new byte[8192];
                        long remaining = fileSize;

                        while (remaining > 0) {
                            int bytesToRead = (int) Math.min(
                                    buffer.length, remaining);

                            int bytesRead = input.read(
                                    buffer, 0, bytesToRead);

                            if (bytesRead == -1) {
                                throw new EOFException(
                                        "File reception interrupted!");
                            }

                            fileOutput.write(buffer, 0, bytesRead);
                            remaining -= bytesRead;
                        }
                    }

                    System.out.println("File received: " + fileName);
                    fileReceivedCallback.accept("File received: "+ fileName);
                }
            }

        } catch (IOException e) {
            System.out.println("File receiver disconnected: "
                    + e.getMessage());
        }
    }

}
