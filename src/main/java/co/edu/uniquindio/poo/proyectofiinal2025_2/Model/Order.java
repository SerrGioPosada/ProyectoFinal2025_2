package co.edu.uniquindio.poo.proyectofiinal2025_2.Model;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Enums.OrderStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Represents an order, acting as the Aggregate Root for the ordering process.
 * <p>An Order manages its own lifecycle through the {@link OrderStatus} enum.
 * It is decoupled from other aggregates like Shipment, Payment, and Invoice,
 * referencing them only by their IDs. This ensures transactional consistency
 * and a clean domain model.</p>
 */

@Getter
@Setter
@ToString
@Builder

public class Order {

    private String id;            // Unique identifier for the order
    private String userId;        // ID of the user who created the order
    private Address origin;       // Origin address for the shipment
    private Address destination;  // Destination address for the shipment
    private LocalDateTime createdAt; // Timestamp when the order was created
    private OrderStatus status;   // Current status of the order

    // IDs for related aggregates
    private String shipmentId;    // ID of the associated shipment
    private String paymentId;     // ID of the associated payment
    private String invoiceId;     // ID of the associated invoice

}
