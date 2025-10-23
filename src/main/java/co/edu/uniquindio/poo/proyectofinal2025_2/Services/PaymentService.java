package co.edu.uniquindio.poo.proyectofinal2025_2.Services;

import co.edu.uniquindio.poo.proyectofinal2025_2.Model.Invoice;
import co.edu.uniquindio.poo.proyectofinal2025_2.Model.Payment;
import co.edu.uniquindio.poo.proyectofinal2025_2.Model.PaymentMethod;
import co.edu.uniquindio.poo.proyectofinal2025_2.Model.Enums.PaymentStatus;
import co.edu.uniquindio.poo.proyectofinal2025_2.Repositories.InvoiceRepository;
import co.edu.uniquindio.poo.proyectofinal2025_2.Repositories.PaymentRepository;
import co.edu.uniquindio.poo.proyectofinal2025_2.Util.UtilService.IdGenerationUtil;

import java.time.LocalDateTime;

/**
 * <p>Provides business logic services related to payment processing.</p>
 * <p>This service handles the processing of payments for invoices. Upon successful
 * payment, it triggers the next step in the order lifecycle by notifying the OrderService.</p>
 */
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final OrderService orderService;

    /**
     * Constructor with dependency injection for repositories and services.
     *
     * @param paymentRepository The PaymentRepository instance.
     * @param invoiceRepository The InvoiceRepository instance.
     * @param orderService The OrderService instance.
     */
    public PaymentService(PaymentRepository paymentRepository, InvoiceRepository invoiceRepository,
                         OrderService orderService) {
        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
        this.orderService = orderService;
    }

    /**
     * Default constructor that uses singleton instances.
     * This provides backward compatibility and ease of use.
     */
    public PaymentService() {
        this(PaymentRepository.getInstance(), InvoiceRepository.getInstance(), new OrderService());
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
}
