package co.edu.uniquindio.poo.ProyectoFinal2025_2.Model;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.AvailabilityStatus;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.CoverageArea;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Represents a delivery person in the system, extending the {@link AuthenticablePerson} class.</p>
 * <p>A delivery person can be authenticated and has specific attributes such as a document ID,
 * availability status, a list of vehicles they own, their currently active vehicle,
 * a coverage area, and a list of shipments they are responsible for.</p>
 */
@Getter
@Setter
@ToString(callSuper = true)
public class DeliveryPerson extends AuthenticablePerson {

    private String documentId;
    private AvailabilityStatus availability;
    private List<String> vehiclePlates; // List of vehicle plates owned by this delivery person
    private String activeVehiclePlate; // Currently active vehicle plate
    private CoverageArea coverageArea;
    private List<Shipment> assignedShipments;

    /**
     * Default constructor.
     * Initializes the lists to avoid NullPointerExceptions.
     */
    public DeliveryPerson() {
        super();
        this.vehiclePlates = new ArrayList<>();
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
        this.vehiclePlates = builder.vehiclePlates != null ? builder.vehiclePlates : new ArrayList<>();
        this.activeVehiclePlate = builder.activeVehiclePlate;
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
        private List<String> vehiclePlates;
        private String activeVehiclePlate;
        private CoverageArea coverageArea;

        public Builder withDocumentId(String documentId) {
            this.documentId = documentId;
            return this;
        }

        public Builder withAvailability(AvailabilityStatus availability) {
            this.availability = availability;
            return this;
        }

        public Builder withVehiclePlates(List<String> vehiclePlates) {
            this.vehiclePlates = vehiclePlates;
            return this;
        }

        public Builder withActiveVehiclePlate(String activeVehiclePlate) {
            this.activeVehiclePlate = activeVehiclePlate;
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
     * Gets the currently assigned vehicle for this delivery person.
     * This method retrieves the Vehicle object from the VehicleRepository using the activeVehiclePlate.
     *
     * @return The assigned Vehicle object, or null if no vehicle is assigned.
     */
    public Vehicle getAssignedVehicle() {
        if (this.activeVehiclePlate == null || this.activeVehiclePlate.isEmpty()) {
            return null;
        }

        co.edu.uniquindio.poo.ProyectoFinal2025_2.Repositories.VehicleRepository vehicleRepo =
            co.edu.uniquindio.poo.ProyectoFinal2025_2.Repositories.VehicleRepository.getInstance();

        return vehicleRepo.findByPlate(this.activeVehiclePlate).orElse(null);
    }

    /**
     * Sets the assigned vehicle for this delivery person.
     * This method updates the activeVehiclePlate based on the provided Vehicle object.
     *
     * @param vehicle The Vehicle to assign, or null to clear the assignment.
     */
    public void setAssignedVehicle(Vehicle vehicle) {
        if (vehicle == null) {
            this.activeVehiclePlate = null;
        } else {
            this.activeVehiclePlate = vehicle.getPlate();
            // Ensure the vehicle plate is in the list of owned vehicles
            addVehiclePlate(vehicle.getPlate());
        }
    }

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

    /**
     * Adds a vehicle plate to the delivery person's list of vehicles.
     *
     * @param plate The vehicle plate to add.
     */
    public void addVehiclePlate(String plate) {
        if (this.vehiclePlates == null) {
            this.vehiclePlates = new ArrayList<>();
        }
        if (!this.vehiclePlates.contains(plate)) {
            this.vehiclePlates.add(plate);
        }
    }

    /**
     * Removes a vehicle plate from the delivery person's list of vehicles.
     *
     * @param plate The vehicle plate to remove.
     */
    public void removeVehiclePlate(String plate) {
        if (this.vehiclePlates != null) {
            this.vehiclePlates.remove(plate);
            // If the removed plate was the active one, clear it
            if (plate.equals(this.activeVehiclePlate)) {
                this.activeVehiclePlate = null;
            }
        }
    }
}
