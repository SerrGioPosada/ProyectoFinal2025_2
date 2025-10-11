package co.edu.uniquindio.poo.proyectofiinal2025_2.Repositories;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.User;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Util.Adapter.LocalDateTimeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Manages the persistence and retrieval of User entities using HashMaps for fast lookups by ID and email.
 * <p>This class is implemented as a Singleton and saves data to a local JSON file.</p>
 */
public class UserRepository {

    // --- Attributes for Persistence ---
    private static final String FILE_PATH = "data/users.json";
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .setPrettyPrinting() // Para ver mejor el JSON
            .create();

    private static UserRepository instance;

    private final Map<String, User> usersById;
    private final Map<String, User> usersByEmail;

    /**
     * Private constructor that loads data from the file upon initialization.
     */
    private UserRepository() {
        this.usersById = new HashMap<>();
        this.usersByEmail = new HashMap<>();
        loadFromFile();
        System.out.println("UserRepository initialized. Users loaded: " + usersById.size());
    }

    /**
     * Returns the singleton instance of the repository.
     *
     * @return the unique instance of {@code UserRepository}
     */
    public static synchronized UserRepository getInstance() {
        if (instance == null) {
            instance = new UserRepository();
        }
        return instance;
    }

    // ======================
    // Private file handling methods
    // ======================

    /**
     * Saves the current list of users to the users.json file.
     * This method ensures that the parent directory exists before writing the file.
     */
    private void saveToFile() {
        try {
            File file = new File(FILE_PATH);
            File parentDir = file.getParentFile();

            // Create the parent directory if it doesn't exist
            if (parentDir != null && !parentDir.exists()) {
                if (!parentDir.mkdirs()) {
                    System.err.println("Failed to create directory: " + parentDir.getAbsolutePath());
                    return;
                }
            }

            // Crear una lista ordenada para guardar
            List<User> userList = new ArrayList<>(usersById.values());

            System.out.println("Saving " + userList.size() + " users to file...");

            // Write the JSON file con try-with-resources para asegurar el cierre
            try (FileWriter writer = new FileWriter(file, false)) { // false = sobrescribir
                gson.toJson(userList, writer);
                writer.flush(); // Asegurar que se escriba
            } // ✅ Cierra el try-with-resources

            System.out.println("Users saved successfully to " + FILE_PATH);

        } catch (IOException e) {
            System.err.println("Error saving users to file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Loads the list of users from the users.json file when the application starts.
     * This method is now more robust against parsing errors.
     */
    private void loadFromFile() {
        File file = new File(FILE_PATH);

        if (!file.exists()) {
            System.out.println("No users.json file found. Starting with empty repository.");
            return;
        }

        if (file.length() == 0) {
            System.out.println("users.json file is empty. Starting with empty repository.");
            return;
        }

        try (Reader reader = new FileReader(file)) {
            Type listType = new TypeToken<ArrayList<User>>() {}.getType();
            List<User> loadedUsers = gson.fromJson(reader, listType);

            if (loadedUsers != null && !loadedUsers.isEmpty()) {
                System.out.println("Loading " + loadedUsers.size() + " users from file...");

                for (User user : loadedUsers) {
                    // Defensive check to prevent NullPointerExceptions from corrupt data
                    if (user != null && user.getId() != null && user.getEmail() != null) {
                        usersById.put(user.getId(), user);
                        usersByEmail.put(user.getEmail().toLowerCase(), user);
                    } else {
                        System.err.println("Warning: Skipping corrupt user entry in JSON file");
                    }
                }

                System.out.println("Successfully loaded " + usersById.size() + " users");
            } else {
                System.out.println("No valid users found in file");
            }
        } catch (Exception e) {
            System.err.println("Error loading or parsing users from file: " + e.getMessage());
            e.printStackTrace();

            // Crear backup del archivo corrupto
            try {
                File backupFile = new File(FILE_PATH + ".backup");
                if (file.renameTo(backupFile)) {
                    System.out.println("Corrupt file backed up to: " + backupFile.getAbsolutePath());
                }
            } catch (Exception backupError) {
                System.err.println("Could not create backup of corrupt file");
            }
        }
    }

    // ======================
    // Handling methods
    // ======================

    /**
     * Adds a new user to the repository.
     * The user is added to both in-memory maps and the updated list is persisted to the file.
     *
     * @param user the user to add to the repository
     */
    public void addUser(User user) {
        if (user == null) {
            System.err.println("ERROR: Cannot add null user");
            return;
        }

        if (user.getId() == null) {
            System.err.println("ERROR: User ID is null for email: " + user.getEmail());
            System.err.println("User object: " + user);
            return;
        }

        if (user.getEmail() == null) {
            System.err.println("ERROR: User email is null for ID: " + user.getId());
            System.err.println("User object: " + user);
            return;
        }

        System.out.println("Adding user: " + user.getEmail() + " (ID: " + user.getId() + ")");

        usersById.put(user.getId(), user);
        usersByEmail.put(user.getEmail().toLowerCase(), user);

        System.out.println("Total users in memory: " + usersById.size());

        saveToFile();
    }

    /**
     * Removes a user from the repository by their ID.
     * The user is removed from both maps and changes are persisted.
     *
     * @param userId the ID of the user to remove.
     */
    public void removeUser(String userId) {
        User userToRemove = usersById.get(userId);
        if (userToRemove != null) {
            System.out.println("Removing user: " + userToRemove.getEmail());
            usersById.remove(userId);
            usersByEmail.remove(userToRemove.getEmail().toLowerCase());
            saveToFile();
        }
    }

    // ======================
    // Query Methods
    // ======================

    /**
     * Retrieves all users stored in the repository.
     *
     * @return a new list containing all users in the repository
     */
    public List<User> getUsers() {
        return new ArrayList<>(usersById.values());
    }

    /**
     * Finds a user by their email address with O(1) complexity.
     *
     * @param email the email to search for
     * @return an {@link Optional} containing the user if found, or an empty Optional.
     */
    public Optional<User> findByEmail(String email) {
        return Optional.ofNullable(usersByEmail.get(email.toLowerCase()));
    }

    /**
     * Finds a user by their unique ID with O(1) complexity.
     *
     * @param id the ID to search for
     * @return an {@link Optional} containing the user if found, or an empty Optional.
     */
    public Optional<User> findById(String id) {
        return Optional.ofNullable(usersById.get(id));
    }

    /**
     * For debugging: prints all users currently in memory
     */
    public void printAllUsers() {
        System.out.println("=== Current Users in Memory ===");
        usersById.values().forEach(user ->
                System.out.println("- " + user.getEmail() + " (ID: " + user.getId() + ")")
        );
        System.out.println("Total: " + usersById.size() + " users");
    }
}