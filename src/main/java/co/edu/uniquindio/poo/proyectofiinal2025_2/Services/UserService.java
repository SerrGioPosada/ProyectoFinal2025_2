package co.edu.uniquindio.poo.proyectofiinal2025_2.Services;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.User;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Repositories.UserRepository;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Utilities.PasswordUtility;

/**
 * <p>Service that handles business logic related to customers (Users).</p>
 * <p>Uses UserRepository for persistence and provides higher-level
 * operations such as registration, ensuring that passwords are securely hashed.</p>
 */
public class UserService {

    private final UserRepository userRepository;

    /**
     * Constructs a UserService with the given repository.
     *
     * @param userRepository repository instance
     */
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ======================
    // User management
    // ======================

    /**
     * Registers a new user, hashing their password for secure storage.
     *
     * @param user The user object containing plain text password to register.
     * @return true if registration is successful, false if the email already exists.
     */
    public boolean registerUser(User user) {
        if (userRepository.findByEmail(user.getEmail()) != null) {
            return false; // Email already registered
        }

        // Security: Hash the plain text password before saving the user.
        String hashedPassword = PasswordUtility.hashPassword(user.getPassword());
        user.setPassword(hashedPassword);

        userRepository.addUser(user);
        return true;
    }

    public User signup(){
        return null;
    }

    // Other user-specific business logic methods will go here.
}
