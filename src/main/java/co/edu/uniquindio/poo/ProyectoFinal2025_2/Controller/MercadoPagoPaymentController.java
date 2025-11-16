package co.edu.uniquindio.poo.ProyectoFinal2025_2.Controller;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Order;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.User;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Invoice;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.LineItem;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.dto.MercadoPagoPreferenceDTO;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.dto.OrderDetailDTO;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.AuthenticationService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.MercadoPagoService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.NotificationService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.OrderService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Repositories.InvoiceRepository;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Repositories.AdminRepository;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.DialogUtil;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.geometry.Insets;
import javafx.concurrent.Worker;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for Mercado Pago payment integration.
 * This controller handles REAL payment processing via Mercado Pago API.
 * Uses WebView to display Mercado Pago checkout within the application.
 * This view is loaded in the Index content area (not as a popup).
 */
public class MercadoPagoPaymentController implements Initializable {

    @FXML private Label lblOrderId;
    @FXML private Label lblTotalAmount;
    @FXML private VBox costBreakdownContainer;
    @FXML private Button btnProceedPayment;
    @FXML private Button btnGoBack;
    @FXML private Button btnCancel;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private WebView webView;
    @FXML private VBox paymentInfoContainer;

    private final AuthenticationService authService = AuthenticationService.getInstance();
    private final MercadoPagoService mercadoPagoService = new MercadoPagoService();
    private final InvoiceRepository invoiceRepository = InvoiceRepository.getInstance();
    private final OrderService orderService = new OrderService();

    private User currentUser;
    private Order currentOrder;
    private OrderDetailDTO orderDetail;
    private Invoice invoice;
    private IndexController indexController;
    private PaymentProcessorSelectionController selectionController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        currentUser = (User) authService.getCurrentPerson();

        if (currentUser == null) {
            Logger.error("No user logged in");
            DialogUtil.showError("Error", "No hay usuario autenticado");
            return;
        }

        setupUI();
        Logger.info("MercadoPagoPaymentController initialized");
    }

    /**
     * Sets the IndexController reference.
     */
    public void setIndexController(IndexController indexController) {
        this.indexController = indexController;
    }

    /**
     * Sets the SelectionController reference.
     */
    public void setSelectionController(PaymentProcessorSelectionController selectionController) {
        this.selectionController = selectionController;
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
        if (progressIndicator != null) {
            progressIndicator.setVisible(false);
        }
        if (webView != null) {
            webView.setVisible(false);
            webView.setManaged(false);
        }
    }

    @FXML
    private void handleProceedPayment() {
        if (currentOrder == null) {
            DialogUtil.showError("Error", "No hay orden para procesar");
            return;
        }
        processMercadoPagoPayment();
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
                // Open in WebView within the application
                Logger.info("Opening Mercado Pago checkout in WebView");
                loadCheckoutInWebView(checkoutUrl);
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

    /**
     * Loads the Mercado Pago checkout URL in the WebView.
     */
    private void loadCheckoutInWebView(String checkoutUrl) {
        if (webView == null) {
            Logger.error("WebView not initialized");
            DialogUtil.showError("Error", "WebView no está disponible");
            return;
        }

        // Hide payment info, show WebView
        if (paymentInfoContainer != null) {
            paymentInfoContainer.setVisible(false);
            paymentInfoContainer.setManaged(false);
        }

        webView.setVisible(true);
        webView.setManaged(true);

        // Hide "Proceder" button, show navigation buttons
        btnProceedPayment.setVisible(false);
        btnProceedPayment.setManaged(false);
        if (btnGoBack != null) {
            btnGoBack.setVisible(true);
            btnGoBack.setManaged(true);
        }
        if (btnCancel != null) {
            btnCancel.setVisible(true);
            btnCancel.setManaged(true);
        }

        // Setup WebView
        WebEngine webEngine = webView.getEngine();

        // Monitor URL changes to detect payment completion
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                String currentUrl = webEngine.getLocation();
                Logger.info("WebView loaded URL: " + currentUrl);

                // Check if payment was completed, cancelled, or failed
                if (currentUrl.contains("/success") || currentUrl.contains("payment_status=approved")) {
                    handlePaymentSuccess();
                } else if (currentUrl.contains("/failure") || currentUrl.contains("payment_status=rejected")) {
                    handlePaymentFailure();
                } else if (currentUrl.contains("/pending") || currentUrl.contains("payment_status=pending")) {
                    handlePaymentPending();
                }
            }
        });

        // Load the checkout URL
        webEngine.load(checkoutUrl);
        Logger.info("Mercado Pago checkout loaded in WebView");
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

        returnToDashboard();
    }

    /**
     * Handles failed payment.
     */
    private void handlePaymentFailure() {
        Logger.info("Payment failed for order: " + currentOrder.getId());

        DialogUtil.showError("Pago Fallido",
                "El pago no pudo ser completado.\n\n" +
                "Por favor intenta nuevamente o contacta con soporte.");

        returnToPaymentSelection();
    }

    /**
     * Handles pending payment.
     */
    private void handlePaymentPending() {
        Logger.info("Payment pending for order: " + currentOrder.getId());

        DialogUtil.showInfo("Pago Pendiente",
                "Tu pago está siendo procesado.\n\n" +
                "Recibirás una confirmación cuando sea aprobado.");

        returnToDashboard();
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

    /**
     * Handles "Volver" button - returns to payment selection.
     */
    @FXML
    private void handleGoBack() {
        Logger.info("Going back to payment selection");
        returnToPaymentSelection();
    }

    /**
     * Handles "Cancelar" button - cancels the order if possible and returns to dashboard.
     */
    @FXML
    private void handleCancel() {
        boolean confirmed = DialogUtil.showConfirmation("Cancelar Pago",
                "¿Está seguro que desea cancelar el proceso de pago?");
        if (confirmed) {
            Logger.info("Payment cancelled by user");

            // Cancel the order if it can be cancelled
            if (currentOrder != null) {
                try {
                    if (orderService.canCancelOrder(currentOrder.getId())) {
                        orderService.cancelOrder(currentOrder.getId());
                        Logger.info("Order " + currentOrder.getId() + " cancelled successfully");
                    } else {
                        Logger.warning("Order " + currentOrder.getId() + " cannot be cancelled in its current state");
                    }
                } catch (Exception e) {
                    Logger.error("Error cancelling order: " + e.getMessage());
                    // Continue with navigation even if cancellation fails
                }
            }

            returnToDashboard();
        }
    }

    /**
     * Returns to the payment processor selection view.
     */
    private void returnToPaymentSelection() {
        if (selectionController != null && indexController != null) {
            try {
                FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/co/edu/uniquindio/poo/ProyectoFinal2025_2/View/PaymentProcessorSelection.fxml")
                );

                indexController.getContentArea().getChildren().clear();
                indexController.getContentArea().getChildren().add(loader.load());

                // Re-configure the selection controller
                PaymentProcessorSelectionController newSelectionController = loader.getController();
                newSelectionController.setIndexController(indexController);
                newSelectionController.setWizardController(
                    selectionController.wizardController != null ? selectionController.wizardController : null
                );
                newSelectionController.setOrder(currentOrder, orderDetail);

                Logger.info("Returned to payment selection view");

            } catch (Exception e) {
                Logger.error("Error loading payment selection view: " + e.getMessage());
                returnToDashboard();
            }
        } else {
            returnToDashboard();
        }
    }

    /**
     * Returns to the user dashboard.
     */
    private void returnToDashboard() {
        if (indexController != null) {
            try {
                indexController.loadView("UserDashboard.fxml");
                Logger.info("Returned to dashboard");
            } catch (Exception e) {
                Logger.error("Error loading dashboard: " + e.getMessage());
            }
        }
    }
}
