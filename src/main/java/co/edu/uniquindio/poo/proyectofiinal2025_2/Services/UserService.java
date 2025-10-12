package co.edu.uniquindio.poo.proyectofiinal2025_2.Services;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Enums.PersonType;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Factory.PersonFactory;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.User;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.dto.PersonCreationData;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.dto.UserSummaryDTO;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Repositories.UserRepository;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Util.UtilModel.Logger;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Util.UtilModel.PasswordUtility;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service that handles business logic related to customers (Users).
 * Uses UserRepository for persistence and provides higher-level
 * operations such as registration, ensuring that passwords are securely hashed.
 */
public class UserService {

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

    /**
     * Orchestrates the registration of a new user from raw creation data.
     * This method handles the entire registration process:
     * 1. Validates that the email is not already in use.
     * 2. Generates a unique ID if not provided.
     * 3. Calls the PersonFactory to create a new User object.
     * 4. Hashes the user's password for secure storage.
     * 5. Persists the new user to the repository.
     *
     * @param data The PersonCreationData DTO containing the user's raw information.
     * @return true if registration is successful, false if the email already exists.
     */
    public boolean registerUser(PersonCreationData data) {
        try {
            if (userRepository.findByEmail(data.getEmail()).isPresent()) {
                Logger.warn("Registration failed: Email already exists - " + data.getEmail());
                return false;
            }

            if (data.getId() == null || data.getId().trim().isEmpty()) {
                data.setId(UUID.randomUUID().toString());
                Logger.debug("Generated new ID in service: " + data.getId());
            }

            User newUser = (User) PersonFactory.createPerson(PersonType.USER, data);

            if (newUser == null) {
                Logger.error("PersonFactory returned null user");
                return false;
            }

            if (newUser.getId() == null || newUser.getId().trim().isEmpty()) {
                String generatedId = UUID.randomUUID().toString();
                newUser.setId(generatedId);
                Logger.debug("Generated new ID for user: " + generatedId);
            }

            if (newUser.getEmail() == null || newUser.getEmail().trim().isEmpty()) {
                Logger.error("User email is null or empty after factory creation");
                return false;
            }

            String hashedPassword = PasswordUtility.hashPassword(newUser.getPassword());
            newUser.setPassword(hashedPassword);

            userRepository.addUser(newUser);

            Logger.info("User successfully registered: " + newUser.getEmail());
            return true;

        } catch (Exception e) {
            Logger.error("Error during user registration: " + e.getMessage(), e);
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
            if (userRepository.findByEmail(email).isPresent()) {
                Logger.info("OAuth registration: User already exists - " + email);
                return userRepository.findByEmail(email).get();
            }

            PersonCreationData data = new PersonCreationData();
            data.setId(UUID.randomUUID().toString());
            data.setName(name);
            data.setEmail(email);
            data.setPassword("oauth_google_user_" + System.currentTimeMillis());

            User newUser = (User) PersonFactory.createPerson(PersonType.USER, data);

            if (newUser == null) {
                Logger.error("PersonFactory returned null for OAuth user");
                return null;
            }

            if (newUser.getId() == null || newUser.getEmail() == null) {
                Logger.error("OAuth user has null ID or email");
                return null;
            }

            userRepository.addUser(newUser);

            Logger.info("OAuth user successfully registered: " + newUser.getEmail());
            return newUser;

        } catch (Exception e) {
            Logger.error("Error during OAuth user registration: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Retrieves all users as UserSummaryDTO objects for display purposes.
     *
     * @return List of UserSummaryDTO containing user summary information.
     */
    public List<UserSummaryDTO> getAllUsersSummary() {
        List<User> allUsers = userRepository.getUsers();
        Logger.debug("UserService: Retrieved " + allUsers.size() + " users from repository");

        List<UserSummaryDTO> summaries = allUsers.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());

        Logger.debug("UserService: Created " + summaries.size() + " UserSummaryDTO objects");
        return summaries;
    }

    /**
     * Converts a User entity to a UserSummaryDTO.
     *
     * @param user The user to convert.
     * @return The UserSummaryDTO object.
     */
    private UserSummaryDTO convertToSummaryDTO(User user) {
        return new UserSummaryDTO(
                user.getId(),
                user.getName() != null ? user.getName() : "",
                user.getLastName() != null ? user.getLastName() : "",
                user.getEmail() != null ? user.getEmail() : "",
                user.getPhone() != null ? user.getPhone() : "",
                user.getOrders() != null ? user.getOrders().size() : 0,
                user.getFrequentAddresses() != null ? user.getFrequentAddresses().size() : 0,
                user.getProfileImagePath(),
                user.isActive()
        );
    }

    /**
     * Deletes a user permanently from the system.
     *
     * @param userId The ID of the user to delete.
     * @return true if deletion was successful, false otherwise.
     */
    public boolean deleteUser(String userId) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                Logger.warn("User not found for deletion: " + userId);
                return false;
            }

            userRepository.removeUser(userId);
            Logger.info("User deleted successfully: " + userOpt.get().getEmail());
            return true;

        } catch (Exception e) {
            Logger.error("Error deleting user: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Disables a user account (soft delete).
     * The user cannot log in but their data is preserved.
     *
     * @param userId The ID of the user to disable.
     * @return true if successful, false otherwise.
     */
    public boolean disableUser(String userId) {
        return updateUserActiveStatus(userId, false, "disabled");
    }

    /**
     * Enables a previously disabled user account.
     *
     * @param userId The ID of the user to enable.
     * @return true if successful, false otherwise.
     */
    public boolean enableUser(String userId) {
        return updateUserActiveStatus(userId, true, "enabled");
    }

    /**
     * Updates the active status of a user.
     *
     * @param userId     The ID of the user.
     * @param isActive   The new active status.
     * @param actionName The action name for logging (enabled/disabled).
     * @return true if successful, false otherwise.
     */
    private boolean updateUserActiveStatus(String userId, boolean isActive, String actionName) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                Logger.warn("User not found for " + actionName + ": " + userId);
                return false;
            }

            User user = userOpt.get();
            user.setActive(isActive);
            userRepository.addUser(user);

            Logger.info("User " + actionName + " successfully: " + user.getEmail());
            return true;

        } catch (Exception e) {
            Logger.error("Error " + actionName + " user: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Gets a user by ID.
     *
     * @param userId The user ID to search for.
     * @return Optional containing the user if found.
     */
    public Optional<User> getUserById(String userId) {
        return userRepository.findById(userId);
    }

    /**
     * Retrieves all users in the system.
     *
     * @return A list of all users.
     */
    public List<User> getAllUsers() {
        return userRepository.getUsers();
    }
}