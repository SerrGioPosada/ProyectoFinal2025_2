package co.edu.uniquindio.poo.proyectofiinal2025_2.Model;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Enums.PermissionLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * <p>Represents an administrator in the system, extending the {@link AuthenticablePerson} class.</p>
 * <p>An administrator has system management privileges defined by an employee ID and a
 * specific permission level.</p>
 */
@Getter
@Setter
@ToString(callSuper = true)
@SuperBuilder
public class Admin extends AuthenticablePerson {

    private String employeeId;
    private PermissionLevel permissionLevel;

    /**
     * Default constructor for Lombok's @SuperBuilder.
     */
    public Admin() {
        super();
    }

    /**
     * Constructs a new Admin with the specified details.
     *
     * @param id              The unique identifier for the admin.
     * @param name            The admin's first name.
     * @param lastName        The admin's last name.
     * @param email           The admin's email address.
     * @param phone           The admin's phone number.
     * @param password        The admin's password for login (will be hashed).
     * @param employeeId      The unique employee identifier.
     * @param permissionLevel The level of permissions assigned to the admin.
     */
    public Admin(String id, String name, String lastName, String email, String phone, String password,
                 String employeeId, PermissionLevel permissionLevel) {
        super(id, name, lastName, email, phone, password);
        this.employeeId = employeeId;
        this.permissionLevel = permissionLevel;
    }
}
