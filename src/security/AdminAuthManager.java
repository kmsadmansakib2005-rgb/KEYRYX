
package security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Base64;

public class AdminAuthManager {

    private static final String ADMIN_FILE = "admin.db";

    // Create the first administrator account
    public static synchronized boolean initializeAdmin(
            String username, String password) throws Exception {

        if (username == null ||
                !username.matches("[a-zA-Z0-9_]{3,20}")) {
            return false;
        }

        if (password == null || password.length() < 12) {
            return false;
        }

        Path path = Path.of(ADMIN_FILE);

        // Prevent creating multiple administrator accounts
        if (Files.exists(path)) {
            return false;
        }

        byte[] salt = PasswordHasher.generateSalt();
        byte[] hash = PasswordHasher.hashPassword(password, salt);

        String record = username + "|" +
                Base64.getEncoder().encodeToString(salt) + "|" +
                Base64.getEncoder().encodeToString(hash);

        try {
            Files.writeString(
                    path,
                    record,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE_NEW
            );
        } catch (FileAlreadyExistsException e) {
            return false;
        }

        return true;
    }

    // Verify administrator credentials
    public static synchronized boolean loginAdmin(
            String username, String password) throws Exception {

        if (username == null || password == null) {
            return false;
        }

        Path path = Path.of(ADMIN_FILE);

        if (!Files.exists(path)) {
            return false;
        }

        String record = Files.readString(path, StandardCharsets.UTF_8);
        String[] data = record.split("\\|", -1);

        if (data.length != 3 ||
                !data[0].equals(username)) {
            return false;
        }

        byte[] salt = Base64.getDecoder().decode(data[1]);
        byte[] storedHash = Base64.getDecoder().decode(data[2]);

        return PasswordHasher.verifyPassword(
                password, salt, storedHash
        );
    }

    // Approve an account only after administrator authentication
    public static synchronized boolean approvePendingUser(
            String adminUsername,
            String adminPassword,
            String usernameToApprove) throws Exception {

        if (!loginAdmin(adminUsername, adminPassword)) {
            return false;
        }

        return AuthManager.approveUserInternal(usernameToApprove);
    }
}
