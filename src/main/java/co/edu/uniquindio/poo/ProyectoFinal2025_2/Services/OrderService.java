package co.edu.uniquindio.poo.ProyectoFinal2025_2.Services;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Address;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.DeliveryPerson;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Invoice;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Order;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Shipment;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Vehicle;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.AvailabilityStatus;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.CoverageArea;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.OrderStatus;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Observer.NotificationObserver;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Repositories.DeliveryPersonRepository;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Repositories.OrderRepository;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Repositories.ShipmentRepository;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilService.IdGenerationUtil;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>Orchestrates the entire order management process, acting as the central service for the Order aggregate.</p>
 * <p>This service manages the lifecycle (Saga) of an order, from initiation to confirmation and handoff
 * to the shipment process. It ensures that all state transitions are valid.</p>
 */
public class OrderService {

    private final OrderRepository orderRepository;
    private final InvoiceService invoiceService;
    private final ShipmentService shipmentService;
    private final DeliveryPersonRepository deliveryPersonRepository;
    private final VehicleService vehicleService;
    private final ShipmentRepository shipmentRepository;

    /**
     * Constructor with dependency injection for repositories and services.
     *
     * @param orderRepository The OrderRepository instance.
     * @param invoiceService The InvoiceService instance.
     * @param shipmentService The ShipmentService instance.
     * @param deliveryPersonRepository The DeliveryPersonRepository instance.
     */
    public OrderService(OrderRepository orderRepository, InvoiceService invoiceService,
                       ShipmentService shipmentService, DeliveryPersonRepository deliveryPersonRepository) {
        this.orderRepository = orderRepository;
        this.invoiceService = invoiceService;
        this.shipmentService = shipmentService;
        this.deliveryPersonRepository = deliveryPersonRepository;
        this.vehicleService = VehicleService.getInstance();
        this.shipmentRepository = ShipmentRepository.getInstance();
    }

    /**
     * Default constructor that uses singleton instances.
     * This provides backward compatibility and ease of use.
     */
    public OrderService() {
        this(OrderRepository.getInstance(), new InvoiceService(),
             new ShipmentService(), DeliveryPersonRepository.getInstance());
    }

    // ===========================
    // Order Management
    // ===========================

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

        // 1. Create the Order in its initial state using the manual builder
        Order newOrder = new Order.Builder()
                .withId(IdGenerationUtil.generateId())
                .withUserId(userId)
                .withOrigin(origin)
                .withDestination(destination)
                .withCreatedAt(LocalDateTime.now())
                .withStatus(OrderStatus.AWAITING_PAYMENT)
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
     * Initiates the creation of a new order with detailed cost breakdown.
     * <p>This version uses OrderDetailDTO to ensure invoice costs match the quote.</p>
     *
     * @param orderDetail The order details with cost breakdown.
     * @return The newly created Order, ready for payment.
     */
    public Order initiateOrderCreationWithDetails(co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.dto.OrderDetailDTO orderDetail) {

        // 1. Create the Order in its initial state using the manual builder
        Order newOrder = new Order.Builder()
                .withId(IdGenerationUtil.generateId())
                .withUserId(orderDetail.getUserId())
                .withOrigin(orderDetail.getOrigin())
                .withDestination(orderDetail.getDestination())
                .withCreatedAt(LocalDateTime.now())
                .withStatus(OrderStatus.AWAITING_PAYMENT)
                .withTotalCost(orderDetail.getTotalCost())
                .build();

        orderRepository.addOrder(newOrder);

        // 2. Call the InvoiceService to create the associated invoice with detailed costs
        Invoice invoice = invoiceService.createInvoiceForOrderWithDetails(newOrder, orderDetail);

        // 3. Update the order with the new invoiceId
        newOrder.setInvoiceId(invoice.getId());
        orderRepository.update(newOrder);

        return newOrder;
    }

    // ===========================
    // Payment Confirmation
    // ===========================

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
            throw new IllegalStateException(
                    "Cannot confirm payment for an order that is not in the AWAITING_PAYMENT state. Current state: "
                            + order.getStatus()
            );
        }

        // 1. Update the order state to PENDING_APPROVAL (after successful payment)
        order.setPaymentId(paymentId);
        order.setStatus(OrderStatus.PENDING_APPROVAL);
        orderRepository.update(order);

        // 2. Order is now PENDING_APPROVAL and ready for admin to approve and create shipment
        // Shipment will be created when admin approves the order
    }

    // ===========================
    // Query Methods
    // ===========================

    /**
     * Retrieves all orders for a specific user.
     * Excludes APPROVED orders as they are now shipments.
     *
     * @param userId The ID of the user whose orders to retrieve.
     * @return A list of orders belonging to the user (excluding approved ones).
     */
    public java.util.List<Order> getOrdersByUser(String userId) {
        return orderRepository.findAll().stream()
                .filter(order -> order.getUserId().equals(userId))
                .filter(order -> order.getStatus() != OrderStatus.APPROVED) // Exclude approved orders
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Retrieves all orders in the system.
     *
     * @return A list of all orders.
     */
    public java.util.List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    // ===========================
    // Delivery Person Assignment
    // ===========================

    /**
     * Assigns a delivery person to an order and creates shipment in PENDING_APPROVAL state.
     * The shipment won't be visible until admin approves the order.
     *
     * @param orderId The ID of the order.
     * @param deliveryPersonId The ID of the delivery person to assign.
     * @return true if assignment was successful, false otherwise.
     */
    public boolean assignDeliveryPerson(String orderId, String deliveryPersonId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            return false;
        }

        // Only allow assignment for PAID orders
        if (order.getStatus() != OrderStatus.PAID) {
            return false;
        }

        // Assign delivery person
        order.setDeliveryPersonId(deliveryPersonId);

        // Create shipment with PENDING_APPROVAL status (not visible yet)
        String shipmentId = shipmentService.createShipmentForOrder(order);
        order.setShipmentId(shipmentId);

        // Change order status to PENDING_APPROVAL
        order.setStatus(OrderStatus.PENDING_APPROVAL);
        orderRepository.update(order);
        return true;
    }

    /**
     * Approves an order and creates the corresponding shipment.
     * In the new workflow, shipments are created at approval time with PENDING_ASSIGNMENT status.
     * Delivery person assignment happens later at the shipment level.
     *
     * @param orderId The ID of the order to approve.
     * @return true if approval was successful, false otherwise.
     */
    public boolean approveOrderAndCreateShipment(String orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            return false;
        }

        // Only allow approval for PENDING_APPROVAL or PAID orders
        if (order.getStatus() != OrderStatus.PENDING_APPROVAL && order.getStatus() != OrderStatus.PAID) {
            return false;
        }

        // Create shipment with PENDING_ASSIGNMENT status (no delivery person yet)
        String shipmentId = shipmentService.createShipmentForOrder(order);
        if (shipmentId == null) {
            return false;
        }

        // Observer Pattern: Register NotificationObserver for this user's shipment
        shipmentService.registerObserver(new NotificationObserver(order.getUserId()));

        // Update order with shipment ID and change status to APPROVED
        order.setShipmentId(shipmentId);
        order.setStatus(OrderStatus.APPROVED);
        orderRepository.update(order);

        return true;
    }

    /**
     * Auto-assigns delivery persons to pending orders based on coverage area and availability.
     * Uses a round-robin approach within each coverage area to distribute workload evenly.
     *
     * @return The number of orders that were successfully auto-assigned.
     */
    public int autoAssignDeliveryPersons() {
        int assignedCount = 0;

        // Get all orders that are paid but don't have a delivery person assigned
        List<Order> pendingOrders = orderRepository.findAll().stream()
                .filter(order -> order.getStatus() == OrderStatus.PAID && order.getDeliveryPersonId() == null)
                .collect(Collectors.toList());

        // Get all available delivery persons with active vehicles
        List<DeliveryPerson> availableDeliveryPersons = deliveryPersonRepository.getAllDeliveryPersons().stream()
                .filter(dp -> dp.getAvailability() == AvailabilityStatus.AVAILABLE)
                .filter(dp -> hasValidActiveVehicle(dp)) // Must have an active vehicle
                .collect(Collectors.toList());

        if (availableDeliveryPersons.isEmpty()) {
            return 0; // No delivery persons available with valid vehicles
        }

        // Try to assign each pending order
        for (Order order : pendingOrders) {
            // Determine coverage area based on destination city
            CoverageArea requiredArea = determineCoverageArea(order.getDestination());

            // Find delivery persons that cover this area
            List<DeliveryPerson> suitableDeliveryPersons = availableDeliveryPersons.stream()
                    .filter(dp -> dp.getCoverageArea() == requiredArea)
                    .sorted(Comparator.comparingInt(dp -> getAssignedOrdersCount(dp.getId())))
                    .collect(Collectors.toList());

            // If no delivery person for specific area, try to use any available
            if (suitableDeliveryPersons.isEmpty()) {
                suitableDeliveryPersons = availableDeliveryPersons.stream()
                        .sorted(Comparator.comparingInt(dp -> getAssignedOrdersCount(dp.getId())))
                        .collect(Collectors.toList());
            }

            // Assign to the delivery person with least orders
            if (!suitableDeliveryPersons.isEmpty()) {
                DeliveryPerson selectedDeliveryPerson = suitableDeliveryPersons.get(0);
                if (assignDeliveryPerson(order.getId(), selectedDeliveryPerson.getId())) {
                    assignedCount++;
                }
            }
        }

        return assignedCount;
    }

    /**
     * Determines the coverage area based on the destination address.
     * This is a simplified implementation based on address location.
     *
     * @param destination The destination address.
     * @return The determined coverage area.
     */
    private CoverageArea determineCoverageArea(Address destination) {
        if (destination == null || destination.getCity() == null) {
            return CoverageArea.CENTRAL;
        }

        String city = destination.getCity().toLowerCase();
        String state = destination.getState() != null ? destination.getState().toLowerCase() : "";

        // Simple heuristic based on common zone keywords
        if (city.contains("norte") || state.contains("norte")) {
            return CoverageArea.NORTH;
        } else if (city.contains("sur") || state.contains("sur")) {
            return CoverageArea.SOUTH;
        }

        // Default to Central zone or City-wide for flexibility
        return CoverageArea.CENTRAL;
    }

    /**
     * Gets the count of orders currently assigned to a delivery person.
     *
     * @param deliveryPersonId The ID of the delivery person.
     * @return The count of assigned orders.
     */
    private int getAssignedOrdersCount(String deliveryPersonId) {
        return (int) orderRepository.findAll().stream()
                .filter(order -> deliveryPersonId.equals(order.getDeliveryPersonId()))
                .filter(order -> order.getStatus() != OrderStatus.APPROVED &&
                               order.getStatus() != OrderStatus.CANCELLED)
                .count();
    }

    // ===========================
    // Order Cancellation
    // ===========================

    /**
     * Cancels an order if it's in a cancellable state.
     * Only orders with status AWAITING_PAYMENT, PAID, or PENDING_APPROVAL can be cancelled.
     *
     * @param orderId The ID of the order to cancel.
     * @throws IllegalArgumentException if the order is not found.
     * @throws IllegalStateException if the order cannot be cancelled in its current state.
     */
    public void cancelOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + orderId));

        // Verify order can be cancelled
        if (order.getStatus() != OrderStatus.AWAITING_PAYMENT &&
            order.getStatus() != OrderStatus.PAID &&
            order.getStatus() != OrderStatus.PENDING_APPROVAL) {
            throw new IllegalStateException(
                    "Cannot cancel order in status: " + order.getStatus().getDisplayName() +
                    ". Only orders in AWAITING_PAYMENT, PAID, or PENDING_APPROVAL can be cancelled."
            );
        }

        // Update order status to CANCELLED
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.update(order);

        Logger.info("Order " + orderId + " has been cancelled");
    }

    /**
     * Checks if an order can be cancelled based on its current status.
     *
     * @param orderId The ID of the order to check.
     * @return true if the order can be cancelled, false otherwise.
     */
    public boolean canCancelOrder(String orderId) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (!orderOpt.isPresent()) {
            Logger.warning("canCancelOrder: Order not found with ID: " + orderId);
            return false;
        }

        Order order = orderOpt.get();
        OrderStatus status = order.getStatus();
        boolean canCancel = status == OrderStatus.AWAITING_PAYMENT ||
                           status == OrderStatus.PAID ||
                           status == OrderStatus.PENDING_APPROVAL;

        Logger.info("canCancelOrder: Order " + orderId + " has status " + status +
                   " (displayName: " + status.getDisplayName() + "), canCancel=" + canCancel);

        return canCancel;
    }

    /**
     * Checks if a delivery person has a valid active vehicle.
     * A valid active vehicle must exist, be available, and be owned by the delivery person.
     *
     * @param deliveryPerson The delivery person to check.
     * @return true if the delivery person has a valid active vehicle, false otherwise.
     */
    private boolean hasValidActiveVehicle(DeliveryPerson deliveryPerson) {
        // Check if delivery person has an active vehicle plate set
        if (deliveryPerson.getActiveVehiclePlate() == null || deliveryPerson.getActiveVehiclePlate().isEmpty()) {
            return false;
        }

        // Check if the vehicle exists
        Optional<Vehicle> vehicleOpt = vehicleService.findVehicleByPlate(deliveryPerson.getActiveVehiclePlate());
        if (!vehicleOpt.isPresent()) {
            return false;
        }

        Vehicle vehicle = vehicleOpt.get();

        // Check if vehicle is available
        if (!vehicle.isAvailable()) {
            return false;
        }

        // Check if vehicle belongs to this delivery person
        return deliveryPerson.getId().equals(vehicle.getDeliveryPersonId());
    }

    /**
     * Deletes an order permanently from the repository.
     * Only cancelled orders can be deleted.
     *
     * @param orderId The ID of the order to delete.
     * @return true if deletion was successful, false otherwise.
     */
    public boolean deleteOrder(String orderId) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (!orderOpt.isPresent()) {
            Logger.warning("deleteOrder: Order not found with ID: " + orderId);
            return false;
        }

        Order order = orderOpt.get();

        // Only allow deletion of cancelled orders
        if (order.getStatus() != OrderStatus.CANCELLED) {
            Logger.warning("deleteOrder: Cannot delete order that is not cancelled. Current status: " + order.getStatus());
            return false;
        }

        // Delete the order from repository
        boolean deleted = orderRepository.deleteOrder(orderId);

        if (deleted) {
            Logger.info("Order " + orderId + " has been permanently deleted");
        } else {
            Logger.error("Failed to delete order " + orderId);
        }

        return deleted;
    }
}
