package co.edu.uniquindio.poo.ProyectoFinal2025_2.Controller;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.DeliveryPerson;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.AvailabilityStatus;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.ShipmentStatus;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Shipment;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Vehicle;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.AuthenticationService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.DeliveryPersonService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.VehicleService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.DialogUtil;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * Controller for the Delivery Person Dashboard view (DeliveryDashboard.fxml).
 * <p>
 * This controller manages the delivery person's main dashboard, displaying
 * current statistics, availability status, assigned vehicle, and shipment summaries.
 * </p>
 */
public class DeliveryDashboardController implements Initializable {

    // =================================================================================================================
    // FXML Fields - Header
    // =================================================================================================================

    @FXML private Label lblWelcome;
    @FXML private Label lblLastUpdated;

    // =================================================================================================================
    // FXML Fields - Status Section
    // =================================================================================================================

    @FXML private Label lblCurrentStatus;
    @FXML private ComboBox<AvailabilityStatus> cmbAvailability;
    @FXML private Button btnUpdateStatus;

    // =================================================================================================================
    // FXML Fields - Vehicle Section
    // =================================================================================================================

    @FXML private Label lblVehiclePlate;
    @FXML private Label lblVehicleType;
    @FXML private Label lblVehicleCapacity;
    @FXML private ComboBox<Vehicle> cmbVehicle;
    @FXML private Button btnUpdateVehicle;

    // =================================================================================================================
    // FXML Fields - Statistics Cards
    // =================================================================================================================

    @FXML private Label lblTotalAssigned;
    @FXML private Label lblInTransit;
    @FXML private Label lblDeliveredToday;
    @FXML private Label lblPending;

    // =================================================================================================================
    // Services
    // =================================================================================================================

    private final AuthenticationService authService;
    private final DeliveryPersonService deliveryPersonService;
    private DeliveryPerson currentDeliveryPerson;

    // =================================================================================================================
    // Constructor
    // =================================================================================================================

    /**
     * Default constructor. Initializes required services.
     */
    public DeliveryDashboardController() {
        this.authService = AuthenticationService.getInstance();
        this.deliveryPersonService = DeliveryPersonService.getInstance();
    }

    // =================================================================================================================
    // Initialization
    // =================================================================================================================

    /**
     * Initializes the controller. Called automatically after FXML loading.
     * <p>
     * Loads the current delivery person data and populates the dashboard with statistics.
     * </p>
     *
     * @param url            The location used to resolve relative paths.
     * @param resourceBundle The resources used to localize the root object.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Logger.info("Initializing DeliveryDashboardController...");

        loadDeliveryPersonData();
        setupAvailabilityComboBox();
        setupVehicleComboBox();
        loadStatistics();
        loadVehicleInfo();
        updateLastRefreshTime();

        Logger.info("DeliveryDashboardController initialized successfully.");
    }

    // =================================================================================================================
    // Data Loading Methods
    // =================================================================================================================

    /**
     * Loads the current delivery person from the authentication service.
     */
    private void loadDeliveryPersonData() {
        if (authService.getCurrentPerson() instanceof DeliveryPerson deliveryPerson) {
            this.currentDeliveryPerson = deliveryPerson;
            lblWelcome.setText("Bienvenido, " + deliveryPerson.getName());
            lblCurrentStatus.setText("Estado: " + getStatusLabel(deliveryPerson.getAvailability()));
        } else {
            Logger.error("Current user is not a DeliveryPerson!");
            DialogUtil.showError("Error", "No se pudo cargar la información del repartidor.");
        }
    }

    /**
     * Sets up the availability combo box with possible status values and Spanish translations.
     */
    private void setupAvailabilityComboBox() {
        if (cmbAvailability != null) {
            cmbAvailability.getItems().addAll(AvailabilityStatus.values());

            // Custom cell factory to display Spanish names in the dropdown
            cmbAvailability.setCellFactory(lv -> new javafx.scene.control.ListCell<AvailabilityStatus>() {
                @Override
                protected void updateItem(AvailabilityStatus item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : getStatusLabel(item));
                }
            });

            // Custom button cell to show placeholder when empty and Spanish text when selected
            cmbAvailability.setButtonCell(new javafx.scene.control.ListCell<AvailabilityStatus>() {
                @Override
                protected void updateItem(AvailabilityStatus item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("Seleccionar Estado");
                        setStyle("-fx-text-fill: #999999;");
                    } else {
                        setText(getStatusLabel(item));
                        setStyle("-fx-text-fill: #032d4d;");
                    }
                }
            });

            if (currentDeliveryPerson != null) {
                cmbAvailability.setValue(currentDeliveryPerson.getAvailability());
            }
        }
    }

    /**
     * Sets up the vehicle combo box with available vehicles assigned to this delivery person.
     */
    private void setupVehicleComboBox() {
        if (cmbVehicle != null && currentDeliveryPerson != null) {
            // Get all available vehicles (delivery person can only have one assigned vehicle)
            // For now, we'll show all available vehicles for selection
            VehicleService vehicleService = VehicleService.getInstance();
            java.util.List<Vehicle> availableVehicles = vehicleService.getAvailableVehicles();

            if (availableVehicles != null && !availableVehicles.isEmpty()) {
                cmbVehicle.getItems().addAll(availableVehicles);

                // Custom cell factory to display vehicle info
                cmbVehicle.setCellFactory(lv -> new javafx.scene.control.ListCell<Vehicle>() {
                    @Override
                    protected void updateItem(Vehicle item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(item.getPlate() + " - " + item.getType().getDisplayName());
                        }
                    }
                });

                // Custom button cell to show placeholder when empty
                cmbVehicle.setButtonCell(new javafx.scene.control.ListCell<Vehicle>() {
                    @Override
                    protected void updateItem(Vehicle item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText("Seleccionar Vehículo");
                            setStyle("-fx-text-fill: #999999;");
                        } else {
                            setText(item.getPlate() + " - " + item.getType().getDisplayName());
                            setStyle("-fx-text-fill: #032d4d;");
                        }
                    }
                });

                // Set current vehicle if assigned
                Vehicle currentVehicle = currentDeliveryPerson.getAssignedVehicle();
                if (currentVehicle != null) {
                    cmbVehicle.setValue(currentVehicle);
                }
            } else {
                cmbVehicle.setDisable(true);
                cmbVehicle.setPromptText("Sin vehículos asignados");
            }
        }
    }

    /**
     * Loads and displays statistics related to assigned shipments.
     */
    private void loadStatistics() {
        if (currentDeliveryPerson == null || currentDeliveryPerson.getAssignedShipments() == null) {
            return;
        }

        int total = currentDeliveryPerson.getAssignedShipments().size();
        long inTransit = currentDeliveryPerson.getAssignedShipments().stream()
                .filter(s -> s.getStatus() == ShipmentStatus.IN_TRANSIT)
                .count();
        long pending = currentDeliveryPerson.getAssignedShipments().stream()
                .filter(s -> s.getStatus() == ShipmentStatus.READY_FOR_PICKUP)
                .count();
        long deliveredToday = currentDeliveryPerson.getAssignedShipments().stream()
                .filter(s -> s.getStatus() == ShipmentStatus.DELIVERED)
                .filter(this::isDeliveredToday)
                .count();

        lblTotalAssigned.setText(String.valueOf(total));
        lblInTransit.setText(String.valueOf(inTransit));
        lblPending.setText(String.valueOf(pending));
        lblDeliveredToday.setText(String.valueOf(deliveredToday));
    }

    /**
     * Loads and displays information about the assigned vehicle.
     */
    private void loadVehicleInfo() {
        if (currentDeliveryPerson == null) return;

        Vehicle vehicle = currentDeliveryPerson.getAssignedVehicle();
        if (vehicle != null) {
            lblVehiclePlate.setText(vehicle.getPlate());
            lblVehicleType.setText(vehicle.getType().toString());
            lblVehicleCapacity.setText(vehicle.getCapacity() + " kg");
        } else {
            lblVehiclePlate.setText("Sin asignar");
            lblVehicleType.setText("N/A");
            lblVehicleCapacity.setText("N/A");
        }
    }

    /**
     * Checks if a shipment was delivered today.
     *
     * @param shipment The shipment to check.
     * @return true if delivered today, false otherwise.
     */
    private boolean isDeliveredToday(Shipment shipment) {
        if (shipment.getDeliveredDate() == null) return false;
        return shipment.getDeliveredDate().toLocalDate().equals(LocalDateTime.now().toLocalDate());
    }

    /**
     * Updates the last refresh timestamp label.
     */
    private void updateLastRefreshTime() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        lblLastUpdated.setText("Última actualización: " + timestamp);
    }

    // =================================================================================================================
    // Event Handlers
    // =================================================================================================================

    /**
     * Handles the refresh button click. Reloads all dashboard data.
     */
    @FXML
    private void handleRefresh() {
        Logger.info("Refreshing delivery dashboard...");
        loadDeliveryPersonData();
        loadStatistics();
        loadVehicleInfo();
        updateLastRefreshTime();
        DialogUtil.showSuccess("Actualizado", "Dashboard actualizado correctamente.");
    }

    /**
     * Handles the update status button click.
     * Updates the delivery person's availability status.
     */
    @FXML
    private void handleUpdateStatus() {
        if (cmbAvailability == null || cmbAvailability.getValue() == null) {
            DialogUtil.showWarning("Advertencia", "Por favor selecciona un estado.");
            return;
        }

        AvailabilityStatus newStatus = cmbAvailability.getValue();
        boolean updated = deliveryPersonService.updateAvailability(
                currentDeliveryPerson.getId(),
                newStatus
        );

        if (updated) {
            currentDeliveryPerson.setAvailability(newStatus);
            lblCurrentStatus.setText("Estado: " + getStatusLabel(newStatus));
            DialogUtil.showSuccess("Éxito", "Estado actualizado correctamente.");
            Logger.info("Availability updated to: " + newStatus);
        } else {
            DialogUtil.showError("Error", "No se pudo actualizar el estado.");
        }
    }

    /**
     * Handles the update vehicle button click.
     * Changes the currently assigned vehicle for this delivery person.
     */
    @FXML
    private void handleUpdateVehicle() {
        if (cmbVehicle == null || cmbVehicle.getValue() == null) {
            DialogUtil.showWarning("Advertencia", "Por favor selecciona un vehículo.");
            return;
        }

        Vehicle newVehicle = cmbVehicle.getValue();

        // Update the assigned vehicle
        currentDeliveryPerson.setAssignedVehicle(newVehicle);

        // Update will happen when the session persists the delivery person
        loadVehicleInfo();
        DialogUtil.showSuccess("Éxito", "Vehículo actualizado correctamente.");
        Logger.info("Vehicle updated to: " + newVehicle.getPlate());
    }

    // =================================================================================================================
    // Helper Methods
    // =================================================================================================================

    /**
     * Converts an AvailabilityStatus enum to a user-friendly Spanish label.
     *
     * @param status The availability status.
     * @return A formatted string representation.
     */
    private String getStatusLabel(AvailabilityStatus status) {
        if (status == null) return "Desconocido";
        return switch (status) {
            case AVAILABLE -> "Disponible";
            case IN_TRANSIT -> "En Tránsito";
            case INACTIVE -> "Inactivo";
            default -> "Desconocido";
        };
    }
}
