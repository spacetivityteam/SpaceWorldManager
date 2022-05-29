package net.spacetivity.world.password;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

public class HashingManager {

    public String createHashedPassword(String originalPassword, String salt) {
        String generatedPassword = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.update(salt.getBytes());
            byte[] bytes = digest.digest(originalPassword.getBytes());
            StringBuilder stringBuilder = new StringBuilder();
            for (byte aByte : bytes) stringBuilder.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
            generatedPassword = stringBuilder.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return generatedPassword;
    }


    public String getSalt() throws NoSuchAlgorithmException {
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Arrays.toString(salt);
    }
}
