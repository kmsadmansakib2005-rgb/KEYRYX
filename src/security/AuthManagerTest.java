
package security;

public class AuthManagerTest {

    public static void main(String[] args) throws Exception {

        String username = "sakib_test2";
        String password = "TestPass12345";

        // 1. Register
        boolean registered = AuthManager.register(username, password);
        System.out.println("Registration: " + registered);

        if (!registered) {
            System.out.println("Choose a username that has never been registered.");
            return;
        }

        // 2. Approve account
        boolean approved = AuthManager.approveUserInternal(username);
        System.out.println("Approval: " + approved);

        // 3. Login
        boolean login = AuthManager.login(username, password);
        System.out.println("Login: " + login);
    }
}
