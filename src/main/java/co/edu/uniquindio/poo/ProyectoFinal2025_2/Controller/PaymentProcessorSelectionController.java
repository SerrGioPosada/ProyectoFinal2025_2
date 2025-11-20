package co.edu.uniquindio.poo.ProyectoFinal2025_2.Controller;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Order;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.dto.OrderDetailDTO;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.OrderService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for payment processor selection.
 * Allows users to choose between:
 * 1. Mercado Pago - Real payment processing via Mercado Pago API with WebView
 * 2. App Payment - Simulated payment within the application
 *
 * This view is loaded in the Index content area (not as a popup).
 */
public class PaymentProcessorSelectionController implements Initializable {

    @FXML private Label lblOrderId;
    @FXML private Label lblTotalAmount;
    @FXML private Button btnGoBack;
    @FXML private Button btnCancel;

    private Order order;
    private OrderDetailDTO orderDetail;
    private IndexController indexController;
    CreateShipmentWizardController wizardController; // package-private for MercadoPagoPaymentController
    private final OrderService orderService = new OrderService();
    private boolean isModalContext = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Logger.info("PaymentProcessorSelectionController initialized");
    }

    /**
     * Configures the view for modal context (hides back button, adjusts cancel button).
     * Should be called after setOrder() when opening in a modal.
     */
    public void configureForModal() {
        isModalContext = true;
        if (btnGoBack != null) {
            btnGoBack.setVisible(false);
            btnGoBack.setManaged(false);
            Logger.info("Configured for modal context - back button hidden");
        }
    }

    /**
     * Sets the IndexController reference.
     */
    public void setIndexController(IndexController indexController) {
        this.indexController = indexController;
    }

    /**
     * Sets the WizardController reference.
     */
    public void setWizardController(CreateShipmentWizardController wizardController) {
        this.wizardController = wizardController;
    }

    /**
     * Sets the order and order details for display.
     *
     * @param order The order to process
     * @param orderDetail The order details (for display)
     */
    public void setOrder(Order order, OrderDetailDTO orderDetail) {
        this.order = order;
        this.orderDetail = orderDetail;
        displayOrderInfo();
    }

    /**
     * Displays order information in the view.
     */
    private void displayOrderInfo() {
        if (order == null || orderDetail == null) {
            Logger.warning("Order or OrderDetail is null");
            return;
        }

        lblOrderId.setText("Orden #" + order.getId());
        lblTotalAmount.setText(String.format("Total: $%,.0f COP", orderDetail.getTotalCost()));
    }

    /**
     * Handles selection of Mercado Pago payment option.
     * Loads the Mercado Pago payment view in the Index content area or modal.
     */
    @FXML
    private void handleSelectMercadoPago() {
        Logger.info("User selected Mercado Pago payment processor");

        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/co/edu/uniquindio/poo/ProyectoFinal2025_2/View/MercadoPagoPayment.fxml")
            );

            if (indexController == null) {
                // Modal context - replace content in the same stage
                Logger.info("Modal context detected - loading Mercado Pago view in modal");

                Parent mercadoPagoView = loader.load();
                MercadoPagoPaymentController controller = loader.getController();
                controller.setSelectionController(this);
                controller.setOrder(order, orderDetail);
                // Don't set indexController - will be handled as modal

                // Replace the current scene content
                Stage stage = (Stage) lblOrderId.getScene().getWindow();
                stage.getScene().setRoot(mercadoPagoView);
                stage.setTitle("Pago con Mercado Pago");

                Logger.info("Mercado Pago payment view loaded in modal");
            } else {
                // Normal context - load in Index content area
                Logger.info("Index context detected - loading Mercado Pago view in content area");

                indexController.getContentArea().getChildren().clear();
                indexController.getContentArea().getChildren().add(loader.load());

                MercadoPagoPaymentController controller = loader.getController();
                controller.setIndexController(indexController);
                controller.setSelectionController(this);
                controller.setOrder(order, orderDetail);

                Logger.info("Mercado Pago payment view loaded in Index");
            }

        } catch (Exception e) {
            Logger.error("Error opening Mercado Pago payment view: " + e.getMessage());
            e.printStackTrace();
            co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.DialogUtil.showError(
                "Error", "Error al abrir vista de Mercado Pago:\n" + e.getMessage());
        }
    }

    /**
     * Handles selection of App payment option.
     * Loads the app's internal payment view in the Index content area or modal.
     */
    @FXML
    private void handleSelectAppPayment() {
        Logger.info("User selected App (simulated) payment processor");

        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/co/edu/uniquindio/poo/ProyectoFinal2025_2/View/Payment.fxml")
            );

            if (indexController == null) {
                // Modal context - replace content in the same stage
                Logger.info("Modal context detected - loading payment view in modal");

                Parent paymentView = loader.load();
                PaymentController controller = loader.getController();
                controller.setOrder(order, orderDetail);
                // Don't set indexController - will be handled as modal

                // Replace the current scene content
                Stage stage = (Stage) lblOrderId.getScene().getWindow();
                stage.getScene().setRoot(paymentView);
                stage.setTitle("Procesar Pago");

                Logger.info("App payment view loaded in modal");
            } else {
                // Normal context - load in Index content area
                Logger.info("Index context detected - loading payment view in content area");

                indexController.getContentArea().getChildren().clear();
                indexController.getContentArea().getChildren().add(loader.load());

                PaymentController controller = loader.getController();
                controller.setIndexController(indexController);
                controller.setOrder(order, orderDetail);

                Logger.info("App payment view loaded in Index");
            }

        } catch (Exception e) {
            Logger.error("Error opening App payment view: " + e.getMessage());
            e.printStackTrace();
            co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.DialogUtil.showError(
                "Error", "Error al abrir vista de pago:\n" + e.getMessage());
        }
    }

    /**
     * Handles "Volver" button click - returns to wizard last step (step 5) preserving all form data.
     */
    @FXML
    private void handleGoBack() {
        Logger.info("Going back to wizard last step (step 5)");

        if (indexController != null && wizardController != null) {
            try {
                // Use the existing wizard controller instance to preserve form data
                // Load the wizard FXML again, but we'll reuse the existing controller
                FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/co/edu/uniquindio/poo/ProyectoFinal2025_2/View/CreateShipmentWizard.fxml")
                );

                // Set the existing wizard controller to preserve state
                loader.setController(wizardController);

                // Load the view in Index content area
                indexController.getContentArea().getChildren().clear();
                indexController.getContentArea().getChildren().add(loader.load());

                // Return to step 5 (review) instead of resetting to step 1
                wizardController.returnToLastStep();

                Logger.info("Returned to wizard view at step 5 with preserved data");
            } catch (Exception e) {
                Logger.error("Error returning to wizard with preserved data: " + e.getMessage());
                e.printStackTrace();

                // Fallback: try loading fresh wizard and return to step 5 (data will be lost)
                try {
                    FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/co/edu/uniquindio/poo/ProyectoFinal2025_2/View/CreateShipmentWizard.fxml")
                    );

                    indexController.getContentArea().getChildren().clear();
                    indexController.getContentArea().getChildren().add(loader.load());

                    CreateShipmentWizardController newController = loader.getController();
                    newController.setIndexController(indexController);
                    newController.returnToLastStep();

                    Logger.warning("Returned to wizard at step 5 but form data was lost");
                } catch (Exception e2) {
                    Logger.error("Fallback also failed: " + e2.getMessage());
                    indexController.loadView("CreateShipmentWizard.fxml");
                }
            }
        } else if (indexController != null) {
            Logger.warning("WizardController reference not available, loading fresh wizard");
            indexController.loadView("CreateShipmentWizard.fxml");
        }
    }

    /**
     * Handles "Cancelar" button click.
     * In modal context: closes the modal window.
     * In normal context: cancels order and resets wizard to step 1.
     */
    @FXML
    private void handleCancel() {
        if (isModalContext) {
            // Modal context - just close the window without canceling the order
            Logger.info("Cancel clicked in modal context - closing modal");
            try {
                Stage stage = (Stage) lblOrderId.getScene().getWindow();
                stage.close();
                Logger.info("Modal window closed");
            } catch (Exception e) {
                Logger.error("Error closing modal: " + e.getMessage());
                e.printStackTrace();
            }
            return;
        }

        // Normal context - cancel order and reset wizard
        Logger.info("Payment cancelled, attempting to cancel order and reset wizard");

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
                // Continue with wizard reset even if cancellation fails
            }
        }

        if (indexController != null) {
            // Simply reload the wizard view - it will start fresh at step 1
            indexController.loadView("CreateShipmentWizard.fxml");
            Logger.info("Wizard reset and reloaded");
        }
    }
}
