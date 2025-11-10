package co.edu.uniquindio.poo.ProyectoFinal2025_2.Services;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Config.MercadoPagoInitialize;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Order;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.User;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Invoice;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.dto.MercadoPagoPreferenceDTO;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.dto.MercadoPagoPaymentDTO;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Repositories.InvoiceRepository;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferencePayerRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for Mercado Pago integration.
 *
 * <p>This service handles:</p>
 * <ul>
 *     <li>Creating payment preferences (checkout links)</li>
 *     <li>Processing payment notifications (webhooks)</li>
 *     <li>Querying payment status</li>
 *     <li>Managing refunds</li>
 * </ul>
 */
public class MercadoPagoService {

    private static final String SUCCESS_URL = "http://localhost:8080/payment/success";
    private static final String FAILURE_URL = "http://localhost:8080/payment/failure";
    private static final String PENDING_URL = "http://localhost:8080/payment/pending";

    private final InvoiceRepository invoiceRepository = InvoiceRepository.getInstance();

    /**
     * Creates a payment preference for an order.
     * This generates a checkout link that the user can use to pay.
     *
     * @param order The order to create preference for
     * @param user The user making the purchase
     * @return Preference DTO with checkout URLs
     * @throws MPException if preference creation fails
     */
    public MercadoPagoPreferenceDTO createPaymentPreference(Order order, User user) throws MPException {
        Logger.info("Creating Mercado Pago preference for order: " + order.getId());

        // Get total amount from invoice
        double totalAmount = 0.0;
        if (order.getInvoiceId() != null) {
            Invoice invoice = invoiceRepository.findById(order.getInvoiceId()).orElse(null);
            if (invoice != null) {
                totalAmount = invoice.getTotalAmount();
            }
        }

        if (totalAmount <= 0) {
            throw new MPException("Order has no valid invoice or total amount is zero");
        }

        try {
            // Ensure Mercado Pago is initialized
            if (!MercadoPagoInitialize.isInitialized()) {
                MercadoPagoInitialize.initialize();
            }

            // Create item for the preference
            PreferenceItemRequest item = PreferenceItemRequest.builder()
                    .id(order.getId())
                    .title("Envío - Orden #" + order.getId())
                    .description("Servicio de envío de paquete")
                    .categoryId("shipping")
                    .quantity(1)
                    .currencyId("COP")
                    .unitPrice(BigDecimal.valueOf(totalAmount))
                    .build();

            List<PreferenceItemRequest> items = new ArrayList<>();
            items.add(item);

            // Create payer info
            PreferencePayerRequest.PreferencePayerRequestBuilder payerBuilder = PreferencePayerRequest.builder()
                    .name(user.getName())
                    .surname(user.getLastName())
                    .email(user.getEmail());

            // Add phone if available
            if (user.getPhone() != null && !user.getPhone().isEmpty()) {
                payerBuilder.phone(com.mercadopago.client.common.PhoneRequest.builder()
                        .areaCode("57")
                        .number(user.getPhone())
                        .build());
            }

            PreferencePayerRequest payer = payerBuilder.build();

            // Create back URLs (where to redirect after payment)
            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success(SUCCESS_URL + "?orderId=" + order.getId())
                    .failure(FAILURE_URL + "?orderId=" + order.getId())
                    .pending(PENDING_URL + "?orderId=" + order.getId())
                    .build();

            // Create the preference request
            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(items)
                    .payer(payer)
                    .backUrls(backUrls)
                    .autoReturn("approved")
                    .externalReference(order.getId())
                    .statementDescriptor("ENVIOS")
                    .notificationUrl("http://localhost:8080/webhooks/mercadopago") // TODO: Configure webhook
                    .build();

            // Create the preference
            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);

            Logger.info("Preference created successfully: " + preference.getId());

            // Build DTO
            return MercadoPagoPreferenceDTO.builder()
                    .preferenceId(preference.getId())
                    .initPoint(preference.getInitPoint())
                    .sandboxInitPoint(preference.getSandboxInitPoint())
                    .orderId(order.getId())
                    .title("Envío - Orden #" + order.getId())
                    .description("Servicio de envío de paquete")
                    .totalAmount(totalAmount)
                    .currency("COP")
                    .payerEmail(user.getEmail())
                    .build();

        } catch (MPApiException e) {
            Logger.error("Mercado Pago API Error: " + e.getApiResponse().getContent());
            throw new MPException("Failed to create payment preference: " + e.getMessage());
        } catch (MPException e) {
            Logger.error("Mercado Pago Error: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Retrieves payment information by payment ID.
     * Used to verify payment status and details.
     *
     * @param paymentId The Mercado Pago payment ID
     * @return Payment DTO with payment details
     * @throws MPException if payment retrieval fails
     */
    public MercadoPagoPaymentDTO getPaymentInfo(Long paymentId) throws MPException {
        Logger.info("Retrieving payment info for ID: " + paymentId);

        try {
            PaymentClient client = new PaymentClient();
            Payment payment = client.get(paymentId);

            Logger.info("Payment retrieved: Status = " + payment.getStatus());

            return MercadoPagoPaymentDTO.builder()
                    .paymentId(payment.getId())
                    .status(payment.getStatus())
                    .statusDetail(payment.getStatusDetail())
                    .orderId(payment.getExternalReference())
                    .transactionAmount(payment.getTransactionAmount().doubleValue())
                    .currency(payment.getCurrencyId())
                    .paymentMethodId(payment.getPaymentMethodId())
                    .paymentTypeId(payment.getPaymentTypeId())
                    .payerEmail(payment.getPayer() != null ? payment.getPayer().getEmail() : null)
                    .dateCreated(convertToLocalDateTime(payment.getDateCreated()))
                    .dateApproved(convertToLocalDateTime(payment.getDateApproved()))
                    .externalReference(payment.getExternalReference())
                    .build();

        } catch (MPApiException e) {
            Logger.error("Mercado Pago API Error: " + e.getApiResponse().getContent());
            throw new MPException("Failed to retrieve payment: " + e.getMessage());
        } catch (MPException e) {
            Logger.error("Mercado Pago Error: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Checks if a payment is approved.
     *
     * @param paymentId The payment ID to check
     * @return true if approved, false otherwise
     */
    public boolean isPaymentApproved(Long paymentId) {
        try {
            MercadoPagoPaymentDTO payment = getPaymentInfo(paymentId);
            return "approved".equals(payment.getStatus());
        } catch (MPException e) {
            Logger.error("Failed to check payment status: " + e.getMessage());
            return false;
        }
    }

    /**
     * Processes a Mercado Pago webhook notification.
     * This method should be called when Mercado Pago sends a notification.
     *
     * @param topic The notification topic (payment, merchant_order, etc.)
     * @param id The resource ID
     * @return Payment DTO if it's a payment notification, null otherwise
     */
    public MercadoPagoPaymentDTO processWebhookNotification(String topic, Long id) {
        Logger.info("Processing webhook: topic=" + topic + ", id=" + id);

        try {
            if ("payment".equals(topic)) {
                return getPaymentInfo(id);
            } else {
                Logger.info("Ignoring non-payment notification: " + topic);
                return null;
            }
        } catch (MPException e) {
            Logger.error("Failed to process webhook: " + e.getMessage());
            return null;
        }
    }

    /**
     * Converts OffsetDateTime to LocalDateTime.
     */
    private java.time.LocalDateTime convertToLocalDateTime(OffsetDateTime offsetDateTime) {
        if (offsetDateTime == null) {
            return null;
        }
        return offsetDateTime.atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * Gets the checkout URL for an order.
     * Uses sandbox URL if in sandbox mode.
     *
     * @param preference The preference DTO
     * @return The checkout URL
     */
    public String getCheckoutUrl(MercadoPagoPreferenceDTO preference) {
        if (MercadoPagoInitialize.isSandbox()) {
            return preference.getSandboxInitPoint();
        } else {
            return preference.getInitPoint();
        }
    }

    /**
     * Validates Mercado Pago configuration before use.
     *
     * @return true if properly configured, false otherwise
     */
    public boolean validateConfiguration() {
        try {
            if (!MercadoPagoInitialize.isInitialized()) {
                MercadoPagoInitialize.initialize();
            }
            Logger.info("Mercado Pago configuration is valid");
            return true;
        } catch (Exception e) {
            Logger.error("Mercado Pago configuration error: " + e.getMessage());
            return false;
        }
    }
}
