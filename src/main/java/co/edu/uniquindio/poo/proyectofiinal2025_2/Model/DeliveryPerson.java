package co.edu.uniquindio.poo.proyectofiinal2025_2.Model;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Enums.AvailabilityStatus;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Enums.CoverageArea;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

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
public class DeliveryPerson extends AuthenticablePerson {

    private String documentId;
    private AvailabilityStatus availability;
    private Vehicle assignedVehicle;
    private CoverageArea coverageArea;
    private List<Shipment> assignedShipments;

    /**
     * Default constructor.
     * Initializes the list of shipments to avoid NullPointerExceptions.
     */
    public DeliveryPerson() {
        super();
        this.assignedShipments = new ArrayList<>();
    }

    /**
     * Protected constructor for the builder pattern.
     * @param builder The builder instance to construct from.
     */
    protected DeliveryPerson(Builder builder) {
        super(builder);
        this.documentId = builder.documentId;
        this.availability = builder.availability;
        this.assignedVehicle = builder.assignedVehicle;
        this.coverageArea = builder.coverageArea;
        this.assignedShipments = new ArrayList<>(); // Always initialize list
    }

    // ======================================
    //               BUILDER
    // ======================================

    /**
     * Concrete builder for creating DeliveryPerson instances.
     */
    public static class Builder extends AuthenticablePerson.Builder<Builder> {
        private String documentId;
        private AvailabilityStatus availability;
        private Vehicle assignedVehicle;
        private CoverageArea coverageArea;

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
         * Returns the concrete builder instance (part of the CRTP pattern).
         * @return The concrete builder instance.
         */
        @Override
        protected Builder self() {
            return this;
        }

        /**
         * Creates a new DeliveryPerson instance from the builder's properties.
         * @return A new DeliveryPerson instance.
         */
        @Override
        public DeliveryPerson build() {
            return new DeliveryPerson(this);
        }
    }

    // ======================================
    //           UTILITY METHODS
    // ======================================

    /**
     * Adds a shipment to the delivery person's list of assigned shipments.
     *
     * @param shipment The Shipment object to add.
     */
    public void addShipment(Shipment shipment) {
        if (this.assignedShipments == null) {
            this.assignedShipments = new ArrayList<>();
        }
        this.assignedShipments.add(shipment);
    }
}
