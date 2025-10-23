package co.edu.uniquindio.poo.proyectofiinal2025_2.Model;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Enums.PermissionLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * <p>Represents an administrator in the system, extending the {@link AuthenticablePerson} class.</p>
 * <p>An administrator has system management privileges defined by an employee ID and a
 * specific permission level.</p>
 */
@Getter
@Setter
@ToString(callSuper = true)
public class Admin extends AuthenticablePerson {

    private String employeeId;
    private PermissionLevel permissionLevel;

    /**
     * Default constructor.
     */
    public Admin() {
        super();
    }

    /**
     * Protected constructor for the builder pattern.
     * @param builder The builder instance to construct from.
     */
    protected Admin(Builder builder) {
        super(builder);
        this.employeeId = builder.employeeId;
        this.permissionLevel = builder.permissionLevel;
    }

    // ======================================
    //               BUILDER
    // ======================================

    /**
     * Concrete builder for creating Admin instances.
     */
    public static class Builder extends AuthenticablePerson.Builder<Builder> {
        private String employeeId;
        private PermissionLevel permissionLevel;

        public Builder withEmployeeId(String employeeId) {
            this.employeeId = employeeId;
            return this;
        }

        public Builder withPermissionLevel(PermissionLevel permissionLevel) {
            this.permissionLevel = permissionLevel;
            return this;
        }

        /**
         * Returns the concrete builder instance (part of the CRTP pattern).
         * @return The concrete builder instance.
         */
        @Override
        protected Builder self() {
            return this;
        }

        /**
         * Creates a new Admin instance from the builder's properties.
         * @return A new Admin instance.
         */
        @Override
        public Admin build() {
            return new Admin(this);
        }
    }
}
