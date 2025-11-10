package co.edu.uniquindio.poo.ProyectoFinal2025_2.Controller;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Order;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.User;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Invoice;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.LineItem;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.PaymentMethod;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.dto.MercadoPagoPreferenceDTO;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.dto.OrderDetailDTO;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.AuthenticationService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.MercadoPagoService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.PaymentMethodService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.CheckoutService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.NotificationService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Repositories.InvoiceRepository;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Repositories.AdminRepository;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.DialogUtil;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.concurrent.Worker;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for Mercado Pago payment integration.
 * This controller handles the payment step of the checkout process.
 */
public class MercadoPagoPaymentController implements Initializable {

    @FXML private Label lblOrderId;
    @FXML private Label lblTotalAmount;
    @FXML private VBox costBreakdownContainer;
    @FXML private ComboBox<PaymentMethod> cmbPaymentMethod;
    @FXML private RadioButton rbMercadoPago;
    @FXML private RadioButton rbSavedMethod;
    @FXML private Button btnProceedPayment;
    @FXML private Label lblMercadoPagoInfo;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private WebView webViewPayment;
    @FXML private VBox vboxPaymentForm;
    @FXML private VBox vboxWebView;

    private final AuthenticationService authService = AuthenticationService.getInstance();
    private final MercadoPagoService mercadoPagoService = new MercadoPagoService();
    private final PaymentMethodService paymentMethodService = new PaymentMethodService();
    private final CheckoutService checkoutService = new CheckoutService();
    private final InvoiceRepository invoiceRepository = InvoiceRepository.getInstance();

    private User currentUser;
    private Order currentOrder;
    private OrderDetailDTO orderDetail;
    private Invoice invoice;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        currentUser = (User) authService.getCurrentPerson();

        if (currentUser == null) {
            Logger.error("No user logged in");
            DialogUtil.showError("Error", "No hay usuario autenticado");
            return;
        }

        setupPaymentMethodToggle();
        loadUserPaymentMethods();
        setupUI();
        Logger.info("MercadoPagoPaymentController initialized");
    }

    /**
     * Sets the order and order details for payment processing.
     * This method should be called before showing the view.
     *
     * @param order The order to process
     * @param orderDetail The order details (for display)
     */
    public void setOrder(Order order, OrderDetailDTO orderDetail) {
        this.currentOrder = order;
        this.orderDetail = orderDetail;

        // Load invoice
        if (order.getInvoiceId() != null) {
            this.invoice = invoiceRepository.findById(order.getInvoiceId()).orElse(null);
        }

        displayOrderInfo();
    }

    private void setupPaymentMethodToggle() {
        ToggleGroup paymentTypeGroup = new ToggleGroup();
        rbMercadoPago.setToggleGroup(paymentTypeGroup);
        rbSavedMethod.setToggleGroup(paymentTypeGroup);
        rbMercadoPago.setSelected(true);

        paymentTypeGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == rbMercadoPago) {
                cmbPaymentMethod.setDisable(true);
                cmbPaymentMethod.setVisible(false);
                lblMercadoPagoInfo.setVisible(true);
            } else {
                cmbPaymentMethod.setDisable(false);
                cmbPaymentMethod.setVisible(true);
                lblMercadoPagoInfo.setVisible(false);
            }
        });
    }

    private void loadUserPaymentMethods() {
        List<PaymentMethod> methods = paymentMethodService.getPaymentMethodsByUserId(currentUser.getId());
        cmbPaymentMethod.getItems().clear();
        cmbPaymentMethod.getItems().addAll(methods);
        if (!methods.isEmpty()) {
            cmbPaymentMethod.setValue(methods.get(0));
        } else {
            rbSavedMethod.setDisable(true);
            rbMercadoPago.setSelected(true);
        }
    }

    private void displayOrderInfo() {
        if (currentOrder == null || orderDetail == null) return;

        lblOrderId.setText("Orden #" + currentOrder.getId());

        // Use invoice total or orderDetail total
        double totalAmount = (invoice != null) ? invoice.getTotalAmount() : orderDetail.getTotalCost();
        lblTotalAmount.setText(String.format("$%.2f COP", totalAmount));

        displayCostBreakdown();
    }

    private void displayCostBreakdown() {
        costBreakdownContainer.getChildren().clear();

        if (invoice != null && invoice.getLineItems() != null && !invoice.getLineItems().isEmpty()) {
            // Display invoice line items
            for (LineItem item : invoice.getLineItems()) {
                addBreakdownItem(item.getDescription(), item.getAmount());
            }
        } else if (orderDetail != null) {
            // Fallback to orderDetail breakdown
            addBreakdownItem("Costo Base", orderDetail.getBaseCost());
            if (orderDetail.getDistanceCost() > 0) {
                addBreakdownItem("Costo por Distancia", orderDetail.getDistanceCost());
            }
            if (orderDetail.getWeightCost() > 0) {
                addBreakdownItem("Costo por Peso", orderDetail.getWeightCost());
            }
            if (orderDetail.getServicesCost() > 0) {
                addBreakdownItem("Servicios Adicionales", orderDetail.getServicesCost());
            }
            if (orderDetail.getPriorityCost() > 0) {
                addBreakdownItem("Recargo por Prioridad", orderDetail.getPriorityCost());
            }
        }

        Separator separator = new Separator();
        separator.setPadding(new Insets(5, 0, 5, 0));
        costBreakdownContainer.getChildren().add(separator);

        double totalAmount = (invoice != null) ? invoice.getTotalAmount() : orderDetail.getTotalCost();
        Label totalLabel = new Label(String.format("TOTAL: $%.2f COP", totalAmount));
        totalLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #032d4d;");
        costBreakdownContainer.getChildren().add(totalLabel);
    }

    private void addBreakdownItem(String description, double amount) {
        Label label = new Label(String.format("%s: $%.2f", description, amount));
        label.setStyle("-fx-font-size: 14px; -fx-text-fill: #6c757d;");
        costBreakdownContainer.getChildren().add(label);
    }

    private void setupUI() {
        progressIndicator.setVisible(false);
        cmbPaymentMethod.setDisable(true);
        cmbPaymentMethod.setVisible(false);

        // Initially hide WebView and show payment form
        if (vboxWebView != null) {
            vboxWebView.setVisible(false);
            vboxWebView.setManaged(false);
        }
        if (vboxPaymentForm != null) {
            vboxPaymentForm.setVisible(true);
            vboxPaymentForm.setManaged(true);
        }
    }

    @FXML
    private void handleProceedPayment() {
        if (currentOrder == null) {
            DialogUtil.showError("Error", "No hay orden para procesar");
            return;
        }
        if (rbMercadoPago.isSelected()) {
            processMercadoPagoPayment();
        } else {
            processSavedMethodPayment();
        }
    }

    private void processMercadoPagoPayment() {
        Logger.info("Processing Mercado Pago payment for order: " + currentOrder.getId());
        progressIndicator.setVisible(true);
        btnProceedPayment.setDisable(true);

        try {
            if (!mercadoPagoService.validateConfiguration()) {
                throw new Exception("Mercado Pago no está configurado correctamente");
            }

            MercadoPagoPreferenceDTO preference = mercadoPagoService.createPaymentPreference(currentOrder, currentUser);
            Logger.info("Payment preference created: " + preference.getPreferenceId());

            String checkoutUrl = mercadoPagoService.getCheckoutUrl(preference);

            double totalAmount = (invoice != null) ? invoice.getTotalAmount() : orderDetail.getTotalCost();

            boolean proceed = DialogUtil.showConfirmation(
                    "Pago con Mercado Pago",
                    "Se abrirá el checkout de Mercado Pago dentro de la aplicación.\n\n" +
                    "Monto total: $" + String.format("%.2f", totalAmount) + " COP\n\n" +
                    "¿Desea continuar?"
            );

            if (proceed) {
                openWebViewPayment(checkoutUrl);
            }

        } catch (Exception e) {
            Logger.error("Failed to process Mercado Pago payment: " + e.getMessage());
            DialogUtil.showError("Error en el Pago",
                    "No se pudo iniciar el pago con Mercado Pago:\n" + e.getMessage());
        } finally {
            progressIndicator.setVisible(false);
            btnProceedPayment.setDisable(false);
        }
    }

    private void processSavedMethodPayment() {
        Logger.info("Processing payment with saved method for order: " + currentOrder.getId());
        PaymentMethod selectedMethod = cmbPaymentMethod.getValue();

        if (selectedMethod == null) {
            DialogUtil.showError("Error", "Seleccione un método de pago");
            return;
        }

        progressIndicator.setVisible(true);
        btnProceedPayment.setDisable(true);

        try {
            boolean success = checkoutService.processOrder(currentOrder);
            if (success) {
                DialogUtil.showSuccess("Pago Exitoso",
                        "Su pago ha sido procesado exitosamente.\n\nOrden: " + currentOrder.getId());
                closeWindow();
            } else {
                DialogUtil.showError("Error en el Pago",
                        "No se pudo procesar el pago. Por favor intente de nuevo.");
            }
        } catch (Exception e) {
            Logger.error("Failed to process payment: " + e.getMessage());
            DialogUtil.showError("Error en el Pago",
                    "Ocurrió un error al procesar el pago:\n" + e.getMessage());
        } finally {
            progressIndicator.setVisible(false);
            btnProceedPayment.setDisable(false);
        }
    }

    /**
     * Opens Mercado Pago checkout in an embedded WebView.
     * Monitors the payment flow and updates order status accordingly.
     */
    private void openWebViewPayment(String checkoutUrl) {
        Logger.info("Opening Mercado Pago checkout in WebView: " + checkoutUrl);

        // Hide payment form, show WebView
        if (vboxPaymentForm != null) {
            vboxPaymentForm.setVisible(false);
            vboxPaymentForm.setManaged(false);
        }
        if (vboxWebView != null) {
            vboxWebView.setVisible(true);
            vboxWebView.setManaged(true);
        }

        // Configure WebView
        if (webViewPayment != null) {
            WebEngine webEngine = webViewPayment.getEngine();

            // Enable JavaScript
            webEngine.setJavaScriptEnabled(true);

            // Monitor page loading and URL changes
            webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == Worker.State.SUCCEEDED) {
                    String currentUrl = webEngine.getLocation();
                    Logger.info("WebView loaded URL: " + currentUrl);

                    // Check if payment was completed (redirected to success URL)
                    if (currentUrl != null && currentUrl.contains("/payment/success")) {
                        handlePaymentSuccess();
                    } else if (currentUrl != null && currentUrl.contains("/payment/failure")) {
                        handlePaymentFailure();
                    } else if (currentUrl != null && currentUrl.contains("/payment/pending")) {
                        handlePaymentPending();
                    }
                }
            });

            // Load the checkout URL
            webEngine.load(checkoutUrl);
        } else {
            Logger.error("WebView component not found in FXML");
            DialogUtil.showError("Error", "No se pudo cargar el componente de pago");
        }
    }

    /**
     * Handles successful payment completion.
     */
    private void handlePaymentSuccess() {
        Logger.info("Payment successful for order: " + currentOrder.getId());

        DialogUtil.showSuccess("Pago Exitoso",
                "¡Tu pago ha sido procesado exitosamente!\n\n" +
                "Orden: " + currentOrder.getId() + "\n" +
                "El administrador revisará tu orden y la aprobará en breve.");

        // Notify admin about new order pending approval
        notifyAdminAboutNewOrder();

        closeWindow();
    }

    /**
     * Handles failed payment.
     */
    private void handlePaymentFailure() {
        Logger.info("Payment failed for order: " + currentOrder.getId());

        DialogUtil.showError("Pago Fallido",
                "El pago no pudo ser completado.\n\n" +
                "Por favor intenta nuevamente o contacta con soporte.");

        // Return to payment form
        showPaymentForm();
    }

    /**
     * Handles pending payment.
     */
    private void handlePaymentPending() {
        Logger.info("Payment pending for order: " + currentOrder.getId());

        DialogUtil.showInfo("Pago Pendiente",
                "Tu pago está siendo procesado.\n\n" +
                "Recibirás una confirmación cuando sea aprobado.");

        closeWindow();
    }

    /**
     * Shows the payment form (hides WebView).
     */
    private void showPaymentForm() {
        if (vboxWebView != null) {
            vboxWebView.setVisible(false);
            vboxWebView.setManaged(false);
        }
        if (vboxPaymentForm != null) {
            vboxPaymentForm.setVisible(true);
            vboxPaymentForm.setManaged(true);
        }
    }

    /**
     * Notifies all admins about a new order that needs approval.
     * Uses Observer pattern through NotificationService.
     * Notifications are sent only once and remain until admin dismisses them.
     */
    private void notifyAdminAboutNewOrder() {
        NotificationService notificationService = NotificationService.getInstance();
        AdminRepository adminRepository = AdminRepository.getInstance();

        // Get all admins
        List<co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Admin> admins = adminRepository.getAdmins();

        if (admins.isEmpty()) {
            Logger.warning("No admins found to notify about new order");
            return;
        }

        String title = "Nueva Orden Pendiente de Aprobación";
        String message = String.format(
                "La orden #%s ha sido pagada y está pendiente de aprobación.\n" +
                "Usuario: %s %s\n" +
                "Total: $%.2f COP",
                currentOrder.getId(),
                currentUser.getName(),
                currentUser.getLastName(),
                (invoice != null) ? invoice.getTotalAmount() : orderDetail.getTotalCost()
        );

        // Notify all admins (each admin will see notification once until they dismiss it)
        for (co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Admin admin : admins) {
            notificationService.addNotification(
                    admin.getId(),
                    title,
                    message,
                    NotificationService.NotificationType.WARNING
            );
            Logger.info("Admin " + admin.getEmail() + " notified about new order: " + currentOrder.getId());
        }
    }

    @FXML
    private void handleCancel() {
        boolean confirmed = DialogUtil.showConfirmation("Cancelar Pago",
                "¿Está seguro que desea cancelar el proceso de pago?");
        if (confirmed) {
            closeWindow();
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) lblOrderId.getScene().getWindow();
        stage.close();
    }
}
