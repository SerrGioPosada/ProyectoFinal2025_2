package co.edu.uniquindio.poo.proyectofiinal2025_2.Model;

/**
 * <p>Represents a person within the system. This is the base abstract class
 * for more specific entities like users and delivery personnel.</p>
 * <p>It contains common personal information such as ID, name, last name,
 * email, and phone number.</p>
 */
public abstract class Person {

    private String id;
    private String name;
    private String lastName;
    private String email;
    private String phone;

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

    // =================================
    // Getters and Setters
    // =================================

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
