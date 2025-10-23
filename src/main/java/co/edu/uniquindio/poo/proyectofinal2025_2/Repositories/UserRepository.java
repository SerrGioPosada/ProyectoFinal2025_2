package co.edu.uniquindio.poo.proyectofinal2025_2.Repositories;

import co.edu.uniquindio.poo.proyectofinal2025_2.Model.User;
import co.edu.uniquindio.poo.proyectofinal2025_2.Util.UtilModel.Logger;
import co.edu.uniquindio.poo.proyectofinal2025_2.Util.UtilRepository.GsonProvider;
import co.edu.uniquindio.poo.proyectofinal2025_2.Util.UtilRepository.JsonFileHandler;
import co.edu.uniquindio.poo.proyectofinal2025_2.Util.UtilRepository.RepositoryPaths;
import co.edu.uniquindio.poo.proyectofinal2025_2.Util.UtilRepository.RepositoryValidator;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
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

    // =================================================================================================================
    // CONSTANTS AND FIELDS
    // =================================================================================================================

    private final Gson gson = GsonProvider.createGsonWithPrettyPrinting();

    private static UserRepository instance;

    private final Map<String, User> usersById;
    private final Map<String, User> usersByEmail;

    // =================================================================================================================
    // CONSTRUCTOR (Singleton)
    // =================================================================================================================

    /**
     * Private constructor that loads data from the file upon initialization.
     */
    private UserRepository() {
        this.usersById = new HashMap<>();
        this.usersByEmail = new HashMap<>();
        loadFromFile();
        Logger.info("UserRepository initialized. Users loaded: " + usersById.size());
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

    // =================================================================================================================
    // FILE I/O OPERATIONS
    // =================================================================================================================

    /**
     * Saves the current list of users to the users.json file.
     * This method ensures that the parent directory exists before writing the file.
     */
    private void saveToFile() {
        List<User> userList = new ArrayList<>(usersById.values());
        JsonFileHandler.saveToFile(RepositoryPaths.USERS_PATH, userList, gson);
    }

    /**
     * Loads the list of users from the users.json file when the application starts.
     * This method is now more robust against parsing errors.
     */
    private void loadFromFile() {
        Type listType = new TypeToken<ArrayList<User>>() {}.getType();
        Optional<List<User>> loadedUsers = JsonFileHandler.loadFromFile(
                RepositoryPaths.USERS_PATH,
                listType,
                gson
        );

        loadedUsers.ifPresent(users -> {
            Logger.info("Loading " + users.size() + " users from file...");
            for (User user : users) {
                if (RepositoryValidator.validateEntityWithIdAndEmail(user, user.getId(), user.getEmail(), "User")) {
                    usersById.put(user.getId(), user);
                    usersByEmail.put(user.getEmail().toLowerCase(), user);
                } else {
                    Logger.warning("Warning: Skipping corrupt user entry in JSON file");
                }
            }
            Logger.info("Successfully loaded " + usersById.size() + " users");
        });
    }

    // =================================================================================================================
    // CRUD OPERATIONS
    // =================================================================================================================

    /**
     * Adds a new user to the repository.
     * The user is added to both in-memory maps and the updated list is persisted to the file.
     *
     * @param user the user to add to the repository
     */
    public void addUser(User user) {
        if (!RepositoryValidator.validateEntityWithIdAndEmail(user, user.getId(), user.getEmail(), "User")) {
            return;
        }

        Logger.info("Adding user: " + user.getEmail() + " (ID: " + user.getId() + ")");

        usersById.put(user.getId(), user);
        usersByEmail.put(user.getEmail().toLowerCase(), user);

        Logger.info("Total users in memory: " + usersById.size());

        saveToFile();
    }

    /**
     * Removes a user from the repository by their ID.
     * The user is removed from both maps and changes are persisted.
     *
     * @param userId the ID of the user to remove.
     */
    public void removeUser(String userId) {
        if (!RepositoryValidator.validateId(userId, "User")) {
            return;
        }

        User userToRemove = usersById.get(userId);
        if (userToRemove != null) {
            Logger.info("Removing user: " + userToRemove.getEmail());
            usersById.remove(userId);
            usersByEmail.remove(userToRemove.getEmail().toLowerCase());
            saveToFile();
        }
    }

    // =================================================================================================================
    // QUERY METHODS
    // =================================================================================================================

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
        if (!RepositoryValidator.validateEmail(email, "User")) {
            return Optional.empty();
        }
        return Optional.ofNullable(usersByEmail.get(email.toLowerCase()));
    }

    /**
     * Finds a user by their unique ID with O(1) complexity.
     *
     * @param id the ID to search for
     * @return an {@link Optional} containing the user if found, or an empty Optional.
     */
    public Optional<User> findById(String id) {
        if (!RepositoryValidator.validateId(id, "User")) {
            return Optional.empty();
        }
        return Optional.ofNullable(usersById.get(id));
    }

    /**
     * For debugging: prints all users currently in memory
     */
    public void printAllUsers() {
        Logger.info("=== Current Users in Memory ===");
        usersById.values().forEach(user ->
                Logger.info("- " + user.getEmail() + " (ID: " + user.getId() + ")")
        );
        Logger.info("Total: " + usersById.size() + " users");
    }
}
