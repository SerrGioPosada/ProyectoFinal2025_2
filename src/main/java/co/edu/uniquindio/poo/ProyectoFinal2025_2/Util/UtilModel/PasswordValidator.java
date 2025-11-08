package co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Utility class for validating password strength and requirements.
 * <p>
 * Provides methods to check password complexity, strength levels,
 * and generate user-friendly validation messages.
 * </p>
 */
public class PasswordValidator {

    // =========================================================================================
    // CONSTANTS
    // =========================================================================================

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 50;
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[!@#$%^&*(),.?\":{}|<>]");

    /**
     * Enum representing password strength levels.
     */
    public enum PasswordStrength {
        WEAK("Débil", 0),
        MEDIUM("Media", 1),
        STRONG("Fuerte", 2);

        private final String displayName;
        private final int level;

        PasswordStrength(String displayName, int level) {
            this.displayName = displayName;
            this.level = level;
        }

        public String getDisplayName() {
            return displayName;
        }

        public int getLevel() {
            return level;
        }
    }

    // =========================================================================================
    // VALIDATION METHODS
    // =========================================================================================

    /**
     * Validates if a password meets all minimum requirements.
     *
     * @param password The password to validate
     * @return List of validation error messages (empty if valid)
     */
    public static List<String> validatePassword(String password) {
        List<String> errors = new ArrayList<>();

        if (password == null || password.isEmpty()) {
            errors.add("La contraseña no puede estar vacía");
            return errors;
        }

        if (password.length() < MIN_LENGTH) {
            errors.add(String.format("La contraseña debe tener al menos %d caracteres", MIN_LENGTH));
        }

        if (password.length() > MAX_LENGTH) {
            errors.add(String.format("La contraseña no puede exceder %d caracteres", MAX_LENGTH));
        }

        if (!UPPERCASE_PATTERN.matcher(password).find()) {
            errors.add("La contraseña debe contener al menos una letra mayúscula");
        }

        if (!LOWERCASE_PATTERN.matcher(password).find()) {
            errors.add("La contraseña debe contener al menos una letra minúscula");
        }

        if (!DIGIT_PATTERN.matcher(password).find()) {
            errors.add("La contraseña debe contener al menos un número");
        }

        if (!SPECIAL_CHAR_PATTERN.matcher(password).find()) {
            errors.add("La contraseña debe contener al menos un carácter especial (!@#$%^&*...)");
        }

        return errors;
    }

    /**
     * Checks if a password is valid (meets all requirements).
     *
     * @param password The password to check
     * @return True if password is valid, false otherwise
     */
    public static boolean isValid(String password) {
        return validatePassword(password).isEmpty();
    }

    /**
     * Calculates the strength of a password.
     *
     * @param password The password to evaluate
     * @return PasswordStrength enum value
     */
    public static PasswordStrength calculateStrength(String password) {
        if (password == null || password.isEmpty()) {
            return PasswordStrength.WEAK;
        }

        int score = 0;

        // Length score
        if (password.length() >= MIN_LENGTH) score++;
        if (password.length() >= 12) score++;

        // Character variety score
        if (UPPERCASE_PATTERN.matcher(password).find()) score++;
        if (LOWERCASE_PATTERN.matcher(password).find()) score++;
        if (DIGIT_PATTERN.matcher(password).find()) score++;
        if (SPECIAL_CHAR_PATTERN.matcher(password).find()) score++;

        // No common patterns
        if (!containsCommonPatterns(password)) score++;

        // Determine strength based on score
        if (score >= 6) {
            return PasswordStrength.STRONG;
        } else if (score >= 4) {
            return PasswordStrength.MEDIUM;
        } else {
            return PasswordStrength.WEAK;
        }
    }

    /**
     * Checks if password contains common patterns that reduce security.
     *
     * @param password The password to check
     * @return True if contains common patterns, false otherwise
     */
    private static boolean containsCommonPatterns(String password) {
        String lowerPassword = password.toLowerCase();

        // Common patterns and sequences
        String[] commonPatterns = {
            "123456", "password", "qwerty", "abc123", "letmein",
            "admin", "welcome", "monkey", "login", "master"
        };

        for (String pattern : commonPatterns) {
            if (lowerPassword.contains(pattern)) {
                return true;
            }
        }

        // Check for repeated characters (e.g., "aaaa")
        if (password.matches(".*(.)\\1{3,}.*")) {
            return true;
        }

        return false;
    }

    /**
     * Validates if two passwords match.
     *
     * @param password The original password
     * @param confirmPassword The confirmation password
     * @return True if passwords match, false otherwise
     */
    public static boolean passwordsMatch(String password, String confirmPassword) {
        if (password == null || confirmPassword == null) {
            return false;
        }
        return password.equals(confirmPassword);
    }

    /**
     * Gets a user-friendly description of password requirements.
     *
     * @return String with password requirements
     */
    public static String getRequirementsDescription() {
        return String.format(
            "La contraseña debe:\n" +
            "• Tener entre %d y %d caracteres\n" +
            "• Contener al menos una letra mayúscula\n" +
            "• Contener al menos una letra minúscula\n" +
            "• Contener al menos un número\n" +
            "• Contener al menos un carácter especial (!@#$%%^&*...)",
            MIN_LENGTH, MAX_LENGTH
        );
    }

    /**
     * Calculates a percentage score for password strength (0-100).
     *
     * @param password The password to evaluate
     * @return Percentage score (0-100)
     */
    public static double getStrengthPercentage(String password) {
        if (password == null || password.isEmpty()) {
            return 0.0;
        }

        PasswordStrength strength = calculateStrength(password);
        return switch (strength) {
            case WEAK -> 33.0;
            case MEDIUM -> 66.0;
            case STRONG -> 100.0;
        };
    }
}
