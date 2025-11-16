package co.edu.uniquindio.poo.ProyectoFinal2025_2.Controller;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.DeliveryPerson;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.VehicleType;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Vehicle;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.AuthenticationService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.DeliveryPersonService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.VehicleService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.DialogUtil;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.TabStateManager;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the Delivery My Vehicles view (DeliveryMyVehicles.fxml).
 * <p>
 * This controller manages the view where delivery persons can see all their
 * assigned vehicles, add new vehicles, view details, and manage their vehicle fleet.
 * </p>
 */
public class DeliveryMyVehiclesController implements Initializable {

    // =================================================================================================================
    // FXML Fields - Tab System
    // =================================================================================================================

    @FXML private VBox collapsibleTabSection;
    @FXML private Button btnCollapseToggle;
    @FXML private Button btnTabStats;
    @FXML private Button btnTabFilters;
    @FXML private Button btnTabAddVehicle;

    @FXML private HBox statsTabContent;
    @FXML private VBox filtersTabContent;
    @FXML private VBox addVehicleTabContent;

    // =================================================================================================================
    // FXML Fields - Stats
    // =================================================================================================================

    @FXML private Label lblTotalVehicles;
    @FXML private Label lblCurrentVehicle;
    @FXML private Label lblAvailableVehicles;

    // =================================================================================================================
    // FXML Fields - Filters
    // =================================================================================================================

    @FXML private ComboBox<VehicleType> filterType;
    @FXML private ComboBox<String> filterStatus;
    @FXML private TextField searchField;

    // =================================================================================================================
    // FXML Fields - Add Vehicle Form
    // =================================================================================================================

    @FXML private TextField txtPlate;
    @FXML private TextField txtCapacity;
    @FXML private ComboBox<VehicleType> cmbType;
    @FXML private Button btnSaveVehicle;

    // =================================================================================================================
    // FXML Fields - Table
    // =================================================================================================================

    @FXML private TableView<Vehicle> tblVehicles;
    @FXML private TableColumn<Vehicle, String> colPlate;
    @FXML private TableColumn<Vehicle, String> colType;
    @FXML private TableColumn<Vehicle, String> colCapacity;
    @FXML private TableColumn<Vehicle, String> colStatus;

    // =================================================================================================================
    // Services and State
    // =================================================================================================================

    private final AuthenticationService authService = AuthenticationService.getInstance();
    private final DeliveryPersonService deliveryPersonService = DeliveryPersonService.getInstance();
    private final VehicleService vehicleService = VehicleService.getInstance();

    private DeliveryPerson currentDeliveryPerson;
    private ObservableList<Vehicle> vehiclesData;
    private FilteredList<Vehicle> filteredVehicles;
    private Vehicle selectedVehicleForEdit = null;

    private static final String VIEW_NAME = "DeliveryMyVehicles";

    // =================================================================================================================
    // Initialization
    // =================================================================================================================

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
        setupFilters();
        setupAddVehicleForm();
        loadVehicles();
        updateStatistics();
        restoreViewState();

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
                    ? getVehicleTypeSpanish(data.getValue().getType())
                    : "--";
            return new SimpleStringProperty(typeSpanish);
        });

        // Capacity column
        colCapacity.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getCapacity() + " kg"));

        // Status column with styled badges
        colStatus.setCellValueFactory(data -> {
            Vehicle vehicle = data.getValue();
            boolean isCurrent = currentDeliveryPerson.getAssignedVehicle() != null &&
                    currentDeliveryPerson.getAssignedVehicle().getPlate().equals(vehicle.getPlate());
            String status = isCurrent ? "En Uso" : "Disponible";
            return new SimpleStringProperty(status);
        });

        // Apply styled cell factory for status column (similar to order status badges)
        colStatus.setCellFactory(column -> new TableCell<Vehicle, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    // Create styled label for status
                    Label badge = new Label(item);
                    String backgroundColor = item.equals("En Uso") ? "#28a745" : "#17a2b8";
                    badge.setStyle(
                        "-fx-background-color: " + backgroundColor + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 5 10 5 10;" +
                        "-fx-background-radius: 12;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 11px;"
                    );

                    setText(null);
                    setGraphic(badge);
                }
            }
        });

        // Setup context menu
        setupContextMenu();
    }

    /**
     * Sets up context menu (right-click) for the vehicles table.
     */
    private void setupContextMenu() {
        tblVehicles.setRowFactory(tv -> {
            TableRow<Vehicle> row = new TableRow<>();
            ContextMenu contextMenu = new ContextMenu();

            // Ver Detalles
            MenuItem viewDetails = new MenuItem("Ver Detalles");
            viewDetails.setOnAction(event -> {
                Vehicle selected = row.getItem();
                if (selected != null) {
                    showVehicleDetailsDialog(selected);
                }
            });

            // Editar
            MenuItem edit = new MenuItem("Editar");
            edit.setOnAction(event -> {
                Vehicle selected = row.getItem();
                if (selected != null) {
                    editVehicle(selected);
                }
            });

            // Eliminar
            MenuItem delete = new MenuItem("Eliminar");
            delete.setOnAction(event -> {
                Vehicle selected = row.getItem();
                if (selected != null) {
                    deleteVehicle(selected);
                }
            });

            contextMenu.getItems().addAll(viewDetails, new SeparatorMenuItem(), edit, delete);

            row.contextMenuProperty().bind(
                javafx.beans.binding.Bindings.when(row.emptyProperty())
                    .then((ContextMenu) null)
                    .otherwise(contextMenu)
            );

            return row;
        });
    }

    /**
     * Sets up filter combo boxes and search field.
     */
    private void setupFilters() {
        // Type filter
        filterType.getItems().clear();
        filterType.getItems().add(null);
        filterType.getItems().addAll(VehicleType.values());

        filterType.setCellFactory(lv -> new ListCell<VehicleType>() {
            @Override
            protected void updateItem(VehicleType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else if (item == null) {
                    setText("Todos los Tipos");
                } else {
                    setText(getVehicleTypeSpanish(item));
                }
            }
        });

        filterType.setButtonCell(new ListCell<VehicleType>() {
            @Override
            protected void updateItem(VehicleType item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText("Todos los Tipos");
                    setStyle("-fx-text-fill: #6c757d;");
                } else {
                    setText(getVehicleTypeSpanish(item));
                    setStyle("-fx-text-fill: #495057;");
                }
            }
        });

        filterType.setValue(null);
        filterType.setOnAction(event -> applyFilters());

        // Status filter
        filterStatus.getItems().clear();
        filterStatus.getItems().addAll("Todos", "En Uso", "Disponible");
        filterStatus.setValue("Todos");

        filterStatus.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText("Todos");
                    setStyle("-fx-text-fill: #6c757d;");
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: #495057;");
                }
            }
        });

        filterStatus.setOnAction(event -> applyFilters());

        // Search field
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        }
    }

    /**
     * Sets up the add vehicle form controls.
     */
    private void setupAddVehicleForm() {
        cmbType.getItems().clear();
        cmbType.getItems().add(null);
        cmbType.getItems().addAll(VehicleType.values());

        cmbType.setCellFactory(lv -> new ListCell<VehicleType>() {
            @Override
            protected void updateItem(VehicleType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else if (item == null) {
                    setText("Seleccione un tipo");
                } else {
                    setText(getVehicleTypeSpanish(item));
                }
            }
        });

        cmbType.setButtonCell(new ListCell<VehicleType>() {
            @Override
            protected void updateItem(VehicleType item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText("Seleccione un tipo");
                    setStyle("-fx-text-fill: #6c757d;");
                } else {
                    setText(getVehicleTypeSpanish(item));
                    setStyle("-fx-text-fill: #495057;");
                }
            }
        });
    }

    // =================================================================================================================
    // Data Loading Methods
    // =================================================================================================================

    /**
     * Loads vehicles from the service.
     */
    private void loadVehicles() {
        List<Vehicle> allVehicles = vehicleService.getAllVehicles();

        if (allVehicles != null && !allVehicles.isEmpty()) {
            vehiclesData = FXCollections.observableArrayList(allVehicles);
            filteredVehicles = new FilteredList<>(vehiclesData, p -> true);
            tblVehicles.setItems(filteredVehicles);
            Logger.info("Loaded " + allVehicles.size() + " vehicles");
        } else {
            vehiclesData = FXCollections.observableArrayList();
            filteredVehicles = new FilteredList<>(vehiclesData, p -> true);
            tblVehicles.setItems(filteredVehicles);
            Logger.info("No vehicles available in the system");
        }
    }

    /**
     * Updates the summary statistics.
     */
    private void updateStatistics() {
        int total = vehiclesData != null ? vehiclesData.size() : 0;
        lblTotalVehicles.setText(String.valueOf(total));

        // Show current assigned vehicle
        Vehicle currentVehicle = currentDeliveryPerson.getAssignedVehicle();
        if (currentVehicle != null) {
            lblCurrentVehicle.setText(currentVehicle.getPlate());
        } else {
            lblCurrentVehicle.setText("Sin vehículo");
        }

        // Count available vehicles (not currently in use)
        long available = vehiclesData.stream()
            .filter(v -> currentDeliveryPerson.getAssignedVehicle() == null ||
                        !v.getPlate().equals(currentDeliveryPerson.getAssignedVehicle().getPlate()))
            .count();
        lblAvailableVehicles.setText(String.valueOf(available));
    }

    /**
     * Applies the current filter settings to the table.
     */
    private void applyFilters() {
        if (filteredVehicles == null) return;

        filteredVehicles.setPredicate(vehicle -> {
            // Type filter
            if (filterType.getValue() != null && vehicle.getType() != filterType.getValue()) {
                return false;
            }

            // Status filter
            boolean isCurrent = currentDeliveryPerson.getAssignedVehicle() != null &&
                    currentDeliveryPerson.getAssignedVehicle().getPlate().equals(vehicle.getPlate());
            String status = isCurrent ? "En Uso" : "Disponible";

            if (!filterStatus.getValue().equals("Todos") && !status.equals(filterStatus.getValue())) {
                return false;
            }

            // Search filter (plate)
            if (searchField != null && searchField.getText() != null && !searchField.getText().trim().isEmpty()) {
                String search = searchField.getText().toLowerCase();
                if (!vehicle.getPlate().toLowerCase().contains(search)) {
                    return false;
                }
            }

            return true;
        });
    }

    // =================================================================================================================
    // Event Handlers - Tab System
    // =================================================================================================================

    @FXML
    private void switchToStatsTab() {
        setActiveTab(btnTabStats);
        showTabContent(statsTabContent, true);
    }

    @FXML
    private void switchToFiltersTab() {
        setActiveTab(btnTabFilters);
        showTabContent(filtersTabContent, true);
    }

    @FXML
    private void switchToAddVehicleTab() {
        setActiveTab(btnTabAddVehicle);
        showTabContent(addVehicleTabContent, true);
    }

    private void setActiveTab(Button activeButton) {
        btnTabStats.getStyleClass().remove("tab-button-active");
        btnTabFilters.getStyleClass().remove("tab-button-active");
        btnTabAddVehicle.getStyleClass().remove("tab-button-active");

        activeButton.getStyleClass().add("tab-button-active");
    }

    private void showTabContent(javafx.scene.Node contentToShow, boolean shouldExpand) {
        // Hide all content panes
        statsTabContent.setVisible(false);
        statsTabContent.setManaged(false);
        filtersTabContent.setVisible(false);
        filtersTabContent.setManaged(false);
        addVehicleTabContent.setVisible(false);
        addVehicleTabContent.setManaged(false);

        // Show selected content
        contentToShow.setVisible(true);
        contentToShow.setManaged(true);

        // If user clicked a tab while collapsed, expand automatically
        if (shouldExpand && !TabStateManager.isExpanded(VIEW_NAME)) {
            TabStateManager.setExpanded(VIEW_NAME, true);
            applyCollapseState(true);
        }

        // Save active tab
        if (contentToShow == statsTabContent) {
            TabStateManager.setActiveTab(VIEW_NAME, "stats");
        } else if (contentToShow == filtersTabContent) {
            TabStateManager.setActiveTab(VIEW_NAME, "filters");
        } else if (contentToShow == addVehicleTabContent) {
            TabStateManager.setActiveTab(VIEW_NAME, "addVehicle");
        }
    }

    @FXML
    private void toggleCollapse() {
        boolean currentlyExpanded = TabStateManager.isExpanded(VIEW_NAME);
        boolean newExpanded = !currentlyExpanded;

        TabStateManager.setExpanded(VIEW_NAME, newExpanded);
        applyCollapseState(newExpanded);
    }

    private void applyCollapseState(boolean expanded) {
        if (expanded) {
            collapsibleTabSection.getStyleClass().removeAll("tab-section-collapsed");
            if (!collapsibleTabSection.getStyleClass().contains("tab-section-expanded")) {
                collapsibleTabSection.getStyleClass().add("tab-section-expanded");
            }

            // Make ONLY content container visible (tabs always visible)
            javafx.scene.Node contentContainer = collapsibleTabSection.lookup(".tab-content-container");
            if (contentContainer != null) {
                contentContainer.setVisible(true);
                contentContainer.setManaged(true);
            }

            btnCollapseToggle.setText("▲");
        } else {
            collapsibleTabSection.getStyleClass().removeAll("tab-section-expanded");
            if (!collapsibleTabSection.getStyleClass().contains("tab-section-collapsed")) {
                collapsibleTabSection.getStyleClass().add("tab-section-collapsed");
            }

            // Hide content container
            javafx.scene.Node contentContainer = collapsibleTabSection.lookup(".tab-content-container");
            if (contentContainer != null) {
                contentContainer.setVisible(false);
                contentContainer.setManaged(false);
            }

            btnCollapseToggle.setText("▼");
        }
    }

    private void restoreViewState() {
        // Restore collapse state
        boolean expanded = TabStateManager.isExpanded(VIEW_NAME);
        applyCollapseState(expanded);

        // Restore active tab
        String activeTab = TabStateManager.getActiveTab(VIEW_NAME);
        if (activeTab != null) {
            switch (activeTab) {
                case "stats":
                    setActiveTab(btnTabStats);
                    showTabContent(statsTabContent, false);
                    break;
                case "filters":
                    setActiveTab(btnTabFilters);
                    showTabContent(filtersTabContent, false);
                    break;
                case "addVehicle":
                    setActiveTab(btnTabAddVehicle);
                    showTabContent(addVehicleTabContent, false);
                    break;
                default:
                    setActiveTab(btnTabStats);
                    showTabContent(statsTabContent, false);
                    break;
            }
        } else {
            setActiveTab(btnTabStats);
            showTabContent(statsTabContent, false);
        }
    }

    // =================================================================================================================
    // Event Handlers - Actions
    // =================================================================================================================

    @FXML
    private void handleRefresh() {
        Logger.info("Refreshing vehicles list...");
        loadVehicles();
        updateStatistics();
        DialogUtil.showSuccess("Actualizado", "Lista de vehículos actualizada correctamente.");
    }

    @FXML
    private void clearFilters() {
        filterType.setValue(null);
        filterStatus.setValue("Todos");
        if (searchField != null) {
            searchField.clear();
        }
        applyFilters();
    }

    @FXML
    private void handleSaveVehicle() {
        try {
            // Validate inputs
            String plate = txtPlate.getText();
            String capacityStr = txtCapacity.getText();
            VehicleType type = cmbType.getValue();

            if (plate == null || plate.trim().isEmpty()) {
                DialogUtil.showWarning("Validación", "Por favor ingrese la placa del vehículo.");
                return;
            }

            if (capacityStr == null || capacityStr.trim().isEmpty()) {
                DialogUtil.showWarning("Validación", "Por favor ingrese la capacidad del vehículo.");
                return;
            }

            if (type == null) {
                DialogUtil.showWarning("Validación", "Por favor seleccione el tipo de vehículo.");
                return;
            }

            double capacity;
            try {
                capacity = Double.parseDouble(capacityStr);
                if (capacity <= 0) {
                    DialogUtil.showWarning("Validación", "La capacidad debe ser un número positivo.");
                    return;
                }
            } catch (NumberFormatException e) {
                DialogUtil.showWarning("Validación", "La capacidad debe ser un número válido.");
                return;
            }

            if (selectedVehicleForEdit != null) {
                // Edit mode
                selectedVehicleForEdit.setPlate(plate);
                selectedVehicleForEdit.setCapacity(capacity);
                selectedVehicleForEdit.setType(type);

                vehicleService.updateVehicle(selectedVehicleForEdit);
                DialogUtil.showSuccess("Éxito", "Vehículo actualizado correctamente.");

                selectedVehicleForEdit = null;
                btnSaveVehicle.setText("Guardar Vehículo");
            } else {
                // Add mode - create vehicle for current delivery person
                boolean created = vehicleService.createVehicleForDeliveryPerson(
                    plate,
                    capacity,
                    type,
                    true, // available by default
                    currentDeliveryPerson.getId()
                );

                if (created) {
                    DialogUtil.showSuccess("Éxito", "Vehículo agregado correctamente.");
                } else {
                    DialogUtil.showError("Error", "No se pudo agregar el vehículo. La placa puede estar duplicada.");
                    return;
                }
            }

            // Clear form
            clearAddVehicleForm();

            // Reload table
            loadVehicles();
            updateStatistics();

        } catch (Exception e) {
            Logger.error("Error saving vehicle: " + e.getMessage());
            DialogUtil.showError("Error", "No se pudo guardar el vehículo: " + e.getMessage());
        }
    }

    private void clearAddVehicleForm() {
        txtPlate.clear();
        txtCapacity.clear();
        cmbType.setValue(null);
        selectedVehicleForEdit = null;
        btnSaveVehicle.setText("Guardar Vehículo");
    }

    private void showVehicleDetailsDialog(Vehicle vehicle) {
        try {
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Detalles del Vehículo");
            dialog.setHeaderText("Información completa del vehículo");

            // Create content
            GridPane grid = new GridPane();
            grid.setHgap(20);
            grid.setVgap(15);
            grid.setPadding(new Insets(20));

            int row = 0;

            // Placa
            Label lblPlacaTitle = new Label("Placa:");
            lblPlacaTitle.setStyle("-fx-font-weight: bold;");
            Label lblPlacaValue = new Label(vehicle.getPlate());
            grid.add(lblPlacaTitle, 0, row);
            grid.add(lblPlacaValue, 1, row++);

            // Tipo
            Label lblTipoTitle = new Label("Tipo:");
            lblTipoTitle.setStyle("-fx-font-weight: bold;");
            Label lblTipoValue = new Label(getVehicleTypeSpanish(vehicle.getType()));
            grid.add(lblTipoTitle, 0, row);
            grid.add(lblTipoValue, 1, row++);

            // Capacidad
            Label lblCapacidadTitle = new Label("Capacidad:");
            lblCapacidadTitle.setStyle("-fx-font-weight: bold;");
            Label lblCapacidadValue = new Label(vehicle.getCapacity() + " kg");
            grid.add(lblCapacidadTitle, 0, row);
            grid.add(lblCapacidadValue, 1, row++);

            // Estado
            Label lblEstadoTitle = new Label("Estado:");
            lblEstadoTitle.setStyle("-fx-font-weight: bold;");
            boolean isCurrent = currentDeliveryPerson.getAssignedVehicle() != null &&
                    currentDeliveryPerson.getAssignedVehicle().getPlate().equals(vehicle.getPlate());
            Label lblEstadoValue = new Label(isCurrent ? "En Uso Actualmente" : "Disponible");
            grid.add(lblEstadoTitle, 0, row);
            grid.add(lblEstadoValue, 1, row++);

            dialog.getDialogPane().setContent(grid);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

            // Style
            dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/co/edu/uniquindio/poo/ProyectoFinal2025_2/Style.css").toExternalForm()
            );

            dialog.showAndWait();

        } catch (Exception e) {
            Logger.error("Error showing vehicle details: " + e.getMessage());
            DialogUtil.showError("Error", "No se pudieron mostrar los detalles del vehículo.");
        }
    }

    private void editVehicle(Vehicle vehicle) {
        // Switch to add vehicle tab
        switchToAddVehicleTab();

        // Populate form with vehicle data
        selectedVehicleForEdit = vehicle;
        txtPlate.setText(vehicle.getPlate());
        txtCapacity.setText(String.valueOf(vehicle.getCapacity()));
        cmbType.setValue(vehicle.getType());
        btnSaveVehicle.setText("Actualizar Vehículo");
    }

    private void deleteVehicle(Vehicle vehicle) {
        boolean confirmed = DialogUtil.showConfirmation(
            "Confirmar Eliminación",
            "¿Está seguro que desea eliminar el vehículo con placa " + vehicle.getPlate() + "?"
        );

        if (!confirmed) return;

        try {
            vehicleService.deleteVehicle(vehicle.getPlate());
            DialogUtil.showSuccess("Éxito", "Vehículo eliminado correctamente.");
            loadVehicles();
            updateStatistics();
        } catch (Exception e) {
            Logger.error("Error deleting vehicle: " + e.getMessage());
            DialogUtil.showError("Error", "No se pudo eliminar el vehículo: " + e.getMessage());
        }
    }

    // =================================================================================================================
    // Helper Methods
    // =================================================================================================================

    private String getVehicleTypeSpanish(VehicleType type) {
        if (type == null) return "";
        return switch (type) {
            case MOTORCYCLE -> "Motocicleta";
            case CAR -> "Automóvil";
            case VAN -> "Camioneta";
            case TRUCK -> "Camión";
            default -> type.toString();
        };
    }
}
