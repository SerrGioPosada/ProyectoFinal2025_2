package co.edu.uniquindio.poo.proyectofiinal2025_2.Util.UtilModel;

/**
 * General validation utilities for common validation operations.
 * Provides reusable validation methods across the application.
 * This class helps maintain consistent validation logic throughout the system.
 */
public class ValidationUtil {

    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private ValidationUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Validates that an object is not null.
     * Throws an exception with a custom message if the object is null.
     *
     * @param object  The object to validate.
     * @param message The exception message if validation fails.
     * @throws IllegalArgumentException if object is null.
     */
    public static void requireNonNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Validates that a string is not null or empty.
     * Considers whitespace-only strings as empty.
     *
     * @param str     The string to validate.
     * @param message The exception message if validation fails.
     * @throws IllegalArgumentException if string is null or empty.
     */
    public static void requireNonEmpty(String str, String message) {
        if (str == null || str.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Validates that a string is a valid email format.
     * Uses a regular expression to check basic email structure.
     *
     * @param email The email to validate.
     * @return true if valid, false otherwise.
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    /**
     * Validates that a number is within a specified range (inclusive).
     *
     * @param value The value to check.
     * @param min   The minimum value (inclusive).
     * @param max   The maximum value (inclusive).
     * @return true if within range, false otherwise.
     */
    public static boolean isInRange(int value, int min, int max) {
        return value >= min && value <= max;
    }

    /**
     * Validates that a string has a minimum length.
     *
     * @param str       The string to validate.
     * @param minLength The minimum required length.
     * @return true if string meets minimum length, false otherwise.
     */
    public static boolean hasMinimumLength(String str, int minLength) {
        return str != null && str.length() >= minLength;
    }

    /**
     * Validates that a string does not exceed a maximum length.
     *
     * @param str       The string to validate.
     * @param maxLength The maximum allowed length.
     * @return true if string does not exceed maximum length, false otherwise.
     */
    public static boolean hasMaximumLength(String str, int maxLength) {
        return str == null || str.length() <= maxLength;
    }
}