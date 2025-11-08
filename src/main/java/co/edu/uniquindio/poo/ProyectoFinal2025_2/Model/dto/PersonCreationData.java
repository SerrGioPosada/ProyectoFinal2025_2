package co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.dto;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.AvailabilityStatus;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.CoverageArea;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.PermissionLevel;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Vehicle;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * A Data Transfer Object (DTO) that serves as a Parameter Object for creating any type of person.
 * <p>
 * It holds a superset of all possible attributes required by the {@link co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Factory.PersonFactory},
 * allowing client code to populate only the necessary fields for a specific person type using its fluent builder.
 * </p>
 */

@Getter
@Setter
@ToString
public class PersonCreationData {

    // =================================================================================================================
    // Common Attributes
    // =================================================================================================================

    private String id;
    private String name;
    private String lastName;
    private String email;
    private String phone;
    private String password;
    private String profileImagePath;
    private boolean isActive = true; // Defaults to active

    // =================================================================================================================
    // Admin-specific Attributes
    // =================================================================================================================

    private String employeeId;
    private PermissionLevel permissionLevel;

    // =================================================================================================================
    // DeliveryPerson-specific Attributes
    // =================================================================================================================

    private String documentId;
    private AvailabilityStatus availability;
    private Vehicle assignedVehicle;
    private CoverageArea coverageArea;

    /**
     * Public no-args constructor for flexibility.
     */
    public PersonCreationData() {
        this.isActive = true; // Default value
    }

    /**
     * Private constructor for the builder pattern.
     * @param builder The builder instance to construct from.
     */
    private PersonCreationData(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.lastName = builder.lastName;
        this.email = builder.email;
        this.phone = builder.phone;
        this.password = builder.password;
        this.profileImagePath = builder.profileImagePath;
        this.isActive = builder.isActive;
        this.employeeId = builder.employeeId;
        this.permissionLevel = builder.permissionLevel;
        this.documentId = builder.documentId;
        this.availability = builder.availability;
        this.assignedVehicle = builder.assignedVehicle;
        this.coverageArea = builder.coverageArea;
    }

    // ======================================
    //               BUILDER
    // ======================================

    /**
     * Static builder class for creating PersonCreationData instances.
     */
    public static class Builder {
        private String id;
        private String name;
        private String lastName;
        private String email;
        private String phone;
        private String password;
        private String profileImagePath;
        private boolean isActive = true; // Default value
        private String employeeId;
        private PermissionLevel permissionLevel;
        private String documentId;
        private AvailabilityStatus availability;
        private Vehicle assignedVehicle;
        private CoverageArea coverageArea;

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder withEmail(String email) {
            this.email = email;
            return this;
        }

        public Builder withPhone(String phone) {
            this.phone = phone;
            return this;
        }

        public Builder withPassword(String password) {
            this.password = password;
            return this;
        }

        public Builder withProfileImagePath(String profileImagePath) {
            this.profileImagePath = profileImagePath;
            return this;
        }

        public Builder withIsActive(boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public Builder withEmployeeId(String employeeId) {
            this.employeeId = employeeId;
            return this;
        }

        public Builder withPermissionLevel(PermissionLevel permissionLevel) {
            this.permissionLevel = permissionLevel;
            return this;
        }

        public Builder withDocumentId(String documentId) {
            this.documentId = documentId;
            return this;
        }

        public Builder withAvailability(AvailabilityStatus availability) {
            this.availability = availability;
            return this;
        }

        public Builder withAssignedVehicle(Vehicle assignedVehicle) {
            this.assignedVehicle = assignedVehicle;
            return this;
        }

        public Builder withCoverageArea(CoverageArea coverageArea) {
            this.coverageArea = coverageArea;
            return this;
        }

        /**
         * Constructs and returns a new {@code PersonCreationData} instance based on the builder's current state.
         * @return A new, configured PersonCreationData instance.
         */
        public PersonCreationData build() {
            return new PersonCreationData(this);
        }
    }
}
