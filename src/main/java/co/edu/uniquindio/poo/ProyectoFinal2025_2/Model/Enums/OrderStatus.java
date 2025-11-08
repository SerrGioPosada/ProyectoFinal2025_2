package co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums;

/**
 * Represents the lifecycle status of an order.
 * <p>In the new workflow, orders only exist until approval. Once approved,
 * they become shipments and are managed separately.</p>
 * <ul>
 *     <li>{@code AWAITING_PAYMENT}: The order has been created but not yet paid for.</li>
 *     <li>{@code PAID}: The order has been successfully paid.</li>
 *     <li>{@code PENDING_APPROVAL}: The order awaits admin approval. This is the final status for orders.</li>
 *     <li>{@code APPROVED}: DEPRECATED - Orders are converted to shipments upon approval.</li>
 *     <li>{@code CANCELLED}: The order has been cancelled and will not be processed.</li>
 * </ul>
 */
public enum OrderStatus {
    AWAITING_PAYMENT("Esperando Pago", "#FFA726"),
    PAID("Pagado", "#66BB6A"),
    PENDING_APPROVAL("Pendiente de Aprobación", "#FF9800"),
    APPROVED("Aprobado", "#42A5F5"), // Deprecated - kept for data migration
    CANCELLED("Cancelado", "#EF5350");

    private final String displayName;
    private final String color;

    /**
     * Constructor for OrderStatus enum.
     * @param displayName The display name in Spanish for the UI
     * @param color The color code for visual representation
     */
    OrderStatus(String displayName, String color) {
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
