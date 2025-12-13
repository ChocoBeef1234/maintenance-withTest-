package main.java.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordUtil {
    private static final String ALGORITHM = "SHA-256";
    private static final int SALT_LENGTH = 16;
    private static final String DELIMITER = ":";

    /**
     * Hashes a plain text password with a randomly generated salt.
     * The result format is: base64(salt):base64(hash)
     * @param plainPassword the plain text password to hash
     * @return hashed password string in format "salt:hash"
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        try {
            // Generate a random salt
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);

            // Hash the password with the salt
            byte[] hash = hashWithSalt(plainPassword, salt);

            // Encode salt and hash to Base64 for storage
            String saltBase64 = Base64.getEncoder().encodeToString(salt);
            String hashBase64 = Base64.getEncoder().encodeToString(hash);

            // Return format: salt:hash
            return saltBase64 + DELIMITER + hashBase64;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    /**
     * Verifies a plain text password against a stored hashed password.
     * Supports multiple formats for backward compatibility:
     * - New format: "salt:hash" (Base64 encoded)
     * - Legacy unsalted SHA-256: hex-encoded hash (64 hex characters)
     * - Plain text: direct comparison
     * 
     * @param plainPassword the plain text password to verify
     * @param hashedPassword the stored password (various formats supported)
     * @return true if the password matches, false otherwise
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }

        // Check if the stored password contains the delimiter (new salted format)
        if (hashedPassword.contains(DELIMITER)) {
            try {
                // Split salt and hash
                String[] parts = hashedPassword.split(DELIMITER, 2);
                if (parts.length != 2) {
                    return false;
                }

                // Decode salt and hash from Base64
                byte[] salt = Base64.getDecoder().decode(parts[0]);
                byte[] storedHash = Base64.getDecoder().decode(parts[1]);

                // Hash the input password with the same salt
                byte[] computedHash = hashWithSalt(plainPassword, salt);

                // Compare the hashes using constant-time comparison
                return MessageDigest.isEqual(storedHash, computedHash);
            } catch (Exception e) {
                return false;
            }
        }

        // Check if it's a legacy unsalted SHA-256 hash (64 hex characters)
        if (isHexHash(hashedPassword)) {
            try {
                // Compute SHA-256 hash of the plain password
                MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
                byte[] computedHashBytes = digest.digest(plainPassword.getBytes());
                
                // Convert to hex string
                StringBuilder hexString = new StringBuilder();
                for (byte b : computedHashBytes) {
                    String hex = Integer.toHexString(0xff & b);
                    if (hex.length() == 1) {
                        hexString.append('0');
                    }
                    hexString.append(hex);
                }
                
                // Compare with stored hash (case-insensitive for hex)
                return hexString.toString().equalsIgnoreCase(hashedPassword);
            } catch (NoSuchAlgorithmException e) {
                return false;
            }
        }

        // Legacy plain text password - do direct comparison for backward compatibility
        // This allows existing plain text passwords to still work
        return hashedPassword.equals(plainPassword);
    }

    /**
     * Checks if a string is a hex-encoded hash (64 hex characters, typical for SHA-256).
     * 
     * @param password the password string to check
     * @return true if it appears to be a hex hash, false otherwise
     */
    private static boolean isHexHash(String password) {
        if (password == null || password.length() != 64) {
            return false;
        }
        return password.matches("[0-9a-fA-F]{64}");
    }

    /**
     * Hashes a password with a given salt using SHA-256.
     * 
     * @param password the plain text password
     * @param salt the salt bytes
     * @return the hashed password bytes
     * @throws NoSuchAlgorithmException if SHA-256 is not available
     */
    private static byte[] hashWithSalt(String password, byte[] salt) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
        digest.update(salt);
        byte[] hash = digest.digest(password.getBytes());
        return hash;
    }

    /**
     * Checks if a password string is already hashed (contains the delimiter).
     * 
     * @param password the password string to check
     * @return true if the password appears to be hashed, false otherwise
     */
    public static boolean isHashed(String password) {
        return password != null && password.contains(DELIMITER);
    }
}

