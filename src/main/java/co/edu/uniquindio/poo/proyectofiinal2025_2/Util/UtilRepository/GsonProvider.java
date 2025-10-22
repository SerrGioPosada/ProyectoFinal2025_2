package co.edu.uniquindio.poo.proyectofiinal2025_2.Util.UtilRepository;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Util.Adapter.LocalDateTimeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.LocalDateTime;

/**
 * Centralized provider for Gson instances with consistent configuration across all repositories.
 *
 * <p>This utility eliminates duplicated Gson configuration code that was scattered across
 * 10+ repository classes. It ensures all repositories use the same Gson configuration,
 * making it easy to add new adapters or modify serialization behavior globally.</p>
 *
 * <p><b>Core Functionality:</b></p>
 * <ul>
 *     <li><b>Standard Gson:</b> {@link #createGson()} - Basic Gson with LocalDateTime adapter</li>
 *     <li><b>Pretty-Printed Gson:</b> {@link #createGsonWithPrettyPrinting()} - For human-readable JSON</li>
 *     <li><b>Cached Instance:</b> {@link #getGson()} - Reusable singleton Gson instance</li>
 * </ul>
 *
 * <p><b>Usage Examples:</b></p>
 * <pre>
 * // In Repositories - Standard usage:
 * private final Gson gson = GsonProvider.createGson();
 *
 * // For admin/user repositories with readable JSON:
 * private final Gson gson = GsonProvider.createGsonWithPrettyPrinting();
 *
 * // Using cached singleton (memory efficient for read-only operations):
 * Gson gson = GsonProvider.getGson();
 * String json = gson.toJson(entity);
 * </pre>
 *
 * <p><b>Benefits:</b></p>
 * <ul>
 *     <li>Single point of configuration for all Gson instances</li>
 *     <li>Eliminates 10+ duplicated GsonBuilder configurations</li>
 *     <li>Easy to add new type adapters globally (just modify this class)</li>
 *     <li>Consistent JSON formatting across all data files</li>
 *     <li>Facilitates future migration to alternative serialization libraries</li>
 * </ul>
 *
 * <p><b>Current Adapters:</b></p>
 * <ul>
 *     <li>{@link LocalDateTimeAdapter} - Handles Java 8+ LocalDateTime serialization</li>
 * </ul>
 *
 * @author Sistema de Gestión de Envíos
 * @version 1.0
 * @since 2025
 * @see com.google.gson.Gson
 * @see LocalDateTimeAdapter
 */
public final class GsonProvider {

    // =================================================================================================================
    // SINGLETON INSTANCE (Cached Gson for reuse)
    // =================================================================================================================

    /**
     * Cached Gson instance for reuse across multiple operations.
     * Initialized lazily on first access.
     */
    private static Gson cachedGson;

    // =================================================================================================================
    // CONSTRUCTOR
    // =================================================================================================================

    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     *
     * @throws UnsupportedOperationException if instantiation is attempted
     */
    private GsonProvider() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // =================================================================================================================
    // GSON FACTORY METHODS
    // =================================================================================================================

    /**
     * Creates a new Gson instance with standard configuration.
     *
     * <p><b>Configuration:</b></p>
     * <ul>
     *     <li>LocalDateTime adapter registered (ISO-8601 format)</li>
     *     <li>Compact JSON output (no pretty printing)</li>
     * </ul>
     *
     * <p><b>Use Cases:</b></p>
     * <ul>
     *     <li>Most repositories (Order, Shipment, Payment, Vehicle, Tariff, etc.)</li>
     *     <li>When file size is a concern</li>
     *     <li>Production environments</li>
     * </ul>
     *
     * <p><b>Example Output:</b></p>
     * <pre>
     * {"id":"123","name":"John","createdAt":"2025-01-15T14:30:00"}
     * </pre>
     *
     * @return A new Gson instance with LocalDateTime adapter
     */
    public static Gson createGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
    }

    /**
     * Creates a new Gson instance with pretty-printing enabled.
     *
     * <p><b>Configuration:</b></p>
     * <ul>
     *     <li>LocalDateTime adapter registered (ISO-8601 format)</li>
     *     <li>Pretty-printed JSON output (human-readable)</li>
     * </ul>
     *
     * <p><b>Use Cases:</b></p>
     * <ul>
     *     <li>Admin and User repositories (for easier manual inspection)</li>
     *     <li>Development and debugging</li>
     *     <li>Configuration files that humans might edit</li>
     * </ul>
     *
     * <p><b>Example Output:</b></p>
     * <pre>
     * {
     *   "id": "123",
     *   "name": "John",
     *   "createdAt": "2025-01-15T14:30:00"
     * }
     * </pre>
     *
     * @return A new Gson instance with LocalDateTime adapter and pretty-printing
     */
    public static Gson createGsonWithPrettyPrinting() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .setPrettyPrinting()
                .create();
    }

    /**
     * Returns a cached singleton Gson instance for reuse.
     *
     * <p>This method provides a memory-efficient way to access a Gson instance
     * without creating a new one each time. The instance is thread-safe for
     * read operations (serialization/deserialization).</p>
     *
     * <p><b>Use Cases:</b></p>
     * <ul>
     *     <li>Utility methods that need Gson for one-off operations</li>
     *     <li>When you don't want to store a Gson instance as a field</li>
     *     <li>Services that occasionally need JSON serialization</li>
     * </ul>
     *
     * <p><b>Thread Safety:</b> Gson instances are thread-safe for read operations.</p>
     *
     * @return A cached Gson instance (created on first call)
     */
    public static Gson getGson() {
        if (cachedGson == null) {
            cachedGson = createGson();
        }
        return cachedGson;
    }
}