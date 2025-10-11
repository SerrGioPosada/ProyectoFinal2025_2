package co.edu.uniquindio.poo.proyectofiinal2025_2.Model;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Enums.ShipmentStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Represents a shipment, which is a component of the Order aggregate.
 * <p>A Shipment is created after an order is paid. It has its own lifecycle
 * and is linked back to the order via an {@code orderId}.</p>
 */
@Getter
@Setter
@ToString
public class Shipment {

    private String id;
    private String orderId;
    private Address origin;
    private Address destination;
    private LocalDateTime createdAt;
    private LocalDateTime estimatedDate;
    private LocalDateTime deliveredDate;
    private ShipmentStatus status;

    /**
     * Default constructor.
     */
    public Shipment() {
    }

    /**
     * Private constructor for the builder pattern.
     * @param builder The builder instance to construct from.
     */
    private Shipment(Builder builder) {
        this.id = builder.id;
        this.orderId = builder.orderId;
        this.origin = builder.origin;
        this.destination = builder.destination;
        this.createdAt = builder.createdAt;
        this.estimatedDate = builder.estimatedDate;
        this.deliveredDate = builder.deliveredDate;
        this.status = builder.status;
    }

    // ======================================
    //               BUILDER
    // ======================================

    /**
     * Static builder class for creating Shipment instances.
     */
    public static class Builder {
        private String id;
        private String orderId;
        private Address origin;
        private Address destination;
        private LocalDateTime createdAt;
        private LocalDateTime estimatedDate;
        private LocalDateTime deliveredDate;
        private ShipmentStatus status;

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withOrderId(String orderId) {
            this.orderId = orderId;
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

        public Builder withEstimatedDate(LocalDateTime estimatedDate) {
            this.estimatedDate = estimatedDate;
            return this;
        }

        public Builder withDeliveredDate(LocalDateTime deliveredDate) {
            this.deliveredDate = deliveredDate;
            return this;
        }

        public Builder withStatus(ShipmentStatus status) {
            this.status = status;
            return this;
        }

        /**
         * Creates a new Shipment instance from the builder's properties.
         * @return A new Shipment instance.
         */
        public Shipment build() {
            return new Shipment(this);
        }
    }
}
