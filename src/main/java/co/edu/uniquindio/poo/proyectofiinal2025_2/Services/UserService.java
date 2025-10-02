package co.edu.uniquindio.poo.proyectofiinal2025_2.Services;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.User;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Repositories.UserRepository;

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
     * Orchestrates the registration of a new user from raw creation data.
     * <p>
     * This method handles the entire registration process:
     * 1. Validates that the email is not already in use.
     * 2. Calls the PersonFactory to create a new User object.
     * 3. Hashes the user's password for secure storage.
     * 4. Persists the new user to the repository.
     * </p>
     *
     * @param data The PersonCreationData DTO containing the user's raw information.
     * @return true if registration is successful, false if the email already exists.

    public boolean registerUser(PersonCreationData data) {
        // 1. Validate that the email doesn't already exist.
        if (userRepository.findByEmail(data.getEmail()).isPresent()) {
            return false; // Email is already registered.
        }

        // 2. Call the factory to create the User object.
        User newUser = (User) personFactory.createPerson(PersonType.USER, data);

        // 3. Hash the password of the newly created object.
        String hashedPassword = PasswordUtility.hashPassword(newUser.getPassword());
        newUser.setPassword(hashedPassword);

        // 4. Save the final user to the repository.
        userRepository.addUser(newUser);

        return true;
    }
 */


    public User signup(){
        return null;
    }

    // Other user-specific business logic methods will go here.
}
