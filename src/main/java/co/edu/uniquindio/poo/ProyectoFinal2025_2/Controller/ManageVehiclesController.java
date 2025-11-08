package co.edu.uniquindio.poo.ProyectoFinal2025_2.Controller;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.VehicleType;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Vehicle;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.VehicleService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.DialogUtil;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.TabStateManager;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Controller for the Vehicle Management view (ManageVehicles.fxml).
 * <p>
 * This controller allows administrators to:
 * <ul>
 *     <li>View all vehicles in a table</li>
 *     <li>Add new vehicles</li>
 *     <li>Update vehicle availability</li>
 *     <li>Filter vehicles by type and availability</li>
 *     <li>Search vehicles by plate</li>
 * </ul>
 * </p>
 */
public class ManageVehiclesController {

    // =================================================================================================================
    // FXML Fields - Table
    // =================================================================================================================

    @FXML private TableView<Vehicle> tableVehicles;
    @FXML private TableColumn<Vehicle, String> colPlate;
    @FXML private TableColumn<Vehicle, String> colType;
    @FXML private TableColumn<Vehicle, Double> colCapacity;
    @FXML private TableColumn<Vehicle, Boolean> colAvailable;

    // =================================================================================================================
    // FXML Fields - Search and Filters
    // =================================================================================================================

    @FXML private TextField txtSearch;
    @FXML private ComboBox<VehicleType> filterType;
    @FXML private ComboBox<String> filterAvailability;

    // =================================================================================================================
    // FXML Fields - Statistics
    // =================================================================================================================

    @FXML private Label lblTotal;
    @FXML private Label lblAvailable;
    @FXML private Label lblInUse;

    // =================================================================================================================
    // Tab System Elements
    // =================================================================================================================

    @FXML private Button btnTabStats;
    @FXML private Button btnTabAddVehicle;
    @FXML private Button btnTabFilters;

    @FXML private javafx.scene.layout.HBox statsTabContent;
    @FXML private VBox addVehicleTabContent;
    @FXML private VBox filtersTabContent;

    // Collapsible Section
    @FXML private VBox collapsibleTabSection;
    @FXML private Button btnCollapseToggle;

    // View name for state persistence
    private static final String VIEW_NAME = "ManageVehicles";

    // =================================================================================================================
    // FXML Fields - Add Vehicle Form
    // =================================================================================================================

    @FXML private TextField txtPlate;
    @FXML private TextField txtCapacity;
    @FXML private ComboBox<VehicleType> cmbType;
    @FXML private CheckBox chkAvailable;
    @FXML private Button btnAddVehicle;

    // =================================================================================================================
    // Services and State
    // =================================================================================================================

    private final VehicleService vehicleService = VehicleService.getInstance();
    private ObservableList<Vehicle> vehiclesList;
    private FilteredList<Vehicle> filteredVehicles;
    private IndexController indexController;

    // =================================================================================================================
    // Initialization
    // =================================================================================================================

    /**
     * Initializes the controller. Sets up table columns, loads data, and configures filters.
     */
    @FXML
    public void initialize() {
        setupTableColumns();
        setupContextMenu();
        setupFilters();
        setupFormControls();
        loadVehicles();
        setupSearchFilter();
        updateStatistics();
        restoreViewState();
        Logger.info("ManageVehiclesController initialized.");
    }

    /**
     * Injects the IndexController reference for navigation.
     *
     * @param indexController The main IndexController instance.
     */
    public void setIndexController(IndexController indexController) {
        this.indexController = indexController;
        Logger.info("IndexController set in ManageVehiclesController.");
    }

    // =================================================================================================================
    // Setup Methods
    // =================================================================================================================

    /**
     * Sets up table columns with cell value factories.
     */
    private void setupTableColumns() {
        colPlate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPlate()));
        colType.setCellValueFactory(data -> new SimpleStringProperty(getSpanishVehicleType(data.getValue().getType())));
        colCapacity.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getCapacity()).asObject());
        colAvailable.setCellValueFactory(data -> new SimpleBooleanProperty(data.getValue().isAvailable()).asObject());

        // Format capacity column
        colCapacity.setCellFactory(column -> new TableCell<Vehicle, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                setText(String.format("%.2f kg", item));
            }
        });

        // Format availability column with badge style
        colAvailable.setCellFactory(column -> new TableCell<Vehicle, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    setGraphic(null);
                    return;
                }

                Label badge = new Label(item ? "Disponible" : "En Uso");

                // Use inline styles similar to order status badges for consistency
                String backgroundColor = item ? "#28a745" : "#dc3545"; // Green for available, red for in use
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
                setStyle(""); // Clear cell style to prevent row selection from affecting badge
            }
        });
    }

    /**
     * Sets up the context menu for right-click actions on table rows.
     */
    private void setupContextMenu() {
        tableVehicles.setRowFactory(tv -> {
            TableRow<Vehicle> row = new TableRow<>();
            ContextMenu contextMenu = new ContextMenu();

            MenuItem toggleAvailabilityItem = new MenuItem();
            toggleAvailabilityItem.textProperty().bind(javafx.beans.binding.Bindings.createStringBinding(() -> {
                Vehicle vehicle = row.getItem();
                if (vehicle != null && vehicle.isAvailable()) {
                    return "Marcar como En Uso";
                } else {
                    return "Marcar como Disponible";
                }
            }, row.itemProperty()));
            toggleAvailabilityItem.setOnAction(event -> {
                Vehicle selected = row.getItem();
                if (selected != null) {
                    handleToggleAvailabilityForVehicle(selected);
                }
            });

            contextMenu.getItems().add(toggleAvailabilityItem);

            row.contextMenuProperty().bind(
                    javafx.beans.binding.Bindings.when(row.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(contextMenu)
            );

            return row;
        });
    }

    /**
     * Toggles the availability of a specific vehicle.
     */
    private void handleToggleAvailabilityForVehicle(Vehicle vehicle) {
        boolean newAvailability = !vehicle.isAvailable();
        boolean updated = vehicleService.updateVehicleAvailability(vehicle.getPlate(), newAvailability);

        if (updated) {
            vehicle.setAvailable(newAvailability);
            tableVehicles.refresh();
            updateStatistics();
            DialogUtil.showSuccess("Disponibilidad actualizada correctamente.");
        } else {
            DialogUtil.showError("No se pudo actualizar la disponibilidad.");
        }
    }

    /**
     * Translates VehicleType enum to Spanish.
     */
    private String getSpanishVehicleType(VehicleType type) {
        if (type == null) return "";
        switch (type) {
            case MOTORCYCLE: return "Motocicleta";
            case CAR: return "Automóvil";
            case VAN: return "Camioneta";
            case TRUCK: return "Camión";
            default: return type.toString();
        }
    }

    /**
     * Sets up filter combo boxes.
     */
    private void setupFilters() {
        // Type filter with ButtonCell for Spanish display
        filterType.getItems().clear();
        filterType.getItems().add(null); // "All" option
        filterType.getItems().addAll(VehicleType.values());

        filterType.setCellFactory(lv -> new javafx.scene.control.ListCell<VehicleType>() {
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

        filterType.setValue(null);

        filterType.setButtonCell(new javafx.scene.control.ListCell<VehicleType>() {
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

        filterType.setOnAction(event -> applyFilters());

        // Availability filter with ButtonCell
        filterAvailability.getItems().clear();
        filterAvailability.getItems().addAll("Todos", "Disponible", "En Uso");
        filterAvailability.setValue("Todos");

        filterAvailability.setButtonCell(new javafx.scene.control.ListCell<String>() {
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

        filterAvailability.setOnAction(event -> applyFilters());
    }

    /**
     * Sets up form controls for adding vehicles.
     */
    private void setupFormControls() {
        cmbType.getItems().add(null); // Add null as first item for placeholder
        cmbType.getItems().addAll(VehicleType.values());

        cmbType.setCellFactory(lv -> new javafx.scene.control.ListCell<VehicleType>() {
            @Override
            protected void updateItem(VehicleType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else if (item == null) {
                    setText("Seleccionar Tipo");
                    setStyle("-fx-text-fill: #6c757d;");
                } else {
                    setText(getVehicleTypeSpanish(item));
                    setStyle("");
                }
            }
        });

        cmbType.setValue(null);

        cmbType.setButtonCell(new javafx.scene.control.ListCell<VehicleType>() {
            @Override
            protected void updateItem(VehicleType item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText("Seleccionar Tipo");
                    setStyle("-fx-text-fill: #6c757d;");
                } else {
                    setText(getVehicleTypeSpanish(item));
                    setStyle("-fx-text-fill: #495057;");
                }
            }
        });

        chkAvailable.setSelected(true);
    }

    /**
     * Returns the Spanish translation for a VehicleType enum.
     */
    private String getVehicleTypeSpanish(VehicleType type) {
        return switch (type) {
            case MOTORCYCLE -> "Motocicleta";
            case CAR -> "Automóvil";
            case VAN -> "Camioneta";
            case TRUCK -> "Camión";
        };
    }

    /**
     * Sets up the search filter listener.
     */
    private void setupSearchFilter() {
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
    }

    // =================================================================================================================
    // Data Loading Methods
    // =================================================================================================================

    /**
     * Loads all vehicles from the service.
     */
    private void loadVehicles() {
        List<Vehicle> vehicles = vehicleService.getAllVehicles();
        vehiclesList = FXCollections.observableArrayList(vehicles);
        filteredVehicles = new FilteredList<>(vehiclesList, p -> true);
        tableVehicles.setItems(filteredVehicles);
        Logger.info("Loaded " + vehicles.size() + " vehicles.");
    }

    /**
     * Updates the statistics labels.
     */
    private void updateStatistics() {
        if (vehiclesList == null) return;

        int total = vehiclesList.size();
        long available = vehiclesList.stream().filter(Vehicle::isAvailable).count();
        long inUse = total - available;

        lblTotal.setText(String.valueOf(total));
        lblAvailable.setText(String.valueOf(available));
        lblInUse.setText(String.valueOf(inUse));
    }

    /**
     * Applies search and filter criteria to the table.
     */
    private void applyFilters() {
        filteredVehicles.setPredicate(vehicle -> {
            boolean matchesType = matchesTypeFilter(vehicle);
            boolean matchesAvailability = matchesAvailabilityFilter(vehicle);
            boolean matchesSearch = matchesSearchFilter(vehicle);
            return matchesType && matchesAvailability && matchesSearch;
        });
    }

    /**
     * Checks if a vehicle matches the type filter.
     */
    private boolean matchesTypeFilter(Vehicle vehicle) {
        VehicleType selectedType = filterType.getValue();
        return selectedType == null || vehicle.getType() == selectedType;
    }

    /**
     * Checks if a vehicle matches the availability filter.
     */
    private boolean matchesAvailabilityFilter(Vehicle vehicle) {
        String selected = filterAvailability.getValue();
        if (selected == null || selected.equals("Todos")) return true;
        if (selected.equals("Disponible")) return vehicle.isAvailable();
        if (selected.equals("En Uso")) return !vehicle.isAvailable();
        return true;
    }

    /**
     * Checks if a vehicle matches the search text.
     */
    private boolean matchesSearchFilter(Vehicle vehicle) {
        String searchText = txtSearch.getText();
        if (searchText == null || searchText.trim().isEmpty()) {
            return true;
        }
        return vehicle.getPlate().toLowerCase().contains(searchText.toLowerCase());
    }

    // =================================================================================================================
    // Event Handlers
    // =================================================================================================================

    /**
     * Handles the add vehicle button click.
     */
    @FXML
    private void handleAddVehicle() {
        String plate = txtPlate.getText();
        String capacityStr = txtCapacity.getText();
        VehicleType type = cmbType.getValue();
        boolean available = chkAvailable.isSelected();

        // Validation
        if (plate == null || plate.trim().isEmpty()) {
            DialogUtil.showWarning("Advertencia", "Por favor ingresa una placa.");
            return;
        }

        if (capacityStr == null || capacityStr.trim().isEmpty()) {
            DialogUtil.showWarning("Advertencia", "Por favor ingresa la capacidad.");
            return;
        }

        if (type == null) {
            DialogUtil.showWarning("Advertencia", "Por favor selecciona un tipo de vehículo.");
            return;
        }

        double capacity;
        try {
            capacity = Double.parseDouble(capacityStr);
        } catch (NumberFormatException e) {
            DialogUtil.showError("Error", "La capacidad debe ser un número válido.");
            return;
        }

        boolean created = vehicleService.createVehicle(plate, capacity, type, available);
        if (created) {
            clearForm();
            loadVehicles();
            updateStatistics();
            DialogUtil.showSuccess("Éxito", "Vehículo agregado correctamente.");
            Logger.info("Vehicle created: " + plate);
        } else {
            DialogUtil.showError("Error", "Ya existe un vehículo con esa placa.");
        }
    }

    /**
     * Handles the refresh button click.
     */
    @FXML
    private void handleRefresh() {
        loadVehicles();
        updateStatistics();
        DialogUtil.showSuccess("Actualizado", "Datos actualizados correctamente.");
    }

    // =================================================================================================================
    // Helper Methods
    // =================================================================================================================

    /**
     * Clears the add vehicle form.
     */
    private void clearForm() {
        txtPlate.clear();
        txtCapacity.clear();
        cmbType.setValue(null);
        chkAvailable.setSelected(true);
    }

    // =================================================================================================================
    // Tab Switching Methods
    // =================================================================================================================

    /**
     * Switches to the Statistics tab.
     * Expands the section if collapsed (user interaction).
     */
    @FXML
    private void switchToStatsTab() {
        setActiveTab(btnTabStats);
        showTabContent(statsTabContent, true);
    }

    /**
     * Switches to the Add Vehicle tab.
     * Expands the section if collapsed (user interaction).
     */
    @FXML
    private void switchToAddVehicleTab() {
        setActiveTab(btnTabAddVehicle);
        showTabContent(addVehicleTabContent, true);
    }

    /**
     * Switches to the Filters tab.
     * Expands the section if collapsed (user interaction).
     */
    @FXML
    private void switchToFiltersTab() {
        setActiveTab(btnTabFilters);
        showTabContent(filtersTabContent, true);
    }

    /**
     * Sets the active tab button and removes active class from others.
     */
    private void setActiveTab(Button activeButton) {
        // Remove active class from all tab buttons
        btnTabStats.getStyleClass().remove("tab-button-active");
        btnTabAddVehicle.getStyleClass().remove("tab-button-active");
        btnTabFilters.getStyleClass().remove("tab-button-active");

        // Add active class to the selected tab
        activeButton.getStyleClass().add("tab-button-active");
    }

    /**
     * Shows the specified tab content and hides all others.
     *
     * @param contentToShow The tab content pane to display
     * @param expandIfCollapsed If true, expands the section if it's currently collapsed (for user clicks).
     *                          If false, respects the current collapsed state (for restoring view state).
     */
    private void showTabContent(javafx.scene.layout.Pane contentToShow, boolean expandIfCollapsed) {
        // If user clicked a tab and section is collapsed, expand it
        if (expandIfCollapsed) {
            boolean isExpanded = TabStateManager.isExpanded(VIEW_NAME);
            if (!isExpanded) {
                TabStateManager.setExpanded(VIEW_NAME, true);
                applyCollapseState(true);
            }
        }

        // Hide all tab contents
        statsTabContent.setVisible(false);
        statsTabContent.setManaged(false);
        addVehicleTabContent.setVisible(false);
        addVehicleTabContent.setManaged(false);
        filtersTabContent.setVisible(false);
        filtersTabContent.setManaged(false);

        // Show the selected content
        contentToShow.setVisible(true);
        contentToShow.setManaged(true);

        // Save active tab state
        String tabId = getTabId(contentToShow);
        if (tabId != null) {
            TabStateManager.setActiveTab(VIEW_NAME, tabId);
        }
    }

    /**
     * Gets the tab ID for a given content pane.
     */
    private String getTabId(javafx.scene.layout.Pane content) {
        if (content == statsTabContent) return "stats";
        if (content == addVehicleTabContent) return "addVehicle";
        if (content == filtersTabContent) return "filters";
        return null;
    }

    // =================================================================================================================
    // Collapse/Expand Methods
    // =================================================================================================================

    /**
     * Toggles the collapsed/expanded state of the tab section.
     */
    @FXML
    private void toggleCollapse() {
        boolean currentlyExpanded = TabStateManager.isExpanded(VIEW_NAME);
        boolean newExpanded = !currentlyExpanded;

        // Save the new state
        TabStateManager.setExpanded(VIEW_NAME, newExpanded);

        // Apply the state
        applyCollapseState(newExpanded);
    }

    /**
     * Applies the collapsed or expanded state to the UI.
     * Only collapses the content container, tabs remain visible.
     */
    private void applyCollapseState(boolean expanded) {
        if (expanded) {
            // Remove collapsed class and add expanded class
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
            // Remove expanded class and add collapsed class
            collapsibleTabSection.getStyleClass().removeAll("tab-section-expanded");
            if (!collapsibleTabSection.getStyleClass().contains("tab-section-collapsed")) {
                collapsibleTabSection.getStyleClass().add("tab-section-collapsed");
            }

            // Hide ONLY content container (tabs always visible)
            javafx.scene.Node contentContainer = collapsibleTabSection.lookup(".tab-content-container");
            if (contentContainer != null) {
                contentContainer.setVisible(false);
                contentContainer.setManaged(false);
            }

            btnCollapseToggle.setText("▼");
        }
    }

    /**
     * Restores the view state from saved preferences.
     */
    private void restoreViewState() {
        // Restore expanded/collapsed state
        boolean expanded = TabStateManager.isExpanded(VIEW_NAME);
        applyCollapseState(expanded);

        // Restore active tab (without auto-expanding)
        String activeTab = TabStateManager.getActiveTab(VIEW_NAME);
        javafx.scene.layout.Pane contentToRestore = switch (activeTab) {
            case "stats" -> statsTabContent;
            case "addVehicle" -> addVehicleTabContent;
            case "filters" -> filtersTabContent;
            default -> statsTabContent;
        };

        // Set active button based on restored tab
        Button buttonToActivate = switch (activeTab) {
            case "stats" -> btnTabStats;
            case "addVehicle" -> btnTabAddVehicle;
            case "filters" -> btnTabFilters;
            default -> btnTabStats;
        };

        setActiveTab(buttonToActivate);
        showTabContent(contentToRestore, false); // false = don't auto-expand
    }
}
