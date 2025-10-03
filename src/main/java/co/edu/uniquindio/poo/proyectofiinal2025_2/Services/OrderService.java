package co.edu.uniquindio.poo.proyectofiinal2025_2.Services;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Address;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Invoice;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Order;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Enums.OrderStatus;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Repositories.OrderRepository;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Repositories.ShipmentRepository;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * <p>Orchestrates the entire order management process, acting as the central service for the Order aggregate.</p>
 * <p>This service manages the lifecycle (Saga) of an order, from initiation to confirmation and handoff
 * to the shipment process. It ensures that all state transitions are valid.</p>
 */
public class OrderService {

    private final OrderRepository orderRepository;
    private final InvoiceService invoiceService;
    private final ShipmentService shipmentService;

    /**
     * Constructs a new OrderService with its dependencies.
     */
    public OrderService() {
        this.orderRepository = OrderRepository.getInstance();
        this.invoiceService = new InvoiceService();
        this.shipmentService = new ShipmentService(ShipmentRepository.getInstance());
    }

    /**
     * Initiates the creation of a new order.
     * <p>This is the first step in the saga. It creates the order, generates the invoice,
     * and leaves the order in the AWAITING_PAYMENT state.</p>
     *
     * @param userId      The ID of the user creating the order.
     * @param origin      The origin address.
     * @param destination The destination address.
     * @return The newly created Order, ready for payment.
     */
    public Order initiateOrderCreation(String userId, Address origin, Address destination) {

        // 1. Create the Order in its initial state
        Order newOrder = Order.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .origin(origin)
                .destination(destination)
                .createdAt(LocalDateTime.now())
                // Es una buena práctica establecer el estado inicial al crear la orden
                .status(OrderStatus.AWAITING_PAYMENT)
                .build();

        orderRepository.addOrder(newOrder);

        // 2. Call the InvoiceService to create the associated invoice
        Invoice invoice = invoiceService.createInvoiceForOrder(newOrder);

        // 3. Update the order with the new invoiceId
        newOrder.setInvoiceId(invoice.getId());
        orderRepository.update(newOrder);

        return newOrder;
    }

    /**
     * Confirms that an order has been paid, and transitions it to the next state.
     * <p>This is a critical step in the saga. It validates the order's state, updates it,
     * and triggers the creation of the shipment.</p>
     *
     * @param orderId   The ID of the order to confirm.
     * @param paymentId The ID of the successful payment transaction.
     * @throws IllegalStateException if the order is not in the AWAITING_PAYMENT state.
     */
    public void confirmOrderPayment(String orderId, String paymentId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + orderId));

        // State Validation: Ensure we can only confirm payment for an order that is awaiting it.
        if (order.getStatus() != OrderStatus.AWAITING_PAYMENT) {
            throw new IllegalStateException("Cannot confirm payment for an order that is not in the AWAITING_PAYMENT state. Current state: " + order.getStatus());
        }

        // 1. Update the order state to PAID
        order.setPaymentId(paymentId);
        order.setStatus(OrderStatus.PAID);
        orderRepository.update(order);

        // 2. Trigger the next step in the saga: creating the shipment
        String shipmentId = shipmentService.createShipmentForOrder(order);

        // 3. Update the order with the shipment ID and transition state to SHIPPED
        order.setShipmentId(shipmentId);
        order.setStatus(OrderStatus.SHIPPED);
        orderRepository.update(order);
    }
}
