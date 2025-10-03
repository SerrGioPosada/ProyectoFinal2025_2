package co.edu.uniquindio.poo.proyectofiinal2025_2.Model;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Enums.PaymentStatus;
import lombok.Builder;
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
@Builder

public class Payment {

    private String id;                 // Unique identifier for the payment
    private String invoiceId;          // ID of the associated invoice
    private double amount;             // Amount paid
    private LocalDateTime date;        // Date and time when the payment was made
    private PaymentStatus status;      // Current status of the payment (e.g., pending, completed)
    private PaymentMethod paymentMethod; // Method used to make the payment (e.g., credit card, cash)

}
