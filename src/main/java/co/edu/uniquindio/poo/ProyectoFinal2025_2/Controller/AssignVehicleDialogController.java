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
 * Controller for the Assign Vehicle Dialog (AssignVehicleDialog.fxml).
 * <p>
 * This controller manages a modal dialog for assigning a vehicle to a delivery person.
 * </p>
 */
public class AssignVehicleDialogController implements Initializable {

    // =================================================================================================================
    // FXML Fields
    // =================================================================================================================

    @FXML private Label lblDeliveryPersonName;
    @FXML private ComboBox<Vehicle> cmbVehicles;

    // =================================================================================================================
    // State
    // =================================================================================================================

    private DeliveryPerson deliveryPerson;
    private Vehicle selectedVehicle;
    private boolean confirmed = false;

    // =================================================================================================================
    // Initialization
    // =================================================================================================================

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupComboBox();
        Logger.info("AssignVehicleDialogController initialized.");
    }

    /**
     * Sets up the vehicles combo box with custom cell factory.
     */
    private void setupComboBox() {
        // Custom cell factory to display vehicle info
        cmbVehicles.setCellFactory(lv -> new ListCell<Vehicle>() {
            @Override
            protected void updateItem(Vehicle item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(formatVehicle(item));
                    setStyle("-fx-font-size: 12px;");
                }
            }
        });

        // Custom button cell to show selected vehicle or placeholder
        cmbVehicles.setButtonCell(new ListCell<Vehicle>() {
            @Override
            protected void updateItem(Vehicle item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null) {
                    setText("Seleccionar vehículo...");
                    setStyle("-fx-text-fill: #999999; -fx-font-size: 13px;");
                } else {
                    setText(formatVehicle(item));
                    setStyle("-fx-text-fill: #032d4d; -fx-font-size: 13px;");
                }
            }
        });
    }

    /**
     * Formats vehicle information for display.
     *
     * @param vehicle The vehicle to format
     * @return Formatted string
     */
    private String formatVehicle(Vehicle vehicle) {
        String vehicleTypeSpanish = switch (vehicle.getType()) {
            case MOTORCYCLE -> "Motocicleta";
            case CAR -> "Automóvil";
            case VAN -> "Camioneta";
            case TRUCK -> "Camión";
        };
        return String.format("%s - %s (%.0f kg)", vehicle.getPlate(), vehicleTypeSpanish, vehicle.getCapacity());
    }

    // =================================================================================================================
    // Public Methods
    // =================================================================================================================

    /**
     * Sets the delivery person and available vehicles.
     *
     * @param deliveryPerson The delivery person to assign a vehicle to
     * @param availableVehicles List of available vehicles
     */
    public void setData(DeliveryPerson deliveryPerson, List<Vehicle> availableVehicles) {
        this.deliveryPerson = deliveryPerson;
        lblDeliveryPersonName.setText("Para: " + deliveryPerson.getName() + " " + deliveryPerson.getLastName());
        cmbVehicles.getItems().setAll(availableVehicles);
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
     * Returns the selected vehicle.
     *
     * @return The selected vehicle, or null if none selected
     */
    public Vehicle getSelectedVehicle() {
        return selectedVehicle;
    }

    // =================================================================================================================
    // Event Handlers
    // =================================================================================================================

    /**
     * Handles the assign button click.
     */
    @FXML
    private void handleAssign() {
        Vehicle selected = cmbVehicles.getValue();
        if (selected == null) {
            DialogUtil.showWarning("Advertencia", "Por favor selecciona un vehículo.");
            return;
        }

        selectedVehicle = selected;
        confirmed = true;
        closeDialog();
    }

    /**
     * Handles the cancel button click.
     */
    @FXML
    private void handleCancel() {
        confirmed = false;
        selectedVehicle = null;
        closeDialog();
    }

    /**
     * Closes the dialog window.
     */
    private void closeDialog() {
        Stage stage = (Stage) cmbVehicles.getScene().getWindow();
        stage.close();
    }
}
