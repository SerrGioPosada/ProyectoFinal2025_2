package co.edu.uniquindio.poo.proyectofinal2025_2.Model;

import co.edu.uniquindio.poo.proyectofinal2025_2.Model.Enums.PaymentStatus;
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
     * Default constructor.
     */
    public Payment() {
    }

    /**
     * Private constructor for the builder pattern.
     * @param builder The builder instance to construct from.
     */
    private Payment(Builder builder) {
        this.id = builder.id;
        this.invoiceId = builder.invoiceId;
        this.amount = builder.amount;
        this.date = builder.date;
        this.status = builder.status;
        this.paymentMethod = builder.paymentMethod;
    }

    // ======================================
    //               BUILDER
    // ======================================

    /**
     * Static builder class for creating Payment instances.
     */
    public static class Builder {
        private String id;
        private String invoiceId;
        private double amount;
        private LocalDateTime date;
        private PaymentStatus status;
        private PaymentMethod paymentMethod;

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withInvoiceId(String invoiceId) {
            this.invoiceId = invoiceId;
            return this;
        }

        public Builder withAmount(double amount) {
            this.amount = amount;
            return this;
        }

        public Builder withDate(LocalDateTime date) {
            this.date = date;
            return this;
        }

        public Builder withStatus(PaymentStatus status) {
            this.status = status;
            return this;
        }

        public Builder withPaymentMethod(PaymentMethod paymentMethod) {
            this.paymentMethod = paymentMethod;
            return this;
        }

        /**
         * Creates a new Payment instance from the builder's properties.
         * @return A new Payment instance.
         */
        public Payment build() {
            return new Payment(this);
        }
    }
}
