package client;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientConnection {
    private final Socket socket;
    private final PrintWriter writer;
    private final BufferedReader reader;

    public ClientConnection() throws IOException {
        socket = new Socket("localhost", 1000);
        writer = new PrintWriter(
                new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
        reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
    }

    public Socket getSocket() {
        return socket;
    }

    public BufferedReader getReader() {
        return reader;
    }

    public String readLine() throws IOException {
        return reader.readLine();
    }

    public void sendMessage(String message) {
        writer.println(message);
    }

    public void close() throws IOException {
        socket.close();
    }
}
