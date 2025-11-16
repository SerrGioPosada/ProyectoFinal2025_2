package co.edu.uniquindio.poo.ProyectoFinal2025_2.Controller;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.AdditionalService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Decorator.CostCalculator;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.ServiceType;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.StatusChange;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.dto.ShipmentDTO;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.ShipmentService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.TariffService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.DialogUtil;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for shipment details view.
 * Shows complete information about a shipment (shared between user and admin).
 */
public class ShipmentDetailController implements Initializable {

    // General Information
    @FXML private Label lblId;
    @FXML private Label lblStatus;
    @FXML private Label lblPriority;
    @FXML private Label lblCreationDate;
    @FXML private Label lblEstimatedDate;
    @FXML private Label lblActualDate;

    // User Information
    @FXML private Label lblUserName;
    @FXML private Label lblUserPhone;
    @FXML private Label lblUserEmail;

    // Addresses
    @FXML private Label lblOriginAddress;
    @FXML private Label lblOriginZone;
    @FXML private Label lblDestinationAddress;
    @FXML private Label lblDestinationZone;

    // Package Details
    @FXML private Label lblWeight;
    @FXML private Label lblDimensions;
    @FXML private Label lblVolume;
    @FXML private Label lblDistance;

    // Delivery Person Section
    @FXML private VBox deliveryPersonSection;
    @FXML private Label lblDeliveryPersonName;
    @FXML private Label lblDeliveryPersonPhone;
    @FXML private Label lblDeliveryPersonZone;
    @FXML private Label lblAssignmentDate;

    // Cost Breakdown
    @FXML private Label lblBaseCost;
    @FXML private Label lblServicesCost;
    @FXML private Label lblTotalCost;

    // Additional Services
    @FXML private ListView<String> listServices;

    // Status History
    @FXML private TableView<StatusChange> statusHistoryTable;
    @FXML private TableColumn<StatusChange, String> colPreviousStatus;
    @FXML private TableColumn<StatusChange, String> colNewStatus;
    @FXML private TableColumn<StatusChange, String> colTimestamp;
    @FXML private TableColumn<StatusChange, String> colChangedBy;
    @FXML private TableColumn<StatusChange, String> colReason;

    // Notes
    @FXML private TextArea txtUserNotes;
    @FXML private TextArea txtInternalNotes;

    // Incident Section
    @FXML private VBox incidentSection;
    @FXML private Label lblIncidentType;
    @FXML private Label lblIncidentDate;
    @FXML private Label lblIncidentDescription;

    // Services
    private final ShipmentService shipmentService = new ShipmentService();
    private final TariffService tariffService = new TariffService();

    // Current shipment
    private ShipmentDTO currentShipment;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupStatusHistoryTable();

        if (deliveryPersonSection != null) {
            deliveryPersonSection.setVisible(false);
            deliveryPersonSection.setManaged(false);
        }

        if (incidentSection != null) {
            incidentSection.setVisible(false);
            incidentSection.setManaged(false);
        }

        Logger.info("ShipmentDetailController initialized");
    }

    /**
     * Loads and displays details for a specific shipment.
     * @param shipmentId Shipment ID
     */
    public void loadShipmentDetails(String shipmentId) {
        if (shipmentId == null || shipmentId.trim().isEmpty()) {
            DialogUtil.showError("Invalid ID", "Shipment ID is required");
            return;
        }

        Optional<ShipmentDTO> shipmentOpt = shipmentService.getShipment(shipmentId);

        if (!shipmentOpt.isPresent()) {
            DialogUtil.showError("Not Found", "Shipment with ID " + shipmentId + " not found");
            return;
        }

        currentShipment = shipmentOpt.get();
        displayShipmentDetails();

        Logger.info("Loaded details for shipment: " + shipmentId);
    }

    /**
     * Displays all shipment details in the UI.
     */
    private void displayShipmentDetails() {
        if (currentShipment == null) return;

        displayGeneralInfo();
        displayUserInfo();
        displayAddresses();
        displayPackageDetails();
        displayDeliveryPersonInfo();
        displayCostBreakdown();
        displayAdditionalServices();
        displayStatusHistory();
        displayNotes();
        displayIncident();
    }

    /**
     * Displays general shipment information.
     */
    private void displayGeneralInfo() {
        lblId.setText(currentShipment.getId());
        lblStatus.setText(currentShipment.getStatusDisplayName());
        lblStatus.setStyle("-fx-text-fill: " + currentShipment.getStatusColor() + "; -fx-font-weight: bold;");
        lblPriority.setText(String.valueOf(currentShipment.getPriority()));

        if (currentShipment.getCreationDate() != null) {
            lblCreationDate.setText(currentShipment.getCreationDate().format(DATE_FORMATTER));
        }

        if (currentShipment.getEstimatedDeliveryDate() != null) {
            lblEstimatedDate.setText(currentShipment.getEstimatedDeliveryDate().format(DATE_FORMATTER));
        }

        if (currentShipment.getActualDeliveryDate() != null) {
            lblActualDate.setText(currentShipment.getActualDeliveryDate().format(DATE_FORMATTER));
        } else {
            lblActualDate.setText("--");
        }
    }

    /**
     * Displays user information.
     */
    private void displayUserInfo() {
        if (currentShipment.getUserName() != null) {
            lblUserName.setText(currentShipment.getUserName());
        }
        if (currentShipment.getUserPhone() != null) {
            lblUserPhone.setText(currentShipment.getUserPhone());
        }
        if (currentShipment.getUserEmail() != null) {
            lblUserEmail.setText(currentShipment.getUserEmail());
        }
    }

    /**
     * Displays address information.
     */
    private void displayAddresses() {
        if (currentShipment.getOriginAddressComplete() != null) {
            lblOriginAddress.setText(currentShipment.getOriginAddressComplete());
        }
        if (currentShipment.getOriginZone() != null) {
            lblOriginZone.setText("Zone: " + currentShipment.getOriginZone());
        }

        if (currentShipment.getDestinationAddressComplete() != null) {
            lblDestinationAddress.setText(currentShipment.getDestinationAddressComplete());
        }
        if (currentShipment.getDestinationZone() != null) {
            lblDestinationZone.setText("Zone: " + currentShipment.getDestinationZone());
        }
    }

    /**
     * Displays package details.
     */
    private void displayPackageDetails() {
        lblWeight.setText(String.format("%.2f kg", currentShipment.getWeightKg()));

        String dimensions = String.format("%.0f x %.0f x %.0f cm",
            currentShipment.getHeightCm(),
            currentShipment.getWidthCm(),
            currentShipment.getLengthCm());
        lblDimensions.setText(dimensions);

        lblVolume.setText(String.format("%.4f m³", currentShipment.getVolumeM3()));
        lblDistance.setText(String.format("%.2f km", currentShipment.getDistanceKm()));
    }

    /**
     * Displays delivery person information if assigned.
     */
    private void displayDeliveryPersonInfo() {
        if (currentShipment.getDeliveryPersonId() == null ||
            currentShipment.getDeliveryPersonName() == null) {
            if (deliveryPersonSection != null) {
                deliveryPersonSection.setVisible(false);
                deliveryPersonSection.setManaged(false);
            }
            return;
        }

        lblDeliveryPersonName.setText(currentShipment.getDeliveryPersonName());
        lblDeliveryPersonPhone.setText(currentShipment.getDeliveryPersonPhone());
        lblDeliveryPersonZone.setText(currentShipment.getDestinationZone());

        if (currentShipment.getAssignmentDate() != null) {
            lblAssignmentDate.setText(currentShipment.getAssignmentDate().format(DATE_FORMATTER));
        }

        if (deliveryPersonSection != null) {
            deliveryPersonSection.setVisible(true);
            deliveryPersonSection.setManaged(true);
        }
    }

    /**
     * Displays cost breakdown.
     */
    private void displayCostBreakdown() {
        lblBaseCost.setText(String.format("$%,.2f", currentShipment.getBaseCost()));
        lblServicesCost.setText(String.format("$%,.2f", currentShipment.getServicesCost()));
        lblTotalCost.setText(String.format("$%,.2f", currentShipment.getTotalCost()));
    }

    /**
     * Displays additional services.
     */
    private void displayAdditionalServices() {
        if (currentShipment.getAdditionalServices() == null ||
            currentShipment.getAdditionalServices().isEmpty()) {
            listServices.setItems(FXCollections.observableArrayList("No additional services"));
            return;
        }

        List<String> services = currentShipment.getAdditionalServices().stream()
            .filter(service -> service.getType() != null)
            .map(service -> service.getType().getName() + " - $" + String.format("%,.2f", service.getCost()))
            .collect(Collectors.toList());

        listServices.setItems(FXCollections.observableArrayList(services));
    }

    /**
     * Sets up status history table.
     */
    private void setupStatusHistoryTable() {
        if (statusHistoryTable == null) return;

        colPreviousStatus.setCellValueFactory(data -> {
            if (data.getValue().getPreviousStatus() == null) return new SimpleStringProperty("--");
            return new SimpleStringProperty(data.getValue().getPreviousStatus().getDisplayName());
        });

        colNewStatus.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getNewStatus().getDisplayName()));

        colTimestamp.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getTimestamp().format(DATE_FORMATTER)));

        colChangedBy.setCellValueFactory(data -> {
            String changedBy = data.getValue().getChangedBy();
            return new SimpleStringProperty(changedBy != null ? changedBy : "SYSTEM");
        });

        colReason.setCellValueFactory(data -> {
            String reason = data.getValue().getReason();
            return new SimpleStringProperty(reason != null ? reason : "--");
        });
    }

    /**
     * Displays status history.
     */
    private void displayStatusHistory() {
        if (statusHistoryTable == null) return;

        // Status history is part of Shipment model but not exposed in DTO
        // For now, show a simple message
        statusHistoryTable.setPlaceholder(new Label("Status history not available"));
    }

    /**
     * Displays notes.
     */
    private void displayNotes() {
        if (txtUserNotes != null && currentShipment.getUserNotes() != null) {
            txtUserNotes.setText(currentShipment.getUserNotes());
            txtUserNotes.setEditable(false);
        }

        if (txtInternalNotes != null && currentShipment.getInternalNotes() != null) {
            txtInternalNotes.setText(currentShipment.getInternalNotes());
            txtInternalNotes.setEditable(false);
        }
    }

    /**
     * Displays incident information if exists.
     */
    private void displayIncident() {
        if (currentShipment.getIncident() == null) {
            if (incidentSection != null) {
                incidentSection.setVisible(false);
                incidentSection.setManaged(false);
            }
            return;
        }

        if (lblIncidentType != null) {
            lblIncidentType.setText(currentShipment.getIncident().getType().getDescription());
        }

        if (lblIncidentDate != null && currentShipment.getIncident().getRegistrationDate() != null) {
            lblIncidentDate.setText(
                currentShipment.getIncident().getRegistrationDate().format(DATE_FORMATTER)
            );
        }

        if (lblIncidentDescription != null) {
            lblIncidentDescription.setText(currentShipment.getIncident().getDescription());
        }

        if (incidentSection != null) {
            incidentSection.setVisible(true);
            incidentSection.setManaged(true);
        }
    }

    // ===========================
    // Button Handlers
    // ===========================

    @FXML
    private void handleBack() {
        Stage stage = (Stage) lblId.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handlePrint() {
        if (currentShipment == null) {
            DialogUtil.showWarning("Sin Datos", "No hay envío cargado para imprimir");
            return;
        }

        try {
            // Generate PDF with shipment details
            String fileName = "shipment_detail_" + currentShipment.getId() + "_" + System.currentTimeMillis();
            String title = "Detalles del Envío";
            String subtitle = "ID: " + currentShipment.getId() + " | Estado: " + currentShipment.getStatusDisplayName();

            // Prepare headers and data
            List<String> headers = Arrays.asList("Campo", "Valor");
            List<List<String>> rows = new ArrayList<>();

            // General Information
            rows.add(Arrays.asList("ID Envío", currentShipment.getId()));
            rows.add(Arrays.asList("Estado", currentShipment.getStatusDisplayName()));
            rows.add(Arrays.asList("Prioridad", String.valueOf(currentShipment.getPriority())));
            rows.add(Arrays.asList("Fecha Creación", currentShipment.getCreationDate() != null ?
                currentShipment.getCreationDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "N/A"));

            // User Information
            rows.add(Arrays.asList("Usuario", currentShipment.getUserEmail() != null ? currentShipment.getUserEmail() : "N/A"));

            // Addresses
            rows.add(Arrays.asList("Origen", currentShipment.getOriginAddressComplete() != null ? currentShipment.getOriginAddressComplete() : "N/A"));
            rows.add(Arrays.asList("Destino", currentShipment.getDestinationAddressComplete() != null ? currentShipment.getDestinationAddressComplete() : "N/A"));

            // Package Details
            rows.add(Arrays.asList("Peso", String.format("%.2f kg", currentShipment.getWeightKg())));
            rows.add(Arrays.asList("Distancia", String.format("%.2f km", currentShipment.getDistanceKm())));

            // Delivery Person
            rows.add(Arrays.asList("Repartidor", currentShipment.getDeliveryPersonName() != null ? currentShipment.getDeliveryPersonName() : "Sin asignar"));

            // Cost
            rows.add(Arrays.asList("Costo Total", String.format("$%.2f", currentShipment.getTotalCost())));

            // Generate PDF
            File pdfFile = co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.PdfUtility.generatePdfReport(
                fileName, title, subtitle, headers, rows);

            if (pdfFile != null && pdfFile.exists()) {
                DialogUtil.showSuccess("PDF Generado",
                    "El PDF se generó exitosamente:\n" + pdfFile.getAbsolutePath());
                Logger.info("Shipment detail PDF generated: " + pdfFile.getAbsolutePath());
            } else {
                DialogUtil.showError("Error", "No se pudo generar el PDF");
            }
        } catch (Exception e) {
            Logger.error("Error generating shipment detail PDF: " + e.getMessage());
            DialogUtil.showError("Error", "Error al generar PDF: " + e.getMessage());
        }
    }

    /**
     * Shows detailed cost breakdown using Decorator pattern.
     */
    @FXML
    private void handleShowDetailedBreakdown() {
        if (currentShipment == null) {
            DialogUtil.showWarning("Sin Datos", "No hay envío cargado");
            return;
        }

        try {
            // Get services from shipment
            List<ServiceType> services = currentShipment.getAdditionalServices() != null ?
                currentShipment.getAdditionalServices().stream()
                    .map(AdditionalService::getType)
                    .collect(Collectors.toList()) :
                List.of();

            // Get breakdown from TariffService using Decorator pattern
            var breakdown = tariffService.getCostBreakdown(
                currentShipment.getDistanceKm(),
                currentShipment.getWeightKg(),
                currentShipment.getVolumeM3(),
                currentShipment.getPriority(),
                services
            );

            // Build breakdown dialog
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Desglose Detallado de Costos");
            dialog.setHeaderText("Envío: " + currentShipment.getId());

            // Create content
            VBox content = new VBox(10);
            content.setPadding(new Insets(20));
            content.setPrefWidth(500);

            Label title = new Label("Desglose Línea por Línea:");
            title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            content.getChildren().add(title);

            // Add separator
            Separator sep1 = new Separator();
            content.getChildren().add(sep1);

            // Add each breakdown item
            double runningTotal = 0.0;
            for (CostCalculator.CostBreakdownItem item : breakdown) {
                runningTotal += item.getAmount();

                Label itemLabel = new Label(String.format("• %s: $%,.0f",
                    item.getDescription(),
                    item.getAmount()
                ));
                itemLabel.setStyle("-fx-font-size: 12px;");
                content.getChildren().add(itemLabel);
            }

            // Add separator
            Separator sep2 = new Separator();
            content.getChildren().add(sep2);

            // Add total
            Label totalLabel = new Label(String.format("TOTAL: $%,.0f", runningTotal));
            totalLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #2196F3;");
            content.getChildren().add(totalLabel);

            // Add comparison with stored total
            if (Math.abs(runningTotal - currentShipment.getTotalCost()) > 0.01) {
                Label warningLabel = new Label(
                    String.format("⚠️ Nota: El total calculado ($%,.0f) difiere del total almacenado ($%,.0f)",
                    runningTotal, currentShipment.getTotalCost())
                );
                warningLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #ff9800;");
                warningLabel.setWrapText(true);
                content.getChildren().add(warningLabel);
            }

            // Add info about decorator pattern
            Separator sep3 = new Separator();
            content.getChildren().add(sep3);

            Label infoLabel = new Label(
                "💡 Este desglose se calcula usando el Patrón Decorator:\n" +
                "   • Tariff: Plantilla de precios configurada\n" +
                "   • BaseShippingCost: Cálculo de costos básicos (base + distancia + peso + volumen)\n" +
                "   • Decorators: Servicios adicionales apilados dinámicamente"
            );
            infoLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
            infoLabel.setWrapText(true);
            content.getChildren().add(infoLabel);

            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
            dialog.showAndWait();

        } catch (Exception e) {
            Logger.error("Error showing detailed breakdown: " + e.getMessage());
            DialogUtil.showError("Error", "Error al calcular el desglose detallado: " + e.getMessage());
        }
    }
}
