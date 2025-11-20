package co.edu.uniquindio.poo.ProyectoFinal2025_2.Model;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.OrderStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Represents an order, acting as the Aggregate Root for the ordering process.
 * <p>An Order manages its own lifecycle through the {@link OrderStatus} enum.
 * It is decoupled from other aggregates like Shipment, Payment, and Invoice,
 * referencing them only by their IDs. This ensures transactional consistency
 * and a clean domain model.</p>
 */
@Getter
@Setter
@ToString
public class Order {

    private String id;
    private String userId;
    private Address origin;
    private Address destination;
    private LocalDateTime createdAt;
    private OrderStatus status;

    // Cost information
    private double totalCost;

    // IDs for related aggregates
    private String shipmentId;
    private String paymentId;
    private String invoiceId;
    private String deliveryPersonId;

    /**
     * Default constructor.
     */
    public Order() {
    }

    /**
     * Private constructor for the builder pattern.
     * @param builder The builder instance to construct from.
     */
    private Order(Builder builder) {
        this.id = builder.id;
        this.userId = builder.userId;
        this.origin = builder.origin;
        this.destination = builder.destination;
        this.createdAt = builder.createdAt;
        this.status = builder.status;
        this.totalCost = builder.totalCost;
        this.shipmentId = builder.shipmentId;
        this.paymentId = builder.paymentId;
        this.invoiceId = builder.invoiceId;
        this.deliveryPersonId = builder.deliveryPersonId;
    }

    // ======================================
    //               BUILDER
    // ======================================

    /**
     * Static builder class for creating Order instances.
     */
    public static class Builder {
        private String id;
        private String userId;
        private Address origin;
        private Address destination;
        private LocalDateTime createdAt;
        private OrderStatus status;
        private double totalCost;
        private String shipmentId;
        private String paymentId;
        private String invoiceId;
        private String deliveryPersonId;

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withUserId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder withOrigin(Address origin) {
            this.origin = origin;
            return this;
        }

        public Builder withDestination(Address destination) {
            this.destination = destination;
            return this;
        }

        public Builder withCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder withStatus(OrderStatus status) {
            this.status = status;
            return this;
        }

        public Builder withTotalCost(double totalCost) {
            this.totalCost = totalCost;
            return this;
        }

        public Builder withShipmentId(String shipmentId) {
            this.shipmentId = shipmentId;
            return this;
        }

        public Builder withPaymentId(String paymentId) {
            this.paymentId = paymentId;
            return this;
        }

        public Builder withInvoiceId(String invoiceId) {
            this.invoiceId = invoiceId;
            return this;
        }

        public Builder withDeliveryPersonId(String deliveryPersonId) {
            this.deliveryPersonId = deliveryPersonId;
            return this;
        }

        /**
         * Creates a new Order instance from the builder's properties.
         * If no status is specified, it defaults to {@code OrderStatus.AWAITING_PAYMENT}.
         * @return A new Order instance.
         */
        public Order build() {
            // Set default status if not provided
            if (this.status == null) {
                this.status = OrderStatus.AWAITING_PAYMENT;
            }
            return new Order(this);
        }
    }
}
