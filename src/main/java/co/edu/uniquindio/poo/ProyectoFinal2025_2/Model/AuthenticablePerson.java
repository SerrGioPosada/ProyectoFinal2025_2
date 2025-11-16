package co.edu.uniquindio.poo.ProyectoFinal2025_2.Model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * <p>Represents an abstract person who can be authenticated in the system.</p>
 * <p>This class extends the base {@link Person} class and adds a password field and a profile image path,
 * serving as a common ancestor for any entity that requires login credentials,
 * such as {@link User}, {@link Admin}, and {@link DeliveryPerson}.</p>
 */
@Getter
@Setter
@ToString(callSuper = true)
public abstract class AuthenticablePerson extends Person {

    private String password; // Encrypted password for user authentication
    private String profileImagePath; // Path to the profile image file

    /**
     * Default constructor.
     */
    public AuthenticablePerson() {
        super();
    }

    /**
     * Constructs a new AuthenticablePerson with the specified details.
     *
     * @param id               The unique identifier for the person.
     * @param name             The person's first name.
     * @param lastName         The person's last name.
     * @param email            The person's email address.
     * @param phone            The person's phone number.
     * @param password         The person's password for authentication (should be a hash).
     * @param profileImagePath The path to the person's profile image.
     */
    public AuthenticablePerson(String id, String name, String lastName, String email, String phone, String password, String profileImagePath) {
        super(id, name, lastName, email, phone);
        this.password = password;
        this.profileImagePath = profileImagePath;
    }

    /**
     * Constructor for the builder pattern.
     * @param builder The builder instance to construct from.
     */
    protected AuthenticablePerson(Builder<?> builder) {
        super(builder);
        this.password = builder.password;
        this.profileImagePath = builder.profileImagePath;
    }

    // ======================================
    //               BUILDER
    // ======================================

    /**
     * Abstract builder for AuthenticablePerson. Extends the Person builder.
     *
     * @param <T> The type of the concrete builder subclass.
     */
    public abstract static class Builder<T extends Builder<T>> extends Person.Builder<T> {
        private String password;
        private String profileImagePath;

        public T withPassword(String password) {
            this.password = password;
            return self();
        }

        public T withProfileImagePath(String profileImagePath) {
            this.profileImagePath = profileImagePath;
            return self();
        }

        /**
         * Abstract method to create a new AuthenticablePerson instance from the builder's properties.
         * @return A new AuthenticablePerson instance.
         */
        @Override
        public abstract AuthenticablePerson build();
    }
}
