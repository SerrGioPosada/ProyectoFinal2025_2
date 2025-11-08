package co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel;

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
        Logger.debug("Hashing a new password...");
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
        Logger.debug("Verifying password...");
        if (plainTextPassword == null || hashedPassword == null) {
            Logger.error("Password check failed: plain text or hashed password is null.");
            return false;
        }
        boolean match = BCrypt.checkpw(plainTextPassword, hashedPassword);
        Logger.debug("Password verification result: " + (match ? "SUCCESS" : "FAILURE"));
        return match;
    }
}
