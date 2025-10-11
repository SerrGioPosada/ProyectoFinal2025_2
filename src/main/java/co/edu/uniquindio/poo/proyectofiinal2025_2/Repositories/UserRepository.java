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
     */
    private void saveToFile() {
        try (Writer writer = new FileWriter(FILE_PATH)) {
            gson.toJson(usersById.values(), writer);
        } catch (IOException e) {
            System.err.println("Error saving users to file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Loads the list of users from the users.json file when the application starts.
     */
    private void loadFromFile() {
        File file = new File(FILE_PATH);
        if (file.exists() && file.length() > 0) {
            try (Reader reader = new FileReader(file)) {
                Type listType = new TypeToken<ArrayList<User>>() {}.getType();
                List<User> loadedUsers = gson.fromJson(reader, listType);
                if (loadedUsers != null) {
                    for (User user : loadedUsers) {
                        usersById.put(user.getId(), user);
                        usersByEmail.put(user.getEmail().toLowerCase(), user);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error loading users from file: " + e.getMessage());
                e.printStackTrace();
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
        usersById.put(user.getId(), user);
        usersByEmail.put(user.getEmail().toLowerCase(), user);
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
}