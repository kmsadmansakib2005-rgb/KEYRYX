
package security;

public class AuthenticationService {

    // Login as a normal user
    public static Session loginUser(
            String username, String password) throws Exception {

        if (!AuthManager.login(username, password)) {
            return null;
        }

        return SessionManager.createSession(
                username, Session.Role.USER
        );
    }

    // Login as administrator
    public static Session loginAdmin(
            String username, String password) throws Exception {

        if (!AdminAuthManager.loginAdmin(username, password)) {
            return null;
        }

        return SessionManager.createSession(
                username, Session.Role.ADMIN
        );
    }

    // Validate an existing session
    public static Session authenticate(String token) {

        return SessionManager.getValidSession(token);
    }

    // Logout
    public static void logout(String token) {

        SessionManager.terminateSession(token);
    }

    // Approve a user using an administrator session
    public static boolean approveUser(
            String adminToken, String username) {

        Session session = authenticate(adminToken);

        if (session == null) {
            return false;
        }

        if (session.getRole() != Session.Role.ADMIN) {
            return false;
        }

        return AuthManager.approveUserInternal(username);
    }
}
