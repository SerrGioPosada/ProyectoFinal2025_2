package co.edu.uniquindio.poo.proyectofiinal2025_2.Controller;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Enums.ShipmentStatus;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.StatusChange;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.dto.ShipmentDTO;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Services.ShipmentService;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Util.UtilController.DialogUtil;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Util.UtilModel.Logger;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for tracking shipment status.
 * Shows visual timeline of shipment progress.
 */
public class TrackShipmentController implements Initializable {

    // Search
    @FXML private TextField txtShipmentId;

    // Tracking Content
    @FXML private VBox trackingContent;

    // Shipment Info
    @FXML private Label lblShipmentId;
    @FXML private Label lblStatus;
    @FXML private Label lblCreated;
    @FXML private Label lblEstimated;

    // Timeline Icons
    @FXML private Label iconRequested;
    @FXML private Label iconAssigned;
    @FXML private Label iconInRoute;
    @FXML private Label iconDelivered;

    // Timeline Times
    @FXML private Label lblTimeRequested;
    @FXML private Label lblTimeAssigned;
    @FXML private Label lblTimeInRoute;
    @FXML private Label lblTimeDelivered;

    // Timeline Progress
    @FXML private ProgressBar progressRequestedToAssigned;
    @FXML private ProgressBar progressAssignedToInRoute;
    @FXML private ProgressBar progressInRouteToDelivered;

    // Delivery Person Card
    @FXML private VBox deliveryPersonCard;
    @FXML private Label lblDeliveryPersonName;
    @FXML private Label lblDeliveryPersonPhone;
    @FXML private Label lblDeliveryPersonZone;

    // Service
    private final ShipmentService shipmentService = new ShipmentService();

    // Current shipment
    private ShipmentDTO currentShipment;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        trackingContent.setVisible(false);
        trackingContent.setManaged(false);

        Logger.info("TrackShipmentController initialized");
    }

    /**
     * Loads and displays tracking information for a specific shipment.
     * @param shipmentId Shipment ID to track
     */
    public void trackShipment(String shipmentId) {
        if (shipmentId == null || shipmentId.trim().isEmpty()) {
            DialogUtil.showWarning("Invalid ID", "Please enter a shipment ID");
            return;
        }

        txtShipmentId.setText(shipmentId);
        loadShipmentTracking(shipmentId);
    }

    // ===========================
    // Button Handlers
    // ===========================

    @FXML
    private void handleTrack() {
        String shipmentId = txtShipmentId.getText();
        if (shipmentId == null || shipmentId.trim().isEmpty()) {
            DialogUtil.showWarning("Invalid ID", "Please enter a shipment ID");
            return;
        }

        loadShipmentTracking(shipmentId.trim());
    }

    @FXML
    private void handleRefresh() {
        if (currentShipment == null) {
            DialogUtil.showWarning("No Shipment", "Please track a shipment first");
            return;
        }

        loadShipmentTracking(currentShipment.getId());
        DialogUtil.showInfo("Refreshed", "Tracking information has been updated");
    }

    @FXML
    private void handleViewDetails() {
        if (currentShipment == null) {
            DialogUtil.showWarning("No Shipment", "Please track a shipment first");
            return;
        }

        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/co/edu/uniquindio/poo/proyectofiinal2025_2/View/ShipmentDetail.fxml")
            );
            javafx.scene.Parent root = loader.load();

            ShipmentDetailController controller = loader.getController();
            controller.loadShipmentDetails(currentShipment.getId());

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Shipment Details - " + currentShipment.getId());
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();

        } catch (Exception e) {
            Logger.error("Failed to load ShipmentDetail view: " + e.getMessage());
            DialogUtil.showError("Error", "Could not open shipment details");
        }
    }

    @FXML
    private void handleBack() {
        javafx.stage.Stage stage = (javafx.stage.Stage) txtShipmentId.getScene().getWindow();
        stage.close();
    }

    // ===========================
    // Private Methods
    // ===========================

    /**
     * Loads shipment tracking information.
     */
    private void loadShipmentTracking(String shipmentId) {
        Optional<ShipmentDTO> shipmentOpt = shipmentService.getShipment(shipmentId);

        if (!shipmentOpt.isPresent()) {
            DialogUtil.showError("Not Found", "Shipment with ID " + shipmentId + " not found");
            trackingContent.setVisible(false);
            trackingContent.setManaged(false);
            return;
        }

        currentShipment = shipmentOpt.get();
        displayShipmentInfo();
        updateTimeline();
        updateDeliveryPersonInfo();

        trackingContent.setVisible(true);
        trackingContent.setManaged(true);

        Logger.info("Loaded tracking for shipment: " + shipmentId);
    }

    /**
     * Displays general shipment information.
     */
    private void displayShipmentInfo() {
        if (currentShipment == null) return;

        lblShipmentId.setText(currentShipment.getId());
        lblStatus.setText(currentShipment.getStatusDisplayName());
        lblStatus.setStyle("-fx-text-fill: " + currentShipment.getStatusColor() + "; -fx-font-weight: bold;");

        if (currentShipment.getCreationDate() != null) {
            lblCreated.setText(currentShipment.getCreationDate().format(TIME_FORMATTER));
        }

        if (currentShipment.getEstimatedDeliveryDate() != null) {
            lblEstimated.setText(currentShipment.getEstimatedDeliveryDate().format(TIME_FORMATTER));
        }
    }

    /**
     * Updates the visual timeline based on shipment status.
     */
    private void updateTimeline() {
        if (currentShipment == null) return;

        ShipmentStatus status = currentShipment.getStatus();

        // Reset all
        resetTimelineIcon(iconRequested);
        resetTimelineIcon(iconAssigned);
        resetTimelineIcon(iconInRoute);
        resetTimelineIcon(iconDelivered);
        progressRequestedToAssigned.setProgress(0);
        progressAssignedToInRoute.setProgress(0);
        progressInRouteToDelivered.setProgress(0);

        // Set times from creation date
        if (currentShipment.getCreationDate() != null) {
            lblTimeRequested.setText(currentShipment.getCreationDate().format(TIME_FORMATTER));
            activateTimelineIcon(iconRequested);
        }

        // Progress based on status
        switch (status) {
            case PENDING_ASSIGNMENT:
                // Only requested is active
                break;

            case IN_TRANSIT:
                if (currentShipment.getAssignmentDate() != null) {
                    lblTimeAssigned.setText(currentShipment.getAssignmentDate().format(TIME_FORMATTER));
                    activateTimelineIcon(iconAssigned);
                }
                progressRequestedToAssigned.setProgress(1.0);
                break;

            case OUT_FOR_DELIVERY:
                if (currentShipment.getAssignmentDate() != null) {
                    lblTimeAssigned.setText(currentShipment.getAssignmentDate().format(TIME_FORMATTER));
                    activateTimelineIcon(iconAssigned);
                    activateTimelineIcon(iconInRoute);
                }
                progressRequestedToAssigned.setProgress(1.0);
                progressAssignedToInRoute.setProgress(1.0);
                lblTimeInRoute.setText(LocalDateTime.now().format(TIME_FORMATTER));
                break;

            case DELIVERED:
                if (currentShipment.getAssignmentDate() != null) {
                    lblTimeAssigned.setText(currentShipment.getAssignmentDate().format(TIME_FORMATTER));
                }
                if (currentShipment.getActualDeliveryDate() != null) {
                    lblTimeDelivered.setText(currentShipment.getActualDeliveryDate().format(TIME_FORMATTER));
                }
                activateTimelineIcon(iconRequested);
                activateTimelineIcon(iconAssigned);
                activateTimelineIcon(iconInRoute);
                activateTimelineIcon(iconDelivered);
                progressRequestedToAssigned.setProgress(1.0);
                progressAssignedToInRoute.setProgress(1.0);
                progressInRouteToDelivered.setProgress(1.0);
                break;

            case CANCELLED:
            case RETURNED:
                // Show as inactive
                break;
        }
    }

    /**
     * Activates a timeline icon (makes it filled).
     */
    private void activateTimelineIcon(Label icon) {
        if (icon == null) return;
        icon.setText("●");
        icon.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 20px;");
    }

    /**
     * Resets a timeline icon (makes it empty).
     */
    private void resetTimelineIcon(Label icon) {
        if (icon == null) return;
        icon.setText("○");
        icon.setStyle("-fx-text-fill: #CCCCCC; -fx-font-size: 20px;");
    }

    /**
     * Updates delivery person information if assigned.
     */
    private void updateDeliveryPersonInfo() {
        if (currentShipment == null) return;

        if (currentShipment.getDeliveryPersonId() == null ||
            currentShipment.getDeliveryPersonName() == null) {
            deliveryPersonCard.setVisible(false);
            deliveryPersonCard.setManaged(false);
            return;
        }

        lblDeliveryPersonName.setText(currentShipment.getDeliveryPersonName());
        lblDeliveryPersonPhone.setText(currentShipment.getDeliveryPersonPhone());
        lblDeliveryPersonZone.setText(currentShipment.getDestinationZone());

        deliveryPersonCard.setVisible(true);
        deliveryPersonCard.setManaged(true);
    }
}
