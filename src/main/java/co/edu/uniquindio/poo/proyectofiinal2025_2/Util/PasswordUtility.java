package co.edu.uniquindio.poo.proyectofiinal2025_2.Util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Provides utility methods for password hashing and verification using the jBCrypt library.
 * <p>
 * This class centralizes all password-related security operations to ensure consistency
 * and prevent insecure handling of passwords. It is a non-instantiable utility class.
 * </p>
 */
public final class PasswordUtility {

    // =================================================================================================================
    // Constructor
    // =================================================================================================================

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private PasswordUtility() {
        // This class should not be instantiated.
    }

    // =================================================================================================================
    // Public Static API
    // =================================================================================================================

    /**
     * Hashes a plain text password using the BCrypt algorithm, which includes salting.
     *
     * @param plainTextPassword The password to hash. Must not be null.
     * @return A salted and hashed password string ready for storage.
     */
    public static String hashPassword(String plainTextPassword) {
        System.out.println("Hashing a new password...");
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt());
    }

    /**
     * Verifies a plain text password against a stored BCrypt hash.
     *
     * @param plainTextPassword The plain text password from a login attempt.
     * @param hashedPassword    The hashed password retrieved from storage.
     * @return {@code true} if the password matches the hash, {@code false} otherwise.
     */
    public static boolean checkPassword(String plainTextPassword, String hashedPassword) {
        System.out.println("Verifying password...");
        if (plainTextPassword == null || hashedPassword == null) {
            System.err.println("Password check failed: plain text or hashed password is null.");
            return false;
        }
        boolean match = BCrypt.checkpw(plainTextPassword, hashedPassword);
        System.out.println("Password verification result: " + (match ? "SUCCESS" : "FAILURE"));
        return match;
    }
}
