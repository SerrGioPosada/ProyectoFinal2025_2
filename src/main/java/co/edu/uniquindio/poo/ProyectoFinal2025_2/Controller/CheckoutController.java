package co.edu.uniquindio.poo.ProyectoFinal2025_2.Controller;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.AdditionalService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Order;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.dto.OrderDetailDTO;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.OrderService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.NavigationUtil;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * Controller for the checkout process.
 * Displays order summary and allows user to proceed to payment.
 */
public class CheckoutController implements Initializable {

    // Order Summary Section
    @FXML private Label lblOrderId;
    @FXML private Label lblOriginAddress;
    @FXML private Label lblDestinationAddress;

    // Package Details
    @FXML private Label lblWeight;
    @FXML private Label lblDimensions;
    @FXML private Label lblVolume;
    @FXML private Label lblDistance;

    // Schedule
    @FXML private Label lblPickupDate;
    @FXML private Label lblEstimatedDelivery;
    @FXML private Label lblPriority;

    // Services
    @FXML private VBox vboxServices;
    @FXML private Label lblServicesCount;

    // Cost Breakdown
    @FXML private Label lblBaseCost;
    @FXML private Label lblDistanceCost;
    @FXML private Label lblWeightCost;
    @FXML private Label lblVolumeCost;
    @FXML private Label lblServicesCost;
    @FXML private Label lblPriorityCost;
    @FXML private Label lblTotalCost;

    // Notes
    @FXML private TextArea txtNotes;

    // Services
    private final OrderService orderService = new OrderService();

    // Data
    private OrderDetailDTO orderDetail;
    private Order createdOrder;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Logger.info("CheckoutController initialized");
    }

    /**
     * Sets the order details to display.
     * This should be called from the CreateShipmentController.
     */
    public void setOrderDetail(OrderDetailDTO orderDetail) {
        this.orderDetail = orderDetail;
        displayOrderSummary();
    }

    /**
     * Displays the order summary in the UI.
     */
    private void displayOrderSummary() {
        if (orderDetail == null) {
            showError("No hay datos de orden para mostrar");
            return;
        }

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        // Addresses
        lblOriginAddress.setText(formatAddress(orderDetail.getOrigin()));
        lblDestinationAddress.setText(formatAddress(orderDetail.getDestination()));

        // Package Details
        lblWeight.setText(String.format("%.2f kg", orderDetail.getWeightKg()));
        lblDimensions.setText(String.format("%.0f x %.0f x %.0f cm",
            orderDetail.getWidthCm(), orderDetail.getHeightCm(), orderDetail.getLengthCm()));
        lblVolume.setText(String.format("%.4f m³", orderDetail.getVolumeM3()));
        lblDistance.setText(String.format("%.2f km", orderDetail.getDistanceKm()));

        // Schedule
        if (orderDetail.getRequestedPickupDate() != null) {
            lblPickupDate.setText(orderDetail.getRequestedPickupDate().format(dateFormatter));
        }
        if (orderDetail.getEstimatedDelivery() != null) {
            lblEstimatedDelivery.setText(orderDetail.getEstimatedDelivery().format(dateFormatter));
        }
        lblPriority.setText("Nivel " + orderDetail.getPriority());

        // Services
        displayServices();

        // Cost Breakdown
        lblBaseCost.setText(formatCurrency(orderDetail.getBaseCost()));
        lblDistanceCost.setText(formatCurrency(orderDetail.getDistanceCost()));
        lblWeightCost.setText(formatCurrency(orderDetail.getWeightCost()));
        lblVolumeCost.setText(formatCurrency(orderDetail.getVolumeCost()));
        lblServicesCost.setText(formatCurrency(orderDetail.getServicesCost()));
        lblPriorityCost.setText(formatCurrency(orderDetail.getPriorityCost()));
        lblTotalCost.setText(formatCurrency(orderDetail.getTotalCost()));

        // Notes
        if (orderDetail.getUserNotes() != null && !orderDetail.getUserNotes().isEmpty()) {
            txtNotes.setText(orderDetail.getUserNotes());
        } else {
            txtNotes.setText("Sin notas adicionales");
        }
        txtNotes.setEditable(false);
    }

    /**
     * Displays additional services.
     */
    private void displayServices() {
        vboxServices.getChildren().clear();

        if (orderDetail.getAdditionalServices() == null || orderDetail.getAdditionalServices().isEmpty()) {
            Label noServices = new Label("Sin servicios adicionales");
            noServices.setStyle("-fx-text-fill: #666;");
            vboxServices.getChildren().add(noServices);
            lblServicesCount.setText("0 servicios");
            return;
        }

        lblServicesCount.setText(orderDetail.getAdditionalServices().size() + " servicios");

        for (AdditionalService service : orderDetail.getAdditionalServices()) {
            Label serviceLabel = new Label("• " + service.getType().toString());
            serviceLabel.setStyle("-fx-font-size: 12px;");
            vboxServices.getChildren().add(serviceLabel);
        }
    }

    /**
     * Formats an address for display.
     */
    private String formatAddress(co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Address address) {
        if (address == null) return "No especificada";
        return String.format("%s - %s, %s",
            address.getAlias(),
            address.getStreet(),
            address.getCity());
    }

    /**
     * Formats currency values.
     */
    private String formatCurrency(double value) {
        return String.format("$%,.0f", value);
    }

    /**
     * Handles the confirm order and proceed to payment button.
     */
    @FXML
    private void handleProceedToPayment() {
        try {
            // Create the order
            createdOrder = orderService.initiateOrderCreation(
                orderDetail.getUserId(),
                orderDetail.getOrigin(),
                orderDetail.getDestination()
            );

            Logger.info("Order created with ID: " + createdOrder.getId());

            // Open payment window
            openPaymentWindow();

        } catch (Exception e) {
            Logger.error("Error creating order: " + e.getMessage());
            showError("Error al crear la orden: " + e.getMessage());
        }
    }

    /**
     * Opens the payment window.
     */
    private void openPaymentWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/co/edu/uniquindio/poo/ProyectoFinal2025_2/View/Payment.fxml"));
            Scene scene = new Scene(loader.load());

            // Get controller and pass order data
            PaymentController paymentController = loader.getController();
            paymentController.setOrder(createdOrder, orderDetail);

            Stage paymentStage = new Stage();
            paymentStage.setTitle("Pago - Orden #" + createdOrder.getId());
            paymentStage.setScene(scene);
            paymentStage.initModality(Modality.APPLICATION_MODAL);
            paymentStage.setResizable(false);

            // Close checkout when payment opens
            paymentStage.setOnHidden(e -> {
                Stage currentStage = (Stage) lblTotalCost.getScene().getWindow();
                currentStage.close();
            });

            paymentStage.showAndWait();

        } catch (Exception e) {
            Logger.error("Error opening payment window: " + e.getMessage());
            showError("Error al abrir ventana de pago");
        }
    }

    /**
     * Handles the cancel button.
     */
    @FXML
    private void handleCancel() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancelar");
        confirm.setHeaderText("¿Cancelar orden?");
        confirm.setContentText("¿Está seguro que desea cancelar esta orden?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Stage stage = (Stage) lblTotalCost.getScene().getWindow();
                stage.close();
            }
        });
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
     * Shows a success alert.
     */
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
