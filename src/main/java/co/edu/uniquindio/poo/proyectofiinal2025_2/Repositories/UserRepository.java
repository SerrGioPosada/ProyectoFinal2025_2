package co.edu.uniquindio.poo.proyectofiinal2025_2.Repositories;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.User;
import java.util.LinkedList;
import java.util.List;

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
     * Finds a user by email.
     *
     * @param email email to search
     * @return user if found, otherwise null
     */
    public User findByEmail(String email) {
        for (User user : users) {
            if (user.getEmail().equalsIgnoreCase(email)) {
                return user;
            }
        }
        return null;
    }
}
