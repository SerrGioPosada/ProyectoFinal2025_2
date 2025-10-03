package co.edu.uniquindio.poo.proyectofiinal2025_2.Model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Represents a financial invoice for an order.
 * <p>This is an immutable entity, meaning its state cannot be changed after creation.
 * It contains all the necessary details for a financial record, including a breakdown
 * of costs in the form of line items.</p>
 */

@Getter
@ToString
@Builder

public final class Invoice {

    private final String id;                // Unique identifier for the invoice
    private final String orderId;           // ID of the order this invoice belongs to
    private final String invoiceNumber;     // Human-readable invoice number
    private final LocalDateTime issuedAt;   // Date and time when the invoice was issued
    private final double totalAmount;       // Total amount of the invoice
    private final List<LineItem> lineItems; // Immutable list of line items detailing costs

}
