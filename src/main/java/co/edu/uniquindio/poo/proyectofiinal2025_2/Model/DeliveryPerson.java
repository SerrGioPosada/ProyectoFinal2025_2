package co.edu.uniquindio.poo.proyectofiinal2025_2.Model;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Enums.AvailabilityStatus;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Enums.CoverageArea;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Represents a delivery person in the system, extending the {@link AuthenticablePerson} class.</p>
 * <p>A delivery person can be authenticated and has specific attributes such as a document ID,
 * availability status, an assigned vehicle, a coverage area, and a list of shipments they are responsible for.</p>
 */

@Getter
@Setter
@ToString(callSuper = true)
@SuperBuilder

public class DeliveryPerson extends AuthenticablePerson {

    private String documentId;                 // Identification document number of the delivery person
    private AvailabilityStatus availability;   // Current availability status (e.g., available, busy)
    private Vehicle assignedVehicle;           // Vehicle assigned for deliveries
    private CoverageArea coverageArea;         // Geographical area the delivery person covers
    private List<Shipment> assignedShipments;  // List of shipments currently assigned

    /**
     * Default constructor for Lombok's @SuperBuilder.
     * Initializes the list of shipments to avoid NullPointerExceptions.
     */
    private DeliveryPerson() {
        super();
        this.assignedShipments = new ArrayList<>();
    }

    // ======================
    // Utility methods
    // ======================

    /**
     * Adds a shipment to the delivery person's list of assigned shipments.
     *
     * @param shipment The shipment to add.
     */
    public void addShipment(Shipment shipment) {
        if (this.assignedShipments == null) {
            this.assignedShipments = new ArrayList<>();
        }
        this.assignedShipments.add(shipment);
    }
}
