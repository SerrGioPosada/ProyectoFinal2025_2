package co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Enums;

/**
 * Represents the lifecycle status of a shipment.
 * <ul>
 *     <li>{@code PENDING_ASSIGNMENT}: The shipment has been created but not yet assigned to a delivery person.</li>
 *     <li>{@code IN_TRANSIT}: The shipment is on its way, assigned to a delivery person.</li>
 *     <li>{@code OUT_FOR_DELIVERY}: The shipment is in the final stage of delivery.</li>
 *     <li>{@code DELIVERED}: The shipment has been successfully delivered.</li>
 *     <li>{@code RETURNED}: The shipment could not be delivered and was returned.</li>
 *     <li>{@code CANCELLED}: The shipment has been cancelled.</li>
 * </ul>
 */
public enum ShipmentStatus {
    PENDING_ASSIGNMENT,
    IN_TRANSIT,
    OUT_FOR_DELIVERY,
    DELIVERED,
    RETURNED,
    CANCELLED
}
