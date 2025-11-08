package co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for date and time operations throughout the application.
 *
 * <p>This utility centralizes all date/time generation and formatting logic, providing
 * consistent timestamp creation across services and models. It serves as a single point
 * of modification for temporal operations and facilitates future enhancements like timezone
 * handling or custom date formatting.</p>
 *
 * <p><b>Core Functionality:</b></p>
 * <ul>
 *     <li><b>Current Timestamps:</b> {@link #now()}, {@link #today()}</li>
 *     <li><b>Formatting:</b> {@link #formatDateTime(LocalDateTime)}, {@link #formatDate(LocalDate)}</li>
 * </ul>
 *
 * <p><b>Usage Examples:</b></p>
 * <pre>
 * // In Services:
 * Order order = new Order.Builder()
 *         .withCreatedAt(DateTimeUtil.now())
 *         .build();
 *
 * Invoice invoice = new Invoice.Builder()
 *         .withIssuedAt(DateTimeUtil.now())
 *         .build();
 *
 * // Formatting for display:
 * String formattedDate = DateTimeUtil.formatDateTime(order.getCreatedAt());
 * </pre>
 *
 * <p><b>Design Benefits:</b></p>
 * <ul>
 *     <li>Eliminates direct {@code LocalDateTime.now()} calls scattered throughout the code</li>
 *     <li>Facilitates testing with mockable timestamp generation</li>
 *     <li>Provides consistent date formatting across the application</li>
 *     <li>Centralizes timezone handling for future internationalization</li>
 * </ul>
 *
 * @author Sistema de Gestión de Envíos
 * @version 1.0
 * @since 2025
 * @see java.time.LocalDateTime
 * @see java.time.LocalDate
 */
public final class DateTimeUtil {

    // =================================================================================================================
    // DATE FORMAT CONSTANTS
    // =================================================================================================================

    /**
     * Default date-time format pattern: "yyyy-MM-dd HH:mm:ss"
     * Example: "2025-01-15 14:30:00"
     */
    private static final DateTimeFormatter DEFAULT_DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Default date format pattern: "yyyy-MM-dd"
     * Example: "2025-01-15"
     */
    private static final DateTimeFormatter DEFAULT_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // =================================================================================================================
    // CONSTRUCTOR
    // =================================================================================================================

    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     *
     * @throws UnsupportedOperationException if instantiation is attempted
     */
    private DateTimeUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // =================================================================================================================
    // CURRENT TIMESTAMP METHODS
    // =================================================================================================================

    /**
     * Returns the current date and time.
     *
     * <p>This method wraps {@link LocalDateTime#now()} to provide a centralized point
     * for timestamp generation. This design facilitates:</p>
     * <ul>
     *     <li>Easy testing by mocking this method</li>
     *     <li>Future timezone customization</li>
     *     <li>Consistent timestamp source across the application</li>
     * </ul>
     *
     * <p><b>Common Use Cases:</b></p>
     * <ul>
     *     <li>Setting creation timestamps on entities (Orders, Payments, Shipments)</li>
     *     <li>Recording event timestamps in logs</li>
     *     <li>Generating audit trail timestamps</li>
     * </ul>
     *
     * @return The current date and time as {@link LocalDateTime}
     *
     * @see LocalDateTime#now()
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    /**
     * Returns the current date (without time component).
     *
     * <p>Use this method when you only need the date part and don't require
     * time precision (e.g., for birthdays, expiration dates, reporting periods).</p>
     *
     * @return The current date as {@link LocalDate}
     *
     * @see LocalDate#now()
     */
    public static LocalDate today() {
        return LocalDate.now();
    }

    // =================================================================================================================
    // FORMATTING METHODS
    // =================================================================================================================

    /**
     * Formats a {@link LocalDateTime} object into a human-readable string.
     *
     * <p>Uses the default format: {@code yyyy-MM-dd HH:mm:ss}</p>
     * <p>Example output: {@code "2025-01-15 14:30:00"}</p>
     *
     * <p><b>Use Cases:</b></p>
     * <ul>
     *     <li>Displaying timestamps in UI tables</li>
     *     <li>Formatting for logging and reports</li>
     *     <li>Generating user-friendly date displays</li>
     * </ul>
     *
     * @param dateTime The date-time to format (must not be null)
     * @return A formatted date-time string
     * @throws NullPointerException if dateTime is null
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DEFAULT_DATETIME_FORMATTER);
    }

    /**
     * Formats a {@link LocalDate} object into a human-readable string.
     *
     * <p>Uses the default format: {@code yyyy-MM-dd}</p>
     * <p>Example output: {@code "2025-01-15"}</p>
     *
     * @param date The date to format (must not be null)
     * @return A formatted date string
     * @throws NullPointerException if date is null
     */
    public static String formatDate(LocalDate date) {
        return date.format(DEFAULT_DATE_FORMATTER);
    }

    /**
     * Formats a {@link LocalDateTime} object using a custom pattern.
     *
     * <p><b>Example Patterns:</b></p>
     * <ul>
     *     <li>{@code "dd/MM/yyyy"} → "15/01/2025"</li>
     *     <li>{@code "MMM dd, yyyy"} → "Jan 15, 2025"</li>
     *     <li>{@code "yyyy-MM-dd'T'HH:mm:ss"} → "2025-01-15T14:30:00" (ISO format)</li>
     * </ul>
     *
     * @param dateTime The date-time to format (must not be null)
     * @param pattern  The pattern string (see {@link DateTimeFormatter} for syntax)
     * @return A formatted date-time string according to the specified pattern
     * @throws IllegalArgumentException if the pattern is invalid
     * @throws NullPointerException     if dateTime or pattern is null
     *
     * @see DateTimeFormatter
     */
    public static String formatDateTime(LocalDateTime dateTime, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return dateTime.format(formatter);
    }
}
