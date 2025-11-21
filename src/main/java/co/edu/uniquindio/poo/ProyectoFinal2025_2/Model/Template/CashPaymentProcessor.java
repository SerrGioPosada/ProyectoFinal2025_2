package co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Template;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Payment;

import java.util.UUID;

/**
 * Cash payment processor implementation.
 * Extends BasePaymentProcessor using Template Method Pattern.
 */
public class CashPaymentProcessor extends BasePaymentProcessor {

    @Override
    protected boolean checkAvailability(Payment payment) {
        // Cash payments are always "available"
        // Just need to verify it's a valid cash payment scenario
        return payment.getPaymentMethod() != null &&
               payment.getPaymentMethod().getType() != null;
    }

    @Override
    protected boolean performCharge(Payment payment) {
        // For cash payments, we just mark as received
        // In real implementation, this might:
        // - Generate payment code for user to pay at physical location
        // - Create QR code for payment
        // - Register with payment collection service (Efecty, Baloto, etc.)

        System.out.println("Pago en efectivo registrado por: $" +
                         String.format("%,.2f", payment.getAmount()));
        return true;
    }

    @Override
    protected String generateReceipt(Payment payment) {
        // Generate receipt for cash payment
        String receiptId = "RCP-CASH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        System.out.println("Comprobante de pago en efectivo generado: " + receiptId);
        System.out.println("El cliente debe presentar este código al realizar el pago");

        return receiptId;
    }

    @Override
    protected void sendConfirmation(Payment payment, String receiptId) {
        // Cash payments need special confirmation with payment instructions
        System.out.println("===================================");
        System.out.println("INSTRUCCIONES DE PAGO EN EFECTIVO");
        System.out.println("===================================");
        System.out.println("Código de pago: " + receiptId);
        System.out.println("Monto a pagar: $" + String.format("%,.2f", payment.getAmount()));
        System.out.println("Presente este código en cualquier punto de pago autorizado");
        System.out.println("===================================");

        // In real implementation:
        // - Send email with payment instructions
        // - Generate PDF with barcode/QR
        // - Send SMS with payment code
    }
}
