package co.edu.uniquindio.poo.ProyectoFinal2025_2.Services;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Order;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.PaymentMethod;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Invoice;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.LineItem;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Decorator.CostCalculator;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Command.PaymentCommand;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Command.ProcessPaymentCommand;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Template.OrderProcessor;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Repositories.OrderRepository;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Repositories.InvoiceRepository;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Checkout service that orchestrates the order checkout process using multiple design patterns.
 *
 * <p>Design Patterns Used:</p>
 * <ul>
 *     <li><b>Template Method:</b> Extends OrderProcessor for consistent workflow</li>
 *     <li><b>Command:</b> Uses PaymentCommand for payment operations</li>
 *     <li><b>Decorator:</b> Uses CostCalculator for flexible cost calculation</li>
 *     <li><b>Strategy:</b> Delegates to TariffCalculationStrategy via CostCalculator</li>
 *     <li><b>Singleton:</b> Uses Singleton repositories</li>
 * </ul>
 */
public class CheckoutService extends OrderProcessor {

    private final OrderRepository orderRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentService paymentService;
    private final List<PaymentCommand> paymentHistory;

    /**
     * Constructor with dependency injection.
     */
    public CheckoutService(OrderRepository orderRepository,
                          InvoiceRepository invoiceRepository,
                          PaymentService paymentService) {
        this.orderRepository = orderRepository;
        this.invoiceRepository = invoiceRepository;
        this.paymentService = paymentService;
        this.paymentHistory = new ArrayList<>();
    }

    /**
     * Default constructor using singletons.
     */
    public CheckoutService() {
        this(OrderRepository.getInstance(),
             InvoiceRepository.getInstance(),
             new PaymentService());
    }

    // ==========================================
    // Template Method Implementation
    // ==========================================

    @Override
    protected boolean validateOrder(Order order) {
        Logger.info("Validating order: " + order.getId());

        if (order == null) {
            Logger.error("Order is null");
            return false;
        }

        if (order.getUserId() == null || order.getUserId().isEmpty()) {
            Logger.error("Order has no user ID");
            return false;
        }

        // Validate that order has an invoice with valid total cost
        if (order.getInvoiceId() != null) {
            Invoice invoice = invoiceRepository.findById(order.getInvoiceId()).orElse(null);
            if (invoice != null && invoice.getTotalAmount() <= 0) {
                Logger.error("Order has invalid total cost in invoice");
                return false;
            }
        }

        Logger.info("Order validation passed");
        return true;
    }

    @Override
    protected boolean calculateCosts(Order order) {
        Logger.info("Calculating costs for order: " + order.getId());

        try {
            // Cost calculation is done via Decorator pattern when order is created
            // This step verifies the calculations by checking the invoice
            if (order.getInvoiceId() == null) {
                Logger.error("Order has no invoice");
                return false;
            }

            Invoice invoice = invoiceRepository.findById(order.getInvoiceId()).orElse(null);
            if (invoice == null) {
                Logger.error("Invoice not found for order");
                return false;
            }

            double totalFromLineItems = invoice.getLineItems().stream()
                    .mapToDouble(LineItem::getAmount)
                    .sum();

            if (Math.abs(totalFromLineItems - invoice.getTotalAmount()) > 0.01) {
                Logger.error("Cost mismatch detected in invoice");
                return false;
            }

            Logger.info("Cost calculation verified: $" + invoice.getTotalAmount());
            return true;
        } catch (Exception e) {
            Logger.error("Failed to calculate costs: " + e.getMessage());
            return false;
        }
    }

    @Override
    protected boolean generateInvoice(Order order) {
        Logger.info("Generating invoice for order: " + order.getId());

        try {
            // Check if invoice already exists
            boolean invoiceExists = invoiceRepository.findAll().stream()
                    .anyMatch(inv -> inv.getOrderId().equals(order.getId()));

            if (invoiceExists) {
                Logger.info("Invoice already exists for order");
                return true;
            }

            // Invoice generation would happen here
            // For now, we assume it's handled by OrderService
            Logger.info("Invoice generation delegated to OrderService");
            return true;
        } catch (Exception e) {
            Logger.error("Failed to generate invoice: " + e.getMessage());
            return false;
        }
    }

    @Override
    protected boolean processPayment(Order order) {
        Logger.info("Processing payment for order: " + order.getId());

        try {
            // This is where the Command pattern is demonstrated
            // In a real checkout flow, the payment method would be provided by user
            // For now, this is a placeholder showing the pattern usage

            Logger.info("Payment processing requires user payment method selection");
            Logger.info("Use processPaymentWithMethod() to complete payment");
            return true;
        } catch (Exception e) {
            Logger.error("Failed to process payment: " + e.getMessage());
            return false;
        }
    }

    /**
     * Processes payment using the Command pattern.
     * This method demonstrates the Command pattern for payment operations.
     *
     * @param invoiceId The invoice ID to pay
     * @param paymentMethod The payment method to use
     * @param amount The amount to pay
     * @return true if successful, false otherwise
     */
    public boolean processPaymentWithMethod(String invoiceId, PaymentMethod paymentMethod, double amount) {
        Logger.info("Creating payment command for invoice: " + invoiceId);

        // Create payment command (Command pattern)
        PaymentCommand paymentCommand = new ProcessPaymentCommand(
                paymentService,
                invoiceId,
                paymentMethod,
                amount
        );

        // Execute command
        boolean success = paymentCommand.execute();

        if (success) {
            // Store command for potential undo/refund
            paymentHistory.add(paymentCommand);
            Logger.info("Payment command executed and stored in history");
        }

        return success;
    }

    /**
     * Refunds the last payment using the Command pattern's undo functionality.
     *
     * @return true if successful, false otherwise
     */
    public boolean refundLastPayment() {
        if (paymentHistory.isEmpty()) {
            Logger.warning("No payments to refund");
            return false;
        }

        PaymentCommand lastPayment = paymentHistory.get(paymentHistory.size() - 1);

        if (!lastPayment.canUndo()) {
            Logger.error("Last payment cannot be undone");
            return false;
        }

        boolean success = lastPayment.undo();

        if (success) {
            paymentHistory.remove(lastPayment);
            Logger.info("Last payment refunded successfully");
        }

        return success;
    }

    @Override
    protected boolean createShipment(Order order) {
        Logger.info("Creating shipment for order: " + order.getId());

        try {
            // Shipment creation is handled by ShipmentService
            Logger.info("Shipment creation delegated to ShipmentService");
            return true;
        } catch (Exception e) {
            Logger.error("Failed to create shipment: " + e.getMessage());
            return false;
        }
    }

    @Override
    protected boolean notifyCustomer(Order order) {
        Logger.info("Notifying customer for order: " + order.getId());

        try {
            // This would integrate with notification service
            // Observer pattern handles notifications when shipment status changes
            Logger.info("Customer notification would be sent here");
            return true;
        } catch (Exception e) {
            Logger.error("Failed to notify customer: " + e.getMessage());
            return false;
        }
    }

    @Override
    protected void finalizeOrder(Order order) {
        Logger.info("Finalizing order: " + order.getId());

        // Update order in repository
        orderRepository.update(order);

        Logger.info("Order finalized successfully");
    }

    /**
     * Gets the payment history (for demonstration of Command pattern).
     *
     * @return List of executed payment commands
     */
    public List<PaymentCommand> getPaymentHistory() {
        return new ArrayList<>(paymentHistory);
    }
}
