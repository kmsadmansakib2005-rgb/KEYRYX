package client;

import java.io.*;
import java.net.*;

public class FileSender {
        public boolean sendFile(String serverAdress, String recipient , File file)
        {
            try (Socket socket = new Socket(serverAdress, 1001);
                 DataOutputStream output = new DataOutputStream(
                         socket.getOutputStream());
                 FileInputStream input = new FileInputStream(file)) {

                output.writeUTF("SEND");
                output.writeUTF(recipient);
                output.writeUTF(file.getName());
                output.writeLong(file.length());

                byte[] buffer= new byte[8192];
                int bytesRead;

                while((bytesRead= input.read(buffer))!=-1)
                {
                    output.write(buffer, 0, bytesRead);
                }
                output.flush();
                System.out.println("File sent: "+file.getName());
                return true;
            }
             catch (IOException e) {
                 System.out.println("File sending failed!"+ e.getMessage());
                 return false;
            }

        }
}
