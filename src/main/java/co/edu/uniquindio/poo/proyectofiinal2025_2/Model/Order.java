package co.edu.uniquindio.poo.proyectofiinal2025_2.Model;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Enums.OrderStatus;

import java.time.LocalDateTime;

/**
 * Represents an order, acting as the Aggregate Root for the ordering process.
 * <p>An Order manages its own lifecycle through the {@link OrderStatus} enum.
 * It is decoupled from other aggregates like Shipment, Payment, and Invoice,
 * referencing them only by their IDs. This ensures transactional consistency
 * and a clean domain model.</p>
 */
public class Order {

    private String id;
    private String userId;
    private Address origin;
    private Address destination;
    private LocalDateTime createdAt;
    private OrderStatus status;

    // IDs for related aggregates
    private String shipmentId;
    private String paymentId;
    private String invoiceId;

    /**
     * Constructs a new Order at the beginning of its lifecycle.
     * The initial status is always set to AWAITING_PAYMENT.
     *
     * @param id          The unique identifier for the order.
     * @param userId      The ID of the user who created the order.
     * @param origin      The origin address for the shipment.
     * @param destination The destination address for the shipment.
     * @param createdAt   The timestamp when the order was created.
     */
    public Order(String id, String userId, Address origin, Address destination, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.origin = origin;
        this.destination = destination;
        this.createdAt = createdAt;
        this.status = OrderStatus.AWAITING_PAYMENT; // Initial state
        this.shipmentId = null; // Null until a shipment is created
        this.paymentId = null;  // Null until a payment is processed
        this.invoiceId = null;  // Null until an invoice is generated
    }

    // ======================
    // Getters
    // ======================

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public Address getOrigin() {
        return origin;
    }

    public Address getDestination() {
        return destination;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public String getShipmentId() {
        return shipmentId;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    // ======================
    // Setters for State Transitions
    // ======================

    public void setId(String id) {
        this.id = id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public void setShipmentId(String shipmentId) {
        this.shipmentId = shipmentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }

    // ======================
    // toString
    // ======================

    @Override
    public String toString() {
        return "Order{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", status=" + status +
                ", shipmentId='" + shipmentId + '\'' +
                ", paymentId='" + paymentId + '\'' +
                ", invoiceId='" + invoiceId + '\'' +
                '}';
    }
}
