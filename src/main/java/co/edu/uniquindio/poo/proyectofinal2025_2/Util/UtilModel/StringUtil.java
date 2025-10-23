package co.edu.uniquindio.poo.proyectofinal2025_2.Util.UtilModel;

/**
 * Utility class for common string operations and null-safe string manipulations.
 *
 * <p>This utility provides null-safe string operations that are commonly needed across
 * the application, eliminating repetitive null checking and providing consistent
 * string handling behavior.</p>
 *
 * <p><b>Core Functionality:</b></p>
 * <ul>
 *     <li><b>Null Safety:</b> {@link #defaultIfNull(String, String)}, {@link #defaultIfEmpty(String, String)}</li>
 *     <li><b>Trimming:</b> {@link #safeTrim(String)}</li>
 *     <li><b>Validation:</b> {@link #isNullOrEmpty(String)}, {@link #isNotEmpty(String)}</li>
 *     <li><b>Concatenation:</b> {@link #concatenate(String...)}</li>
 * </ul>
 *
 * <p><b>Usage Examples:</b></p>
 * <pre>
 * // In DTOs or Services:
 * String displayName = StringUtil.defaultIfNull(user.getName(), "Unknown");
 * String email = StringUtil.defaultIfEmpty(user.getEmail(), "no-email@example.com");
 *
 * // Safe trimming:
 * String trimmedInput = StringUtil.safeTrim(userInput);
 *
 * // Validation:
 * if (StringUtil.isNullOrEmpty(description)) {
 *     throw new IllegalArgumentException("Description is required");
 * }
 * </pre>
 *
 * <p><b>Benefits:</b></p>
 * <ul>
 *     <li>Eliminates repetitive {@code != null} checks throughout the codebase</li>
 *     <li>Provides consistent empty string handling</li>
 *     <li>Reduces NullPointerException risks</li>
 *     <li>Makes code more readable and maintainable</li>
 * </ul>
 *
 * @author Sistema de Gestión de Envíos
 * @version 1.0
 * @since 2025
 */
public final class StringUtil {

    // =================================================================================================================
    // CONSTRUCTOR
    // =================================================================================================================

    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     *
     * @throws UnsupportedOperationException if instantiation is attempted
     */
    private StringUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // =================================================================================================================
    // NULL-SAFE DEFAULT VALUE METHODS
    // =================================================================================================================

    /**
     * Returns the provided value if not null, otherwise returns the default value.
     *
     * <p>This method is useful for providing fallback values when a string might be null.</p>
     *
     * <p><b>Examples:</b></p>
     * <pre>
     * StringUtil.defaultIfNull("Hello", "Default")  → "Hello"
     * StringUtil.defaultIfNull(null, "Default")     → "Default"
     * StringUtil.defaultIfNull("", "Default")       → "" (empty string is not null)
     * </pre>
     *
     * @param value        The string to check
     * @param defaultValue The value to return if the string is null
     * @return The original value if not null, otherwise the default value
     */
    public static String defaultIfNull(String value, String defaultValue) {
        return value != null ? value : defaultValue;
    }

    /**
     * Returns the provided value if not null or empty (after trimming), otherwise returns the default value.
     *
     * <p>This method considers whitespace-only strings as empty.</p>
     *
     * <p><b>Examples:</b></p>
     * <pre>
     * StringUtil.defaultIfEmpty("Hello", "Default")     → "Hello"
     * StringUtil.defaultIfEmpty(null, "Default")        → "Default"
     * StringUtil.defaultIfEmpty("", "Default")          → "Default"
     * StringUtil.defaultIfEmpty("   ", "Default")       → "Default"
     * StringUtil.defaultIfEmpty("  Hi  ", "Default")    → "  Hi  "
     * </pre>
     *
     * @param value        The string to check
     * @param defaultValue The value to return if the string is null or empty
     * @return The original value if not null/empty, otherwise the default value
     */
    public static String defaultIfEmpty(String value, String defaultValue) {
        return (value == null || value.trim().isEmpty()) ? defaultValue : value;
    }

    // =================================================================================================================
    // SAFE TRIMMING METHODS
    // =================================================================================================================

    /**
     * Trims a string safely, returning an empty string if the input is null.
     *
     * <p>This method eliminates the need for null checks before trimming.</p>
     *
     * <p><b>Examples:</b></p>
     * <pre>
     * StringUtil.safeTrim("  Hello  ")  → "Hello"
     * StringUtil.safeTrim(null)         → ""
     * StringUtil.safeTrim("   ")        → ""
     * </pre>
     *
     * @param value The string to trim (may be null)
     * @return The trimmed string, or empty string if input is null
     */
    public static String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    // =================================================================================================================
    // STRING VALIDATION METHODS
    // =================================================================================================================

    /**
     * Checks if a string is null or empty after trimming.
     *
     * <p>This method considers whitespace-only strings as empty.</p>
     *
     * <p><b>Examples:</b></p>
     * <pre>
     * StringUtil.isNullOrEmpty(null)      → true
     * StringUtil.isNullOrEmpty("")        → true
     * StringUtil.isNullOrEmpty("   ")     → true
     * StringUtil.isNullOrEmpty("Hello")   → false
     * </pre>
     *
     * @param value The string to check
     * @return {@code true} if the string is null or empty (after trimming), {@code false} otherwise
     */
    public static boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * Checks if a string is not null and not empty after trimming.
     *
     * <p>This is the inverse of {@link #isNullOrEmpty(String)}.</p>
     *
     * <p><b>Examples:</b></p>
     * <pre>
     * StringUtil.isNotEmpty(null)      → false
     * StringUtil.isNotEmpty("")        → false
     * StringUtil.isNotEmpty("   ")     → false
     * StringUtil.isNotEmpty("Hello")   → true
     * </pre>
     *
     * @param value The string to check
     * @return {@code true} if the string is not null and not empty (after trimming), {@code false} otherwise
     */
    public static boolean isNotEmpty(String value) {
        return !isNullOrEmpty(value);
    }

    // =================================================================================================================
    // STRING CONCATENATION METHODS
    // =================================================================================================================

    /**
     * Concatenates multiple strings, skipping null values.
     *
     * <p>This method provides safe concatenation without worrying about null values.</p>
     *
     * <p><b>Examples:</b></p>
     * <pre>
     * StringUtil.concatenate("Hello", " ", "World")       → "Hello World"
     * StringUtil.concatenate("Hello", null, "World")      → "HelloWorld"
     * StringUtil.concatenate(null, null, null)            → ""
     * StringUtil.concatenate("A", "B", "C", "D")          → "ABCD"
     * </pre>
     *
     * @param parts Variable number of string parts to concatenate (null values are skipped)
     * @return The concatenated string (empty string if all parts are null)
     */
    public static String concatenate(String... parts) {
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part != null) {
                sb.append(part);
            }
        }
        return sb.toString();
    }

    /**
     * Concatenates multiple strings with a delimiter, skipping null or empty values.
     *
     * <p><b>Examples:</b></p>
     * <pre>
     * StringUtil.join(", ", "John", "Doe")                → "John, Doe"
     * StringUtil.join(", ", "John", null, "Doe")          → "John, Doe"
     * StringUtil.join(" - ", "A", "B", "C")               → "A - B - C"
     * StringUtil.join("|", "First", "", "Last")           → "First|Last"
     * </pre>
     *
     * @param delimiter The delimiter to use between parts
     * @param parts     Variable number of string parts to join (null/empty values are skipped)
     * @return The joined string
     */
    public static String join(String delimiter, String... parts) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String part : parts) {
            if (isNotEmpty(part)) {
                if (!first) {
                    sb.append(delimiter);
                }
                sb.append(part);
                first = false;
            }
        }
        return sb.toString();
    }

    // =================================================================================================================
    // CAPITALIZATION METHODS
    // =================================================================================================================

    /**
     * Capitalizes the first letter of a string.
     *
     * <p><b>Examples:</b></p>
     * <pre>
     * StringUtil.capitalize("hello")      → "Hello"
     * StringUtil.capitalize("HELLO")      → "HELLO"
     * StringUtil.capitalize(null)         → null
     * StringUtil.capitalize("")           → ""
     * StringUtil.capitalize("h")          → "H"
     * </pre>
     *
     * @param value The string to capitalize
     * @return The capitalized string, or null/empty if input is null/empty
     */
    public static String capitalize(String value) {
        if (isNullOrEmpty(value)) {
            return value;
        }
        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }
}