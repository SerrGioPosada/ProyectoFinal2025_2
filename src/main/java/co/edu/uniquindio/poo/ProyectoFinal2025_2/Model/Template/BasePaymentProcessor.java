package co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Template;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Payment;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.PaymentStatus;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Repositories.PaymentRepository;

import java.time.LocalDateTime;

/**
 * Abstract base class for payment processors using Template Method Pattern.
 * Defines the skeleton of the payment processing algorithm.
 */
public abstract class BasePaymentProcessor {

    /**
     * Template Method - defines the payment processing flow.
     * This method is final to prevent subclasses from changing the algorithm structure.
     *
     * @param payment The payment to process
     * @return PaymentResult indicating success or failure
     */
    public final PaymentResult processPayment(Payment payment) {
        try {
            // Step 1: Validate payment
            validatePayment(payment);

            // Step 2: Check availability/funds
            if (!checkAvailability(payment)) {
                return PaymentResult.failed("Fondos insuficientes o método no disponible");
            }

            // Step 3: Perform the actual charge (implemented by subclasses)
            boolean charged = performCharge(payment);
            if (!charged) {
                return PaymentResult.failed("Error al realizar el cargo");
            }

            // Step 4: Generate receipt
            String receiptId = generateReceipt(payment);

            // Step 5: Send confirmation (hook method - can be overridden)
            sendConfirmation(payment, receiptId);

            // Step 6: Update payment status
            updatePaymentStatus(payment, PaymentStatus.APPROVED);

            return PaymentResult.success(receiptId);

        } catch (Exception e) {
            // Handle error
            handleError(payment, e);
            return PaymentResult.failed(e.getMessage());
        }
    }

    // ======================================
    //       COMMON METHODS (Implemented)
    // ======================================

    /**
     * Validates the payment object.
     * @param payment The payment to validate
     * @throws IllegalArgumentException if payment is invalid
     */
    protected void validatePayment(Payment payment) {
        if (payment == null) {
            throw new IllegalArgumentException("El pago no puede ser nulo");
        }
        if (payment.getAmount() <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a cero");
        }
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalStateException("El pago ya fue procesado o está en un estado inválido");
        }
        if (payment.getPaymentMethod() == null) {
            throw new IllegalArgumentException("Debe especificar un método de pago");
        }
    }

    /**
     * Updates the payment status and persists changes.
     * @param payment The payment to update
     * @param status The new status
     */
    protected void updatePaymentStatus(Payment payment, PaymentStatus status) {
        payment.setStatus(status);
        payment.setDate(LocalDateTime.now());

        PaymentRepository repository = PaymentRepository.getInstance();
        repository.addPayment(payment); // addPayment acts as upsert
    }

    /**
     * Handles errors during payment processing.
     * @param payment The payment that failed
     * @param e The exception that occurred
     */
    protected void handleError(Payment payment, Exception e) {
        payment.setStatus(PaymentStatus.FAILED);
        payment.setDate(LocalDateTime.now());

        PaymentRepository repository = PaymentRepository.getInstance();
        repository.addPayment(payment); // addPayment acts as upsert

        System.err.println("Error procesando pago " + payment.getId() + ": " + e.getMessage());
    }

    // ======================================
    //    ABSTRACT METHODS (Must Override)
    // ======================================

    /**
     * Checks if the payment method has sufficient funds/availability.
     * Must be implemented by concrete processors.
     *
     * @param payment The payment to check
     * @return true if funds/availability confirmed
     */
    protected abstract boolean checkAvailability(Payment payment);

    /**
     * Performs the actual charge using the specific payment method.
     * Must be implemented by concrete processors.
     *
     * @param payment The payment to charge
     * @return true if charge successful
     */
    protected abstract boolean performCharge(Payment payment);

    /**
     * Generates a receipt for the payment.
     * Must be implemented by concrete processors.
     *
     * @param payment The payment to generate receipt for
     * @return Receipt/transaction ID
     */
    protected abstract String generateReceipt(Payment payment);

    // ======================================
    //      HOOKS (Optional Override)
    // ======================================

    /**
     * Hook method for sending payment confirmation.
     * Default implementation does nothing.
     * Subclasses can override to provide custom notification.
     *
     * @param payment The payment that was processed
     * @param receiptId The receipt ID
     */
    protected void sendConfirmation(Payment payment, String receiptId) {
        // Default: do nothing
        // Subclasses can override to send email, SMS, etc.
        System.out.println("Pago procesado: " + receiptId);
    }
}
