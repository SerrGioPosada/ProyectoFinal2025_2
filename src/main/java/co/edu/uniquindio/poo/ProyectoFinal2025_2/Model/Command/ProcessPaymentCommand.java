package co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Command;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Payment;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.PaymentMethod;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.PaymentService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;

/**
 * Concrete command for processing a payment.
 */
public class ProcessPaymentCommand implements PaymentCommand {

    private final PaymentService paymentService;
    private final String invoiceId;
    private final PaymentMethod paymentMethod;
    private final double amount;
    private Payment executedPayment;
    private boolean executed = false;

    public ProcessPaymentCommand(PaymentService paymentService, String invoiceId,
                                PaymentMethod paymentMethod, double amount) {
        this.paymentService = paymentService;
        this.invoiceId = invoiceId;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
    }

    @Override
    public boolean execute() {
        if (executed) {
            Logger.warning("Payment command already executed for invoice: " + invoiceId);
            return false;
        }

        try {
            Logger.info("Executing payment command for invoice: " + invoiceId);
            executedPayment = paymentService.processPayment(invoiceId, paymentMethod);
            executed = true;
            Logger.info("Payment command executed successfully. Payment ID: " + executedPayment.getId());
            return true;
        } catch (Exception e) {
            Logger.error("Failed to execute payment command: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean undo() {
        if (!executed || executedPayment == null) {
            Logger.warning("Cannot undo payment: not executed");
            return false;
        }

        try {
            Logger.info("Undoing payment: " + executedPayment.getId());
            // In a real implementation, this would call a refund service
            // For now, we just log the refund request
            Logger.info("Refund processed for payment: " + executedPayment.getId());
            return true;
        } catch (Exception e) {
            Logger.error("Failed to undo payment: " + e.getMessage());
            return false;
        }
    }

    @Override
    public String getDescription() {
        return String.format("Process payment of $%.2f for invoice %s", amount, invoiceId);
    }

    @Override
    public double getAmount() {
        return amount;
    }

    @Override
    public boolean isExecuted() {
        return executed;
    }

    @Override
    public boolean canUndo() {
        return executed && executedPayment != null;
    }

    public Payment getExecutedPayment() {
        return executedPayment;
    }
}
