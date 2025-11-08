package co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.Adapter;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.OrderStatus;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.ShipmentStatus;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Order;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Shipment;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.StatusChange;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.dto.TrackingEventDTO;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Repositories.OrderRepository;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Repositories.ShipmentRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Utility class to generate unified tracking timeline combining Order and Shipment events.
 * This is used for the user-facing tracking view to show the complete journey.
 */
public class TrackingTimelineUtil {

    private static final OrderRepository orderRepository = OrderRepository.getInstance();
    private static final ShipmentRepository shipmentRepository = ShipmentRepository.getInstance();

    /**
     * Generates a complete tracking timeline for a shipment, including its associated order history.
     *
     * @param shipmentId The shipment ID to track
     * @return List of tracking events in chronological order, or empty list if shipment not found
     */
    public static List<TrackingEventDTO> generateUnifiedTimeline(String shipmentId) {
        List<TrackingEventDTO> events = new ArrayList<>();

        // Get shipment
        Optional<Shipment> shipmentOpt = shipmentRepository.findById(shipmentId);
        if (!shipmentOpt.isPresent()) {
            return events; // Return empty list if shipment not found
        }

        Shipment shipment = shipmentOpt.get();

        // Get associated order
        Optional<Order> orderOpt = Optional.empty();
        if (shipment.getOrderId() != null) {
            orderOpt = orderRepository.findById(shipment.getOrderId());
        }

        // Add Order events
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            events.addAll(extractOrderEvents(order));
        }

        // Add Shipment events
        events.addAll(extractShipmentEvents(shipment));

        // Sort events chronologically
        Collections.sort(events);

        return events;
    }

    /**
     * Extracts tracking events from an Order.
     */
    private static List<TrackingEventDTO> extractOrderEvents(Order order) {
        List<TrackingEventDTO> events = new ArrayList<>();

        if (order == null) return events;

        // Event 1: Order Created (AWAITING_PAYMENT)
        events.add(new TrackingEventDTO(
            "AWAITING_PAYMENT",
            "Orden Creada",
            "La orden ha sido creada y está esperando el pago",
            order.getCreatedAt(),
            "#FFA726",
            true,
            true
        ));

        // Event 2: Payment Completed (PAID)
        // We infer this happened if status is PAID or beyond
        if (order.getStatus() == OrderStatus.PAID ||
            order.getStatus() == OrderStatus.PENDING_APPROVAL ||
            order.getStatus() == OrderStatus.APPROVED) {

            // Estimate payment time as a few minutes after creation
            LocalDateTime paymentTime = order.getCreatedAt() != null ?
                order.getCreatedAt().plusMinutes(5) : LocalDateTime.now();

            events.add(new TrackingEventDTO(
                "PAID",
                "Pago Completado",
                "El pago de la orden ha sido procesado exitosamente",
                paymentTime,
                "#66BB6A",
                true,
                true
            ));
        }

        // Event 3: Pending Approval
        if (order.getStatus() == OrderStatus.PENDING_APPROVAL ||
            order.getStatus() == OrderStatus.APPROVED) {

            LocalDateTime pendingTime = order.getCreatedAt() != null ?
                order.getCreatedAt().plusMinutes(10) : LocalDateTime.now();

            events.add(new TrackingEventDTO(
                "PENDING_APPROVAL",
                "Pendiente de Aprobación",
                "La orden está siendo revisada por el administrador",
                pendingTime,
                "#FF9800",
                true,
                true
            ));
        }

        // Event 4: Order Approved
        if (order.getStatus() == OrderStatus.APPROVED) {
            LocalDateTime approvalTime = order.getCreatedAt() != null ?
                order.getCreatedAt().plusMinutes(15) : LocalDateTime.now();

            events.add(new TrackingEventDTO(
                "APPROVED",
                "Orden Aprobada",
                "La orden ha sido aprobada y se ha creado el envío",
                approvalTime,
                "#42A5F5",
                true,
                true
            ));
        }

        // Event for Cancelled orders
        if (order.getStatus() == OrderStatus.CANCELLED) {
            events.add(new TrackingEventDTO(
                "CANCELLED",
                "Orden Cancelada",
                "La orden ha sido cancelada",
                LocalDateTime.now(),
                "#EF5350",
                true,
                true
            ));
        }

        return events;
    }

    /**
     * Extracts tracking events from a Shipment's status history.
     */
    private static List<TrackingEventDTO> extractShipmentEvents(Shipment shipment) {
        List<TrackingEventDTO> events = new ArrayList<>();

        if (shipment == null) return events;

        // If shipment has status history, use it
        if (shipment.getStatusHistory() != null && !shipment.getStatusHistory().isEmpty()) {
            for (StatusChange change : shipment.getStatusHistory()) {
                TrackingEventDTO event = createEventFromStatusChange(change);
                if (event != null) {
                    events.add(event);
                }
            }
        } else {
            // Fallback: create event from current status
            events.add(createEventFromShipmentStatus(shipment.getStatus(), shipment.getCreatedAt(), true));
        }

        return events;
    }

    /**
     * Creates a tracking event from a StatusChange.
     */
    private static TrackingEventDTO createEventFromStatusChange(StatusChange change) {
        if (change == null || change.getNewStatus() == null) return null;

        ShipmentStatus status = change.getNewStatus();
        return createEventFromShipmentStatus(status, change.getTimestamp(), true);
    }

    /**
     * Creates a tracking event from a ShipmentStatus.
     */
    private static TrackingEventDTO createEventFromShipmentStatus(ShipmentStatus status, LocalDateTime timestamp, boolean isCompleted) {
        if (status == null) return null;

        String displayName;
        String description;
        String color;

        switch (status) {
            case PENDING_ASSIGNMENT:
                displayName = "Esperando Asignación";
                description = "El envío está esperando ser asignado a un repartidor";
                color = "#FF9800";
                break;

            case READY_FOR_PICKUP:
                displayName = "Listo para Recoger";
                description = "El repartidor ha sido asignado y el paquete está listo para ser recogido";
                color = "#FFA726";
                break;

            case IN_TRANSIT:
                displayName = "En Tránsito";
                description = "El paquete ha sido recogido y está en camino";
                color = "#42A5F5";
                break;

            case OUT_FOR_DELIVERY:
                displayName = "En Reparto";
                description = "El paquete está en la fase final de entrega";
                color = "#66BB6A";
                break;

            case DELIVERED:
                displayName = "Entregado";
                description = "El paquete ha sido entregado exitosamente";
                color = "#4CAF50";
                break;

            case RETURNED:
                displayName = "Devuelto";
                description = "El paquete no pudo ser entregado y fue devuelto";
                color = "#FF9800";
                break;

            case CANCELLED:
                displayName = "Cancelado";
                description = "El envío ha sido cancelado";
                color = "#EF5350";
                break;

            default:
                displayName = status.getDisplayName();
                description = "Estado del envío: " + status.getDisplayName();
                color = "#6c757d";
                break;
        }

        return new TrackingEventDTO(
            status.name(),
            displayName,
            description,
            timestamp,
            color,
            false, // isOrderEvent = false (it's a shipment event)
            isCompleted
        );
    }

    /**
     * Gets the display name for a tracking status.
     */
    public static String getDisplayName(String status) {
        if (status == null) return "N/A";

        // Try OrderStatus first
        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status);
            return orderStatus.getDisplayName();
        } catch (IllegalArgumentException e) {
            // Not an OrderStatus, try ShipmentStatus
        }

        // Try ShipmentStatus
        try {
            ShipmentStatus shipmentStatus = ShipmentStatus.valueOf(status);
            return shipmentStatus.getDisplayName();
        } catch (IllegalArgumentException e) {
            return status;
        }
    }

    /**
     * Gets the color for a tracking status.
     */
    public static String getColor(String status) {
        if (status == null) return "#6c757d";

        // Try OrderStatus first
        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status);
            return orderStatus.getColor();
        } catch (IllegalArgumentException e) {
            // Not an OrderStatus, try ShipmentStatus
        }

        // Try ShipmentStatus
        try {
            ShipmentStatus shipmentStatus = ShipmentStatus.valueOf(status);
            return shipmentStatus.getColor();
        } catch (IllegalArgumentException e) {
            return "#6c757d";
        }
    }
}
