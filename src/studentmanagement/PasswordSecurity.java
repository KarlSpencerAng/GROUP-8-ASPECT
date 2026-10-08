package studentmanagement;

import java.security.SecureRandom;
import java.security.MessageDigest;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** New passwords use salted PBKDF2; existing demo plaintext accounts remain compatible. */
public final class PasswordSecurity {
    private PasswordSecurity() {}
    public static String hash(char[] password) {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, 210000, 256);
            byte[] digest = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
            spec.clearPassword();
            return "pbkdf2$210000$" + Base64.getEncoder().encodeToString(salt) + "$" + Base64.getEncoder().encodeToString(digest);
        } catch (Exception ex) { throw new IllegalStateException("Password hashing failed", ex); }
    }
    public static boolean matches(String entered, String stored) {
        if (stored == null) return false;
        if (!stored.startsWith("pbkdf2$")) return MessageDigest.isEqual(entered.getBytes(java.nio.charset.StandardCharsets.UTF_8), stored.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        try {
            String[] parts = stored.split("\\$");
            PBEKeySpec spec = new PBEKeySpec(entered.toCharArray(), Base64.getDecoder().decode(parts[2]), Integer.parseInt(parts[1]), 256);
            byte[] actual = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
            spec.clearPassword();
            return MessageDigest.isEqual(actual, Base64.getDecoder().decode(parts[3]));
        } catch (Exception ex) { return false; }
    }
}
