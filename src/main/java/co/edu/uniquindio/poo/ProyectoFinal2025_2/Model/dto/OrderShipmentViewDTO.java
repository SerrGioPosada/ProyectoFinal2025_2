package co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.dto;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.OrderStatus;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.ShipmentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Unified DTO for displaying both Orders and Shipments in the user's view.
 * Uses ItemType to distinguish between the two.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderShipmentViewDTO {

    public enum ItemType {
        ORDER("Orden"),
        SHIPMENT("Envío");

        private final String displayName;

        ItemType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Common fields
    private String id;
    private ItemType itemType;
    private String route; // origin → destination
    private String statusDisplay;
    private String statusColor;
    private LocalDateTime createdDate;
    private double cost;
    private boolean canCancel;
    private boolean canTrack;
    private boolean canViewDetails;

    // Order-specific fields (null for shipments)
    private OrderStatus orderStatus;
    private String shipmentId; // The shipment linked to this order (if any)
    private String paymentId;
    private String invoiceId;

    // Shipment-specific fields (null for orders)
    private ShipmentStatus shipmentStatus;
    private String orderId; // The order that created this shipment
    private String deliveryPersonName;
    private double weightKg;
    private int priority;
    private LocalDateTime estimatedDeliveryDate;

    // Helper methods for UI
    public String getTypeDisplay() {
        return itemType != null ? itemType.getDisplayName() : "N/A";
    }

    public String getCostFormatted() {
        return String.format("$%,.2f", cost);
    }

    public String getDateFormatted() {
        if (createdDate == null) return "--";
        return createdDate.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
}