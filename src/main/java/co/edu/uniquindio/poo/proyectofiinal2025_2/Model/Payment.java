package co.edu.uniquindio.poo.proyectofiinal2025_2.Model;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Enums.PaymentStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Represents a payment transaction, which is directly associated with an invoice.
 * <p>
 * A payment contains information about the amount, date, status, the method used,
 * and a reference to the specific invoice it is paying for.</p>
 */
@Getter
@Setter
@ToString
public class Payment {

    private String id;
    private String invoiceId;
    private double amount;
    private LocalDateTime date;
    private PaymentStatus status;
    private PaymentMethod paymentMethod;

    /**
     * Constructs a new Payment with the provided data.
     *
     * @param id            The unique identifier for this payment transaction.
     * @param invoiceId     The ID of the invoice this payment is for.
     * @param amount        The total amount paid.
     * @param date          The date and time of the payment.
     * @param status        The current status of the payment.
     * @param paymentMethod The payment method used in the transaction.
     */
    public Payment(String id, String invoiceId, double amount, LocalDateTime date, PaymentStatus status, PaymentMethod paymentMethod) {
        this.id = id;
        this.invoiceId = invoiceId;
        this.amount = amount;
        this.date = date;
        this.status = status;
        this.paymentMethod = paymentMethod;
    }
}
