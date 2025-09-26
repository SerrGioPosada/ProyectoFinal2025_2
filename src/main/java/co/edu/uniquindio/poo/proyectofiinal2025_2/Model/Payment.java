package co.edu.uniquindio.poo.proyectofiinal2025_2.Model;

import java.time.LocalDateTime;

/**
 * Represents a payment transaction for an order.
 * <p>
 * A payment contains information about the amount, date,
 * status, and the method used.
 * </p>
 */
public class Payment {

    private double amount;
    private LocalDateTime date;
    private String status; // e.g., "PENDING", "COMPLETED", "FAILED"
    private PaymentMethod paymentMethod;

    /**
     * Constructs a new Payment with the provided data.
     *
     * @param amount        total amount paid
     * @param date          date and time of the payment
     * @param status        current status of the payment
     * @param paymentMethod payment method used in the transaction
     */
    public Payment(double amount, LocalDateTime date, String status, PaymentMethod paymentMethod) {
        this.amount = amount;
        this.date = date;
        this.status = status;
        this.paymentMethod = paymentMethod;
    }

    // ======================
    // Getters
    // ======================

    public double getAmount() {
        return amount;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public String getStatus() {
        return status;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    // ======================
    // Setters
    // ======================

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    // ======================
    // toString
    // ======================

    @Override
    public String toString() {
        return "Payment{" +
                "amount=" + amount +
                ", date=" + date +
                ", status='" + status + '\'' +
                ", paymentMethod=" + (paymentMethod != null ? paymentMethod.getType() : "null") +
                '}';
    }
}
