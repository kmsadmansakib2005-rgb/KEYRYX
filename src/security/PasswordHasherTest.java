
package security;

public class PasswordHasherTest {

    public static void main(String[] args) throws Exception {

        String password = "myPassword123";

        byte[] salt = PasswordHasher.generateSalt();

        byte[] hash = PasswordHasher.hashPassword(password, salt);

        boolean correct = PasswordHasher.verifyPassword(
                "myPassword123", salt, hash
        );

        boolean incorrect = PasswordHasher.verifyPassword(
                "wrongPassword", salt, hash
        );

        System.out.println("Correct password: " + correct);
        System.out.println("Wrong password: " + incorrect);
    }
}
