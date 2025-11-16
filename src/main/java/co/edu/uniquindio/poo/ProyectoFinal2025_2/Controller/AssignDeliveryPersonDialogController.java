package co.edu.uniquindio.poo.ProyectoFinal2025_2.Controller;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.DeliveryPerson;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Vehicle;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.DialogUtil;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the Assign Delivery Person Dialog (AssignDeliveryPersonDialog.fxml).
 * <p>
 * This controller manages a modal dialog for assigning a delivery person to a vehicle.
 * </p>
 */
public class AssignDeliveryPersonDialogController implements Initializable {

    // =================================================================================================================
    // FXML Fields
    // =================================================================================================================

    @FXML private Label lblVehicleInfo;
    @FXML private ComboBox<DeliveryPerson> cmbDeliveryPersons;

    // =================================================================================================================
    // State
    // =================================================================================================================

    private Vehicle vehicle;
    private DeliveryPerson selectedDeliveryPerson;
    private boolean confirmed = false;

    // =================================================================================================================
    // Initialization
    // =================================================================================================================

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupComboBox();
        Logger.info("AssignDeliveryPersonDialogController initialized.");
    }

    /**
     * Sets up the delivery persons combo box with custom cell factory.
     */
    private void setupComboBox() {
        // Custom cell factory to display delivery person info
        cmbDeliveryPersons.setCellFactory(lv -> new ListCell<DeliveryPerson>() {
            @Override
            protected void updateItem(DeliveryPerson item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(formatDeliveryPerson(item));
                    setStyle("-fx-font-size: 12px;");
                }
            }
        });

        // Custom button cell to show selected delivery person or placeholder
        cmbDeliveryPersons.setButtonCell(new ListCell<DeliveryPerson>() {
            @Override
            protected void updateItem(DeliveryPerson item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null) {
                    setText("Seleccionar repartidor...");
                    setStyle("-fx-text-fill: #999999; -fx-font-size: 13px;");
                } else {
                    setText(formatDeliveryPerson(item));
                    setStyle("-fx-text-fill: #032d4d; -fx-font-size: 13px;");
                }
            }
        });
    }

    /**
     * Formats delivery person information for display.
     *
     * @param deliveryPerson The delivery person to format
     * @return Formatted string
     */
    private String formatDeliveryPerson(DeliveryPerson deliveryPerson) {
        String availabilityText = switch (deliveryPerson.getAvailability()) {
            case AVAILABLE -> "Disponible";
            case IN_TRANSIT -> "En Tránsito";
            case INACTIVE -> "Inactivo";
        };
        return String.format("%s %s - %s",
            deliveryPerson.getName(),
            deliveryPerson.getLastName(),
            availabilityText);
    }

    // =================================================================================================================
    // Public Methods
    // =================================================================================================================

    /**
     * Sets the vehicle and available delivery persons.
     *
     * @param vehicle The vehicle to assign a delivery person to
     * @param availableDeliveryPersons List of available delivery persons
     */
    public void setData(Vehicle vehicle, List<DeliveryPerson> availableDeliveryPersons) {
        this.vehicle = vehicle;
        String vehicleTypeSpanish = switch (vehicle.getType()) {
            case MOTORCYCLE -> "Motocicleta";
            case CAR -> "Automóvil";
            case VAN -> "Camioneta";
            case TRUCK -> "Camión";
        };
        lblVehicleInfo.setText(String.format("Vehículo: %s - %s", vehicle.getPlate(), vehicleTypeSpanish));
        cmbDeliveryPersons.getItems().setAll(availableDeliveryPersons);
    }

    /**
     * Returns whether the user confirmed the assignment.
     *
     * @return true if confirmed, false otherwise
     */
    public boolean isConfirmed() {
        return confirmed;
    }

    /**
     * Returns the selected delivery person.
     *
     * @return The selected delivery person, or null if none selected
     */
    public DeliveryPerson getSelectedDeliveryPerson() {
        return selectedDeliveryPerson;
    }

    // =================================================================================================================
    // Event Handlers
    // =================================================================================================================

    /**
     * Handles the assign button click.
     */
    @FXML
    private void handleAssign() {
        DeliveryPerson selected = cmbDeliveryPersons.getValue();
        if (selected == null) {
            DialogUtil.showWarning("Advertencia", "Por favor selecciona un repartidor.");
            return;
        }

        selectedDeliveryPerson = selected;
        confirmed = true;
        closeDialog();
    }

    /**
     * Handles the cancel button click.
     */
    @FXML
    private void handleCancel() {
        confirmed = false;
        selectedDeliveryPerson = null;
        closeDialog();
    }

    /**
     * Closes the dialog window.
     */
    private void closeDialog() {
        Stage stage = (Stage) cmbDeliveryPersons.getScene().getWindow();
        stage.close();
    }
}
