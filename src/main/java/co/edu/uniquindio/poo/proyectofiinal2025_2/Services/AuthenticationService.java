package co.edu.uniquindio.poo.proyectofiinal2025_2.Services;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Admin;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.DeliveryPerson;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Person;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.User;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Repositories.AdminRepository;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Repositories.DeliveryPersonRepository;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Repositories.UserRepository;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Utilities.PasswordUtility;

import java.util.Optional;

/**
 * <p>Provides a centralized service for authenticating all types of persons.</p>
 * <p>This class is a Singleton that handles login, logout, and session state for the
 * current person (User, Admin, or DeliveryPerson) using secure password verification.</p>
 */
public class AuthenticationService {

    private static AuthenticationService instance;
    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final DeliveryPersonRepository deliveryPersonRepository;

    private Person currentPerson;

    /**
     * Private constructor to initialize the service and its repository dependencies.
     */
    private AuthenticationService() {
        this.adminRepository = AdminRepository.getInstance();
        this.userRepository = UserRepository.getInstance();
        this.deliveryPersonRepository = DeliveryPersonRepository.getInstance();
    }

    /**
     * Returns the single instance of the authentication service.
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
     * Attempts to log in a person with the given credentials using secure password checking.
     * <p>It checks for a match in the Admin, User, and DeliveryPerson repositories.</p>
     *
     * @param email             The email address to check.
     * @param plainTextPassword The plain text password to verify against the stored hash.
     * @return true if login is successful, false otherwise.
     */
    public boolean login(String email, String plainTextPassword) {
        // Try to find an admin
        Optional<Admin> adminOpt = adminRepository.findByEmail(email);
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            if (PasswordUtility.checkPassword(plainTextPassword, admin.getPassword())) {
                this.currentPerson = admin;
                return true;
            }
        }

        // Try to find a user
        User user = userRepository.findByEmail(email);
        if (user != null) {
            if (PasswordUtility.checkPassword(plainTextPassword, user.getPassword())) {
                this.currentPerson = user;
                return true;
            }
        }

        // Try to find a delivery person
        Optional<DeliveryPerson> deliveryPersonOpt = deliveryPersonRepository.findDeliveryPersonByEmail(email);
        if (deliveryPersonOpt.isPresent()) {
            DeliveryPerson deliveryPerson = deliveryPersonOpt.get();
            if (PasswordUtility.checkPassword(plainTextPassword, deliveryPerson.getPassword())) {
                this.currentPerson = deliveryPerson;
                return true;
            }
        }

        // If no match is found
        return false;
    }

    /**
     * Logs out the currently authenticated person.
     */
    public void logout() {
        this.currentPerson = null;
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
     * Checks if the currently logged-in person is an administrator.
     *
     * @return true if the current person is an instance of Admin, false otherwise.
     */
    public boolean isCurrentPersonAdmin() {
        return currentPerson instanceof Admin;
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
     * Checks if any person is currently logged in.
     *
     * @return true if a person is logged in, false otherwise.
     */
    public boolean isPersonLoggedIn() {
        return currentPerson != null;
    }
}
