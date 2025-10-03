package co.edu.uniquindio.poo.proyectofiinal2025_2.Model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Represents a person within the system. This is the base abstract class
 * for more specific entities like users and delivery personnel.
 * It contains common personal information such as ID, name, last name,
 * email, and phone number.
 */

@Getter
@Setter
@ToString
@SuperBuilder

public abstract class Person {

    private String id;        // Unique identifier for the person
    private String name;      // First name of the person
    private String lastName;  // Last name of the person
    private String email;     // Email address
    private String phone;     // Phone number

    /**
     * Default constructor for Lombok's @SuperBuilder.
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
}
