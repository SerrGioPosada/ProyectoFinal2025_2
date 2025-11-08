package co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.Adapter;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Order;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.OrderStatus;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.ShipmentStatus;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.dto.OrderShipmentViewDTO;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.dto.ShipmentDTO;

/**
 * Utility class for converting Orders and Shipments to unified OrderShipmentViewDTO.
 */
public final class OrderShipmentConverterUtil {

    private OrderShipmentConverterUtil() {
        // Prevent instantiation
    }

    /**
     * Converts an Order to OrderShipmentViewDTO.
     *
     * @param order The order to convert.
     * @return The unified DTO.
     */
    public static OrderShipmentViewDTO fromOrder(Order order) {
        OrderShipmentViewDTO dto = new OrderShipmentViewDTO();

        dto.setId(order.getId());
        dto.setItemType(OrderShipmentViewDTO.ItemType.ORDER);

        // Build route
        String origin = order.getOrigin() != null ? order.getOrigin().getCity() : "N/A";
        String destination = order.getDestination() != null ? order.getDestination().getCity() : "N/A";
        dto.setRoute(origin + " → " + destination);

        // Status
        dto.setOrderStatus(order.getStatus());
        dto.setStatusDisplay(order.getStatus() != null ? translateOrderStatus(order.getStatus()) : "N/A");
        dto.setStatusColor(getOrderStatusColor(order.getStatus()));

        dto.setCreatedDate(order.getCreatedAt());
        dto.setCost(0.0); // Orders don't have cost directly, it's in the shipment

        // Order-specific fields
        dto.setShipmentId(order.getShipmentId());
        dto.setPaymentId(order.getPaymentId());
        dto.setInvoiceId(order.getInvoiceId());

        // Capabilities
        dto.setCanCancel(order.getStatus() == OrderStatus.AWAITING_PAYMENT);
        dto.setCanTrack(order.getShipmentId() != null);
        dto.setCanViewDetails(true);

        return dto;
    }

    /**
     * Converts a ShipmentDTO to OrderShipmentViewDTO.
     *
     * @param shipment The shipment DTO to convert.
     * @return The unified DTO.
     */
    public static OrderShipmentViewDTO fromShipment(ShipmentDTO shipment) {
        OrderShipmentViewDTO dto = new OrderShipmentViewDTO();

        dto.setId(shipment.getId());
        dto.setItemType(OrderShipmentViewDTO.ItemType.SHIPMENT);

        // Route
        dto.setRoute(shipment.getOriginAddressComplete() + " → " + shipment.getDestinationAddressComplete());

        // Status
        dto.setShipmentStatus(shipment.getStatus());
        dto.setStatusDisplay(shipment.getStatusDisplayName());
        dto.setStatusColor(shipment.getStatusColor());

        dto.setCreatedDate(shipment.getCreationDate());
        dto.setCost(shipment.getTotalCost());

        // Shipment-specific fields
        dto.setOrderId(shipment.getOrderId());
        dto.setDeliveryPersonName(shipment.getDeliveryPersonName());
        dto.setWeightKg(shipment.getWeightKg());
        dto.setPriority(shipment.getPriority());
        dto.setEstimatedDeliveryDate(shipment.getEstimatedDeliveryDate());

        // Capabilities
        dto.setCanCancel(shipment.isCanBeCancelled());
        dto.setCanTrack(true);
        dto.setCanViewDetails(true);

        return dto;
    }

    /**
     * Translates OrderStatus to Spanish display name.
     */
    private static String translateOrderStatus(OrderStatus status) {
        if (status == null) return "N/A";

        return switch (status) {
            case AWAITING_PAYMENT -> "Esperando Pago";
            case PAID -> "Pagada";
            case PENDING_APPROVAL -> "Pendiente de Aprobación";
            case APPROVED -> "Aprobada";
            case CANCELLED -> "Cancelada";
        };
    }

    /**
     * Gets color code for OrderStatus.
     */
    private static String getOrderStatusColor(OrderStatus status) {
        if (status == null) return "#6c757d";

        return switch (status) {
            case AWAITING_PAYMENT -> "#ffc107"; // Yellow/warning
            case PAID -> "#17a2b8"; // Cyan/info
            case PENDING_APPROVAL -> "#FF9800"; // Orange
            case APPROVED -> "#007bff"; // Blue
            case CANCELLED -> "#dc3545"; // Red
        };
    }
}
