
package security;

public class SessionTest {

    public static void main(String[] args) throws Exception {

        // Test normal user login
        Session userSession =
                AuthenticationService.loginUser(
                        "admin_test_user",
                        "TestPassword123"
                );

        System.out.println("User session created: "
                + (userSession != null));

        if (userSession != null) {
            System.out.println("Username: "
                    + userSession.getUsername());

            System.out.println("Role: "
                    + userSession.getRole());

            System.out.println("Token: "
                    + userSession.getToken());

            System.out.println("Session valid: "
                    + (AuthenticationService.authenticate(
                    userSession.getToken()) != null));

            // Test logout
            AuthenticationService.logout(
                    userSession.getToken());

            System.out.println("Session after logout: "
                    + (AuthenticationService.authenticate(
                    userSession.getToken()) != null));
        }

        // Test administrator login
        Session adminSession =
                AuthenticationService.loginAdmin(
                        "keyryx_admin",
                        "AdminSecure12345"
                );

        System.out.println("Admin session created: "
                + (adminSession != null));

        if (adminSession != null) {
            System.out.println("Admin role: "
                    + adminSession.getRole());

            System.out.println("User approval: "
                    + AuthenticationService.approveUser(
                    adminSession.getToken(),
                    "admin_test_user"
            ));
        }

        // Test whether a normal user can approve another user
        Session normalUser =
                AuthenticationService.loginUser(
                        "admin_test_user",
                        "TestPassword123"
                );

        if (normalUser != null) {
            System.out.println("Normal user approval attempt: "
                    + AuthenticationService.approveUser(
                    normalUser.getToken(),
                    "another_test_user"
            ));
        }
    }
}
