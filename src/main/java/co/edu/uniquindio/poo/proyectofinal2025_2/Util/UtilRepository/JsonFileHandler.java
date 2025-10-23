package co.edu.uniquindio.poo.proyectofinal2025_2.Util.UtilRepository;

import co.edu.uniquindio.poo.proyectofinal2025_2.Util.UtilModel.Logger;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Centralized JSON file I/O handler for all repositories.
 *
 * <p>This utility eliminates duplicated file reading/writing code across 9+ repository classes,
 * providing consistent error handling, directory creation, and JSON serialization/deserialization.</p>
 *
 * <p><b>Core Functionality:</b></p>
 * <ul>
 *     <li><b>Save Operations:</b> {@link #saveToFile(String, List, Gson)}</li>
 *     <li><b>Load Operations:</b> {@link #loadFromFile(String, Type, Gson)}</li>
 *     <li><b>File Utilities:</b> {@link #ensureDirectoryExists(String)}, {@link #fileExists(String)}</li>
 * </ul>
 *
 * <p><b>Usage Examples:</b></p>
 * <pre>
 * // In Repositories - Save:
 * Gson gson = GsonProvider.createGsonWithPrettyPrinting();
 * List&lt;Admin&gt; admins = new ArrayList&lt;&gt;(adminsById.values());
 * JsonFileHandler.saveToFile(RepositoryPaths.ADMINS_PATH, admins, gson);
 *
 * // In Repositories - Load:
 * Type listType = new TypeToken&lt;ArrayList&lt;Admin&gt;&gt;() {}.getType();
 * Optional&lt;List&lt;Admin&gt;&gt; loadedAdmins = JsonFileHandler.loadFromFile(
 *     RepositoryPaths.ADMINS_PATH,
 *     listType,
 *     gson
 * );
 * </pre>
 *
 * <p><b>Design Benefits:</b></p>
 * <ul>
 *     <li>Eliminates 9+ duplicated save/load methods across repositories</li>
 *     <li>Consistent error handling and logging</li>
 *     <li>Automatic directory creation for data files</li>
 *     <li>Type-safe deserialization with Gson TypeToken</li>
 *     <li>Single point of modification for file I/O behavior</li>
 * </ul>
 *
 * <p><b>Error Handling:</b></p>
 * <ul>
 *     <li>Returns {@code Optional.empty()} on load failures (null-safe)</li>
 *     <li>Logs errors to {@link Logger} instead of throwing exceptions</li>
 *     <li>Handles missing files, empty files, and corrupt JSON gracefully</li>
 * </ul>
 *
 * @author Sistema de Gestión de Envíos
 * @version 1.0
 * @since 2025
 * @see GsonProvider
 * @see RepositoryPaths
 * @see Logger
 */
public final class JsonFileHandler {

    // =================================================================================================================
    // CONSTRUCTOR
    // =================================================================================================================

    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     *
     * @throws UnsupportedOperationException if instantiation is attempted
     */
    private JsonFileHandler() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // =================================================================================================================
    // SAVE OPERATIONS
    // =================================================================================================================

    /**
     * Saves a list of entities to a JSON file.
     *
     * <p>This method handles directory creation, file writing, and error logging automatically.
     * It uses the provided Gson instance for serialization, allowing repositories to control
     * formatting (pretty-printing, adapters, etc.).</p>
     *
     * <p><b>Behavior:</b></p>
     * <ul>
     *     <li>Creates parent directories if they don't exist</li>
     *     <li>Overwrites existing file (use append=false mode)</li>
     *     <li>Flushes and closes file writer properly</li>
     *     <li>Logs success/failure via {@link Logger}</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>
     * Gson gson = GsonProvider.createGsonWithPrettyPrinting();
     * List&lt;User&gt; users = userRepository.getAllUsers();
     * JsonFileHandler.saveToFile(RepositoryPaths.USERS_PATH, users, gson);
     * </pre>
     *
     * @param <T>      The type of entities in the list
     * @param filePath The absolute or relative path to the JSON file
     * @param entities The list of entities to serialize and save
     * @param gson     The Gson instance to use for serialization
     * @return {@code true} if save was successful, {@code false} otherwise
     */
    public static <T> boolean saveToFile(String filePath, List<T> entities, Gson gson) {
        try {
            // Ensure parent directory exists
            if (!ensureDirectoryExists(filePath)) {
                Logger.error("JsonFileHandler: Failed to create directory for: " + filePath);
                return false;
            }

            File file = new File(filePath);
            Logger.info("JsonFileHandler: Saving " + entities.size() + " entities to " + filePath);

            // Write JSON to file
            try (FileWriter writer = new FileWriter(file, false)) {
                gson.toJson(entities, writer);
                writer.flush();
            }

            Logger.info("JsonFileHandler: Successfully saved " + entities.size() + " entities to " + filePath);
            return true;

        } catch (IOException e) {
            Logger.error("JsonFileHandler: Error saving to file: " + filePath, e);
            return false;
        }
    }

    // =================================================================================================================
    // LOAD OPERATIONS
    // =================================================================================================================

    /**
     * Loads a list of entities from a JSON file.
     *
     * <p>This method handles file existence checks, empty file detection, and JSON parsing
     * errors gracefully. It returns an {@link Optional} to encourage null-safe handling.</p>
     *
     * <p><b>Behavior:</b></p>
     * <ul>
     *     <li>Returns {@code Optional.empty()} if file doesn't exist (not an error)</li>
     *     <li>Returns {@code Optional.empty()} if file is empty (not an error)</li>
     *     <li>Returns {@code Optional.empty()} if JSON parsing fails (logs error)</li>
     *     <li>Returns {@code Optional.of(list)} on successful load</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>
     * Gson gson = GsonProvider.createGson();
     * Type listType = new TypeToken&lt;ArrayList&lt;Order&gt;&gt;() {}.getType();
     *
     * Optional&lt;List&lt;Order&gt;&gt; loadedOrders = JsonFileHandler.loadFromFile(
     *     RepositoryPaths.ORDERS_PATH,
     *     listType,
     *     gson
     * );
     *
     * loadedOrders.ifPresent(orders -> {
     *     orders.forEach(order -> ordersById.put(order.getId(), order));
     *     Logger.info("Loaded " + orders.size() + " orders");
     * });
     * </pre>
     *
     * @param <T>      The type of entities in the list
     * @param filePath The absolute or relative path to the JSON file
     * @param listType The {@link Type} of the list (use {@link TypeToken})
     * @param gson     The Gson instance to use for deserialization
     * @return {@link Optional} containing the list of entities, or {@code Optional.empty()} if loading failed
     *
     * @see TypeToken
     */
    public static <T> Optional<List<T>> loadFromFile(String filePath, Type listType, Gson gson) {
        File file = new File(filePath);

        // File doesn't exist - this is normal on first run
        if (!file.exists()) {
            Logger.info("JsonFileHandler: File not found, starting with empty repository: " + filePath);
            return Optional.empty();
        }

        // File is empty - this is normal on first run
        if (file.length() == 0) {
            Logger.info("JsonFileHandler: File is empty, starting with empty repository: " + filePath);
            return Optional.empty();
        }

        try (Reader reader = new FileReader(file)) {
            List<T> loadedEntities = gson.fromJson(reader, listType);

            if (loadedEntities == null || loadedEntities.isEmpty()) {
                Logger.info("JsonFileHandler: No valid entities found in file: " + filePath);
                return Optional.empty();
            }

            Logger.info("JsonFileHandler: Successfully loaded " + loadedEntities.size() + " entities from " + filePath);
            return Optional.of(loadedEntities);

        } catch (IOException e) {
            Logger.error("JsonFileHandler: Error reading file: " + filePath, e);
            return Optional.empty();
        } catch (Exception e) {
            Logger.error("JsonFileHandler: Error parsing JSON from file: " + filePath, e);
            return Optional.empty();
        }
    }

    // =================================================================================================================
    // FILE UTILITY METHODS
    // =================================================================================================================

    /**
     * Ensures the parent directory of a file exists, creating it if necessary.
     *
     * <p>This method is called automatically by {@link #saveToFile}, but can also be
     * used standalone for pre-creating data directories.</p>
     *
     * <p><b>Example:</b></p>
     * <pre>
     * if (JsonFileHandler.ensureDirectoryExists("data/backups/users.json")) {
     *     // Directory "data/backups/" now exists
     * }
     * </pre>
     *
     * @param filePath The file path whose parent directory should be created
     * @return {@code true} if directory exists or was created successfully, {@code false} otherwise
     */
    public static boolean ensureDirectoryExists(String filePath) {
        File file = new File(filePath);
        File parentDir = file.getParentFile();

        if (parentDir == null) {
            return true; // No parent directory needed (file in current directory)
        }

        if (parentDir.exists()) {
            return true; // Directory already exists
        }

        // Try to create directory
        if (parentDir.mkdirs()) {
            Logger.info("JsonFileHandler: Created directory: " + parentDir.getAbsolutePath());
            return true;
        } else {
            Logger.error("JsonFileHandler: Failed to create directory: " + parentDir.getAbsolutePath());
            return false;
        }
    }

    /**
     * Checks if a file exists at the given path.
     *
     * <p><b>Example:</b></p>
     * <pre>
     * if (JsonFileHandler.fileExists(RepositoryPaths.USERS_PATH)) {
     *     // File exists, can load data
     * }
     * </pre>
     *
     * @param filePath The file path to check
     * @return {@code true} if the file exists, {@code false} otherwise
     */
    public static boolean fileExists(String filePath) {
        return new File(filePath).exists();
    }

    /**
     * Checks if a file exists and is not empty.
     *
     * <p><b>Example:</b></p>
     * <pre>
     * if (JsonFileHandler.isFileNotEmpty(RepositoryPaths.ADMINS_PATH)) {
     *     // File has data, proceed with loading
     * }
     * </pre>
     *
     * @param filePath The file path to check
     * @return {@code true} if file exists and has content, {@code false} otherwise
     */
    public static boolean isFileNotEmpty(String filePath) {
        File file = new File(filePath);
        return file.exists() && file.length() > 0;
    }
}