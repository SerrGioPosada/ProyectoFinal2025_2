package co.edu.uniquindio.poo.proyectofiinal2025_2.Services;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Enums.ShipmentStatus;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Order;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Shipment;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Repositories.ShipmentRepository;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Util.UtilService.IdGenerationUtil;

import java.time.LocalDateTime;

/**
 * <p>Manages the business logic related to shipments.</p>
 * <p>This service is responsible for creating, updating, and tracking shipments.
 * It is typically called by other services, like OrderService, after business
 * rules (like payment confirmation) have been met.</p>
 */
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;

    public ShipmentService(ShipmentRepository shipmentRepository) {
        this.shipmentRepository = shipmentRepository;
    }

    // ===========================
    // Shipment Management
    // ===========================

    /**
     * Creates a new shipment for a confirmed and paid order.
     * <p>This method is a key part of the order fulfillment saga. It takes an order,
     * creates the corresponding shipment entity, saves it, and returns its unique ID.</p>
     *
     * @param order The paid order for which to create a shipment.
     * @return The unique ID of the newly created shipment.
     */
    public String createShipmentForOrder(Order order) {
        Shipment newShipment = new Shipment.Builder()
                .withId(IdGenerationUtil.generateId())
                .withOrderId(order.getId())
                .withOrigin(order.getOrigin())
                .withDestination(order.getDestination())
                .withCreatedAt(LocalDateTime.now())
                .withStatus(ShipmentStatus.PENDING_ASSIGNMENT)
                .build();

        shipmentRepository.addShipment(newShipment);

        return newShipment.getId();
    }
}
