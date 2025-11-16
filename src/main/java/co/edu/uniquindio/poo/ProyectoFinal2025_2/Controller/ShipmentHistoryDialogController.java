package co.edu.uniquindio.poo.ProyectoFinal2025_2.Controller;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.dto.ShipmentDTO;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.dto.TrackingEventDTO;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.ShipmentService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.Adapter.TrackingTimelineUtil;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for the Shipment History Dialog.
 * Shows unified timeline of Order + Shipment events in a modal window.
 */
public class ShipmentHistoryDialogController implements Initializable {

    @FXML private Label lblShipmentId;
    @FXML private Label lblCurrentStatus;
    @FXML private VBox timelineContainer;

    private final ShipmentService shipmentService = new ShipmentService();
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private String shipmentId;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Logger.info("ShipmentHistoryDialogController initialized");
    }

    /**
     * Loads and displays the shipment history.
     * @param shipmentId The shipment ID
     */
    public void loadHistory(String shipmentId) {
        this.shipmentId = shipmentId;

        // Load shipment info
        Optional<ShipmentDTO> shipmentOpt = shipmentService.getShipment(shipmentId);
        if (!shipmentOpt.isPresent()) {
            Logger.error("Shipment not found: " + shipmentId);
            return;
        }

        ShipmentDTO shipment = shipmentOpt.get();
        lblShipmentId.setText(shipment.getId());
        lblCurrentStatus.setText(shipment.getStatusDisplayName());
        lblCurrentStatus.setStyle("-fx-text-fill: " + shipment.getStatusColor() + "; -fx-font-weight: bold;");

        // Load timeline from shipment
        loadTimelineFromShipment();
    }

    /**
     * Loads and displays the history from an order ID.
     * Shows order events and shipment events if the order has an associated shipment.
     * @param orderId The order ID
     */
    public void loadHistoryFromOrder(String orderId) {
        Logger.info("Loading history from order: " + orderId);

        // Display order ID in the label
        lblShipmentId.setText("Orden: " + orderId);

        // Load and display current status from timeline
        List<TrackingEventDTO> events = TrackingTimelineUtil.generateTimelineFromOrder(orderId);
        if (!events.isEmpty()) {
            // Get the most recent completed event
            TrackingEventDTO currentEvent = events.stream()
                    .filter(TrackingEventDTO::isCompleted)
                    .reduce((first, second) -> second) // Get last
                    .orElse(events.get(0));

            lblCurrentStatus.setText(currentEvent.getDisplayName());
            lblCurrentStatus.setStyle("-fx-text-fill: " + currentEvent.getColor() + "; -fx-font-weight: bold;");
        } else {
            lblCurrentStatus.setText("Sin información");
            lblCurrentStatus.setStyle("-fx-text-fill: #999999;");
        }

        // Load timeline from order
        loadTimelineFromOrder(orderId);
    }

    /**
     * Loads the unified timeline from a shipment ID.
     */
    private void loadTimelineFromShipment() {
        if (timelineContainer == null) return;

        timelineContainer.getChildren().clear();

        // Get unified timeline events
        List<TrackingEventDTO> events = TrackingTimelineUtil.generateUnifiedTimeline(shipmentId);

        buildTimelineUI(events);
    }

    /**
     * Loads the unified timeline from an order ID.
     */
    private void loadTimelineFromOrder(String orderId) {
        if (timelineContainer == null) return;

        timelineContainer.getChildren().clear();

        // Get unified timeline events from order
        List<TrackingEventDTO> events = TrackingTimelineUtil.generateTimelineFromOrder(orderId);

        buildTimelineUI(events);
    }

    /**
     * Builds the timeline UI from a list of events.
     */
    private void buildTimelineUI(List<TrackingEventDTO> events) {
        if (events.isEmpty()) {
            Label noEvents = new Label("No hay información de seguimiento disponible");
            noEvents.setStyle("-fx-text-fill: #999; -fx-font-style: italic; -fx-padding: 20;");
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
                progress.setPrefHeight(3);
                progress.setStyle("-fx-accent: #4CAF50;");

                // If current event is completed and next event exists, show partial or full progress
                TrackingEventDTO nextEvent = events.get(i + 1);
                if (event.isCompleted()) {
                    progress.setProgress(nextEvent.isCompleted() ? 1.0 : 0.5);
                } else {
                    progress.setProgress(0.0);
                }

                VBox progressContainer = new VBox(progress);
                progressContainer.setPadding(new Insets(5, 0, 5, 20));
                timelineContainer.getChildren().add(progressContainer);
            }
        }
    }

    /**
     * Creates a timeline event row UI element.
     */
    private HBox createTimelineEventRow(TrackingEventDTO event) {
        HBox row = new HBox(12);
        row.setAlignment(javafx.geometry.Pos.TOP_LEFT);
        row.setPadding(new Insets(5, 0, 5, 0));

        // Icon
        Label icon = new Label(event.isCompleted() ? "●" : "○");
        icon.setStyle(String.format("-fx-text-fill: %s; -fx-font-size: 22px; -fx-padding: 0;",
            event.isCompleted() ? event.getColor() : "#CCCCCC"));
        icon.setMinWidth(30);

        // Event details container
        VBox details = new VBox(4);
        details.setPrefWidth(500);

        // Status name with badge indicator
        HBox statusRow = new HBox(10);
        statusRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label statusLabel = new Label(event.getDisplayName());
        statusLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

        // Add badge to indicate if it's from Order or Shipment
        Label badge = new Label(event.isOrderEvent() ? "ORDEN" : "ENVÍO");
        badge.setStyle(String.format(
            "-fx-background-color: %s; -fx-text-fill: white; " +
            "-fx-padding: 2 8 2 8; -fx-background-radius: 3; -fx-font-size: 9px; -fx-font-weight: bold;",
            event.isOrderEvent() ? "#42A5F5" : "#66BB6A"
        ));

        statusRow.getChildren().addAll(statusLabel, badge);

        // Description
        Label descriptionLabel = new Label(event.getDescription());
        descriptionLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxWidth(450);

        // Time
        Label timeLabel = new Label(
            event.getTimestamp() != null ?
                "⏱ " + event.getTimestamp().format(TIME_FORMATTER) :
                "⏱ Pendiente"
        );
        timeLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 11px;");

        details.getChildren().addAll(statusRow, descriptionLabel, timeLabel);

        row.getChildren().addAll(icon, details);
        return row;
    }

    /**
     * Closes the dialog.
     */
    @FXML
    private void handleClose() {
        Stage stage = (Stage) lblShipmentId.getScene().getWindow();
        stage.close();
    }
}
