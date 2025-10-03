package co.edu.uniquindio.poo.proyectofiinal2025_2.Services;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Admin;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.AuthenticablePerson;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.DeliveryPerson;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Person;
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

    /**
     * Attempts to log in a person with the given credentials using secure password checking.
     * <p>It checks for a match in the Admin, User, and DeliveryPerson repositories.</p>
     *
     * @param email             The email address to check.
     * @param plainTextPassword The plain text password to verify against the stored hash.
     * @return true if login is successful, false otherwise.
     */
    public boolean login(String email, String plainTextPassword) {
        // Sequentially try to authenticate as Admin, User, or DeliveryPerson
        return tryAuthenticate(() -> adminRepository.findByEmail(email), plainTextPassword) ||
                tryAuthenticate(() -> userRepository.findByEmail(email), plainTextPassword) ||
                tryAuthenticate(() -> deliveryPersonRepository.findDeliveryPersonByEmail(email), plainTextPassword);
    }

    /**
     * A generic helper method to authenticate a person from an Optional.
     *
     * @param supplier         The Optional containing the person to authenticate.
     * @param plainTextPassword The plain text password to verify.
     * @param <T>               A type that extends AuthenticablePerson.
     * @return true if authentication is successful, false otherwise.
     */
    private <T extends AuthenticablePerson> boolean tryAuthenticate(java.util.function.Supplier<Optional<T>> supplier, String plainTextPassword) {
        Optional<T> personOpt = supplier.get();
        if (personOpt.isPresent()) {
            T person = personOpt.get();
            if (PasswordUtility.checkPassword(plainTextPassword, person.getPassword())) {
                this.currentPerson = (Person) person; // Cast to Person for storing in currentPerson
                return true;
            }
        }
        return false;
    }
}