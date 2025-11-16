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
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.NavigationUtil;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the Delivery Person Dashboard view.
 */
public class DeliveryDashboardController implements Initializable {

    @FXML private Label lblWelcome;
    @FXML private Label lblLastUpdated;
    @FXML private Label lblAvailabilityStatus;
    @FXML private Button btnChangeStatus;
    @FXML private Label lblVehiclePlate;
    @FXML private Label lblVehicleType;
    @FXML private Label lblVehicleCapacity;
    @FXML private Button btnChangeVehicle;
    @FXML private Label lblTotalAssigned;
    @FXML private Label lblInTransit;
    @FXML private Label lblDeliveredToday;
    @FXML private Label lblPending;

    private final AuthenticationService authService;
    private final DeliveryPersonService deliveryPersonService;
    private final VehicleService vehicleService;
    private DeliveryPerson currentDeliveryPerson;
    private IndexController indexController;

    public DeliveryDashboardController() {
        this.authService = AuthenticationService.getInstance();
        this.deliveryPersonService = DeliveryPersonService.getInstance();
        this.vehicleService = VehicleService.getInstance();
    }

    public void setIndexController(IndexController indexController) {
        this.indexController = indexController;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Logger.info("Initializing DeliveryDashboardController...");
        loadDeliveryPersonData();
        loadStatistics();
        loadVehicleInfo();
        updateAvailabilityStatusDisplay();
        updateLastRefreshTime();
        Logger.info("DeliveryDashboardController initialized successfully.");
    }

    private void loadDeliveryPersonData() {
        if (authService.getCurrentPerson() instanceof DeliveryPerson deliveryPerson) {
            this.currentDeliveryPerson = deliveryPerson;
            lblWelcome.setText("Bienvenido, " + deliveryPerson.getName());
        } else {
            Logger.error("Current user is not a DeliveryPerson!");
            DialogUtil.showError("Error", "No se pudo cargar la información del repartidor.");
        }
    }

    private void loadStatistics() {
        if (currentDeliveryPerson == null || currentDeliveryPerson.getAssignedShipments() == null) {
            return;
        }

        int total = currentDeliveryPerson.getAssignedShipments().size();
        long inTransit = currentDeliveryPerson.getAssignedShipments().stream()
                .filter(s -> s.getStatus() == ShipmentStatus.IN_TRANSIT || s.getStatus() == ShipmentStatus.OUT_FOR_DELIVERY)
                .count();
        long pending = currentDeliveryPerson.getAssignedShipments().stream()
                .filter(s -> s.getStatus() == ShipmentStatus.READY_FOR_PICKUP || s.getStatus() == ShipmentStatus.PENDING_ASSIGNMENT)
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

    private void loadVehicleInfo() {
        if (currentDeliveryPerson == null) return;

        Vehicle vehicle = currentDeliveryPerson.getAssignedVehicle();
        if (vehicle != null) {
            lblVehiclePlate.setText(vehicle.getPlate());
            lblVehicleType.setText(getVehicleTypeLabel(vehicle.getType().toString()));
            lblVehicleCapacity.setText(vehicle.getCapacity() + " kg");
        } else {
            lblVehiclePlate.setText("Sin asignar");
            lblVehicleType.setText("N/A");
            lblVehicleCapacity.setText("N/A");
        }
    }

    private void updateAvailabilityStatusDisplay() {
        if (currentDeliveryPerson == null) return;

        AvailabilityStatus status = currentDeliveryPerson.getAvailability();
        String statusText = getStatusLabel(status);
        lblAvailabilityStatus.setText(statusText);

        // Clear previous styles
        lblAvailabilityStatus.getStyleClass().removeAll(
                "status-badge-available",
                "status-badge-in-transit",
                "status-badge-inactive"
        );

        // Apply new style based on status
        switch (status) {
            case AVAILABLE -> lblAvailabilityStatus.getStyleClass().add("status-badge-available");
            case IN_TRANSIT -> lblAvailabilityStatus.getStyleClass().add("status-badge-in-transit");
            case INACTIVE -> lblAvailabilityStatus.getStyleClass().add("status-badge-inactive");
        }
    }

    private boolean isDeliveredToday(Shipment shipment) {
        if (shipment.getDeliveredDate() == null) return false;
        return shipment.getDeliveredDate().toLocalDate().equals(LocalDateTime.now().toLocalDate());
    }

    private void updateLastRefreshTime() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        lblLastUpdated.setText("Última actualización: " + timestamp);
    }

    @FXML
    private void handleRefresh() {
        Logger.info("Refreshing delivery dashboard...");

        // Reload current delivery person from service
        currentDeliveryPerson = (DeliveryPerson) authService.getCurrentPerson();

        loadDeliveryPersonData();
        loadStatistics();
        loadVehicleInfo();
        updateAvailabilityStatusDisplay();
        updateLastRefreshTime();

        DialogUtil.showSuccess("Actualizado", "Dashboard actualizado correctamente.");
    }

    @FXML
    private void handleChangeStatus() {
        if (currentDeliveryPerson == null) return;

        AvailabilityStatus currentStatus = currentDeliveryPerson.getAvailability();
        AvailabilityStatus nextStatus = getNextStatus(currentStatus);

        // Update status in service
        boolean updated = deliveryPersonService.updateAvailability(currentDeliveryPerson.getId(), nextStatus);

        if (updated) {
            currentDeliveryPerson.setAvailability(nextStatus);
            updateAvailabilityStatusDisplay();
            DialogUtil.showSuccess("Estado Actualizado", "Tu estado cambió a: " + getStatusLabel(nextStatus));
        } else {
            DialogUtil.showError("Error", "No se pudo actualizar el estado.");
        }
    }

    private AvailabilityStatus getNextStatus(AvailabilityStatus current) {
        return switch (current) {
            case AVAILABLE -> AvailabilityStatus.IN_TRANSIT;
            case IN_TRANSIT -> AvailabilityStatus.INACTIVE;
            case INACTIVE -> AvailabilityStatus.AVAILABLE;
        };
    }

    @FXML
    private void handleChangeVehicle() {
        if (currentDeliveryPerson == null) return;

        // Get available vehicles for this delivery person
        List<Vehicle> availableVehicles = vehicleService.getAllVehicles()
                .stream()
                .filter(v -> v.getDeliveryPersonId().equals(currentDeliveryPerson.getId()))
                .filter(Vehicle::isAvailable)
                .toList();

        if (availableVehicles.isEmpty()) {
            DialogUtil.showWarning("Sin Vehículos", "No tienes vehículos disponibles para asignar.");
            return;
        }

        // Create dialog
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Cambiar Vehículo");
        dialog.setResizable(false);

        VBox dialogContent = new VBox(20);
        dialogContent.setPadding(new Insets(25));
        dialogContent.setStyle("-fx-background-color: white;");

        // Title
        Label title = new Label("🚗 Cambiar Vehículo Asignado");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #032d4d;");

        // Subtitle
        Label subtitle = new Label("Selecciona uno de tus vehículos disponibles:");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #6c757d;");

        // ComboBox with improved styling
        ComboBox<Vehicle> cmbVehicle = new ComboBox<>();
        cmbVehicle.getItems().addAll(availableVehicles);
        cmbVehicle.setPrefWidth(400);
        cmbVehicle.setPrefHeight(40);
        cmbVehicle.setStyle("-fx-font-size: 13px;");
        cmbVehicle.setPromptText("Selecciona un vehículo...");

        // Custom cell factory
        cmbVehicle.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Vehicle item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getPlate() + " - " + getVehicleTypeLabel(item.getType().toString()) + " (" + item.getCapacity() + " kg)");
                }
            }
        });

        cmbVehicle.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Vehicle item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null) {
                    setText("Selecciona un vehículo...");
                    setStyle("-fx-text-fill: #999999;");
                } else {
                    setText(item.getPlate() + " - " + getVehicleTypeLabel(item.getType().toString()));
                    setStyle("-fx-text-fill: #032d4d;");
                }
            }
        });

        // Buttons in horizontal layout
        javafx.scene.layout.HBox btnContainer = new javafx.scene.layout.HBox(15);
        btnContainer.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        Button btnCancel = new Button("Cancelar");
        btnCancel.getStyleClass().add("btn-secondary");
        btnCancel.setPrefWidth(120);
        btnCancel.setPrefHeight(36);
        btnCancel.setStyle("-fx-font-size: 13px;");
        btnCancel.setOnAction(e -> dialog.close());

        Button btnConfirm = new Button("Confirmar");
        btnConfirm.getStyleClass().add("btn-primary");
        btnConfirm.setPrefWidth(120);
        btnConfirm.setPrefHeight(36);
        btnConfirm.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        btnConfirm.setOnAction(e -> {
            Vehicle selected = cmbVehicle.getValue();
            if (selected == null) {
                DialogUtil.showWarning("Selección Requerida", "Por favor selecciona un vehículo.");
                return;
            }

            currentDeliveryPerson.setAssignedVehicle(selected);
            loadVehicleInfo();
            dialog.close();
            DialogUtil.showSuccess("Vehículo Actualizado", "Vehículo asignado correctamente.");
        });

        btnContainer.getChildren().addAll(btnCancel, btnConfirm);

        dialogContent.getChildren().addAll(title, subtitle, cmbVehicle, btnContainer);

        Scene scene = new Scene(dialogContent, 450, 250);
        String stylesheet = getClass().getResource("/co/edu/uniquindio/poo/proyectofinal2025_2/Style.css").toExternalForm();
        scene.getStylesheets().add(stylesheet);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    @FXML
    private void handleGoToShipments() {
        if (indexController != null) {
            NavigationUtil.navigate(indexController, "DeliveryShipments.fxml", DeliveryShipmentsController.class);
        }
    }

    @FXML
    private void handleGoToHistory() {
        if (indexController != null) {
            NavigationUtil.navigate(indexController, "DeliveryHistory.fxml", DeliveryHistoryController.class);
        }
    }

    @FXML
    private void handleGoToVehicles() {
        if (indexController != null) {
            NavigationUtil.navigate(indexController, "DeliveryMyVehicles.fxml", DeliveryMyVehiclesController.class);
        }
    }

    @FXML
    private void handleGoToProfile() {
        if (indexController != null) {
            NavigationUtil.navigate(indexController, "DeliveryProfile.fxml", DeliveryProfileController.class);
        }
    }

    private String getStatusLabel(AvailabilityStatus status) {
        return switch (status) {
            case AVAILABLE -> "DISPONIBLE";
            case IN_TRANSIT -> "EN TRÁNSITO";
            case INACTIVE -> "INACTIVO";
        };
    }

    private String getVehicleTypeLabel(String type) {
        return switch (type) {
            case "MOTORCYCLE" -> "Motocicleta";
            case "CAR" -> "Automóvil";
            case "VAN" -> "Camioneta";
            case "TRUCK" -> "Camión";
            default -> type;
        };
    }
}
