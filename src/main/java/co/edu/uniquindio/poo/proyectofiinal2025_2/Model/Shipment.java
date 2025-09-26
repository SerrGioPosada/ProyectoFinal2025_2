package co.edu.uniquindio.poo.proyectofiinal2025_2.Model;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Enums.ShipmentStatus;

import java.time.LocalDateTime;

/**
 * Represents a shipment generated from an order.
 * Each shipment has an origin, destination, vehicle, and status.
 */
public class Shipment {

    private String id;                     // Unique shipment identifier
    private Order order;                   // The order that generated this shipment
    private Address origin;                // Origin address
    private Address destination;           // Destination address
    private Vehicle vehicle;               // Vehicle assigned to the shipment
    private ShipmentStatus status;         // Current status
    private LocalDateTime estimatedDate;   // Estimated delivery date
    private LocalDateTime deliveredDate;   // Actual delivery date (if delivered)

    /**
     * Constructs a new Shipment.
     *
     * @param id            unique shipment id
     * @param order         related order
     * @param origin        origin address
     * @param destination   destination address
     * @param vehicle       vehicle assigned
     * @param status        current shipment status
     * @param estimatedDate estimated delivery date
     */
    public Shipment(String id, Order order, Address origin, Address destination,
                    Vehicle vehicle, ShipmentStatus status, LocalDateTime estimatedDate) {
        this.id = id;
        this.order = order;
        this.origin = origin;
        this.destination = destination;
        this.vehicle = vehicle;
        this.status = status;
        this.estimatedDate = estimatedDate;
    }

    // ======================
    // Getters
    // ======================

    public String getId() {
        return id;
    }

    public Order getOrder() {
        return order;
    }

    public Address getOrigin() {
        return origin;
    }

    public Address getDestination() {
        return destination;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public ShipmentStatus getStatus() {
        return status;
    }

    public LocalDateTime getEstimatedDate() {
        return estimatedDate;
    }

    public LocalDateTime getDeliveredDate() {
        return deliveredDate;
    }

    // ======================
    // Setters
    // ======================

    public void setId(String id) {
        this.id = id;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public void setOrigin(Address origin) {
        this.origin = origin;
    }

    public void setDestination(Address destination) {
        this.destination = destination;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public void setStatus(ShipmentStatus status) {
        this.status = status;
    }

    public void setEstimatedDate(LocalDateTime estimatedDate) {
        this.estimatedDate = estimatedDate;
    }

    public void setDeliveredDate(LocalDateTime deliveredDate) {
        this.deliveredDate = deliveredDate;
    }

    // ======================
    // ToString
    // ======================

    @Override
    public String toString() {
        return "Shipment{" +
                "id='" + id + '\'' +
                ", order=" + (order != null ? order.getId() : "null") +
                ", origin=" + origin +
                ", destination=" + destination +
                ", vehicle=" + (vehicle != null ? vehicle.getPlate() : "null") +
                ", status=" + status +
                ", estimatedDate=" + estimatedDate +
                ", deliveredDate=" + deliveredDate +
                '}';
    }
}
