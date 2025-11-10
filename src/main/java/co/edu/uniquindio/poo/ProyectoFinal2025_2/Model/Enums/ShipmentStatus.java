package co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums;

/**
 * Represents the lifecycle status of a shipment.
 * <p>Flujo completo:</p>
 * <ul>
 *     <li>{@code PENDING_ASSIGNMENT}: Admin aprob\u00f3 la orden, esperando asignaci\u00f3n de repartidor.</li>
 *     <li>{@code READY_FOR_PICKUP}: Repartidor asignado, notifica al usuario que est\u00e1 listo para recoger.</li>
 *     <li>{@code PICKED_UP}: El usuario recogi\u00f3 el paquete del repartidor (estado final exitoso).</li>
 *     <li>{@code IN_TRANSIT}: DEPRECATED - Ya no se usa en el nuevo flujo.</li>
 *     <li>{@code OUT_FOR_DELIVERY}: DEPRECATED - Ya no se usa en el nuevo flujo.</li>
 *     <li>{@code DELIVERED}: DEPRECATED - Ahora se usa PICKED_UP.</li>
 *     <li>{@code RETURNED}: El env\u00edo no pudo completarse y fue devuelto.</li>
 *     <li>{@code CANCELLED}: El env\u00edo ha sido cancelado.</li>
 * </ul>
 */
public enum ShipmentStatus {
    PENDING_ASSIGNMENT("Pendiente de Asignación", "#FF9800"),
    READY_FOR_PICKUP("Listo para Recoger", "#FFA726"),
    PICKED_UP("Recogido por el Usuario", "#4CAF50"),
    IN_TRANSIT("En Tránsito", "#42A5F5"), // Deprecated
    OUT_FOR_DELIVERY("En Reparto", "#66BB6A"), // Deprecated
    DELIVERED("Entregado", "#4CAF50"), // Deprecated - Use PICKED_UP instead
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
