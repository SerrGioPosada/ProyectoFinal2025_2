package co.edu.uniquindio.poo.ProyectoFinal2025_2.Services;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Admin;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.AuthenticablePerson;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.DeliveryPerson;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Person;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.User;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Repositories.AdminRepository;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Repositories.DeliveryPersonRepository;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Repositories.UserRepository;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.TabStateManager;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.PasswordUtility;

import java.util.Optional;

/**
 * Provides a centralized service for authenticating all types of persons.
 * This class is a Singleton that handles login, logout, and session state for the
 * current person (User, Admin, or DeliveryPerson) using secure password verification.
 */
public class AuthenticationService {

    private static AuthenticationService instance;
    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final DeliveryPersonRepository deliveryPersonRepository;
    private Person currentPerson;

    /**
     * Package-private constructor for testing and dependency injection.
     * Allows injecting custom repository implementations.
     *
     * @param adminRepository The AdminRepository instance to use.
     * @param userRepository The UserRepository instance to use.
     * @param deliveryPersonRepository The DeliveryPersonRepository instance to use.
     */
    AuthenticationService(AdminRepository adminRepository, UserRepository userRepository,
                          DeliveryPersonRepository deliveryPersonRepository) {
        this.adminRepository = adminRepository;
        this.userRepository = userRepository;
        this.deliveryPersonRepository = deliveryPersonRepository;
    }

    /**
     * Private constructor to enforce the Singleton pattern and initialize repositories.
     * Delegates to the dependency injection constructor with singleton repositories.
     */
    private AuthenticationService() {
        this(AdminRepository.getInstance(), UserRepository.getInstance(),
             DeliveryPersonRepository.getInstance());
    }

    /**
     * Returns the single instance of the AuthenticationService.
     *
     * @return The singleton instance of AuthenticationService.
     */
    public static synchronized AuthenticationService getInstance() {
        if (instance == null) {
            instance = new AuthenticationService();
        }
        return instance;
    }

    /**
     * Allows setting a custom instance for testing purposes.
     * WARNING: This should only be used in test environments.
     *
     * @param customInstance The custom AuthenticationService instance.
     */
    static void setInstance(AuthenticationService customInstance) {
        instance = customInstance;
    }

    /**
     * Logs out the currently authenticated person.
     * Clears the current session and resets the authenticated user to null.
     * Also clears the TabStateManager's current user ID.
     */
    public void logout() {
        this.currentPerson = null;
        TabStateManager.clearCurrentUserId();
        Logger.info("User logged out successfully");
    }

    /**
     * Retrieves the currently logged-in person.
     *
     * @return The authenticated Person (User, Admin, or DeliveryPerson), or null if no one is logged in.
     */
    public Person getCurrentPerson() {
        return currentPerson;
    }

    /**
     * Returns the authenticated user as an AuthenticablePerson.
     * This is useful for accessing common authentication properties.
     *
     * @return The authenticated person, or null if not logged in.
     */
    public AuthenticablePerson getAuthenticatedUser() {
        return (AuthenticablePerson) currentPerson;
    }

    /**
     * Manually sets the authenticated person for the current session.
     * This is useful for external authentication flows like OAuth (e.g., Google Sign-In)
     * where the authentication is verified externally.
     * Also sets the user ID in TabStateManager for preference scoping.
     *
     * @param person The person to be set as the current authenticated user.
     */
    public void setAuthenticatedUser(Person person) {
        this.currentPerson = person;
        String email = person instanceof AuthenticablePerson
                ? ((AuthenticablePerson) person).getEmail()
                : "Unknown";

        // Set user ID in TabStateManager for preference scoping
        if (person != null) {
            TabStateManager.setCurrentUserId(person.getId());
        }

        Logger.info("Authenticated user set: " + email);
    }

    /**
     * Checks if any person is currently logged in.
     *
     * @return true if a person is logged in, false otherwise.
     */
    public boolean isPersonLoggedIn() {
        return currentPerson != null;
    }

    /**
     * Checks if the currently logged-in person is an administrator.
     *
     * @return true if the current person is an instance of Admin, false otherwise.
     */
    public boolean isCurrentPersonAdmin() {
        return currentPerson instanceof Admin;
    }

    /**
     * Checks if the currently logged-in person is a regular user.
     *
     * @return true if the current person is an instance of User, false otherwise.
     */
    public boolean isCurrentPersonUser() {
        return currentPerson instanceof User;
    }

    /**
     * Checks if the currently logged-in person is a delivery person.
     *
     * @return true if the current person is an instance of DeliveryPerson, false otherwise.
     */
    public boolean isCurrentPersonDelivery() {
        return currentPerson instanceof DeliveryPerson;
    }

    /**
     * Attempts to log in a person with the given credentials using secure password checking.
     * It checks for a match in the Admin, User, and DeliveryPerson repositories in that order.
     * For Users, it also verifies that the account is active (not disabled).
     *
     * @param email             The email address to check.
     * @param plainTextPassword The plain text password to verify against the stored hash.
     * @return true if login is successful, false otherwise.
     */
    public boolean login(String email, String plainTextPassword) {
        Logger.info("Attempting login for: " + email);

        if (tryAuthenticateAdmin(email, plainTextPassword) ||
                tryAuthenticateUser(email, plainTextPassword) ||
                tryAuthenticateDeliveryPerson(email, plainTextPassword)) {
            return true;
        }

        Logger.warn("Login failed for: " + email);
        return false;
    }

    /**
     * Attempts to authenticate as an Admin.
     * Checks if the email exists in the admin repository and verifies the password.
     * Sets the user ID in TabStateManager for preference scoping.
     *
     * @param email             The email address.
     * @param plainTextPassword The plain text password.
     * @return true if authentication is successful, false otherwise.
     */
    private boolean tryAuthenticateAdmin(String email, String plainTextPassword) {
        Optional<Admin> adminOpt = adminRepository.findByEmail(email);

        if (adminOpt.isEmpty()) {
            return false;
        }

        Admin admin = adminOpt.get();
        if (PasswordUtility.checkPassword(plainTextPassword, admin.getPassword())) {
            this.currentPerson = admin;
            TabStateManager.setCurrentUserId(admin.getId());
            Logger.info("Admin logged in successfully: " + email);
            return true;
        }

        Logger.warn("Invalid password for admin: " + email);
        return false;
    }

    /**
     * Attempts to authenticate as a User.
     * Verifies that the user account is active before allowing login.
     * Inactive accounts cannot log in to the system.
     * Sets the user ID in TabStateManager for preference scoping.
     *
     * @param email             The email address.
     * @param plainTextPassword The plain text password.
     * @return true if authentication is successful, false otherwise.
     */
    private boolean tryAuthenticateUser(String email, String plainTextPassword) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();

        if (!user.isActive()) {
            Logger.warn("Login failed: User account is disabled - " + email);
            return false;
        }

        if (PasswordUtility.checkPassword(plainTextPassword, user.getPassword())) {
            this.currentPerson = user;
            TabStateManager.setCurrentUserId(user.getId());
            Logger.info("User logged in successfully: " + email);
            return true;
        }

        Logger.warn("Invalid password for user: " + email);
        return false;
    }

    /**
     * Attempts to authenticate as a DeliveryPerson.
     * Checks if the email exists in the delivery person repository and verifies the password.
     * Sets the user ID in TabStateManager for preference scoping.
     *
     * @param email             The email address.
     * @param plainTextPassword The plain text password.
     * @return true if authentication is successful, false otherwise.
     */
    private boolean tryAuthenticateDeliveryPerson(String email, String plainTextPassword) {
        Optional<DeliveryPerson> deliveryOpt = deliveryPersonRepository.findDeliveryPersonByEmail(email);

        if (deliveryOpt.isEmpty()) {
            return false;
        }

        DeliveryPerson deliveryPerson = deliveryOpt.get();
        if (PasswordUtility.checkPassword(plainTextPassword, deliveryPerson.getPassword())) {
            this.currentPerson = deliveryPerson;
            TabStateManager.setCurrentUserId(deliveryPerson.getId());
            Logger.info("Delivery person logged in successfully: " + email);
            return true;
        }

        Logger.warn("Invalid password for delivery person: " + email);
        return false;
    }
}
