package co.edu.uniquindio.poo.ProyectoFinal2025_2.Controller;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.DeliveryPerson;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Vehicle;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.AuthenticationService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.DeliveryPersonService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.VehicleService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.DialogUtil;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the Delivery My Vehicles view (DeliveryMyVehicles.fxml).
 * <p>
 * This controller manages the view where delivery persons can see all their
 * assigned vehicles, view details, and change their active vehicle.
 * </p>
 */
public class DeliveryMyVehiclesController implements Initializable {

    // =================================================================================================================
    // FXML Fields - Summary
    // =================================================================================================================

    @FXML private Label lblTotalVehicles;
    @FXML private Label lblCurrentVehicle;

    // =================================================================================================================
    // FXML Fields - Table
    // =================================================================================================================

    @FXML private TableView<Vehicle> tblVehicles;
    @FXML private TableColumn<Vehicle, String> colPlate;
    @FXML private TableColumn<Vehicle, String> colType;
    @FXML private TableColumn<Vehicle, String> colCapacity;
    @FXML private TableColumn<Vehicle, String> colModel;
    @FXML private TableColumn<Vehicle, String> colYear;
    @FXML private TableColumn<Vehicle, String> colStatus;
    @FXML private TableColumn<Vehicle, Void> colActions;

    // =================================================================================================================
    // FXML Fields - Collapsible Section
    // =================================================================================================================

    @FXML private VBox collapsibleSection;
    @FXML private Button btnCollapseToggle;

    // =================================================================================================================
    // FXML Fields - Details Card
    // =================================================================================================================

    @FXML private VBox vehicleDetailsCard;
    @FXML private Label lblDetailPlate;
    @FXML private Label lblDetailType;
    @FXML private Label lblDetailModel;
    @FXML private Label lblDetailYear;
    @FXML private Label lblDetailCapacity;
    @FXML private Label lblDetailColor;
    @FXML private Label lblDetailStatus;
    @FXML private Label lblDetailMileage;

    // =================================================================================================================
    // Services and State
    // =================================================================================================================

    private final AuthenticationService authService = AuthenticationService.getInstance();
    private final DeliveryPersonService deliveryPersonService = DeliveryPersonService.getInstance();
    private DeliveryPerson currentDeliveryPerson;
    private Vehicle selectedVehicle;
    private boolean isCollapsed = false;

    // =================================================================================================================
    // Initialization
    // =================================================================================================================

    /**
     * Initializes the controller. Loads delivery person data and populates the table.
     *
     * @param url            The location used to resolve relative paths.
     * @param resourceBundle The resources used to localize the root object.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Logger.info("Initializing DeliveryMyVehiclesController...");

        currentDeliveryPerson = (DeliveryPerson) authService.getCurrentPerson();

        if (currentDeliveryPerson == null) {
            Logger.error("No delivery person logged in");
            DialogUtil.showError("Error", "No hay repartidor autenticado");
            return;
        }

        setupTable();
        loadVehicles();
        updateSummary();

        Logger.info("DeliveryMyVehiclesController initialized successfully.");
    }

    // =================================================================================================================
    // Setup Methods
    // =================================================================================================================

    /**
     * Sets up the table columns and cell factories.
     */
    private void setupTable() {
        // Plate column
        colPlate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPlate()));

        // Type column with Spanish translation
        colType.setCellValueFactory(data -> {
            String typeSpanish = data.getValue().getType() != null
                    ? data.getValue().getType().getDisplayName()
                    : "--";
            return new SimpleStringProperty(typeSpanish);
        });

        // Capacity column
        colCapacity.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().getCapacity())));

        // Model column - placeholder (Vehicle model doesn't have this field)
        colModel.setCellValueFactory(data -> new SimpleStringProperty("--"));

        // Year column - placeholder (Vehicle model doesn't have this field)
        colYear.setCellValueFactory(data -> new SimpleStringProperty("--"));

        // Status column
        colStatus.setCellValueFactory(data -> {
            Vehicle vehicle = data.getValue();
            boolean isCurrent = currentDeliveryPerson.getAssignedVehicle() != null &&
                    currentDeliveryPerson.getAssignedVehicle().getPlate().equals(vehicle.getPlate());
            String status = isCurrent ? "En Uso" : "Disponible";
            return new SimpleStringProperty(status);
        });

        // Actions column with buttons
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnView = new Button("Ver");
            private final Button btnUse = new Button("Usar");
            private final HBox hbox = new HBox(5, btnView, btnUse);

            {
                btnView.getStyleClass().add("btn-table-action");
                btnUse.getStyleClass().add("btn-table-action-primary");

                btnView.setOnAction(event -> {
                    Vehicle vehicle = getTableView().getItems().get(getIndex());
                    showVehicleDetails(vehicle);
                });

                btnUse.setOnAction(event -> {
                    Vehicle vehicle = getTableView().getItems().get(getIndex());
                    useVehicle(vehicle);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Vehicle vehicle = getTableView().getItems().get(getIndex());
                    boolean isCurrent = currentDeliveryPerson.getAssignedVehicle() != null &&
                            currentDeliveryPerson.getAssignedVehicle().getPlate().equals(vehicle.getPlate());

                    btnUse.setDisable(isCurrent);
                    btnUse.setText(isCurrent ? "En Uso" : "Usar");

                    setGraphic(hbox);
                }
            }
        });

        // Enable row selection
        tblVehicles.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                showVehicleDetails(newSelection);
            }
        });

        // Setup context menu
        setupContextMenu();
    }

    /**
     * Sets up context menu (right-click) for the vehicles table.
     */
    private void setupContextMenu() {
        ContextMenu contextMenu = new ContextMenu();

        // Ver Detalles
        MenuItem viewDetails = new MenuItem("Ver Detalles");
        viewDetails.setOnAction(event -> {
            Vehicle selected = tblVehicles.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showVehicleDetails(selected);
            }
        });

        // Usar Este Vehículo
        MenuItem useVehicle = new MenuItem("Usar Este Vehículo");
        useVehicle.setOnAction(event -> {
            Vehicle selected = tblVehicles.getSelectionModel().getSelectedItem();
            if (selected != null) {
                useVehicle(selected);
            }
        });

        // Ver Historial
        MenuItem viewHistory = new MenuItem("Ver Historial");
        viewHistory.setOnAction(event -> {
            Vehicle selected = tblVehicles.getSelectionModel().getSelectedItem();
            if (selected != null) {
                handleViewHistory();
            }
        });

        contextMenu.getItems().addAll(
                viewDetails,
                new SeparatorMenuItem(),
                useVehicle,
                viewHistory
        );

        // Show context menu only on rows with data
        tblVehicles.setRowFactory(tv -> {
            TableRow<Vehicle> row = new TableRow<>();
            row.setOnContextMenuRequested(event -> {
                if (!row.isEmpty()) {
                    tblVehicles.getSelectionModel().select(row.getItem());

                    // Enable/disable "Usar" option based on if it's current vehicle
                    Vehicle vehicle = row.getItem();
                    boolean isCurrent = currentDeliveryPerson.getAssignedVehicle() != null &&
                            currentDeliveryPerson.getAssignedVehicle().getPlate().equals(vehicle.getPlate());
                    useVehicle.setDisable(isCurrent);

                    contextMenu.show(row, event.getScreenX(), event.getScreenY());
                }
            });
            return row;
        });
    }

    // =================================================================================================================
    // Data Loading Methods
    // =================================================================================================================

    /**
     * Loads vehicles - currently just shows all available vehicles for selection.
     * In a full system, this would show vehicles assigned to this delivery person.
     */
    private void loadVehicles() {
        // Get all available vehicles from the system
        VehicleService vehicleService = VehicleService.getInstance();
        List<Vehicle> allVehicles = vehicleService.getAllVehicles();

        if (allVehicles != null && !allVehicles.isEmpty()) {
            ObservableList<Vehicle> vehicleList = FXCollections.observableArrayList(allVehicles);
            tblVehicles.setItems(vehicleList);
            Logger.info("Loaded " + allVehicles.size() + " vehicles");
        } else {
            tblVehicles.setItems(FXCollections.observableArrayList());
            Logger.info("No vehicles available in the system");
        }
    }

    /**
     * Updates the summary statistics.
     */
    private void updateSummary() {
        // Show total available vehicles in system
        int total = tblVehicles.getItems() != null ? tblVehicles.getItems().size() : 0;
        lblTotalVehicles.setText(String.valueOf(total));

        // Show current assigned vehicle
        Vehicle currentVehicle = currentDeliveryPerson.getAssignedVehicle();
        if (currentVehicle != null) {
            lblCurrentVehicle.setText(currentVehicle.getPlate());
        } else {
            lblCurrentVehicle.setText("Sin vehículo");
        }
    }

    // =================================================================================================================
    // Event Handlers
    // =================================================================================================================

    /**
     * Handles the refresh button click.
     */
    @FXML
    private void handleRefresh() {
        Logger.info("Refreshing vehicles list...");
        loadVehicles();
        updateSummary();
        DialogUtil.showSuccess("Actualizado", "Lista de vehículos actualizada correctamente.");
    }

    /**
     * Toggles the collapse/expand state of the table section.
     */
    @FXML
    private void toggleCollapse() {
        isCollapsed = !isCollapsed;

        if (isCollapsed) {
            collapsibleSection.setVisible(false);
            collapsibleSection.setManaged(false);
            btnCollapseToggle.setText("▼");
        } else {
            collapsibleSection.setVisible(true);
            collapsibleSection.setManaged(true);
            btnCollapseToggle.setText("▲");
        }

        Logger.info("Table section " + (isCollapsed ? "collapsed" : "expanded"));
    }

    /**
     * Shows the details card for the selected vehicle.
     *
     * @param vehicle The vehicle to show details for.
     */
    private void showVehicleDetails(Vehicle vehicle) {
        if (vehicle == null) return;

        selectedVehicle = vehicle;

        lblDetailPlate.setText(vehicle.getPlate());
        lblDetailType.setText(vehicle.getType() != null ? vehicle.getType().getDisplayName() : "--");
        lblDetailModel.setText("--"); // Vehicle model doesn't have this field
        lblDetailYear.setText("--"); // Vehicle model doesn't have this field
        lblDetailCapacity.setText(vehicle.getCapacity() + " kg");
        lblDetailColor.setText("--"); // Vehicle model doesn't have this field

        boolean isCurrent = currentDeliveryPerson.getAssignedVehicle() != null &&
                currentDeliveryPerson.getAssignedVehicle().getPlate().equals(vehicle.getPlate());
        lblDetailStatus.setText(isCurrent ? "En Uso Actualmente" : "Disponible");

        // Placeholder for mileage (can be added to Vehicle model later)
        lblDetailMileage.setText("No disponible");

        vehicleDetailsCard.setVisible(true);
        vehicleDetailsCard.setManaged(true);

        Logger.info("Showing details for vehicle: " + vehicle.getPlate());
    }

    /**
     * Handles the close details button click.
     */
    @FXML
    private void handleCloseDetails() {
        vehicleDetailsCard.setVisible(false);
        vehicleDetailsCard.setManaged(false);
        selectedVehicle = null;
        tblVehicles.getSelectionModel().clearSelection();
    }

    /**
     * Handles the use vehicle button click from details card.
     */
    @FXML
    private void handleUseVehicle() {
        if (selectedVehicle != null) {
            useVehicle(selectedVehicle);
        }
    }

    /**
     * Changes the active vehicle for the delivery person.
     *
     * @param vehicle The vehicle to set as active.
     */
    private void useVehicle(Vehicle vehicle) {
        if (vehicle == null) return;

        // Check if already using this vehicle
        if (currentDeliveryPerson.getAssignedVehicle() != null &&
                currentDeliveryPerson.getAssignedVehicle().getPlate().equals(vehicle.getPlate())) {
            DialogUtil.showInfo("Información", "Ya estás usando este vehículo.");
            return;
        }

        // Update vehicle
        currentDeliveryPerson.setAssignedVehicle(vehicle);

        // Update will happen when the session persists the delivery person
        updateSummary();
        tblVehicles.refresh();

        if (vehicleDetailsCard.isVisible()) {
            showVehicleDetails(vehicle);
        }

        DialogUtil.showSuccess("Éxito",
                "Ahora estás usando el vehículo: " + vehicle.getPlate());
        Logger.info("Changed active vehicle to: " + vehicle.getPlate());
    }

    /**
     * Handles the view history button click.
     * Placeholder for future implementation of vehicle usage history.
     */
    @FXML
    private void handleViewHistory() {
        if (selectedVehicle != null) {
            DialogUtil.showInfo("Próximamente",
                    "El historial del vehículo " + selectedVehicle.getPlate() +
                            " estará disponible próximamente.");
        }
    }
}
