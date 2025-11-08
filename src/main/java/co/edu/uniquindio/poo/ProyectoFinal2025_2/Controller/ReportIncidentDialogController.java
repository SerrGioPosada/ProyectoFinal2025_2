package co.edu.uniquindio.poo.ProyectoFinal2025_2.Controller;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.DeliveryPerson;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.IncidentType;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.ShipmentStatus;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Incident;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Shipment;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.AuthenticationService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.ShipmentService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.DialogUtil;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilService.IdGenerationUtil;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the Report Incident Dialog (ReportIncidentDialog.fxml).
 * <p>
 * This controller manages the incident reporting form where delivery persons
 * can report problems encountered during deliveries.
 * </p>
 */
public class ReportIncidentDialogController implements Initializable {

    private static final int MAX_DESCRIPTION_LENGTH = 500;

    // =================================================================================================================
    // FXML Fields
    // =================================================================================================================

    @FXML private Label lblOrderId;
    @FXML private Label lblAddress;
    @FXML private Label lblCharCount;

    @FXML private ComboBox<String> cmbIncidentType;
    @FXML private TextArea txtDescription;

    @FXML private CheckBox chkContactCustomer;
    @FXML private CheckBox chkAttachPhoto;
    @FXML private CheckBox chkRequiresFollowUp;

    // =================================================================================================================
    // Services and Data
    // =================================================================================================================

    private final AuthenticationService authService;
    private final ShipmentService shipmentService;

    private Shipment currentShipment;
    private DeliveryPerson currentDeliveryPerson;
    private boolean incidentReported = false;

    // =================================================================================================================
    // Constructor
    // =================================================================================================================

    /**
     * Default constructor. Initializes services.
     */
    public ReportIncidentDialogController() {
        this.authService = AuthenticationService.getInstance();
        this.shipmentService = new ShipmentService();
    }

    // =================================================================================================================
    // Initialization
    // =================================================================================================================

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Logger.info("Initializing ReportIncidentDialogController");

        // Get current delivery person
        this.currentDeliveryPerson = (DeliveryPerson) authService.getCurrentPerson();

        if (currentDeliveryPerson == null) {
            DialogUtil.showError("Error", "No se pudo obtener la información del repartidor.");
            return;
        }

        // Initialize incident type options
        initializeIncidentTypes();

        // Set up character counter for description
        setupCharacterCounter();

        Logger.info("ReportIncidentDialogController initialized successfully");
    }

    /**
     * Initializes the incident type ComboBox with available options.
     */
    private void initializeIncidentTypes() {
        cmbIncidentType.getItems().addAll(
                translateIncidentType(IncidentType.INCORRECT_ADDRESS),
                translateIncidentType(IncidentType.RECIPIENT_ABSENT),
                translateIncidentType(IncidentType.DAMAGED_PACKAGE),
                translateIncidentType(IncidentType.DELAY),
                translateIncidentType(IncidentType.LOST_PACKAGE),
                translateIncidentType(IncidentType.REFUSED_DELIVERY),
                translateIncidentType(IncidentType.OTHER)
        );
    }

    /**
     * Sets up the character counter for the description text area.
     */
    private void setupCharacterCounter() {
        txtDescription.textProperty().addListener((observable, oldValue, newValue) -> {
            int length = newValue != null ? newValue.length() : 0;
            lblCharCount.setText(length + "/" + MAX_DESCRIPTION_LENGTH + " caracteres");

            // Limit to max length
            if (length > MAX_DESCRIPTION_LENGTH) {
                txtDescription.setText(newValue.substring(0, MAX_DESCRIPTION_LENGTH));
            }

            // Change color when approaching limit
            if (length > MAX_DESCRIPTION_LENGTH * 0.9) {
                lblCharCount.setStyle("-fx-text-fill: #dc3545;");
            } else {
                lblCharCount.setStyle("-fx-text-fill: #6c757d;");
            }
        });
    }

    // =================================================================================================================
    // Public Methods
    // =================================================================================================================

    /**
     * Sets the shipment for which the incident is being reported.
     * @param shipment The shipment object
     */
    public void setShipment(Shipment shipment) {
        if (shipment == null) {
            Logger.error("Cannot set null shipment in ReportIncidentDialogController");
            return;
        }

        this.currentShipment = shipment;

        // Update UI with shipment information
        lblOrderId.setText(shipment.getId());

        if (shipment.getDestination() != null) {
            lblAddress.setText(String.format("%s, %s",
                    shipment.getDestination().getCity(),
                    shipment.getDestination().getCountry()));
        } else {
            lblAddress.setText("Dirección no disponible");
        }

        Logger.info("Shipment set for incident report: " + shipment.getId());
    }

    /**
     * Returns whether an incident was successfully reported.
     * @return true if incident was reported, false otherwise
     */
    public boolean isIncidentReported() {
        return incidentReported;
    }

    // =================================================================================================================
    // Event Handlers
    // =================================================================================================================

    /**
     * Handles the submit button action to report the incident.
     */
    @FXML
    private void handleSubmit() {
        Logger.info("Submit incident report requested");

        // Validate form
        if (!validateForm()) {
            return;
        }

        try {
            // Get selected incident type
            String selectedTypeText = cmbIncidentType.getValue();
            IncidentType incidentType = parseIncidentType(selectedTypeText);

            // Create incident object
            String incidentId = IdGenerationUtil.generateId();
            Incident incident = new Incident(
                    incidentId,
                    incidentType,
                    txtDescription.getText().trim(),
                    currentDeliveryPerson.getId()
            );

            // Set incident on the shipment
            currentShipment.setIncident(incident);

            // Update shipment status to indicate incident (use RETURNED status for incidents)
            shipmentService.changeStatus(currentShipment.getId(), ShipmentStatus.RETURNED,
                    "Incidencia reportada: " + incidentType.getDescription(),
                    currentDeliveryPerson.getId());

            Logger.info("Incident reported successfully: " + incidentId);

            // Show success message
            DialogUtil.showSuccess("Incidencia Reportada",
                    "La incidencia ha sido registrada exitosamente.\n" +
                            "ID de Incidencia: " + incidentId);

            incidentReported = true;

            // Close dialog
            closeDialog();

        } catch (Exception e) {
            Logger.error("Error reporting incident: " + e.getMessage());
            DialogUtil.showError("Error",
                    "No se pudo reportar la incidencia. Por favor, inténtelo nuevamente.");
        }
    }

    /**
     * Handles the cancel button action.
     */
    @FXML
    private void handleCancel() {
        Logger.info("Incident report cancelled by user");

        // Ask for confirmation if there's text in the description
        if (txtDescription.getText() != null && !txtDescription.getText().trim().isEmpty()) {
            boolean confirmed = DialogUtil.showConfirmation(
                    "Cancelar Reporte",
                    "¿Estás seguro de que deseas cancelar? Se perderá la información ingresada."
            );

            if (!confirmed) {
                return;
            }
        }

        incidentReported = false;
        closeDialog();
    }

    // =================================================================================================================
    // Helper Methods
    // =================================================================================================================

    /**
     * Validates the form before submission.
     * @return true if form is valid, false otherwise
     */
    private boolean validateForm() {
        // Check if shipment is set
        if (currentShipment == null) {
            DialogUtil.showError("Error", "No se ha seleccionado ningún envío.");
            return false;
        }

        // Check incident type
        if (cmbIncidentType.getValue() == null || cmbIncidentType.getValue().trim().isEmpty()) {
            DialogUtil.showWarning("Campo Requerido",
                    "Por favor selecciona el tipo de incidencia.");
            cmbIncidentType.requestFocus();
            return false;
        }

        // Check description
        String description = txtDescription.getText();
        if (description == null || description.trim().isEmpty()) {
            DialogUtil.showWarning("Campo Requerido",
                    "Por favor describe la incidencia.");
            txtDescription.requestFocus();
            return false;
        }

        if (description.trim().length() < 10) {
            DialogUtil.showWarning("Descripción Insuficiente",
                    "La descripción debe tener al menos 10 caracteres.");
            txtDescription.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Translates IncidentType enum to Spanish text.
     */
    private String translateIncidentType(IncidentType type) {
        return switch (type) {
            case INCORRECT_ADDRESS -> "Dirección Incorrecta";
            case RECIPIENT_ABSENT -> "Destinatario Ausente";
            case DAMAGED_PACKAGE -> "Paquete Dañado";
            case DELAY -> "Retraso en Entrega";
            case LOST_PACKAGE -> "Paquete Perdido";
            case REFUSED_DELIVERY -> "Entrega Rechazada";
            case OTHER -> "Otro";
        };
    }

    /**
     * Parses Spanish incident type text back to IncidentType enum.
     */
    private IncidentType parseIncidentType(String text) {
        return switch (text) {
            case "Dirección Incorrecta" -> IncidentType.INCORRECT_ADDRESS;
            case "Destinatario Ausente" -> IncidentType.RECIPIENT_ABSENT;
            case "Paquete Dañado" -> IncidentType.DAMAGED_PACKAGE;
            case "Retraso en Entrega" -> IncidentType.DELAY;
            case "Paquete Perdido" -> IncidentType.LOST_PACKAGE;
            case "Entrega Rechazada" -> IncidentType.REFUSED_DELIVERY;
            default -> IncidentType.OTHER;
        };
    }

    /**
     * Closes the dialog window.
     */
    private void closeDialog() {
        Stage stage = (Stage) txtDescription.getScene().getWindow();
        stage.close();
    }
}
