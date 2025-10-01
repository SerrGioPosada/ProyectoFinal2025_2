package co.edu.uniquindio.poo.proyectofiinal2025_2.Repositories;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.User;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Repository that manages the persistence of users in memory.
 * <p>
 * Implements the Singleton pattern to ensure a single instance
 * across the application. It is responsible only for storing and
 * retrieving users, not business logic.
 * </p>
 */
public class UserRepository {


    private static UserRepository instance;
    private final List<User> users;

    /**
     * Private constructor initializes the repository.
     */
    private UserRepository() {
        this.users = new LinkedList<>();
    }

    /**
     * Returns the singleton instance of the repository.
     *
     * @return unique instance of UserRepository
     */
    public static UserRepository getInstance() {
        if (instance == null) {
            instance = new UserRepository();
        }
        return instance;
    }

    // ======================
    // User persistence
    // ======================

    /**
     * Registers a new user in the repository.
     *
     * @param user the user to add
     */
    public void addUser(User user) {
        users.add(user);
    }

    /**
     * Gets all registered users.
     *
     * @return list of users
     */
    public List<User> getUsers() {
        return users;
    }

    /**
     * Finds a user by email Address.
     *
     * @param email The email to search for.
     * @return An Optional containing the user if found, otherwise an empty Optional.
     */
    public Optional<User> findByEmail(String email) {
        return users.stream()
                .filter(user -> user.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }
}
