package co.edu.uniquindio.poo.proyectofiinal2025_2.Model;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Enums.ShipmentStatus;
import lombok.Builder;
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
@Builder

public class Shipment {

    private String id;                 // Unique identifier for the shipment
    private String orderId;            // ID of the associated order (link back to Order aggregate root)
    private Address origin;            // Origin address for the shipment
    private Address destination;       // Destination address for the shipment
    private LocalDateTime createdAt;   // Timestamp when the shipment was created
    private LocalDateTime estimatedDate; // Estimated delivery date
    private LocalDateTime deliveredDate; // Actual delivery date
    private ShipmentStatus status;     // Current status of the shipment (e.g., pending, in transit, delivered)

}
