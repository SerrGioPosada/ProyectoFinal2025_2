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
import java.util.Optional;
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
    @FXML private RadioButton rbSavedPayment;
    @FXML private RadioButton rbCreditCard;
    @FXML private RadioButton rbDebitCard;
    @FXML private RadioButton rbCash;
    @FXML private ToggleGroup paymentMethodGroup;

    // Saved Payment Method Details
    @FXML private VBox vboxSavedPaymentDetails;
    @FXML private Label lblSavedPaymentInfo;
    @FXML private Button btnSelectPaymentMethod;

    // Credit/Debit Card Details (for traditional payment)
    @FXML private VBox vboxCardDetails;
    @FXML private TextField txtCardNumber;
    @FXML private TextField txtCardHolder;
    @FXML private TextField txtExpiry;  // Combined MM/YY field
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
    private IndexController indexController;
    private PaymentMethod selectedPaymentMethod; // Selected payment method when user has multiple

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
     * Sets the IndexController reference for navigation.
     */
    public void setIndexController(IndexController indexController) {
        this.indexController = indexController;
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
        rbSavedPayment.setToggleGroup(paymentMethodGroup);
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
        // Card number - only digits with automatic spacing every 4 digits, max 19 digits
        txtCardNumber.textProperty().addListener((obs, oldVal, newVal) -> {
            // Remove all non-digit characters to get raw input
            String digitsOnly = newVal.replaceAll("[^\\d]", "");

            // Limit to 19 digits (max for some card types)
            if (digitsOnly.length() > 19) {
                digitsOnly = digitsOnly.substring(0, 19);
            }

            // Format with spaces every 4 digits
            StringBuilder formatted = new StringBuilder();
            for (int i = 0; i < digitsOnly.length(); i++) {
                if (i > 0 && i % 4 == 0) {
                    formatted.append(" ");
                }
                formatted.append(digitsOnly.charAt(i));
            }

            String formattedText = formatted.toString();

            // Only update if the text has changed to avoid infinite loop
            if (!newVal.equals(formattedText)) {
                txtCardNumber.setText(formattedText);
                // Position cursor at the end
                txtCardNumber.positionCaret(formattedText.length());
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

        // Expiry date (MM/YY) - auto-format with slash
        txtExpiry.textProperty().addListener((obs, oldVal, newVal) -> {
            // Remove any non-digit characters except slash
            String digitsOnly = newVal.replaceAll("[^\\d]", "");

            // Limit to 4 digits (MMYY)
            if (digitsOnly.length() > 4) {
                digitsOnly = digitsOnly.substring(0, 4);
            }

            // Format as MM/YY
            StringBuilder formatted = new StringBuilder();
            for (int i = 0; i < digitsOnly.length(); i++) {
                if (i == 2) {
                    formatted.append("/");
                }
                formatted.append(digitsOnly.charAt(i));
            }

            String formattedText = formatted.toString();
            if (!newVal.equals(formattedText)) {
                txtExpiry.setText(formattedText);
                txtExpiry.positionCaret(formattedText.length());
            }
        });
    }

    /**
     * Loads user's preferred/saved payment methods.
     * Shows information message if user has saved methods.
     */
    private void loadUserPreferredPaymentMethods() {
        if (currentUser == null) {
            Logger.warning("No user logged in - cannot load preferred payment methods");
            rbSavedPayment.setDisable(true);
            return;
        }

        try {
            var savedMethods = paymentMethodService.getPaymentMethodsByUserId(currentUser.getId());

            if (savedMethods != null && !savedMethods.isEmpty()) {
                Logger.info("Found " + savedMethods.size() + " saved payment methods for user");

                // Enable saved payment option
                rbSavedPayment.setDisable(false);

                // Update label with saved payment info
                String methodInfo;
                if (savedMethods.size() == 1) {
                    PaymentMethod savedMethod = savedMethods.get(0);
                    selectedPaymentMethod = savedMethod; // Auto-select if only one
                    methodInfo = String.format("%s - %s",
                        savedMethod.getProvider().toString().replace("_", " "),
                        savedMethod.getAccountNumber());
                    btnSelectPaymentMethod.setVisible(false);
                    btnSelectPaymentMethod.setManaged(false);
                } else {
                    methodInfo = savedMethods.size() + " métodos guardados";
                    btnSelectPaymentMethod.setVisible(true);
                    btnSelectPaymentMethod.setManaged(true);
                }
                lblSavedPaymentInfo.setText(methodInfo);

                Logger.info("Saved payment methods available: " + methodInfo);
            } else {
                Logger.info("No saved payment methods found for user");
                rbSavedPayment.setDisable(true);
            }
        } catch (Exception e) {
            Logger.error("Error loading preferred payment methods: " + e.getMessage());
            rbSavedPayment.setDisable(true);
        }
    }

    /**
     * Handles the selection button click - opens dialog to select payment method.
     */
    @FXML
    private void handleSelectPaymentMethod() {
        if (currentUser == null) {
            return;
        }

        var savedMethods = paymentMethodService.getPaymentMethodsByUserId(currentUser.getId());

        if (savedMethods == null || savedMethods.isEmpty()) {
            showWarning("No hay métodos de pago guardados");
            return;
        }

        // Show selection dialog
        PaymentMethod defaultSelection = selectedPaymentMethod != null ? selectedPaymentMethod : savedMethods.get(0);
        ChoiceDialog<PaymentMethod> dialog = new ChoiceDialog<>(defaultSelection, savedMethods);
        dialog.setTitle("Seleccionar Método de Pago");
        dialog.setHeaderText("Selecciona el método que deseas usar");
        dialog.setContentText("Métodos de pago disponibles:");

        // Apply stylesheet
        dialog.getDialogPane().getStylesheets().add(
            getClass().getResource("/co/edu/uniquindio/poo/ProyectoFinal2025_2/Style.css").toExternalForm()
        );
        dialog.getDialogPane().getStyleClass().add("dialog-pane");

        Optional<PaymentMethod> result = dialog.showAndWait();
        if (result.isPresent()) {
            selectedPaymentMethod = result.get();
            // Update label to show selected method
            String methodInfo = selectedPaymentMethod.toString();
            lblSavedPaymentInfo.setText(methodInfo);
            Logger.info("User selected payment method: " + methodInfo);
        }
    }

    /**
     * Gets the saved payment method that was selected by the user.
     * Returns null if no method was selected.
     */
    private PaymentMethod getSavedPaymentMethod() {
        return selectedPaymentMethod;
    }

    /**
     * Updates visibility of payment sections based on selected method.
     */
    private void updatePaymentSectionVisibility() {
        hideAllPaymentSections();

        if (rbSavedPayment.isSelected()) {
            vboxSavedPaymentDetails.setVisible(true);
            vboxSavedPaymentDetails.setManaged(true);
        } else if (rbCreditCard.isSelected() || rbDebitCard.isSelected()) {
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
        vboxSavedPaymentDetails.setVisible(false);
        vboxSavedPaymentDetails.setManaged(false);
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

            // Check if user selected saved payment method
            if (rbSavedPayment.isSelected()) {
                PaymentMethod savedMethod = getSavedPaymentMethod();
                if (savedMethod != null) {
                    // Process payment with saved method
                    processPaymentWithSavedMethod(savedMethod);
                    return;
                } else {
                    showError("No se encontró el método de pago guardado");
                    return;
                }
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

                // Navigate to MyShipments and filter by order ID
                navigateToMyShipmentsWithFilter();
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
            txtExpiry.getText().trim().isEmpty() ||
            txtCVV.getText().trim().isEmpty() ||
            cmbCardProvider.getValue() == null) {

            showWarning("Por favor, complete todos los datos de la tarjeta");
            return null;
        }

        // Validate expiry date format (MM/YY)
        String expiry = txtExpiry.getText().trim();
        if (!expiry.matches("\\d{2}/\\d{2}")) {
            showWarning("Formato de fecha de vencimiento inválido.\nUse MM/AA (ejemplo: 12/25)");
            return null;
        }

        // Validate month (01-12)
        int month = Integer.parseInt(expiry.substring(0, 2));
        if (month < 1 || month > 12) {
            showWarning("Mes inválido. Debe estar entre 01 y 12");
            return null;
        }

        // Get card number without spaces
        String cardNumber = txtCardNumber.getText().replaceAll("\\s", "");

        // Validate card number length (13-19 digits depending on card type)
        PaymentProvider provider = cmbCardProvider.getValue();
        int minLength = getMinCardLength(provider);
        int maxLength = getMaxCardLength(provider);

        if (cardNumber.length() < minLength || cardNumber.length() > maxLength) {
            showWarning(String.format("Número de tarjeta inválido.\n%s requiere entre %d y %d dígitos.",
                provider.name(), minLength, maxLength));
            return null;
        }

        // Mask card number for security (show only last 4 digits)
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
     * Gets the minimum card number length for a given provider.
     * @param provider The payment provider
     * @return Minimum number of digits
     */
    private int getMinCardLength(PaymentProvider provider) {
        return switch (provider) {
            case AMERICAN_EXPRESS -> 15; // Amex uses 15 digits
            case VISA, MASTERCARD -> 16; // Visa and Mastercard use 16 digits
            default -> 13; // Other cards minimum 13 digits
        };
    }

    /**
     * Gets the maximum card number length for a given provider.
     * @param provider The payment provider
     * @return Maximum number of digits
     */
    private int getMaxCardLength(PaymentProvider provider) {
        return switch (provider) {
            case AMERICAN_EXPRESS -> 15; // Amex uses exactly 15 digits
            case VISA, MASTERCARD -> 16; // Visa and Mastercard use exactly 16 digits
            default -> 19; // Some cards can have up to 19 digits
        };
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
     * Processes payment using a saved payment method.
     */
    private void processPaymentWithSavedMethod(PaymentMethod savedMethod) {
        try {
            // Show progress
            progressPayment.setVisible(true);
            lblPaymentStatus.setText("Procesando pago con método guardado...");

            // Process payment
            Payment payment = processPaymentWithMethod(savedMethod);

            if (payment != null && payment.getStatus() == PaymentStatus.APPROVED) {
                progressPayment.setVisible(false);
                lblPaymentStatus.setText("Pago aprobado (método guardado)");
                showSuccess(String.format(
                    "¡Pago procesado exitosamente!\n\n" +
                    "ID de Pago: %s\n" +
                    "Método: %s\n" +
                    "Su envío será procesado en breve.\n\n" +
                    "⚠️ Nota: Este fue un pago simulado para demostración.",
                    payment.getId(),
                    savedMethod.getProvider().toString().replace("_", " ")
                ));

                // Navigate to MyShipments and filter by order ID
                navigateToMyShipmentsWithFilter();
            } else {
                progressPayment.setVisible(false);
                lblPaymentStatus.setText("Pago rechazado");
                showError("El pago fue rechazado. Por favor, intente nuevamente.");
            }
        } catch (Exception e) {
            progressPayment.setVisible(false);
            lblPaymentStatus.setText("Error en el pago");
            Logger.error("Error processing payment with saved method: " + e.getMessage());
            showError("Error al procesar el pago: " + e.getMessage());
        }
    }

    /**
     * Handles cancel button - cancels the order and returns to wizard step 1.
     */
    @FXML
    private void handleCancel() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancelar Pago");
        confirm.setHeaderText("¿Cancelar el pago?");
        confirm.setContentText("Si cancela, la orden será cancelada y volverá al inicio.\n¿Desea continuar?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Cancel the order if it can be cancelled
                if (order != null) {
                    try {
                        Logger.info("Attempting to cancel order: " + order.getId() + " with status: " + order.getStatus());
                        if (orderService.canCancelOrder(order.getId())) {
                            orderService.cancelOrder(order.getId());
                            Logger.info("Order " + order.getId() + " cancelled successfully");
                        } else {
                            Logger.warning("Order " + order.getId() + " cannot be cancelled - current status: " + order.getStatus());
                        }
                    } catch (Exception e) {
                        Logger.error("Error cancelling order: " + e.getMessage());
                        e.printStackTrace();
                        // Continue with navigation even if cancellation fails
                    }
                }

                // Navigate back to wizard at step 1
                returnToWizardStep1();
            }
        });
    }

    /**
     * Navigates to MyShipments view and filters by the current order ID.
     * If opened in a modal window (indexController is null), closes the modal instead.
     */
    private void navigateToMyShipmentsWithFilter() {
        if (indexController == null) {
            Logger.info("IndexController not set - assuming modal window context, closing modal");
            // This means we're in a modal window (opened from MyShipments)
            // Just close the modal - MyShipments will refresh automatically
            try {
                Stage stage = (Stage) lblOrderId.getScene().getWindow();
                stage.close();
                Logger.info("Modal window closed successfully");
            } catch (Exception e) {
                Logger.error("Could not close modal window: " + e.getMessage());
            }
            return;
        }

        if (order == null) {
            Logger.warning("Order is null, navigating to MyShipments without filter");
            indexController.loadView("MyShipments.fxml");
            return;
        }

        try {
            Logger.info("Navigating to MyShipments with filter for order: " + order.getId());

            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/co/edu/uniquindio/poo/ProyectoFinal2025_2/View/MyShipments.fxml")
            );

            indexController.getContentArea().getChildren().clear();
            indexController.getContentArea().getChildren().add(loader.load());

            MyShipmentsController controller = loader.getController();
            controller.filterByOrderId(order.getId());

            Logger.info("Successfully navigated to MyShipments with order filter: " + order.getId());
        } catch (Exception e) {
            Logger.error("Error navigating to MyShipments: " + e.getMessage());
            e.printStackTrace();
            // Fallback to simple navigation
            indexController.loadView("MyShipments.fxml");
        }
    }

    /**
     * Returns to wizard at step 1, clearing all data.
     */
    private void returnToWizardStep1() {
        if (indexController != null) {
            // Simply reload the wizard view - it will start fresh at step 1
            indexController.loadView("CreateShipmentWizard.fxml");
            Logger.info("Navigated back to wizard step 1");
        } else {
            Logger.warning("IndexController not set, cannot navigate to wizard");
            // Fallback: try to close window if running as popup
            try {
                Stage stage = (Stage) lblOrderId.getScene().getWindow();
                stage.close();
            } catch (Exception e) {
                Logger.error("Could not close window or navigate: " + e.getMessage());
            }
        }
    }

    /**
     * Closes the payment window (deprecated - kept for compatibility).
     * @deprecated Use returnToWizardStep1() instead
     */
    @Deprecated
    private void closeWindow() {
        try {
            Stage stage = (Stage) lblOrderId.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            Logger.warning("Could not close window: " + e.getMessage());
        }
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
