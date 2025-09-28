package co.edu.uniquindio.poo.proyectofiinal2025_2.Model;

/**
 * <p>Represents an abstract person who can be authenticated in the system.</p>
 * <p>This class extends the base {@link Person} class and adds a password field,
 * serving as a common ancestor for any entity that requires login credentials,
 * such as {@link User}, {@link Admin}, and {@link DeliveryPerson}.</p>
 */
public abstract class AuthenticablePerson extends Person {

    private String password;

    /**
     * Constructs a new AuthenticablePerson with the specified details.
     *
     * @param id       The unique identifier for the person.
     * @param name     The person's first name.
     * @param lastName The person's last name.
     * @param email    The person's email address.
     * @param phone    The person's phone number.
     * @param password The person's password for authentication (should be a hash).
     */
    public AuthenticablePerson(String id, String name, String lastName, String email, String phone, String password) {
        super(id, name, lastName, email, phone);
        this.password = password;
    }

    // =================================
    // Getters and Setters
    // =================================

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
