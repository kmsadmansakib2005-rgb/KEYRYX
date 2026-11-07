package client;

import java.io.*;
import java.net.Socket;

public class FileSender {

    public boolean sendFile(
            String serverAddress,
            String username,
            String sessionToken,
            String recipient,
            File file) {
        try (Socket socket = new Socket(serverAddress, 1001);
             DataInputStream input = new DataInputStream(socket.getInputStream());
             DataOutputStream output = new DataOutputStream(socket.getOutputStream());
             FileInputStream fileInput = new FileInputStream(file)) {

            output.writeUTF("SEND");
            output.writeUTF(sessionToken);
            output.writeUTF(username);
            output.writeUTF(recipient);
            output.writeUTF(file.getName());
            output.writeLong(file.length());
            output.flush();

            String response = input.readUTF();
            if (!"OK".equals(response)) {
                System.out.println("File transfer rejected: " + response);
                return false;
            }

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fileInput.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
            output.flush();

            System.out.println("File sent: " + file.getName());
            return true;
        } catch (IOException e) {
            System.out.println("File sending failed: " + e.getMessage());
            return false;
        }
    }
}
