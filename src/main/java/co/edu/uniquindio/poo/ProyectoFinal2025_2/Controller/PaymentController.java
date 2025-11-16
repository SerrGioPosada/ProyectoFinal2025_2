package co.edu.uniquindio.poo.ProyectoFinal2025_2.Controller;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Order;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Payment;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.PaymentMethod;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.PaymentMethodType;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.PaymentProvider;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.PaymentStatus;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.dto.OrderDetailDTO;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.User;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.AuthenticationService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.PaymentService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.PaymentMethodService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.OrderService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilService.IdGenerationUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for simulated in-app payment processing.
 * This controller handles simulated payments for demonstration purposes.
 * For real payment processing, use MercadoPagoPaymentController.
 *
 * Supported payment methods:
 * - Credit Card (simulated)
 * - Debit Card (simulated)
 * - Cash on Delivery
 */
public class PaymentController implements Initializable {

    // Order Information
    @FXML private Label lblOrderId;
    @FXML private Label lblInvoiceId;
    @FXML private Label lblTotalAmount;

    // Payment Method Selection (Simulated)
    @FXML private RadioButton rbCreditCard;
    @FXML private RadioButton rbDebitCard;
    @FXML private RadioButton rbCash;
    @FXML private ToggleGroup paymentMethodGroup;

    // Credit/Debit Card Details (for traditional payment)
    @FXML private VBox vboxCardDetails;
    @FXML private TextField txtCardNumber;
    @FXML private TextField txtCardHolder;
    @FXML private TextField txtExpiryMonth;
    @FXML private TextField txtExpiryYear;
    @FXML private TextField txtCVV;
    @FXML private ComboBox<PaymentProvider> cmbCardProvider;

    // Cash Details
    @FXML private VBox vboxCashDetails;
    @FXML private Label lblCashInstructions;

    // Progress
    @FXML private ProgressIndicator progressPayment;
    @FXML private Label lblPaymentStatus;

    // Services
    private final AuthenticationService authService = AuthenticationService.getInstance();
    private final PaymentService paymentService = new PaymentService();
    private final PaymentMethodService paymentMethodService = new PaymentMethodService();
    private final OrderService orderService = new OrderService();

    // Data
    private Order order;
    private OrderDetailDTO orderDetail;
    private User currentUser;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Get current user
        var currentPerson = authService.getCurrentPerson();
        if (currentPerson instanceof User) {
            currentUser = (User) currentPerson;
        }

        setupPaymentMethodListeners();
        setupCardProviderComboBox();
        setupCardValidation();
        hideAllPaymentSections();
        loadUserPreferredPaymentMethods();

        Logger.info("PaymentController initialized");
    }

    /**
     * Sets the order and order details.
     */
    public void setOrder(Order order, OrderDetailDTO orderDetail) {
        this.order = order;
        this.orderDetail = orderDetail;
        displayOrderInfo();
    }

    /**
     * Displays order information.
     */
    private void displayOrderInfo() {
        if (order == null || orderDetail == null) {
            showError("No hay información de orden");
            return;
        }

        lblOrderId.setText("Orden #" + order.getId());
        lblInvoiceId.setText("Factura #" + order.getInvoiceId());
        lblTotalAmount.setText(formatCurrency(orderDetail.getTotalCost()));
    }

    /**
     * Sets up payment method radio button listeners.
     */
    private void setupPaymentMethodListeners() {
        paymentMethodGroup = new ToggleGroup();
        rbCreditCard.setToggleGroup(paymentMethodGroup);
        rbDebitCard.setToggleGroup(paymentMethodGroup);
        rbCash.setToggleGroup(paymentMethodGroup);

        paymentMethodGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updatePaymentSectionVisibility();
            }
        });
    }

    /**
     * Sets up card provider combo box.
     */
    private void setupCardProviderComboBox() {
        cmbCardProvider.getItems().addAll(
            PaymentProvider.VISA,
            PaymentProvider.MASTERCARD,
            PaymentProvider.AMERICAN_EXPRESS
        );
        cmbCardProvider.setPromptText("Seleccionar emisor");
    }

    /**
     * Sets up card field validation.
     */
    private void setupCardValidation() {
        // Card number - only digits, max 16
        txtCardNumber.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                txtCardNumber.setText(oldVal);
            }
            if (newVal.length() > 16) {
                txtCardNumber.setText(newVal.substring(0, 16));
            }
        });

        // CVV - only digits, max 4
        txtCVV.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                txtCVV.setText(oldVal);
            }
            if (newVal.length() > 4) {
                txtCVV.setText(newVal.substring(0, 4));
            }
        });

        // Expiry month - only digits, max 2
        txtExpiryMonth.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                txtExpiryMonth.setText(oldVal);
            }
            if (newVal.length() > 2) {
                txtExpiryMonth.setText(newVal.substring(0, 2));
            }
        });

        // Expiry year - only digits, max 4
        txtExpiryYear.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                txtExpiryYear.setText(oldVal);
            }
            if (newVal.length() > 4) {
                txtExpiryYear.setText(newVal.substring(0, 4));
            }
        });
    }

    /**
     * Loads user's preferred/saved payment methods.
     * Pre-fills card information if user has saved payment methods.
     */
    private void loadUserPreferredPaymentMethods() {
        if (currentUser == null) {
            Logger.warning("No user logged in - cannot load preferred payment methods");
            return;
        }

        try {
            var savedMethods = paymentMethodService.getPaymentMethodsByUserId(currentUser.getId());

            if (savedMethods != null && !savedMethods.isEmpty()) {
                Logger.info("Found " + savedMethods.size() + " saved payment methods for user");

                // Get the first saved method (you could add logic to get the default/preferred one)
                PaymentMethod preferredMethod = savedMethods.get(0);

                // Pre-select the appropriate radio button based on type
                switch (preferredMethod.getType()) {
                    case CREDIT_CARD:
                        rbCreditCard.setSelected(true);
                        break;
                    case DEBIT_CARD:
                        rbDebitCard.setSelected(true);
                        break;
                    case CASH:
                        rbCash.setSelected(true);
                        break;
                    case DIGITAL_WALLET:
                    case PAYPAL:
                        // Digital wallets not supported in simulated payment
                        Logger.warning("Digital wallet payment method not supported in simulated mode");
                        break;
                    default:
                        Logger.warning("Unsupported payment method type: " + preferredMethod.getType());
                        break;
                }

                // Pre-fill card information if it's a card method
                if (preferredMethod.getType() == PaymentMethodType.CREDIT_CARD ||
                    preferredMethod.getType() == PaymentMethodType.DEBIT_CARD) {

                    if (preferredMethod.getProvider() != null) {
                        cmbCardProvider.setValue(preferredMethod.getProvider());
                    }

                    // Show masked account number
                    if (preferredMethod.getAccountNumber() != null) {
                        txtCardNumber.setText(preferredMethod.getAccountNumber());
                        txtCardNumber.setPromptText("Número guardado (termina en " +
                            preferredMethod.getAccountNumber().substring(
                                Math.max(0, preferredMethod.getAccountNumber().length() - 4)) + ")");
                    }
                }

                Logger.info("Pre-loaded preferred payment method: " + preferredMethod.getType());
            } else {
                Logger.info("No saved payment methods found for user");
            }
        } catch (Exception e) {
            Logger.error("Error loading preferred payment methods: " + e.getMessage());
        }
    }

    /**
     * Updates visibility of payment sections based on selected method.
     */
    private void updatePaymentSectionVisibility() {
        hideAllPaymentSections();

        if (rbCreditCard.isSelected() || rbDebitCard.isSelected()) {
            vboxCardDetails.setVisible(true);
            vboxCardDetails.setManaged(true);
        } else if (rbCash.isSelected()) {
            vboxCashDetails.setVisible(true);
            vboxCashDetails.setManaged(true);
        }
    }

    /**
     * Hides all payment method sections.
     */
    private void hideAllPaymentSections() {
        vboxCardDetails.setVisible(false);
        vboxCardDetails.setManaged(false);
        vboxCashDetails.setVisible(false);
        vboxCashDetails.setManaged(false);
    }

    /**
     * Handles the process payment button.
     * This is a SIMULATED payment - no real transaction occurs.
     */
    @FXML
    private void handleProcessPayment() {
        try {
            // Validate payment method selection
            if (paymentMethodGroup.getSelectedToggle() == null) {
                showWarning("Por favor, seleccione un método de pago");
                return;
            }

            // Create payment method based on selection
            PaymentMethod paymentMethod = createPaymentMethod();
            if (paymentMethod == null) {
                return; // Error already shown
            }

            // Show progress
            progressPayment.setVisible(true);
            lblPaymentStatus.setText("Procesando pago simulado...");

            // Process SIMULATED payment through service
            Payment payment = processPaymentWithMethod(paymentMethod);

            if (payment != null && payment.getStatus() == PaymentStatus.APPROVED) {
                progressPayment.setVisible(false);
                lblPaymentStatus.setText("Pago aprobado (simulado)");
                showSuccess("¡Pago simulado procesado exitosamente!\n\nID de Pago: " + payment.getId() +
                          "\nSu envío será procesado en breve.\n\n⚠️ Nota: Este fue un pago simulado para demostración.");

                // Close payment window
                closeWindow();
            } else {
                progressPayment.setVisible(false);
                lblPaymentStatus.setText("Pago rechazado");
                showError("El pago fue rechazado. Por favor, intente nuevamente.");
            }

        } catch (Exception e) {
            progressPayment.setVisible(false);
            lblPaymentStatus.setText("Error en el pago");
            Logger.error("Error processing payment: " + e.getMessage());
            showError("Error al procesar el pago: " + e.getMessage());
        }
    }

    /**
     * Creates a PaymentMethod object based on user selection (simulated).
     */
    private PaymentMethod createPaymentMethod() {
        if (rbCreditCard.isSelected()) {
            return createCardPaymentMethod(PaymentMethodType.CREDIT_CARD);
        } else if (rbDebitCard.isSelected()) {
            return createCardPaymentMethod(PaymentMethodType.DEBIT_CARD);
        } else if (rbCash.isSelected()) {
            return createCashPaymentMethod();
        }
        return null;
    }

    /**
     * Creates a card payment method (credit or debit).
     */
    private PaymentMethod createCardPaymentMethod(PaymentMethodType type) {
        // Validate card fields
        if (txtCardNumber.getText().trim().isEmpty() ||
            txtCardHolder.getText().trim().isEmpty() ||
            txtExpiryMonth.getText().trim().isEmpty() ||
            txtExpiryYear.getText().trim().isEmpty() ||
            txtCVV.getText().trim().isEmpty() ||
            cmbCardProvider.getValue() == null) {

            showWarning("Por favor, complete todos los datos de la tarjeta");
            return null;
        }

        // Validate card number length
        if (txtCardNumber.getText().length() < 13) {
            showWarning("Número de tarjeta inválido");
            return null;
        }

        // Mask card number for security (show only last 4 digits)
        String cardNumber = txtCardNumber.getText();
        String maskedNumber = "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);

        return new PaymentMethod.Builder()
            .withId(IdGenerationUtil.generateId())
            .withType(type)
            .withProvider(cmbCardProvider.getValue())
            .withAccountNumber(maskedNumber)
            .build();
    }

    /**
     * Creates a cash payment method.
     */
    private PaymentMethod createCashPaymentMethod() {
        return new PaymentMethod.Builder()
            .withId(IdGenerationUtil.generateId())
            .withType(PaymentMethodType.CASH)
            .withProvider(PaymentProvider.CASH)
            .withAccountNumber("CASH-ON-DELIVERY")
            .build();
    }

    /**
     * Processes SIMULATED payment with the given method.
     * This calls the PaymentService which handles the order saga.
     */
    private Payment processPaymentWithMethod(PaymentMethod paymentMethod) {
        // PaymentService will:
        // 1. Create the payment record (simulated)
        // 2. Call OrderService.confirmOrderPayment()
        // 3. OrderService will create the shipment
        Logger.info("Processing simulated payment for order: " + order.getId());
        return paymentService.processPayment(order.getInvoiceId(), paymentMethod);
    }

    /**
     * Handles cancel button - cancels the order if possible and closes the window.
     */
    @FXML
    private void handleCancel() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancelar Pago");
        confirm.setHeaderText("¿Cancelar el pago?");
        confirm.setContentText("Si cancela, la orden no será procesada.\n¿Desea continuar?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Cancel the order if it can be cancelled
                if (order != null) {
                    try {
                        if (orderService.canCancelOrder(order.getId())) {
                            orderService.cancelOrder(order.getId());
                            Logger.info("Order " + order.getId() + " cancelled successfully");
                        } else {
                            Logger.warning("Order " + order.getId() + " cannot be cancelled in its current state");
                        }
                    } catch (Exception e) {
                        Logger.error("Error cancelling order: " + e.getMessage());
                        // Continue with closing even if cancellation fails
                    }
                }

                closeWindow();
            }
        });
    }

    /**
     * Closes the payment window.
     */
    private void closeWindow() {
        Stage stage = (Stage) lblOrderId.getScene().getWindow();
        stage.close();
    }

    /**
     * Formats currency values.
     */
    private String formatCurrency(double value) {
        return String.format("$%,.0f COP", value);
    }

    /**
     * Shows an error alert.
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Shows a warning alert.
     */
    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Advertencia");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Shows a success alert.
     */
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Shows an info alert.
     */
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
