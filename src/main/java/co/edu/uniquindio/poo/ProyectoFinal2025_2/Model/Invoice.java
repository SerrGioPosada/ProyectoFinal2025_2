package co.edu.uniquindio.poo.ProyectoFinal2025_2.Model;

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
     * Private constructor for the builder pattern.
     * @param builder The builder instance to construct from.
     */
    private Invoice(Builder builder) {
        this.id = builder.id;
        this.orderId = builder.orderId;
        this.invoiceNumber = builder.invoiceNumber;
        this.issuedAt = builder.issuedAt;
        this.totalAmount = builder.totalAmount;
        this.lineItems = (builder.lineItems != null)
                ? Collections.unmodifiableList(builder.lineItems)
                : Collections.emptyList(); // Ensure immutability and prevent nulls
    }

    // ======================================
    //               BUILDER
    // ======================================

    /**
     * Static builder class for creating Invoice instances.
     */
    public static class Builder {
        private String id;
        private String orderId;
        private String invoiceNumber;
        private LocalDateTime issuedAt;
        private double totalAmount;
        private List<LineItem> lineItems;

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withOrderId(String orderId) {
            this.orderId = orderId;
            return this;
        }

        public Builder withInvoiceNumber(String invoiceNumber) {
            this.invoiceNumber = invoiceNumber;
            return this;
        }

        public Builder withIssuedAt(LocalDateTime issuedAt) {
            this.issuedAt = issuedAt;
            return this;
        }

        public Builder withTotalAmount(double totalAmount) {
            this.totalAmount = totalAmount;
            return this;
        }

        public Builder withLineItems(List<LineItem> lineItems) {
            this.lineItems = lineItems;
            return this;
        }

        /**
         * Creates a new Invoice instance from the builder's properties.
         * @return A new Invoice instance.
         */
        public Invoice build() {
            return new Invoice(this);
        }
    }
}
