package co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel;

import java.util.regex.Pattern;

/**
 * Comprehensive validation utility class for common validation operations across the application.
 *
 * <p>This utility consolidates all validation logic from both the original ValidationUtil
 * and ValidationService to provide a single, consistent validation API for the entire system.</p>
 *
 * <p><b>Validation Categories:</b></p>
 * <ul>
 *     <li><b>Null/Empty Checks:</b> {@link #requireNonNull(Object, String)}, {@link #requireNonEmpty(String, String)}</li>
 *     <li><b>Email Validation:</b> {@link #isValidEmail(String)}</li>
 *     <li><b>Name Validation:</b> {@link #isValidName(String)}</li>
 *     <li><b>Phone Validation:</b> {@link #isValidPhone(String)}</li>
 *     <li><b>Password Validation:</b> {@link #isValidPassword(String)}, {@link #passwordsMatch(String, String)}</li>
 *     <li><b>Length Validation:</b> {@link #hasMinimumLength(String, int)}, {@link #hasMaximumLength(String, int)}</li>
 *     <li><b>Range Validation:</b> {@link #isInRange(int, int, int)}</li>
 * </ul>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>
 * // In Controllers:
 * if (!ValidationUtil.isValidEmail(email)) {
 *     showError("Invalid email format");
 * }
 *
 * // In Services:
 * ValidationUtil.requireNonNull(user, "User cannot be null");
 * ValidationUtil.requireNonEmpty(name, "Name is required");
 * </pre>
 *
 * @author Sistema de Gestión de Envíos
 * @version 2.0 (Consolidated from ValidationUtil + ValidationService)
 * @since 2025
 */
public final class ValidationUtil {

    // =================================================================================================================
    // REGEX PATTERNS - Pre-compiled for performance
    // =================================================================================================================

    /**
     * Email validation pattern.
     * Matches standard email formats: username@domain.tld
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    /**
     * Phone validation pattern.
     * Matches exactly 10 digits (Colombian phone format).
     */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10}$");

    // =================================================================================================================
    // CONSTRUCTOR
    // =================================================================================================================

    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     *
     * @throws UnsupportedOperationException if instantiation is attempted
     */
    private ValidationUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // =================================================================================================================
    // NULL/EMPTY VALIDATION METHODS
    // =================================================================================================================

    /**
     * Validates that an object is not null.
     *
     * <p>Throws an {@link IllegalArgumentException} with the provided message if validation fails.</p>
     *
     * @param object  The object to validate
     * @param message The exception message if validation fails
     * @throws IllegalArgumentException if object is null
     */
    public static void requireNonNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Validates that a string is not null, empty, or whitespace-only.
     *
     * <p>Throws an {@link IllegalArgumentException} with the provided message if validation fails.</p>
     *
     * @param str     The string to validate
     * @param message The exception message if validation fails
     * @throws IllegalArgumentException if string is null, empty, or whitespace-only
     */
    public static void requireNonEmpty(String str, String message) {
        if (str == null || str.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    // =================================================================================================================
    // EMAIL VALIDATION
    // =================================================================================================================

    /**
     * Validates that a string conforms to standard email format.
     *
     * <p>Validation rules:</p>
     * <ul>
     *     <li>Not null or empty (after trimming)</li>
     *     <li>Contains @ symbol</li>
     *     <li>Has valid domain format (domain.tld)</li>
     *     <li>Matches pattern: username@domain.tld</li>
     * </ul>
     *
     * @param email The email address to validate
     * @return {@code true} if the email is valid, {@code false} otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    // =================================================================================================================
    // NAME VALIDATION
    // =================================================================================================================

    /**
     * Validates that a name meets minimum length requirements.
     *
     * <p>Validation rules:</p>
     * <ul>
     *     <li>Not null</li>
     *     <li>At least 2 characters (after trimming)</li>
     * </ul>
     *
     * <p>This method is used for validating first names, last names, and other person name fields.</p>
     *
     * @param name The name to validate
     * @return {@code true} if the name is valid, {@code false} otherwise
     */
    public static boolean isValidName(String name) {
        return name != null && name.trim().length() >= 2;
    }

    // =================================================================================================================
    // PHONE VALIDATION
    // =================================================================================================================

    /**
     * Validates that a phone number conforms to the expected format.
     *
     * <p>Validation rules:</p>
     * <ul>
     *     <li>Not null</li>
     *     <li>Exactly 10 digits</li>
     *     <li>No spaces, dashes, or other characters allowed</li>
     * </ul>
     *
     * <p><b>Note:</b> Currently validates Colombian phone format (10 digits).
     * Modify {@link #PHONE_PATTERN} for other country formats.</p>
     *
     * @param phone The phone number to validate
     * @return {@code true} if the phone is valid, {@code false} otherwise
     */
    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    // =================================================================================================================
    // PASSWORD VALIDATION
    // =================================================================================================================

    /**
     * Validates that a password meets minimum security requirements.
     *
     * <p>Validation rules:</p>
     * <ul>
     *     <li>Not null</li>
     *     <li>At least 6 characters (after trimming)</li>
     * </ul>
     *
     * <p><b>Note:</b> For production systems, consider adding additional requirements such as:
     * uppercase letters, lowercase letters, numbers, special characters.</p>
     *
     * @param password The password to validate
     * @return {@code true} if the password is valid, {@code false} otherwise
     */
    public static boolean isValidPassword(String password) {
        return password != null && password.trim().length() >= 6;
    }

    /**
     * Validates that two password strings match exactly.
     *
     * <p>This is typically used for password confirmation fields in registration forms.</p>
     *
     * <p>Validation rules:</p>
     * <ul>
     *     <li>First password is not null</li>
     *     <li>Both passwords are exactly equal (case-sensitive)</li>
     * </ul>
     *
     * @param password        The original password
     * @param confirmPassword The confirmation password
     * @return {@code true} if both passwords match, {@code false} otherwise
     */
    public static boolean passwordsMatch(String password, String confirmPassword) {
        return password != null && password.equals(confirmPassword);
    }

    // =================================================================================================================
    // LENGTH VALIDATION METHODS
    // =================================================================================================================

    /**
     * Validates that a string has at least the specified minimum length.
     *
     * @param str       The string to validate
     * @param minLength The minimum required length
     * @return {@code true} if string meets minimum length, {@code false} otherwise
     */
    public static boolean hasMinimumLength(String str, int minLength) {
        return str != null && str.length() >= minLength;
    }

    /**
     * Validates that a string does not exceed the specified maximum length.
     *
     * @param str       The string to validate
     * @param maxLength The maximum allowed length
     * @return {@code true} if string does not exceed maximum length, {@code false} otherwise
     */
    public static boolean hasMaximumLength(String str, int maxLength) {
        return str == null || str.length() <= maxLength;
    }

    // =================================================================================================================
    // RANGE VALIDATION METHODS
    // =================================================================================================================

    /**
     * Validates that a numeric value is within a specified range (inclusive).
     *
     * @param value The value to check
     * @param min   The minimum value (inclusive)
     * @param max   The maximum value (inclusive)
     * @return {@code true} if within range, {@code false} otherwise
     */
    public static boolean isInRange(int value, int min, int max) {
        return value >= min && value <= max;
    }
}
