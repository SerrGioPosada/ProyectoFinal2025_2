package co.edu.uniquindio.poo.ProyectoFinal2025_2.Controller;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.ShipmentStatus;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.StatusChange;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.dto.ShipmentDTO;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.dto.TrackingEventDTO;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.ShipmentService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.Adapter.TrackingTimelineUtil;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.DialogUtil;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
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

    // Dynamic Timeline Container
    @FXML private VBox timelineContainer;

    // Delivery Person Card
    @FXML private VBox deliveryPersonCard;
    @FXML private Label lblDeliveryPersonName;
    @FXML private Label lblDeliveryPersonPhone;
    @FXML private Label lblDeliveryPersonZone;

    // Service
    private final ShipmentService shipmentService = new ShipmentService();

    // Current shipment
    private ShipmentDTO currentShipment;

    // Index controller for navigation
    private IndexController indexController;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        trackingContent.setVisible(false);
        trackingContent.setManaged(false);

        Logger.info("TrackShipmentController initialized");
    }

    /**
     * Sets the IndexController reference for navigation.
     * @param indexController The IndexController instance
     */
    public void setIndexController(IndexController indexController) {
        this.indexController = indexController;
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
                getClass().getResource("/co/edu/uniquindio/poo/ProyectoFinal2025_2/View/ShipmentDetail.fxml")
            );
            javafx.scene.Parent root = loader.load();

            ShipmentDetailController controller = loader.getController();
            controller.loadShipmentDetails(currentShipment.getId());

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Detalles del Envío - " + currentShipment.getId());
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 650, 800);
            co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.ThemeManager.getInstance().applyThemeToScene(scene);
            stage.setScene(scene);
            stage.setResizable(true);
            stage.setMaximized(false);
            stage.show();

        } catch (Exception e) {
            Logger.error("Failed to load ShipmentDetail view: " + e.getMessage());
            DialogUtil.showError("Error", "No se pudo abrir los detalles del envío");
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
     * Updates the visual timeline with unified Order + Shipment history.
     */
    private void updateTimeline() {
        if (currentShipment == null || timelineContainer == null) return;

        // Clear existing timeline
        timelineContainer.getChildren().clear();

        // Get unified timeline events
        List<TrackingEventDTO> events = TrackingTimelineUtil.generateUnifiedTimeline(currentShipment.getId());

        if (events.isEmpty()) {
            Label noEvents = new Label("No hay información de seguimiento disponible");
            noEvents.setStyle("-fx-text-fill: #999; -fx-font-style: italic;");
            timelineContainer.getChildren().add(noEvents);
            return;
        }

        // Build timeline UI dynamically
        for (int i = 0; i < events.size(); i++) {
            TrackingEventDTO event = events.get(i);

            // Create event row
            HBox eventRow = createTimelineEventRow(event);
            timelineContainer.getChildren().add(eventRow);

            // Add progress bar between events (except after last event)
            if (i < events.size() - 1) {
                ProgressBar progress = new ProgressBar();
                progress.setMaxWidth(Double.MAX_VALUE);
                progress.getStyleClass().add("timeline-progress");

                // If current event is completed and next event exists, show partial or full progress
                TrackingEventDTO nextEvent = events.get(i + 1);
                if (event.isCompleted()) {
                    progress.setProgress(nextEvent.isCompleted() ? 1.0 : 0.5);
                } else {
                    progress.setProgress(0.0);
                }

                VBox progressContainer = new VBox(progress);
                progressContainer.setPadding(new Insets(0, 0, 0, 15));
                timelineContainer.getChildren().add(progressContainer);
            }
        }
    }

    /**
     * Creates a timeline event row UI element.
     */
    private HBox createTimelineEventRow(TrackingEventDTO event) {
        HBox row = new HBox(10);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Icon
        Label icon = new Label(event.isCompleted() ? "●" : "○");
        icon.getStyleClass().add("timeline-icon");
        icon.setStyle(String.format("-fx-text-fill: %s; -fx-font-size: 20px;",
            event.isCompleted() ? event.getColor() : "#CCCCCC"));

        // Event details
        VBox details = new VBox(2);

        // Status name with badge indicator
        HBox statusRow = new HBox(8);
        statusRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label statusLabel = new Label(event.getDisplayName());
        statusLabel.getStyleClass().add("timeline-status");
        statusLabel.setStyle("-fx-font-weight: bold;");

        // Add badge to indicate if it's from Order or Shipment
        Label badge = new Label(event.isOrderEvent() ? "ORDEN" : "ENVÍO");
        badge.setStyle(String.format(
            "-fx-background-color: %s; -fx-text-fill: white; " +
            "-fx-padding: 2 6 2 6; -fx-background-radius: 3; -fx-font-size: 10px;",
            event.isOrderEvent() ? "#42A5F5" : "#66BB6A"
        ));

        statusRow.getChildren().addAll(statusLabel, badge);

        // Description
        Label descriptionLabel = new Label(event.getDescription());
        descriptionLabel.getStyleClass().add("timeline-description");
        descriptionLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxWidth(400);

        // Time
        Label timeLabel = new Label(
            event.getTimestamp() != null ?
                event.getTimestamp().format(TIME_FORMATTER) :
                "Pendiente"
        );
        timeLabel.getStyleClass().add("timeline-time");
        timeLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 11px;");

        details.getChildren().addAll(statusRow, descriptionLabel, timeLabel);

        row.getChildren().addAll(icon, details);
        return row;
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

    /**
     * Handles navigation to My Shipments view.
     */
    @FXML
    private void handleGoToMyShipments() {
        if (indexController != null) {
            co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.NavigationUtil.navigate(
                indexController,
                "MyShipments.fxml",
                MyShipmentsController.class
            );
        }
    }

    /**
     * Handles navigation to Create New Shipment view.
     */
    @FXML
    private void handleNewShipment() {
        if (indexController != null) {
            co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.NavigationUtil.navigate(
                indexController,
                "CreateShipmentWizard.fxml",
                CreateShipmentWizardController.class
            );
        }
    }
}
