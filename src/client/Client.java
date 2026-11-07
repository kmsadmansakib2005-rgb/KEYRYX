package client;

import java.io.IOException;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();

            System.out.print("Password: ");
            String password = scanner.nextLine();

            ClientConnection connection = new ClientConnection();
            connection.sendMessage("LOGIN|" + username + "|" + password);

            String response = connection.readLine();
            if (response == null || !response.startsWith("AUTH_OK|")) {
                System.out.println("Login failed. Check credentials and administrator approval.");
                connection.close();
                return;
            }

            String welcome = connection.readLine();
            System.out.println(welcome);

            ClientReceiver receiver = new ClientReceiver(connection.getReader());
            Thread receiverThread = new Thread(receiver, "KEYRYX-console-receiver");
            receiverThread.setDaemon(true);
            receiverThread.start();

            System.out.println("Enter messages as recipient|message. Use ALL|message for group chat.");
            System.out.println("Type /logout to disconnect.");

            while (true) {
                String message = scanner.nextLine();
                if ("/logout".equalsIgnoreCase(message.trim())) {
                    connection.sendMessage("LOGOUT");
                    break;
                }
                connection.sendMessage(message);
            }

            connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
