package co.edu.uniquindio.poo.proyectofiinal2025_2.Services;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.User;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Repositories.UserRepository;

/**
 * Service that handles business logic related to users.
 * <p>
 * Uses UserRepository for persistence and provides higher-level
 * operations such as login, logout, and registration.
 * </p>
 */
public class UserService {

    private final UserRepository userRepository;
    private User currentUser;

    /**
     * Constructs a UserService with the given repository.
     *
     * @param userRepository repository instance
     */
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ======================
    // Authentication
    // ======================

    /**
     * Attempts to log in with the given credentials.
     *
     * @param email    user email
     * @param password user password
     * @return true if login successful, false otherwise
     */
    public boolean login(String email, String password) {
        User user = userRepository.findByEmail(email);
        if (user != null && user.getPassword().equals(password)) {
            currentUser = user;
            return true;
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

    // ======================
    // User management
    // ======================

    /**
     * Registers a new user if the email is not already used.
     *
     * @param user the user to register
     * @return true if registration successful, false if email already exists
     */
    public boolean registerUser(User user) {
        if (userRepository.findByEmail(user.getEmail()) != null) {
            return false; // Email already registered
        }
        userRepository.addUser(user);
        return true;
    }
}
