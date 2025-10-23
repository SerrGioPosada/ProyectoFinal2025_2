package co.edu.uniquindio.poo.proyectofinal2025_2.Util.UtilModel;

/**
 * Simple logging utility for the application.
 * Provides different log levels for better debugging and monitoring.
 * This class centralizes all logging operations to maintain consistency
 * and make it easier to change logging implementation in the future.
 */
public class Logger {

    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private Logger() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Logs an informational message.
     * Use this for general information about application flow.
     *
     * @param message The message to log.
     */
    public static void info(String message) {
        System.out.println("[INFO] " + message);
    }

    /**
     * Logs a warning message.
     * Use this for potentially harmful situations that are not errors.
     *
     * @param message The warning message to log.
     */
    public static void warn(String message) {
        System.out.println("[WARN] " + message);
    }

    /**
     * Logs a warning message (alias for warn).
     * Use this for potentially harmful situations that are not errors.
     *
     * @param message The warning message to log.
     */
    public static void warning(String message) {
        warn(message);
    }

    /**
     * Logs an error message.
     * Use this for error events that might still allow the application to continue running.
     *
     * @param message The error message to log.
     */
    public static void error(String message) {
        System.err.println("[ERROR] " + message);
    }

    /**
     * Logs a debug message.
     * Use this for detailed information useful during development and debugging.
     *
     * @param message The debug message to log.
     */
    public static void debug(String message) {
        System.out.println("[DEBUG] " + message);
    }

    /**
     * Logs an exception with its stack trace.
     * Use this when you need to log exception details.
     *
     * @param message   The context message.
     * @param exception The exception to log.
     */
    public static void error(String message, Exception exception) {
        System.err.println("[ERROR] " + message);
        exception.printStackTrace();
    }
}