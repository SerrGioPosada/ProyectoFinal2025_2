package co.edu.uniquindio.poo.proyectofiinal2025_2.Model.dto;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Enums.AvailabilityStatus;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Enums.CoverageArea;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Enums.PermissionLevel;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Vehicle;
import javafx.scene.image.Image;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A Data Transfer Object (DTO) that acts as a Parameter Object for creating
 * any type of person (User, Admin, DeliveryPerson).
 * <p>
 * It holds a superset of all possible attributes required by the PersonFactory,
 * allowing client code to populate only the necessary fields for a specific
 * person type. This class is simplified using Project Lombok.
 */

@Data
@NoArgsConstructor

public class PersonCreationData {

    // --- Common Attributes for all Persons ---
    private String id;
    private String name;
    private String lastName;
    private String email;
    private String phone;
    private String password;

    // --- User-specific Attribute ---
    private Image profileImage;

    // --- Admin-specific Attributes ---
    private String employeeId;
    private PermissionLevel permissionLevel;

    // --- DeliveryPerson-specific Attributes ---
    private String documentId;
    private AvailabilityStatus availability;
    private Vehicle assignedVehicle;
    private CoverageArea coverageArea;
}