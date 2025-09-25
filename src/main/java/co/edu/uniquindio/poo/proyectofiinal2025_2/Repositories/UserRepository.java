package co.edu.uniquindio.poo.proyectofiinal2025_2.Repositories;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.User;
import javafx.scene.image.Image;

import java.util.LinkedList;
import java.util.List;

/**
 * Repository that manages users in the system.
 * <p>
 * Implements the Singleton pattern to ensure a single instance
 * across the application. Stores all registered users and the
 * currently logged-in user.
 * </p>
 */
public class UserRepository {

    /**
     * Singleton instance of the repository.
     */
    private static UserRepository instance;

    /**
     * List of all registered users.
     */
    private final List<User> users;

    /**
     * Reference to the currently logged-in user.
     */
    private User currentUser;

    /**
     * Private constructor initializes the repository with a test admin user.
     */
    private UserRepository() {
        users = new LinkedList<>();

        // Default test user (Admin)
        users.add(new User(
                "Admin",             // First name
                "System",                 // Last name
                "admin@email.com",        // Email
                "1234",                   // Password
                new Image(getClass().getResource(
                        "/co/edu/uniquindio/poo/proyectofiinal2025_2/Images/default-userImage.png"
                ).toExternalForm())
        ));
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
    // User management
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

    // ======================
    // Authentication
    // ======================

    /**
     * Attempts to log in with the given credentials.
     *
     * @param email    user email
     * @param password user password
     * @return true if credentials are valid and user is logged in
     */
    public boolean login(String email, String password) {
        for (User user : users) {
            if (user.getCorreo().equals(email) && user.getPassword().equals(password)) {
                currentUser = user;
                return true;
            }
        }
        return false;
    }

    /**
     * Logs out the current user.
     */
    public void logout() {
        currentUser = null;
    }

    /**
     * Gets the currently logged-in user.
     *
     * @return current user, or null if no user is logged in
     */
    public User getCurrentUser() {
        return currentUser;
    }
}
