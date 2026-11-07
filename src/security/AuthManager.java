
package security;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.nio.charset.StandardCharsets;

public class AuthManager {

    private static final String USER_FILE = "users.db";

    // Register a new user
    public static synchronized boolean register(
            String username, String password) throws Exception {

        // Validate username
        if (username == null ||
                !username.matches("[a-zA-Z0-9_]{3,20}")) {
            return false;
        }

        // Validate password
        if (password == null || password.length() < 10) {
            return false;
        }

        // Check duplicate username
        if (findUser(username) != null) {
            return false;
        }

        // Generate salt and password hash
        byte[] salt = PasswordHasher.generateSalt();

        byte[] hash = PasswordHasher.hashPassword(
                password, salt
        );

        // Convert binary data into text
        String encodedSalt = Base64.getEncoder()
                .encodeToString(salt);

        String encodedHash = Base64.getEncoder()
                .encodeToString(hash);

        // New users require administrator approval
        String record = username + "|" +
                encodedSalt + "|" +
                encodedHash + "|PENDING";

        Files.writeString(
                Path.of(USER_FILE),
                record + System.lineSeparator(),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
        );

        return true;
    }

    // Find an existing account
    private static String[] findUser(String username)
            throws IOException {

        Path path = Path.of(USER_FILE);

        if (!Files.exists(path)) {
            return null;
        }

        for (String line : Files.readAllLines(path)) {

            String[] data = line.split("\\|", -1);

            if (data.length == 4 &&
                    data[0].equalsIgnoreCase(username)) {

                return data;
            }
        }

        return null;
    }

    // Authenticate an approved user
    public static synchronized boolean login(
            String username, String password) throws Exception {

        if (username == null || password == null) {
            return false;
        }

        String[] user = findUser(username);

        if (user == null) {
            return false;
        }

        // Check administrator approval
        if (!user[3].equals("APPROVED")) {
            return false;
        }

        byte[] salt = Base64.getDecoder()
                .decode(user[1]);

        byte[] storedHash = Base64.getDecoder()
                .decode(user[2]);

        return PasswordHasher.verifyPassword(
                password, salt, storedHash
        );
    }
    // Return the stored spelling so online identity checks are consistent.
    static synchronized String canonicalUsername(String username)
            throws IOException {
        String[] user = findUser(username);
        return user == null ? null : user[0];
    }

    // Package-private: approval must be requested through an authenticated admin session.
    static synchronized boolean approveUserInternal(String username)
            throws IOException {

        if (username == null) {
            return false;
        }

        Path path = Path.of(USER_FILE);

        if (!Files.exists(path)) {
            return false;
        }

        List<String> lines = Files.readAllLines(path);
        boolean approved = false;

        for (int i = 0; i < lines.size(); i++) {
            String[] data = lines.get(i).split("\\|", -1);

            if (data.length == 4 &&
                    data[0].equalsIgnoreCase(username)) {
                if (!data[3].equals("PENDING")) {
                    return false;
                }

                data[3] = "APPROVED";
                lines.set(i, String.join("|", data));
                approved = true;
                break;
            }
        }

        if (approved) {
            Files.write(path, lines, StandardCharsets.UTF_8);
        }

        return approved;
    }
}
