package co.edu.uniquindio.poo.ProyectoFinal2025_2.Services;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Invoice;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Payment;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.PaymentMethod;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Order;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.PaymentStatus;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.dto.PaymentReceiptDTO;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Repositories.InvoiceRepository;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Repositories.PaymentRepository;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Repositories.OrderRepository;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilService.IdGenerationUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * <p>Provides business logic services related to payment processing.</p>
 * <p>This service handles the processing of payments for invoices. Upon successful
 * payment, it triggers the next step in the order lifecycle by notifying the OrderService.</p>
 */
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final OrderRepository orderRepository;
    private final OrderService orderService;

    /**
     * Constructor with dependency injection for repositories and services.
     *
     * @param paymentRepository The PaymentRepository instance.
     * @param invoiceRepository The InvoiceRepository instance.
     * @param orderRepository The OrderRepository instance.
     * @param orderService The OrderService instance.
     */
    public PaymentService(PaymentRepository paymentRepository, InvoiceRepository invoiceRepository,
                         OrderRepository orderRepository, OrderService orderService) {
        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
        this.orderRepository = orderRepository;
        this.orderService = orderService;
    }

    /**
     * Default constructor that uses singleton instances.
     * This provides backward compatibility and ease of use.
     */
    public PaymentService() {
        this(PaymentRepository.getInstance(), InvoiceRepository.getInstance(),
             OrderRepository.getInstance(), new OrderService());
    }

    // ===========================
    // Payment Processing
    // ===========================

    /**
     * Processes a payment for a given invoice.
     *
     * @param invoiceId     The ID of the invoice to be paid.
     * @param paymentMethod The method of payment being used.
     * @return The created Payment object.
     * @throws IllegalArgumentException if the invoice is not found.
     */
    public Payment processPayment(String invoiceId, PaymentMethod paymentMethod) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found with ID: " + invoiceId));

        // --- Payment Gateway Simulation --- //
        // In a real application, this would interact with an external payment gateway (e.g., Stripe, PayPal).
        // For now, we will assume the payment is always successful.
        boolean paymentSuccessful = true;

        Payment newPayment = new Payment.Builder()
                .withId(IdGenerationUtil.generateId())
                .withInvoiceId(invoiceId)
                .withAmount(invoice.getTotalAmount())
                .withDate(LocalDateTime.now())
                .withStatus(paymentSuccessful ? PaymentStatus.APPROVED : PaymentStatus.FAILED)
                .withPaymentMethod(paymentMethod)
                .build();

        paymentRepository.addPayment(newPayment);

        // If payment was successful, trigger the next step in the Order Saga.
        if (paymentSuccessful) {
            orderService.confirmOrderPayment(invoice.getOrderId(), newPayment.getId());
        }

        return newPayment;
    }

    // ===========================
    // Query Methods
    // ===========================

    /**
     * Retrieves all payment receipts for a specific user.
     *
     * @param userId The ID of the user.
     * @return A list of payment receipt DTOs.
     */
    public List<PaymentReceiptDTO> getUserPaymentReceipts(String userId) {
        List<PaymentReceiptDTO> receipts = new ArrayList<>();

        // Get all orders for the user
        List<Order> userOrders = orderRepository.findAll().stream()
                .filter(order -> order.getUserId().equals(userId))
                .collect(java.util.stream.Collectors.toList());

        for (Order order : userOrders) {
            // Find invoice for the order
            Optional<Invoice> invoiceOpt = invoiceRepository.findAll().stream()
                    .filter(inv -> inv.getOrderId().equals(order.getId()))
                    .findFirst();

            if (invoiceOpt.isPresent()) {
                Invoice invoice = invoiceOpt.get();

                // Find payment for the invoice
                Optional<Payment> paymentOpt = paymentRepository.findByInvoiceId(invoice.getId());

                if (paymentOpt.isPresent()) {
                    Payment payment = paymentOpt.get();

                    PaymentReceiptDTO receipt = PaymentReceiptDTO.builder()
                            .paymentId(payment.getId())
                            .invoiceId(invoice.getId())
                            .invoiceNumber(invoice.getInvoiceNumber())
                            .orderId(order.getId())
                            .amount(payment.getAmount())
                            .paymentDate(payment.getDate())
                            .status(payment.getStatus())
                            .paymentMethodType(payment.getPaymentMethod().getType())
                            .paymentProvider(payment.getPaymentMethod().getProvider())
                            .accountNumber(payment.getPaymentMethod().getAccountNumber())
                            .build();

                    receipts.add(receipt);
                }
            }
        }

        // Sort by payment date (most recent first)
        receipts.sort((r1, r2) -> r2.getPaymentDate().compareTo(r1.getPaymentDate()));

        return receipts;
    }
}
