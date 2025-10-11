package co.edu.uniquindio.poo.proyectofiinal2025_2.Services;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Enums.PersonType;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Factory.PersonFactory;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.User;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.dto.PersonCreationData;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Repositories.UserRepository;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Util.PasswordUtility;

import java.util.UUID;

/**
 * <p>Service that handles business logic related to customers (Users).</p>
 * <p>Uses UserRepository for persistence and provides higher-level
 * operations such as registration, ensuring that passwords are securely hashed.</p>
 */
public class UserService {

    // --- Singleton Implementation ---
    private static UserService instance;
    private final UserRepository userRepository;

    /**
     * Private constructor to enforce the Singleton pattern.
     */
    private UserService() {
        this.userRepository = UserRepository.getInstance();
    }

    /**
     * Returns the single instance of the UserService.
     *
     * @return The singleton instance of UserService.
     */
    public static synchronized UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }

    // ===========================
    // User Management
    // ===========================

    /**
     * Orchestrates the registration of a new user from raw creation data.
     * <p>
     * This method handles the entire registration process:
     * 1. Validates that the email is not already in use.
     * 2. Calls the PersonFactory to create a new User object.
     * 3. Ensures the user has a valid ID (generates one if missing).
     * 4. Hashes the user's password for secure storage.
     * 5. Persists the new user to the repository.
     * </p>
     *
     * @param data The PersonCreationData DTO containing the user's raw information.
     * @return true if registration is successful, false if the email already exists.
     */
    public boolean registerUser(PersonCreationData data) {
        try {
            // 1. Validate that the email doesn't already exist.
            if (userRepository.findByEmail(data.getEmail()).isPresent()) {
                System.out.println("Registration failed: Email already exists - " + data.getEmail());
                return false;
            }

            // 1.5. Generate ID if not provided
            if (data.getId() == null || data.getId().trim().isEmpty()) {
                data.setId(UUID.randomUUID().toString());
                System.out.println("Generated new ID in service: " + data.getId());
            }

            // 2. Call the factory to create the User object.
            User newUser = (User) PersonFactory.createPerson(PersonType.USER, data);

            // CRITICAL: Verify the user object was created properly
            if (newUser == null) {
                System.err.println("ERROR: PersonFactory returned null user");
                return false;
            }

            System.out.println("User created by factory: " + newUser.getEmail());
            System.out.println("User ID from factory: " + newUser.getId());

            // 3. Ensure the user has an ID (generate if missing)
            if (newUser.getId() == null || newUser.getId().trim().isEmpty()) {
                String generatedId = UUID.randomUUID().toString();
                newUser.setId(generatedId);
                System.out.println("Generated new ID for user: " + generatedId);
            }

            // Double-check email is set
            if (newUser.getEmail() == null || newUser.getEmail().trim().isEmpty()) {
                System.err.println("ERROR: User email is null or empty after factory creation");
                return false;
            }

            // 4. Hash the password of the newly created object.
            String hashedPassword = PasswordUtility.hashPassword(newUser.getPassword());
            newUser.setPassword(hashedPassword);

            // 5. Final validation before saving
            System.out.println("Final user before save:");
            System.out.println("  - ID: " + newUser.getId());
            System.out.println("  - Email: " + newUser.getEmail());
            System.out.println("  - Name: " + newUser.getName());

            // 6. Save the final user to the repository.
            userRepository.addUser(newUser);

            System.out.println("User successfully registered: " + newUser.getEmail());
            return true;

        } catch (Exception e) {
            System.err.println("ERROR during user registration: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Registers a user from an external OAuth provider (e.g., Google).
     * This method is similar to registerUser but doesn't hash the password
     * since OAuth users don't use traditional passwords.
     *
     * @param name  The user's name from the OAuth provider.
     * @param email The user's email from the OAuth provider.
     * @return The newly created User object, or null if registration fails.
     */
    public User registerOAuthUser(String name, String email) {
        try {
            // 1. Check if user already exists
            if (userRepository.findByEmail(email).isPresent()) {
                System.out.println("OAuth registration: User already exists - " + email);
                return userRepository.findByEmail(email).get();
            }

            // 2. Create PersonCreationData with generated ID
            PersonCreationData data = new PersonCreationData();
            data.setId(UUID.randomUUID().toString());
            data.setName(name);
            data.setEmail(email);
            data.setPassword("oauth_google_user_" + System.currentTimeMillis());

            // 3. Create User object via factory
            User newUser = (User) PersonFactory.createPerson(PersonType.USER, data);

            if (newUser == null) {
                System.err.println("ERROR: PersonFactory returned null for OAuth user");
                return null;
            }

            // 4. Validate user data
            if (newUser.getId() == null || newUser.getEmail() == null) {
                System.err.println("ERROR: OAuth user has null ID or email");
                return null;
            }

            // 5. Save to repository (password is already set, no need to hash for OAuth)
            userRepository.addUser(newUser);

            System.out.println("OAuth user successfully registered: " + newUser.getEmail());
            return newUser;

        } catch (Exception e) {
            System.err.println("ERROR during OAuth user registration: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}