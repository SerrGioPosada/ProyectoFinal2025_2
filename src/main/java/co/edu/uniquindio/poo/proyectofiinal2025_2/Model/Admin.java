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

    private String employeeId;          // Unique identifier of the employee
    private PermissionLevel permissionLevel; // Permission level assigned to the employee

    /**
     * Default constructor for Lombok's @SuperBuilder.
     */
    private Admin() {
        super();
    }
}
