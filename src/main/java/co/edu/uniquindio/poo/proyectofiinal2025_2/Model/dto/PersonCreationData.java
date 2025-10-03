package co.edu.uniquindio.poo.proyectofiinal2025_2.Model.dto;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Enums.AvailabilityStatus;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Enums.CoverageArea;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Enums.PermissionLevel;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Vehicle;
import javafx.scene.image.Image;

/**
 * A Data Transfer Object (DTO) that acts as a Parameter Object for creating
 * any type of person (User, Admin, DeliveryPerson).
 * <p>
 * It holds a superset of all possible attributes required by the PersonFactory,
 * allowing client code to populate only the necessary fields for a specific
 * person type.
 */
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

    /**
     * Default constructor.
     */
    public PersonCreationData() {
    }

    // =================================
    // Getters and Setters for all fields
    // =================================

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Image getProfileImage() { return profileImage; }
    public void setProfileImage(Image profileImage) { this.profileImage = profileImage; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public PermissionLevel getPermissionLevel() { return permissionLevel; }
    public void setPermissionLevel(PermissionLevel permissionLevel) { this.permissionLevel = permissionLevel; }

    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }

    public AvailabilityStatus getAvailability() { return availability; }
    public void setAvailability(AvailabilityStatus availability) { this.availability = availability; }

    public Vehicle getAssignedVehicle() { return assignedVehicle; }
    public void setAssignedVehicle(Vehicle assignedVehicle) { this.assignedVehicle = assignedVehicle; }

    public CoverageArea getCoverageArea() { return coverageArea; }
    public void setCoverageArea(CoverageArea coverageArea) { this.coverageArea = coverageArea; }

}