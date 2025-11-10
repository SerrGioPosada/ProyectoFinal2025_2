package co.edu.uniquindio.poo.ProyectoFinal2025_2.Controller;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Order;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Payment;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.PaymentMethod;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.PaymentMethodType;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.PaymentProvider;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.PaymentStatus;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.dto.OrderDetailDTO;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.PaymentService;
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
 * Controller for payment processing.
 * Prepared for Mercado Pago API integration but currently simulates payment.
 *
 * TODO: Integrate Mercado Pago SDK
 * - Add Mercado Pago dependency to pom.xml
 * - Configure API credentials (public key, access token)
 * - Implement preference creation
 * - Handle payment callbacks
 * - Process webhooks for payment notifications
 */
public class PaymentController implements Initializable {

    // Order Information
    @FXML private Label lblOrderId;
    @FXML private Label lblInvoiceId;
    @FXML private Label lblTotalAmount;

    // Payment Method Selection
    @FXML private RadioButton rbCreditCard;
    @FXML private RadioButton rbDebitCard;
    @FXML private RadioButton rbCash;
    @FXML private RadioButton rbMercadoPago;
    @FXML private ToggleGroup paymentMethodGroup;

    // Credit/Debit Card Details (for traditional payment)
    @FXML private VBox vboxCardDetails;
    @FXML private TextField txtCardNumber;
    @FXML private TextField txtCardHolder;
    @FXML private TextField txtExpiryMonth;
    @FXML private TextField txtExpiryYear;
    @FXML private TextField txtCVV;
    @FXML private ComboBox<PaymentProvider> cmbCardProvider;

    // Mercado Pago Section (for MP integration)
    @FXML private VBox vboxMercadoPago;
    @FXML private Label lblMercadoPagoInstructions;
    @FXML private Button btnOpenMercadoPago;

    // Cash Details
    @FXML private VBox vboxCashDetails;
    @FXML private Label lblCashInstructions;

    // Progress
    @FXML private ProgressIndicator progressPayment;
    @FXML private Label lblPaymentStatus;

    // Services
    private final PaymentService paymentService = new PaymentService();

    // Data
    private Order order;
    private OrderDetailDTO orderDetail;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupPaymentMethodListeners();
        setupCardProviderComboBox();
        setupCardValidation();
        hideAllPaymentSections();

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
        rbMercadoPago.setToggleGroup(paymentMethodGroup);

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
     * Updates visibility of payment sections based on selected method.
     */
    private void updatePaymentSectionVisibility() {
        hideAllPaymentSections();

        if (rbCreditCard.isSelected() || rbDebitCard.isSelected()) {
            vboxCardDetails.setVisible(true);
            vboxCardDetails.setManaged(true);
        } else if (rbMercadoPago.isSelected()) {
            vboxMercadoPago.setVisible(true);
            vboxMercadoPago.setManaged(true);
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
        vboxMercadoPago.setVisible(false);
        vboxMercadoPago.setManaged(false);
        vboxCashDetails.setVisible(false);
        vboxCashDetails.setManaged(false);
    }

    /**
     * Handles the process payment button.
     */
    @FXML
    private void handleProcessPayment() {
        try {
            // Validate payment method selection
            if (paymentMethodGroup.getSelectedToggle() == null) {
                showWarning("Por favor, seleccione un método de pago");
                return;
            }

            // If Mercado Pago is selected, open specialized window
            if (rbMercadoPago.isSelected()) {
                handleOpenMercadoPago();
                return;
            }

            // Create payment method based on selection
            PaymentMethod paymentMethod = createPaymentMethod();
            if (paymentMethod == null) {
                return; // Error already shown
            }

            // Show progress
            progressPayment.setVisible(true);
            lblPaymentStatus.setText("Procesando pago...");

            // Process payment through service
            Payment payment = processPaymentWithMethod(paymentMethod);

            if (payment != null && payment.getStatus() == PaymentStatus.APPROVED) {
                progressPayment.setVisible(false);
                lblPaymentStatus.setText("Pago aprobado");
                showSuccess("¡Pago procesado exitosamente!\n\nID de Pago: " + payment.getId() +
                          "\nSu envío será procesado en breve.");

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
     * Creates a PaymentMethod object based on user selection.
     */
    private PaymentMethod createPaymentMethod() {
        if (rbCreditCard.isSelected()) {
            return createCardPaymentMethod(PaymentMethodType.CREDIT_CARD);
        } else if (rbDebitCard.isSelected()) {
            return createCardPaymentMethod(PaymentMethodType.DEBIT_CARD);
        } else if (rbMercadoPago.isSelected()) {
            return createMercadoPagoPaymentMethod();
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
     * Creates a Mercado Pago payment method.
     * TODO: Implement real Mercado Pago integration
     */
    private PaymentMethod createMercadoPagoPaymentMethod() {
        // TODO: Here you would:
        // 1. Create a Mercado Pago preference
        // 2. Get the init_point URL
        // 3. Open it in browser or embedded view
        // 4. Wait for webhook callback
        // For now, simulate MP payment

        return new PaymentMethod.Builder()
            .withId(IdGenerationUtil.generateId())
            .withType(PaymentMethodType.DIGITAL_WALLET)
            .withProvider(PaymentProvider.MERCADO_PAGO)
            .withAccountNumber("MP-SIMULATED")
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
     * Processes payment with the given method.
     * This calls the PaymentService which handles the order saga.
     */
    private Payment processPaymentWithMethod(PaymentMethod paymentMethod) {
        // PaymentService will:
        // 1. Create the payment record
        // 2. Call OrderService.confirmOrderPayment()
        // 3. OrderService will create the shipment
        return paymentService.processPayment(order.getInvoiceId(), paymentMethod);
    }

    /**
     * Opens Mercado Pago payment window.
     * Launches the MercadoPagoPayment.fxml view with order details.
     */
    @FXML
    private void handleOpenMercadoPago() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/co/edu/uniquindio/poo/ProyectoFinal2025_2/View/MercadoPagoPayment.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());

            // Get controller and pass order data
            MercadoPagoPaymentController mpController = loader.getController();
            mpController.setOrder(order, orderDetail);

            javafx.stage.Stage mpStage = new javafx.stage.Stage();
            mpStage.setTitle("Pago con Mercado Pago - Orden #" + order.getId());
            mpStage.setScene(scene);
            mpStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            mpStage.setResizable(false);

            // Close payment window when MP window closes
            mpStage.setOnHidden(e -> {
                closeWindow();
            });

            mpStage.showAndWait();

        } catch (Exception e) {
            Logger.error("Error opening Mercado Pago payment window: " + e.getMessage());
            showError("Error al abrir ventana de Mercado Pago:\n" + e.getMessage());
        }
    }

    /**
     * Handles cancel button.
     */
    @FXML
    private void handleCancel() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancelar Pago");
        confirm.setHeaderText("¿Cancelar el pago?");
        confirm.setContentText("Si cancela, la orden no será procesada.\n¿Desea continuar?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
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
