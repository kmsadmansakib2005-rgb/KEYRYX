
package security;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {

    private static final long IDLE_TIMEOUT =
            30 * 60 * 1000; // 30 minutes

    private static final ConcurrentHashMap<String, Session> sessions =
            new ConcurrentHashMap<>();

    private static final SecureRandom random = new SecureRandom();

    // Create a new session (security package only)
    static Session createSession(String username, Session.Role role) {

        byte[] tokenBytes = new byte[32];
        random.nextBytes(tokenBytes);

        String token = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(tokenBytes);

        Session session = new Session(token, username, role);

        sessions.put(token, session);

        return session;
    }

    // Validate and retrieve a session
    static Session getValidSession(String token) {

        if (token == null) {
            return null;
        }

        Session session = sessions.get(token);

        if (session == null) {
            return null;
        }

        long currentTime = System.currentTimeMillis();

        if (currentTime - session.getLastActivity() > IDLE_TIMEOUT) {
            sessions.remove(token, session);
            return null;
        }

        session.updateActivity();

        return session;
    }

    // Terminate a session
    static void terminateSession(String token) {

        if (token != null) {
            sessions.remove(token);
        }
    }
}
