package co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums;

/**
 * Represents the status of a payment transaction.
 * <ul>
 *     <li>{@code PENDING}: The payment has been initiated but is not yet confirmed.</li>
 *     <li>{@code APPROVED}: The payment was successfully processed.</li>
 *     <li>{@code FAILED}: The payment could not be processed.</li>
 * </ul>
 */
public enum PaymentStatus {
    PENDING("Pendiente", "#FFA726"),
    APPROVED("Aprobado", "#4CAF50"),
    FAILED("Fallido", "#EF5350");

    private final String displayName;
    private final String color;

    /**
     * Constructor for PaymentStatus enum.
     * @param displayName The display name in Spanish for the UI
     * @param color The color code for visual representation
     */
    PaymentStatus(String displayName, String color) {
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
