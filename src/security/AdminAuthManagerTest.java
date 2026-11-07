
package security;

public class AdminAuthManagerTest {

    public static void main(String[] args) throws Exception {

        String adminUsername = "keyryx_admin";
        String adminPassword = "AdminSecure12345";

        String userUsername = "admin_test_user";
        String userPassword = "TestPassword123";

        // STEP 1: Register a test user
        boolean registered = AuthManager.register(
                userUsername, userPassword
        );

        System.out.println("User registration: " + registered);

        // STEP 2: Initialize administrator
        boolean adminCreated = AdminAuthManager.initializeAdmin(
                adminUsername, adminPassword
        );

        System.out.println("Admin created: " + adminCreated);

        // STEP 3: Authenticate administrator
        boolean adminLogin = AdminAuthManager.loginAdmin(
                adminUsername, adminPassword
        );

        System.out.println("Admin login: " + adminLogin);

        // STEP 4: Approve the pending user
        boolean approved = AdminAuthManager.approvePendingUser(
                adminUsername,
                adminPassword,
                userUsername
        );

        System.out.println("User approval: " + approved);

        // STEP 5: Test the user's login
        boolean userLogin = AuthManager.login(
                userUsername, userPassword
        );

        System.out.println("User login: " + userLogin);
    }
}
