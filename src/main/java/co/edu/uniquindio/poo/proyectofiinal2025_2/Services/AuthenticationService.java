package co.edu.uniquindio.poo.proyectofiinal2025_2.Services;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Admin;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.AuthenticablePerson;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.DeliveryPerson;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Person;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.User;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Repositories.AdminRepository;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Repositories.DeliveryPersonRepository;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Repositories.UserRepository;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Util.PasswordUtility;

import java.util.Optional;

/**
 * <p>Provides a centralized service for authenticating all types of persons.</p>
 * <p>This class is a Singleton that handles login, logout, and session state for the
 * current person (User, Admin, or DeliveryPerson) using secure password verification.</p>
 */
public class AuthenticationService {

    // --- Singleton Implementation ---
    private static AuthenticationService instance;
    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final DeliveryPersonRepository deliveryPersonRepository;

    private Person currentPerson;

    /**
     * Private constructor to enforce the Singleton pattern and initialize repositories.
     */
    private AuthenticationService() {
        this.adminRepository = AdminRepository.getInstance();
        this.userRepository = UserRepository.getInstance();
        this.deliveryPersonRepository = DeliveryPersonRepository.getInstance();
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

    // ===========================
    // Session Management
    // ===========================

    /**
     * Logs out the currently authenticated person.
     */
    public void logout() {
        this.currentPerson = null;
        System.out.println("User logged out successfully");
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
     *
     * @param person The person to be set as the current authenticated user.
     */
    public void setAuthenticatedUser(Person person) {
        this.currentPerson = person;
        System.out.println("Authenticated user set: " +
                (person instanceof AuthenticablePerson ? ((AuthenticablePerson) person).getEmail() : "Unknown"));
    }

    /**
     * Checks if any person is currently logged in.
     *
     * @return true if a person is logged in, false otherwise.
     */
    public boolean isPersonLoggedIn() {
        return currentPerson != null;
    }

    // ===========================
    // Role Checks
    // ===========================

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

    // ===========================
    // Authentication
    // ===========================

    /**
     * Attempts to log in a person with the given credentials using secure password checking.
     * <p>It checks for a match in the Admin, User, and DeliveryPerson repositories.
     * For Users, it also verifies that the account is active (not disabled).</p>
     *
     * @param email             The email address to check.
     * @param plainTextPassword The plain text password to verify against the stored hash.
     * @return true if login is successful, false otherwise.
     */
    public boolean login(String email, String plainTextPassword) {
        System.out.println("Attempting login for: " + email);

        // Try Admin login first (admins don't have isActive check)
        if (tryAuthenticateAdmin(email, plainTextPassword)) {
            return true;
        }

        // Try User login (with isActive verification)
        if (tryAuthenticateUser(email, plainTextPassword)) {
            return true;
        }

        // Try DeliveryPerson login
        if (tryAuthenticateDeliveryPerson(email, plainTextPassword)) {
            return true;
        }

        System.out.println("Login failed for: " + email);
        return false;
    }

    /**
     * Attempts to authenticate as an Admin.
     */
    private boolean tryAuthenticateAdmin(String email, String plainTextPassword) {
        Optional<Admin> adminOpt = adminRepository.findByEmail(email);
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            if (PasswordUtility.checkPassword(plainTextPassword, admin.getPassword())) {
                this.currentPerson = admin;
                System.out.println("✅ Admin logged in successfully: " + email);
                return true;
            }
            System.out.println("❌ Invalid password for admin: " + email);
        }
        return false;
    }

    /**
     * Attempts to authenticate as a User.
     * ✅ Verifies that the user account is active before allowing login.
     */
    private boolean tryAuthenticateUser(String email, String plainTextPassword) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // ✅ Check if account is active
            if (!user.isActive()) {
                System.out.println("❌ Login failed: User account is disabled - " + email);
                return false;
            }

            // Check password
            if (PasswordUtility.checkPassword(plainTextPassword, user.getPassword())) {
                this.currentPerson = user;
                System.out.println("✅ User logged in successfully: " + email);
                return true;
            }
            System.out.println("❌ Invalid password for user: " + email);
        }
        return false;
    }

    /**
     * Attempts to authenticate as a DeliveryPerson.
     */
    private boolean tryAuthenticateDeliveryPerson(String email, String plainTextPassword) {
        Optional<DeliveryPerson> deliveryOpt = deliveryPersonRepository.findDeliveryPersonByEmail(email);
        if (deliveryOpt.isPresent()) {
            DeliveryPerson deliveryPerson = deliveryOpt.get();
            if (PasswordUtility.checkPassword(plainTextPassword, deliveryPerson.getPassword())) {
                this.currentPerson = deliveryPerson;
                System.out.println("✅ Delivery person logged in successfully: " + email);
                return true;
            }
            System.out.println("❌ Invalid password for delivery person: " + email);
        }
        return false;
    }
}