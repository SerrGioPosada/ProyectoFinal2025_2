package co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Enums;

/**
 * Represents the status of a payment transaction.
 * <ul>
 *     <li>{@code PENDING}: The payment has been initiated but is not yet confirmed.</li>
 *     <li>{@code APPROVED}: The payment was successfully processed.</li>
 *     <li>{@code FAILED}: The payment could not be processed.</li>
 * </ul>
 */
public enum PaymentStatus {
    PENDING,
    APPROVED,
    FAILED
}
