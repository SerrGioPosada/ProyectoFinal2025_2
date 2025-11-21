package co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Template;

import lombok.Getter;

/**
 * Represents the result of a payment processing operation.
 */
@Getter
public class PaymentResult {

    private final boolean success;
    private final String message;
    private final String receiptId;

    private PaymentResult(boolean success, String message, String receiptId) {
        this.success = success;
        this.message = message;
        this.receiptId = receiptId;
    }

    /**
     * Creates a successful payment result.
     * @param receiptId The receipt/transaction ID
     * @return Successful PaymentResult
     */
    public static PaymentResult success(String receiptId) {
        return new PaymentResult(true, "Pago procesado exitosamente", receiptId);
    }

    /**
     * Creates a failed payment result.
     * @param message Error message
     * @return Failed PaymentResult
     */
    public static PaymentResult failed(String message) {
        return new PaymentResult(false, message, null);
    }
}
