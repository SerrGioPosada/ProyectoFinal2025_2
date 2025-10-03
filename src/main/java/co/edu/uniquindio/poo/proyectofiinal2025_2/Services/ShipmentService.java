package co.edu.uniquindio.poo.proyectofiinal2025_2.Services;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Enums.ShipmentStatus;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Order;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Shipment;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Repositories.ShipmentRepository;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * <p>Manages the business logic related to shipments.</p>
 * <p>This service is responsible for creating, updating, and tracking shipments.
 * It is typically called by other services, like OrderService, after business
 * rules (like payment confirmation) have been met.</p>
 */
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;

    /**
     * Constructs a new ShipmentService with its dependency.
     *
     * @param shipmentRepository The repository for shipment data persistence.
     */
    public ShipmentService(ShipmentRepository shipmentRepository) {
        this.shipmentRepository = shipmentRepository;
    }

    /**
     * Creates a new shipment for a confirmed and paid order.
     * <p>This method is a key part of the order fulfillment saga. It takes an order,
     * creates the corresponding shipment entity, saves it, and returns its unique ID.</p>
     *
     * @param order The paid order for which to create a shipment.
     * @return The unique ID of the newly created shipment.
     */
    public String createShipmentForOrder(Order order) {
        // Create a new Shipment instance
        Shipment newShipment = Shipment.builder()
                .id(UUID.randomUUID().toString())
                .orderId(order.getId())
                .origin(order.getOrigin())
                .destination(order.getDestination())
                .createdAt(LocalDateTime.now())
                .status(ShipmentStatus.PENDING_ASSIGNMENT)
                .build();

        // Persist the new shipment
        shipmentRepository.addShipment(newShipment);

        // Return the new shipment's ID
        return newShipment.getId();
    }

    // Other shipment-related methods will go here in the future
    // (e.g., assignVehicleToShipment, updateShipmentStatus, etc.)
}
