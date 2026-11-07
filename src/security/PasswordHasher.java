package security;

import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;

public class PasswordHasher {
    private static final int SALT_LENGTH= 16;
    private static final int ITERATIONS= 600000;
    private static final int KEY_LENGTH= 256;

    //generating random salt
    public static byte[] generateSalt()
    {
        SecureRandom random= new SecureRandom();
        byte[] salt= new byte[SALT_LENGTH];
        random.nextBytes(salt);

        return salt;
    }

    //genarating hash password using PBKDF2
    public static byte[] hashPassword(String password, byte[] salt)
            throws Exception
    {
        PBEKeySpec spec= new PBEKeySpec(password.toCharArray(),
                salt, ITERATIONS, KEY_LENGTH);

        SecretKeyFactory factory= SecretKeyFactory.getInstance
                ("PBKDF2WithHmacSHA256"); //cryptographioc key genaration factory

        byte[] hash= factory.generateSecret(spec).getEncoded();
        spec.clearPassword();
        return hash;
    }

    //verifying the password
    public static boolean verifyPassword(String password,
    byte[] salt,byte[] storedHash) throws Exception
    {
        byte[] newHash= hashPassword(password,salt);
        return MessageDigest.isEqual(newHash, storedHash);
    }

}
