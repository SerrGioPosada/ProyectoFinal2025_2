package co.edu.uniquindio.poo.proyectofiinal2025_2.Model;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Enums.ShipmentStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

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
    private String orderId; // Link back to the Order aggregate root
    private Address origin;
    private Address destination;
    private LocalDateTime createdAt;
    private LocalDateTime estimatedDate;
    private LocalDateTime deliveredDate;
    private ShipmentStatus status;

    /**
     * Constructs a new Shipment.
     *
     * @param id                The unique identifier for the shipment.
     * @param orderId           The ID of the order that this shipment fulfills.
     * @param origin            The origin address.
     * @param destination       The destination address.
     * @param createdAt         The timestamp when the shipment was created.
     * @param estimatedDate     The estimated delivery date.
     * @param status            The initial status of the shipment.
     */
    public Shipment(String id, String orderId, Address origin, Address destination,
                    LocalDateTime createdAt, LocalDateTime estimatedDate, ShipmentStatus status) {
        this.id = id;
        this.orderId = orderId;
        this.origin = origin;
        this.destination = destination;
        this.createdAt = createdAt;
        this.estimatedDate = estimatedDate;
        this.status = status;
    }
}
