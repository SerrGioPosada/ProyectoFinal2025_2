package co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Template;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Payment;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.PaymentMethod;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Credit card payment processor implementation.
 * Extends BasePaymentProcessor using Template Method Pattern.
 */
public class CreditCardPaymentProcessor extends BasePaymentProcessor {

    @Override
    protected boolean checkAvailability(Payment payment) {
        PaymentMethod method = payment.getPaymentMethod();

        // Validate account number exists
        if (method.getAccountNumber() == null || method.getAccountNumber().isEmpty()) {
            return false;
        }

        // Simulate checking with credit card processor
        // In real implementation, this would call an external API
        return simulateCreditCheck(method.getAccountNumber(), payment.getAmount());
    }

    @Override
    protected boolean performCharge(Payment payment) {
        PaymentMethod method = payment.getPaymentMethod();

        try {
            // Simulate charging the card
            // In real implementation, this would integrate with payment gateway
            boolean chargeSuccessful = simulateCardCharge(
                method.getAccountNumber(),
                payment.getAmount()
            );

            if (chargeSuccessful) {
                // Store transaction reference
                String transactionId = generateTransactionId();
                // Note: Payment class may not have setExternalTransactionId
                // In real implementation, you'd add this field or use a different approach
                return true;
            }

            return false;

        } catch (Exception e) {
            System.err.println("Error al cargar tarjeta: " + e.getMessage());
            return false;
        }
    }

    @Override
    protected String generateReceipt(Payment payment) {
        // Generate receipt ID
        String receiptId = "RCP-CC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // In real implementation, this might:
        // - Create Invoice object
        // - Generate PDF receipt
        // - Store in database
        System.out.println("Recibo generado: " + receiptId);

        return receiptId;
    }

    @Override
    protected void sendConfirmation(Payment payment, String receiptId) {
        // Override to send credit card specific confirmation
        System.out.println("Enviando confirmación de pago con tarjeta...");
        System.out.println("Recibo: " + receiptId);
        System.out.println("Monto: $" + String.format("%,.2f", payment.getAmount()));

        // In real implementation:
        // - Send email with receipt
        // - Send SMS notification
        // - Update mobile app notification
    }

    // ======================================
    //         HELPER METHODS
    // ======================================

    private boolean simulateCreditCheck(String cardNumber, double amount) {
        // Simulate credit check
        // In real app, would call credit card processor API
        // For now, always return true unless amount is too high
        return amount <= 100_000_000; // 100 million max
    }

    private boolean simulateCardCharge(String accountNumber, double amount) {
        // Simulate charging the card
        // In real app, would call payment gateway API (Stripe, PayU, etc.)

        // Basic validation
        if (accountNumber == null || accountNumber.length() < 4) {
            return false;
        }

        // Simulate 95% success rate
        return Math.random() > 0.05;
    }

    private String generateTransactionId() {
        return "TXN-" + System.currentTimeMillis() + "-" +
               UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }
}
