package co.edu.uniquindio.poo.proyectofinal2025_2.Model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Represents a person within the system. This is the base abstract class
 * for more specific entities like users and delivery personnel.
 * It contains common personal information such as ID, name, last name,
 * email, and phone number.
 */
@Getter
@Setter
@ToString
public abstract class Person {

    private String id;
    private String name;
    private String lastName;
    private String email;
    private String phone;

    /**
     * Default constructor.
     */
    public Person() {
    }

    /**
     * Constructs a new Person with the specified details.
     *
     * @param id       The unique identifier for the person.
     * @param name     The person's first name.
     * @param lastName The person's last name.
     * @param email    The person's email address.
     * @param phone    The person's phone number.
     */
    public Person(String id, String name, String lastName, String email, String phone) {
        this.id = id;
        this.name = name;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
    }

    /**
     * Constructor for the builder pattern.
     * @param builder The builder instance to construct from.
     */
    protected Person(Builder<?> builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.lastName = builder.lastName;
        this.email = builder.email;
        this.phone = builder.phone;
    }

    // ======================================
    //               BUILDER
    // ======================================

    /**
     * Abstract base builder for Person and its subclasses.
     * Uses the "Curiously Recurring Template Pattern" (CRTP) for fluent inheritance.
     *
     * @param <T> The type of the concrete builder subclass.
     */
    public abstract static class Builder<T extends Builder<T>> {
        private String id;
        private String name;
        private String lastName;
        private String email;
        private String phone;

        public T withId(String id) {
            this.id = id;
            return self();
        }

        public T withName(String name) {
            this.name = name;
            return self();
        }

        public T withLastName(String lastName) {
            this.lastName = lastName;
            return self();
        }

        public T withEmail(String email) {
            this.email = email;
            return self();
        }

        public T withPhone(String phone) {
            this.phone = phone;
            return self();
        }

        /**
         * Returns the concrete builder instance (part of the CRTP pattern).
         * @return The concrete builder instance.
         */
        protected abstract T self();

        /**
         * Abstract method to create a new Person instance from the builder's properties.
         * @return A new Person instance.
         */
        public abstract Person build();
    }
}
