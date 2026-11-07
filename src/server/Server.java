package server;

import security.AdminAuthManager;

import java.net.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Server {
    private static ArrayList<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) {
        try {
            ensureAdminAccount();

            Thread fileServerThread = new Thread(new FileTransferServer());
            fileServerThread.start();

            ServerSocket serverSocket = new ServerSocket(1000);
            System.out.println("Server has Started!");
            ChatHistory.readHistory();
            System.out.println("Waiting for the client....");

            int clientCount = 0;

            while (true) {
                Socket socket = serverSocket.accept();
                clientCount++;
                System.out.println("Client- " + clientCount + " has joined!");

                ClientHandler clientHandler = new ClientHandler(socket, clientCount);
                clients.add(clientHandler);

                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (Exception e) {
            System.out.println("Server could not start: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Create the first administrator on the server machine, once only.
    private static void ensureAdminAccount() throws Exception {
        if (Files.exists(Path.of("admin.db"))) {
            return;
        }

        System.out.println("No administrator account was found. Create the first KEYRYX administrator.");
        Scanner scanner = new Scanner(System.in);

        System.out.print("Admin username (3-20 letters, numbers or underscores): ");
        String adminUsername = scanner.nextLine().trim();

        String adminPassword;
        Console console = System.console();
        if (console != null) {
            adminPassword = new String(console.readPassword("Admin password (at least 12 characters): "));
        } else {
            System.out.print("Admin password (at least 12 characters): ");
            adminPassword = scanner.nextLine();
        }

        if (!AdminAuthManager.initializeAdmin(adminUsername, adminPassword)) {
            throw new IllegalStateException(
                    "Could not create the administrator. Check the username and password requirements.");
        }

        System.out.println("Administrator account created successfully.");
    }

    public static void broadcastMessage(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public static void broadcastUserList() {
        StringBuilder userList = new StringBuilder("USERS|");

        for (ClientHandler client : clients) {
            if (client.getUser() != null && client.getUser().isOnline()) {
                userList.append(client.getUser().getUserName()).append(",");
            }
        }

        for (ClientHandler client : clients) {
            client.sendMessage(userList.toString());
        }
    }

    public static void removeClient(ClientHandler client) {
        clients.remove(client);
    }

    public static ClientHandler findCleint(String username) {
        for (ClientHandler client : clients) {
            if (client.getUser() != null &&
                    client.getUser().getUserName().equals(username)) {
                return client;
            }
        }
        return null;
    }

    public static void sendMessage(String username, String message) {
        ClientHandler client = findCleint(username);
        if (client != null) {
            client.sendMessage(message);
        }
    }
}
