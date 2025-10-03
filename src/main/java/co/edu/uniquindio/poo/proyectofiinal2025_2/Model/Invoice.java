package co.edu.uniquindio.poo.proyectofiinal2025_2.Model;

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
public final class Invoice {

    private final String id;
    private final String orderId;
    private final String invoiceNumber;
    private final LocalDateTime issuedAt;
    private final double totalAmount;
    private final List<LineItem> lineItems;

    /**
     * Constructs a new, immutable Invoice.
     *
     * @param id            The unique identifier for this invoice.
     * @param orderId       The ID of the order this invoice is for.
     * @param invoiceNumber A unique, human-readable invoice number.
     * @param issuedAt      The date and time the invoice was issued.
     * @param totalAmount   The total amount of the invoice.
     * @param lineItems     A list of line items detailing the costs.
     */
    public Invoice(String id, String orderId, String invoiceNumber, LocalDateTime issuedAt, double totalAmount, List<LineItem> lineItems) {
        this.id = id;
        this.orderId = orderId;
        this.invoiceNumber = invoiceNumber;
        this.issuedAt = issuedAt;
        this.totalAmount = totalAmount;
        this.lineItems = Collections.unmodifiableList(lineItems); // Ensure immutability
    }
}
