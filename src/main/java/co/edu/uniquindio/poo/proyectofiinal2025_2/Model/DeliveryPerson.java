package co.edu.uniquindio.poo.proyectofiinal2025_2.Model;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Enums.AvailabilityStatus;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Enums.CoverageArea;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Represents a delivery person in the system, extending the {@link AuthenticablePerson} class.</p>
 * <p>A delivery person can be authenticated and has specific attributes such as a document ID,
 * availability status, an assigned vehicle, a coverage area, and a list of shipments they are responsible for.</p>
 */
public class DeliveryPerson extends AuthenticablePerson {

    private String documentId;
    private AvailabilityStatus availability;
    private Vehicle assignedVehicle;
    private CoverageArea coverageArea;
    private List<Shipment> assignedShipments;

    /**
     * Constructs a new DeliveryPerson with the specified details.
     *
     * @param id              The unique identifier for the delivery person.
     * @param name            The person's first name.
     * @param lastName        The person's last name.
     * @param email           The person's email address.
     * @param phone           The person's phone number.
     * @param password        The person's password for login (will be hashed).
     * @param documentId      The national identification number.
     * @param availability    The current availability status.
     * @param assignedVehicle The vehicle assigned to the person.
     * @param coverageArea    The geographical area the person covers.
     */
    public DeliveryPerson(String id, String name, String lastName, String email, String phone, String password,
                          String documentId, AvailabilityStatus availability, Vehicle assignedVehicle, CoverageArea coverageArea) {
        super(id, name, lastName, email, phone, password);
        this.documentId = documentId;
        this.availability = availability;
        this.assignedVehicle = assignedVehicle;
        this.coverageArea = coverageArea;
        this.assignedShipments = new ArrayList<>();
    }

    // =================================
    // Getters and Setters
    // =================================

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public AvailabilityStatus getAvailability() {
        return availability;
    }

    public void setAvailability(AvailabilityStatus availability) {
        this.availability = availability;
    }

    public Vehicle getAssignedVehicle() {
        return assignedVehicle;
    }

    public void setAssignedVehicle(Vehicle assignedVehicle) {
        this.assignedVehicle = assignedVehicle;
    }

    public CoverageArea getCoverageArea() {
        return coverageArea;
    }

    public void setCoverageArea(CoverageArea coverageArea) {
        this.coverageArea = coverageArea;
    }

    public List<Shipment> getAssignedShipments() {
        return assignedShipments;
    }

    public void setAssignedShipments(List<Shipment> assignedShipments) {
        this.assignedShipments = assignedShipments;
    }

    /**
     * Adds a shipment to the delivery person's list of assigned shipments.
     *
     * @param shipment The shipment to add.
     */
    public void addShipment(Shipment shipment) {
        this.assignedShipments.add(shipment);
    }
}
