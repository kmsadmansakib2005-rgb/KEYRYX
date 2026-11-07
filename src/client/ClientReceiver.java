package client;

import java.io.BufferedReader;
import java.io.IOException;

public class ClientReceiver implements Runnable {

    private final BufferedReader reader;

    public ClientReceiver(BufferedReader reader) {
        this.reader = reader;
    }

    @Override
    public void run() {
        try {
            String message;
            while ((message = reader.readLine()) != null) {
                System.out.println("Server: " + message);
                if ("AUTH_EXPIRED".equals(message)) {
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("Disconnected from Server!");
        }
    }
}
