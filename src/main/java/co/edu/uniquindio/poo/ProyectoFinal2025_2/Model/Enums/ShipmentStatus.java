package co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums;

/**
 * Represents the lifecycle status of a shipment.
 * <p>In the new workflow, shipments are created when orders are approved.
 * Delivery person assignment happens at the shipment level.</p>
 * <ul>
 *     <li>{@code PENDING_ASSIGNMENT}: The shipment has been created but no delivery person assigned yet.</li>
 *     <li>{@code READY_FOR_PICKUP}: The shipment has a delivery person assigned and is ready for pickup.</li>
 *     <li>{@code IN_TRANSIT}: The shipment has been picked up and is on its way.</li>
 *     <li>{@code OUT_FOR_DELIVERY}: The shipment is in the final stage of delivery.</li>
 *     <li>{@code DELIVERED}: The shipment has been successfully delivered.</li>
 *     <li>{@code RETURNED}: The shipment could not be delivered and was returned.</li>
 *     <li>{@code CANCELLED}: The shipment has been cancelled.</li>
 * </ul>
 */
public enum ShipmentStatus {
    PENDING_ASSIGNMENT("Pendiente de Asignación", "#FF9800"),
    READY_FOR_PICKUP("Listo para Recoger", "#FFA726"),
    IN_TRANSIT("En Tránsito", "#42A5F5"),
    OUT_FOR_DELIVERY("En Reparto", "#66BB6A"),
    DELIVERED("Entregado", "#4CAF50"),
    RETURNED("Devuelto", "#FF9800"),
    CANCELLED("Cancelado", "#EF5350");

    private final String displayName;
    private final String color;

    /**
     * Constructor for ShipmentStatus enum.
     * @param displayName The display name in Spanish for the UI
     * @param color The color code for visual representation
     */
    ShipmentStatus(String displayName, String color) {
        this.displayName = displayName;
        this.color = color;
    }

    /**
     * Gets the display name in Spanish.
     * @return The display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the color code.
     * @return The color code
     */
    public String getColor() {
        return color;
    }
}
