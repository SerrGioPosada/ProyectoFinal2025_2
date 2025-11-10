package co.edu.uniquindio.poo.ProyectoFinal2025_2.Model;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.ShipmentStatus;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.VehicleType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a shipment, which is a component of the Order aggregate.
 * <p>A Shipment is created after an order is paid. It has its own lifecycle
 * and is linked back to the order via an {@code orderId}.</p>
 */
@Getter
@Setter
@ToString
public class Shipment {

    private String id;
    private String orderId;
    private String userId;
    private String deliveryPersonId;
    private Address origin;
    private Address destination;
    private LocalDateTime createdAt;
    private LocalDateTime estimatedDate;
    private LocalDateTime deliveredDate;
    private LocalDateTime assignmentDate;
    private LocalDateTime requestedPickupDate;
    private ShipmentStatus status;

    // Package details
    private double weightKg;
    private double heightCm;
    private double widthCm;
    private double lengthCm;
    private double volumeM3;

    // Cost breakdown
    private double baseCost;
    private double servicesCost;
    private double totalCost;

    // Additional features
    private int priority; // 1-5
    private VehicleType vehicleType; // Type of vehicle assigned/recommended
    private String assignedVehiclePlate; // Plate of the vehicle assigned to this shipment
    private List<AdditionalService> additionalServices;
    private Incident incident;
    private String userNotes;
    private String internalNotes;
    private boolean active;
    private List<StatusChange> statusHistory;

    /**
     * Default constructor.
     */
    public Shipment() {
        this.additionalServices = new ArrayList<>();
        this.statusHistory = new ArrayList<>();
        this.active = true;
        this.priority = 3;
    }

    /**
     * Private constructor for the builder pattern.
     * @param builder The builder instance to construct from.
     */
    private Shipment(Builder builder) {
        this.id = builder.id;
        this.orderId = builder.orderId;
        this.userId = builder.userId;
        this.deliveryPersonId = builder.deliveryPersonId;
        this.origin = builder.origin;
        this.destination = builder.destination;
        this.createdAt = builder.createdAt;
        this.estimatedDate = builder.estimatedDate;
        this.deliveredDate = builder.deliveredDate;
        this.assignmentDate = builder.assignmentDate;
        this.requestedPickupDate = builder.requestedPickupDate;
        this.status = builder.status;
        this.weightKg = builder.weightKg;
        this.heightCm = builder.heightCm;
        this.widthCm = builder.widthCm;
        this.lengthCm = builder.lengthCm;
        this.volumeM3 = builder.volumeM3;
        this.baseCost = builder.baseCost;
        this.servicesCost = builder.servicesCost;
        this.totalCost = builder.totalCost;
        this.priority = builder.priority;
        this.vehicleType = builder.vehicleType;
        this.assignedVehiclePlate = builder.assignedVehiclePlate;
        this.additionalServices = builder.additionalServices != null ? builder.additionalServices : new ArrayList<>();
        this.incident = builder.incident;
        this.userNotes = builder.userNotes;
        this.internalNotes = builder.internalNotes;
        this.active = builder.active;
        this.statusHistory = builder.statusHistory != null ? builder.statusHistory : new ArrayList<>();
    }

    // ======================================
    //               BUILDER
    // ======================================

    /**
     * Static builder class for creating Shipment instances.
     */
    public static class Builder {
        private String id;
        private String orderId;
        private String userId;
        private String deliveryPersonId;
        private Address origin;
        private Address destination;
        private LocalDateTime createdAt;
        private LocalDateTime estimatedDate;
        private LocalDateTime deliveredDate;
        private LocalDateTime assignmentDate;
        private LocalDateTime requestedPickupDate;
        private ShipmentStatus status;
        private double weightKg;
        private double heightCm;
        private double widthCm;
        private double lengthCm;
        private double volumeM3;
        private double baseCost;
        private double servicesCost;
        private double totalCost;
        private int priority = 3;
        private VehicleType vehicleType;
        private String assignedVehiclePlate;
        private List<AdditionalService> additionalServices;
        private Incident incident;
        private String userNotes;
        private String internalNotes;
        private boolean active = true;
        private List<StatusChange> statusHistory;

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withOrderId(String orderId) {
            this.orderId = orderId;
            return this;
        }

        public Builder withUserId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder withDeliveryPersonId(String deliveryPersonId) {
            this.deliveryPersonId = deliveryPersonId;
            return this;
        }

        public Builder withOrigin(Address origin) {
            this.origin = origin;
            return this;
        }

        public Builder withDestination(Address destination) {
            this.destination = destination;
            return this;
        }

        public Builder withCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder withEstimatedDate(LocalDateTime estimatedDate) {
            this.estimatedDate = estimatedDate;
            return this;
        }

        public Builder withDeliveredDate(LocalDateTime deliveredDate) {
            this.deliveredDate = deliveredDate;
            return this;
        }

        public Builder withAssignmentDate(LocalDateTime assignmentDate) {
            this.assignmentDate = assignmentDate;
            return this;
        }

        public Builder withRequestedPickupDate(LocalDateTime requestedPickupDate) {
            this.requestedPickupDate = requestedPickupDate;
            return this;
        }

        public Builder withStatus(ShipmentStatus status) {
            this.status = status;
            return this;
        }

        public Builder withWeightKg(double weightKg) {
            this.weightKg = weightKg;
            return this;
        }

        public Builder withHeightCm(double heightCm) {
            this.heightCm = heightCm;
            return this;
        }

        public Builder withWidthCm(double widthCm) {
            this.widthCm = widthCm;
            return this;
        }

        public Builder withLengthCm(double lengthCm) {
            this.lengthCm = lengthCm;
            return this;
        }

        public Builder withVolumeM3(double volumeM3) {
            this.volumeM3 = volumeM3;
            return this;
        }

        public Builder withBaseCost(double baseCost) {
            this.baseCost = baseCost;
            return this;
        }

        public Builder withServicesCost(double servicesCost) {
            this.servicesCost = servicesCost;
            return this;
        }

        public Builder withTotalCost(double totalCost) {
            this.totalCost = totalCost;
            return this;
        }

        public Builder withPriority(int priority) {
            this.priority = priority;
            return this;
        }

        public Builder withVehicleType(VehicleType vehicleType) {
            this.vehicleType = vehicleType;
            return this;
        }

        public Builder withAssignedVehiclePlate(String assignedVehiclePlate) {
            this.assignedVehiclePlate = assignedVehiclePlate;
            return this;
        }

        public Builder withAdditionalServices(List<AdditionalService> additionalServices) {
            this.additionalServices = additionalServices;
            return this;
        }

        public Builder withIncident(Incident incident) {
            this.incident = incident;
            return this;
        }

        public Builder withUserNotes(String userNotes) {
            this.userNotes = userNotes;
            return this;
        }

        public Builder withInternalNotes(String internalNotes) {
            this.internalNotes = internalNotes;
            return this;
        }

        public Builder withActive(boolean active) {
            this.active = active;
            return this;
        }

        public Builder withStatusHistory(List<StatusChange> statusHistory) {
            this.statusHistory = statusHistory;
            return this;
        }

        /**
         * Creates a new Shipment instance from the builder's properties.
         * @return A new Shipment instance.
         */
        public Shipment build() {
            return new Shipment(this);
        }
    }

    // ======================================
    //         UTILITY METHODS
    // ======================================

    /**
     * Adds a status change to the history.
     * @param statusChange The status change to add
     */
    public void addStatusChange(StatusChange statusChange) {
        if (this.statusHistory == null) {
            this.statusHistory = new ArrayList<>();
        }
        this.statusHistory.add(statusChange);
    }

    /**
     * Adds an additional service to the shipment.
     * @param service The service to add
     */
    public void addAdditionalService(AdditionalService service) {
        if (this.additionalServices == null) {
            this.additionalServices = new ArrayList<>();
        }
        this.additionalServices.add(service);
    }

    /**
     * Calculates the volume based on dimensions.
     * @return The calculated volume in cubic meters
     */
    public double calculateVolume() {
        return (heightCm * widthCm * lengthCm) / 1000000.0;
    }
}
