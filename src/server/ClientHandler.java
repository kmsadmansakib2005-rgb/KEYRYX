package server;

import model.Message;
import model.User;
import security.AuthenticationService;
import security.Session;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final int clientCount;
    private PrintWriter writer;
    private User user;
    private String sessionToken;
    private Session.Role role;

    public ClientHandler(Socket socket, int clientCount) {
        this.socket = socket;
        this.clientCount = clientCount;
    }

    @Override
    public void run() {
        System.out.println("Client Handler for client-" + clientCount + " has started.");

        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            writer = new PrintWriter(
                    new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

            String request = reader.readLine();
            if (!authenticateConnection(request)) {
                return;
            }

            String username = user.getUserName();
            writer.println("Welcome- " + username);

            ArrayList<String> history = ChatHistory.getUserHistory(username);
            for (String oldMessage : history) {
                writer.println("HISTORY|" + oldMessage);
            }
            Server.broadcastUserList();

            String message;
            while ((message = reader.readLine()) != null) {
                Session activeSession = AuthenticationService.authenticate(sessionToken);
                if (activeSession == null ||
                        !activeSession.getUsername().equals(username)) {
                    writer.println("AUTH_EXPIRED");
                    break;
                }

                if ("LOGOUT".equals(message)) {
                    break;
                }

                if (message.startsWith("APPROVE|")) {
                    if (role != Session.Role.ADMIN) {
                        writer.println("APPROVE_RESULT|DENIED");
                        continue;
                    }

                    String targetUsername = message.substring("APPROVE|".length()).trim();
                    boolean approved = AuthenticationService.approveUser(
                            sessionToken, targetUsername);
                    writer.println("APPROVE_RESULT|" +
                            (approved ? "SUCCESS" : "FAILED") + "|" + targetUsername);
                    continue;
                }

                String[] parts = message.split("\\|", 2);
                if (parts.length < 2 || parts[0].trim().isEmpty()
                        || parts[1].trim().isEmpty()) {
                    continue;
                }

                String recipient = parts[0].trim();
                String content = parts[1];

                Message msg = new Message(
                        username,
                        recipient,
                        content,
                        new java.util.Date().toString()
                );
                ChatHistory.saveMessage(msg);

                System.out.println(msg.getTimeStamp() + " | " +
                        msg.getSender() + " says: " + msg.getContent());

                if ("ALL".equals(msg.getReceiver())) {
                    String groupMessage = "GROUP|" +
                            msg.getTimeStamp() + " | \n" +
                            msg.getSender() + ": " + msg.getContent();
                    Server.broadcastMessage(groupMessage);
                } else {
                    String privateMessage = "PRIVATE|" +
                            msg.getSender() + "|" +
                            msg.getTimeStamp() + " | \n" +
                            msg.getSender() + ": " + msg.getContent();

                    Server.sendMessage(msg.getReceiver(), privateMessage);

                    if (!msg.getSender().equals(msg.getReceiver())) {
                        String senderCopy = "PRIVATE|" +
                                msg.getReceiver() + "|" +
                                msg.getTimeStamp() + " | \n" +
                                msg.getSender() + ": " + msg.getContent();
                        Server.sendMessage(msg.getSender(), senderCopy);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Connection error with client: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Authentication or chat error: " + e.getMessage());
            if (writer != null) writer.println("SERVER_ERROR");
        } finally {
            if (sessionToken != null) {
                AuthenticationService.logout(sessionToken);
            }
            if (user != null) {
                user.setOnline(false);
            }
            // Remove failed-login handlers too; Server registers the socket before authentication.
            Server.removeClient(this);
            if (user != null) {
                Server.broadcastUserList();
                System.out.println(user.getUserName() + " disconnected!");
            }
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }

    private boolean authenticateConnection(String request) throws Exception {
        if (request == null) {
            return false;
        }

        String[] fields = request.split("\\|", 3);
        if (fields.length != 3) {
            writer.println("AUTH_FAILED");
            return false;
        }

        Session session;
        if ("LOGIN".equals(fields[0])) {
            session = AuthenticationService.loginUser(fields[1], fields[2]);
        } else if ("ADMIN_LOGIN".equals(fields[0])) {
            session = AuthenticationService.loginAdmin(fields[1], fields[2]);
        } else if ("REGISTER".equals(fields[0])) {
            boolean registered = security.AuthManager.register(fields[1], fields[2]);
            writer.println(registered ? "REGISTERED_PENDING" : "REGISTER_FAILED");
            return false;
        } else {
            writer.println("AUTH_FAILED");
            return false;
        }

        if (session == null) {
            writer.println("AUTH_FAILED");
            return false;
        }

        String username = session.getUsername();
        if (Server.findCleint(username) != null) {
            AuthenticationService.logout(session.getToken());
            writer.println("ALREADY_ONLINE");
            return false;
        }

        sessionToken = session.getToken();
        role = session.getRole();
        user = new User(username, true);
        writer.println("AUTH_OK|" + sessionToken + "|" + username + "|" + role.name());
        return true;
    }

    public void sendMessage(String message) {
        if (writer != null) writer.println(message);
    }

    public User getUser() {
        return user;
    }
}
