package co.edu.uniquindio.poo.proyectofiinal2025_2.Utilities;

import org.mindrot.jbcrypt.BCrypt;

/**
 * <p>Provides utility methods for password hashing and verification using jBCrypt.</p>
 * <p>This class centralizes all password-related security operations to ensure consistency
 * and prevent insecure handling of passwords.</p>
 */
public class PasswordUtility {

    /**
     * Hashes a plain text password using the BCrypt algorithm.
     *
     * @param plainTextPassword The password to hash.
     * @return A salted and hashed password string.
     */
    public static String hashPassword(String plainTextPassword) {
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt());
    }

    /**
     * Verifies a plain text password against a stored BCrypt hash.
     *
     * @param plainTextPassword The plain text password from a login attempt.
     * @param hashedPassword    The hashed password stored in the database.
     * @return true if the password matches the hash, false otherwise.
     */
    public static boolean checkPassword(String plainTextPassword, String hashedPassword) {
        return BCrypt.checkpw(plainTextPassword, hashedPassword);
    }
}
