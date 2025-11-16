package co.edu.uniquindio.poo.ProyectoFinal2025_2.Controller;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.DeliveryPerson;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.AvailabilityStatus;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.CoverageArea;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.PersonType;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Vehicle;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.dto.PersonCreationData;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.DeliveryPersonService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.VehicleService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.DialogUtil;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.TabStateManager;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for the Delivery Persons Management view (ManageDeliveryPersons.fxml).
 * <p>
 * This controller allows administrators to:
 * <ul>
 *     <li>View all delivery persons in a table</li>
 *     <li>Search and filter delivery persons</li>
 *     <li>Change availability status</li>
 *     <li>Assign/unassign vehicles</li>
 *     <li>View assigned shipments</li>
 * </ul>
 * </p>
 */
public class ManageDeliveryPersonsController {

    // =================================================================================================================
    // FXML Fields - Table
    // =================================================================================================================

    @FXML private TableView<DeliveryPerson> tableDeliveryPersons;
    @FXML private TableColumn<DeliveryPerson, String> colName;
    @FXML private TableColumn<DeliveryPerson, String> colEmail;
    @FXML private TableColumn<DeliveryPerson, String> colPhone;
    @FXML private TableColumn<DeliveryPerson, String> colDocumentId;
    @FXML private TableColumn<DeliveryPerson, String> colAvailability;
    @FXML private TableColumn<DeliveryPerson, String> colCoverageArea;
    @FXML private TableColumn<DeliveryPerson, String> colVehicle;
    @FXML private TableColumn<DeliveryPerson, Integer> colShipments;

    // =================================================================================================================
    // FXML Fields - Search and Filters
    // =================================================================================================================

    @FXML private TextField txtSearch;
    @FXML private ComboBox<AvailabilityStatus> filterAvailability;

    // =================================================================================================================
    // FXML Fields - Statistics
    // =================================================================================================================

    @FXML private Label lblTotal;
    @FXML private Label lblAvailable;
    @FXML private Label lblInTransit;
    @FXML private Label lblUnavailable;

    // =================================================================================================================
    // Tab System Elements
    // =================================================================================================================

    @FXML private Button btnTabStats;
    @FXML private Button btnTabFilters;

    @FXML private javafx.scene.layout.HBox statsTabContent;
    @FXML private VBox filtersTabContent;

    // Collapsible Section
    @FXML private VBox collapsibleTabSection;
    @FXML private Button btnCollapseToggle;

    // View name for state persistence
    private static final String VIEW_NAME = "ManageDeliveryPersons";

    // =================================================================================================================
    // FXML Fields - Add Delivery Person Form
    // =================================================================================================================

    @FXML private TextField txtName;
    @FXML private TextField txtLastName;
    @FXML private TextField txtEmail;
    @FXML private TextField txtPhone;
    @FXML private TextField txtPassword;
    @FXML private TextField txtDocumentId;
    @FXML private ComboBox<CoverageArea> cmbCoverageArea;
    @FXML private ComboBox<AvailabilityStatus> cmbInitialStatus;
    @FXML private Button btnAddDeliveryPerson;

    // =================================================================================================================
    // Services and State
    // =================================================================================================================

    private final DeliveryPersonService deliveryPersonService = DeliveryPersonService.getInstance();
    private final VehicleService vehicleService = VehicleService.getInstance();
    private final co.edu.uniquindio.poo.ProyectoFinal2025_2.Repositories.DeliveryPersonRepository deliveryPersonRepository = co.edu.uniquindio.poo.ProyectoFinal2025_2.Repositories.DeliveryPersonRepository.getInstance();
    private ObservableList<DeliveryPerson> deliveryPersonsList;
    private FilteredList<DeliveryPerson> filteredDeliveryPersons;
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
        loadDeliveryPersons();
        setupSearchFilter();
        updateStatistics();
        restoreViewState();
        Logger.info("ManageDeliveryPersonsController initialized.");
    }


    /**
     * Injects the IndexController reference for navigation.
     *
     * @param indexController The main IndexController instance.
     */
    public void setIndexController(IndexController indexController) {
        this.indexController = indexController;
        Logger.info("IndexController set in ManageDeliveryPersonsController.");
    }

    // =================================================================================================================
    // Setup Methods
    // =================================================================================================================

    /**
     * Sets up the context menu for right-click actions on table rows.
     */
    private void setupContextMenu() {
        tableDeliveryPersons.setRowFactory(tv -> {
            TableRow<DeliveryPerson> row = new TableRow<>();
            ContextMenu contextMenu = new ContextMenu();

            // Cambiar Estado submenu
            Menu changeStatusMenu = new Menu("Cambiar Estado");
            for (AvailabilityStatus status : AvailabilityStatus.values()) {
                MenuItem statusItem = new MenuItem(getAvailabilityLabel(status));
                statusItem.setOnAction(event -> {
                    DeliveryPerson selected = row.getItem();
                    if (selected != null) {
                        handleUpdateStatusForPerson(selected, status);
                    }
                });
                changeStatusMenu.getItems().add(statusItem);
            }

            // Asignar Vehículo submenu
            Menu assignVehicleMenu = new Menu("Asignar Vehículo");
            MenuItem refreshVehiclesItem = new MenuItem("Cargar Vehículos Disponibles");
            refreshVehiclesItem.setOnAction(event -> {
                DeliveryPerson selected = row.getItem();
                if (selected != null) {
                    showAssignVehicleDialog(selected);
                }
            });
            assignVehicleMenu.getItems().add(refreshVehiclesItem);

            MenuItem removeVehicleItem = new MenuItem("Desasignar Vehículo");
            removeVehicleItem.setOnAction(event -> {
                DeliveryPerson selected = row.getItem();
                if (selected != null && selected.getAssignedVehicle() != null) {
                    handleRemoveVehicleForPerson(selected);
                }
            });

            MenuItem viewVehiclesItem = new MenuItem("Ver Vehículos");
            viewVehiclesItem.setOnAction(event -> {
                DeliveryPerson selected = row.getItem();
                if (selected != null) {
                    handleViewVehiclesForDeliveryPerson(selected);
                }
            });

            MenuItem deleteItem = new MenuItem("Eliminar Repartidor");
            deleteItem.setOnAction(event -> {
                DeliveryPerson selected = row.getItem();
                if (selected != null) {
                    handleRemoveDeliveryPersonForPerson(selected);
                }
            });

            contextMenu.getItems().addAll(
                    changeStatusMenu,
                    assignVehicleMenu,
                    removeVehicleItem,
                    new SeparatorMenuItem(),
                    viewVehiclesItem,
                    deleteItem
            );

            row.contextMenuProperty().bind(
                    javafx.beans.binding.Bindings.when(row.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(contextMenu)
            );

            return row;
        });
    }

    /**
     * Sets up table columns with cell value factories.
     */
    private void setupTableColumns() {
        colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName() + " " + data.getValue().getLastName()));
        colEmail.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        colPhone.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPhone() != null ? data.getValue().getPhone() : "--"));
        colDocumentId.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDocumentId() != null ? data.getValue().getDocumentId() : "--"));
        colAvailability.setCellValueFactory(data -> new SimpleStringProperty(getAvailabilityLabel(data.getValue().getAvailability())));
        colCoverageArea.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCoverageArea() != null ? getCoverageAreaSpanish(data.getValue().getCoverageArea()) : "--"));
        colVehicle.setCellValueFactory(data -> new SimpleStringProperty(getVehicleLabel(data.getValue().getAssignedVehicle())));
        colShipments.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getAssignedShipments() != null ? data.getValue().getAssignedShipments().size() : 0).asObject());

        // Apply badge style to availability column
        colAvailability.setCellFactory(column -> new TableCell<DeliveryPerson, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    setGraphic(null);
                    return;
                }

                DeliveryPerson person = getTableView().getItems().get(getIndex());
                if (person == null) return;

                // Create styled label for status badge
                Label badge = new Label(item);
                String backgroundColor = getAvailabilityColor(person.getAvailability());
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
                setStyle(""); // Clear cell style
            }
        });
    }

    /**
     * Sets up filter combo boxes.
     */
    private void setupFilters() {
        // Availability filter with ButtonCell
        filterAvailability.getItems().clear();
        filterAvailability.getItems().add(null); // "All" option
        filterAvailability.getItems().addAll(AvailabilityStatus.values());

        filterAvailability.setCellFactory(lv -> new javafx.scene.control.ListCell<AvailabilityStatus>() {
            @Override
            protected void updateItem(AvailabilityStatus item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else if (item == null) {
                    setText("Todos");
                } else {
                    setText(getAvailabilityStatusSpanish(item));
                }
            }
        });

        filterAvailability.setButtonCell(new javafx.scene.control.ListCell<AvailabilityStatus>() {
            @Override
            protected void updateItem(AvailabilityStatus item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null) {
                    setText("Todos");
                    setStyle("-fx-text-fill: #6c757d;");
                } else {
                    setText(getAvailabilityStatusSpanish(item));
                    setStyle("-fx-text-fill: #495057;");
                }
            }
        });

        filterAvailability.setValue(null);
        filterAvailability.setOnAction(event -> applyFilters());

        // Add delivery person form combo boxes with ButtonCell
        if (cmbCoverageArea != null) {
            cmbCoverageArea.getItems().addAll(CoverageArea.values());

            cmbCoverageArea.setCellFactory(lv -> new javafx.scene.control.ListCell<CoverageArea>() {
                @Override
                protected void updateItem(CoverageArea item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(getCoverageAreaSpanish(item));
                    }
                }
            });

            cmbCoverageArea.setButtonCell(new javafx.scene.control.ListCell<CoverageArea>() {
                @Override
                protected void updateItem(CoverageArea item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null) {
                        setText("Seleccionar Área");
                        setStyle("-fx-text-fill: #6c757d;");
                    } else {
                        setText(getCoverageAreaSpanish(item));
                        setStyle("-fx-text-fill: #495057;");
                    }
                }
            });
        }

        if (cmbInitialStatus != null) {
            cmbInitialStatus.getItems().addAll(AvailabilityStatus.values());
            cmbInitialStatus.setValue(AvailabilityStatus.AVAILABLE);

            cmbInitialStatus.setCellFactory(lv -> new javafx.scene.control.ListCell<AvailabilityStatus>() {
                @Override
                protected void updateItem(AvailabilityStatus item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(getAvailabilityStatusSpanish(item));
                    }
                }
            });

            cmbInitialStatus.setButtonCell(new javafx.scene.control.ListCell<AvailabilityStatus>() {
                @Override
                protected void updateItem(AvailabilityStatus item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null) {
                        setText("Estado Inicial");
                        setStyle("-fx-text-fill: #6c757d;");
                    } else {
                        setText(getAvailabilityStatusSpanish(item));
                        setStyle("-fx-text-fill: #495057;");
                    }
                }
            });
        }
    }

    /**
     * Returns the Spanish translation for AvailabilityStatus enum.
     */
    private String getAvailabilityStatusSpanish(AvailabilityStatus status) {
        return switch (status) {
            case AVAILABLE -> "Disponible";
            case IN_TRANSIT -> "En Tránsito";
            case INACTIVE -> "Inactivo";
        };
    }

    /**
     * Returns the Spanish translation for CoverageArea enum.
     */
    private String getCoverageAreaSpanish(CoverageArea area) {
        return switch (area) {
            case NORTH -> "Zona Norte";
            case SOUTH -> "Zona Sur";
            case CENTRAL -> "Zona Central";
            case CITY_WIDE -> "Toda la Ciudad";
        };
    }

    /**
     * Loads available vehicles into the combo box.
     */

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
     * Loads all delivery persons from the service.
     */
    private void loadDeliveryPersons() {
        List<DeliveryPerson> persons = deliveryPersonService.getAllDeliveryPersons();
        deliveryPersonsList = FXCollections.observableArrayList(persons);
        filteredDeliveryPersons = new FilteredList<>(deliveryPersonsList, p -> true);
        tableDeliveryPersons.setItems(filteredDeliveryPersons);
        Logger.info("Loaded " + persons.size() + " delivery persons.");
    }

    /**
     * Updates the statistics labels.
     */
    private void updateStatistics() {
        if (deliveryPersonsList == null) return;

        int total = deliveryPersonsList.size();
        long available = deliveryPersonsList.stream().filter(p -> p.getAvailability() == AvailabilityStatus.AVAILABLE).count();
        long inTransit = deliveryPersonsList.stream().filter(p -> p.getAvailability() == AvailabilityStatus.IN_TRANSIT).count();
        long unavailable = deliveryPersonsList.stream().filter(p -> p.getAvailability() == AvailabilityStatus.INACTIVE).count();

        lblTotal.setText(String.valueOf(total));
        lblAvailable.setText(String.valueOf(available));
        lblInTransit.setText(String.valueOf(inTransit));
        lblUnavailable.setText(String.valueOf(unavailable));
    }

    /**
     * Applies search and filter criteria to the table.
     */
    private void applyFilters() {
        filteredDeliveryPersons.setPredicate(person -> {
            boolean matchesStatus = matchesAvailabilityFilter(person);
            boolean matchesSearch = matchesSearchFilter(person);
            return matchesStatus && matchesSearch;
        });
    }

    /**
     * Checks if a person matches the availability filter.
     */
    private boolean matchesAvailabilityFilter(DeliveryPerson person) {
        AvailabilityStatus selectedStatus = filterAvailability.getValue();
        return selectedStatus == null || person.getAvailability() == selectedStatus;
    }

    /**
     * Checks if a person matches the search text.
     */
    private boolean matchesSearchFilter(DeliveryPerson person) {
        String searchText = txtSearch.getText();
        if (searchText == null || searchText.trim().isEmpty()) {
            return true;
        }

        String lowerSearch = searchText.toLowerCase();
        return person.getName().toLowerCase().contains(lowerSearch) ||
               person.getLastName().toLowerCase().contains(lowerSearch) ||
               person.getEmail().toLowerCase().contains(lowerSearch);
    }

    // =================================================================================================================
    // Event Handlers
    // =================================================================================================================

    /**
     * Handles the add delivery person button click.
     * Opens a modal dialog to add a new delivery person.
     */
    @FXML
    private void handleAddDeliveryPerson() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/co/edu/uniquindio/poo/ProyectoFinal2025_2/View/AddDeliveryPersonDialog.fxml")
            );
            javafx.scene.Parent root = loader.load();

            AddDeliveryPersonDialogController dialogController = loader.getController();
            dialogController.setParentController(this);

            javafx.stage.Stage dialogStage = new javafx.stage.Stage();
            dialogStage.setTitle("Agregar Repartidor");
            dialogStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            dialogStage.initOwner(tableDeliveryPersons.getScene().getWindow());

            javafx.scene.Scene dialogScene = new javafx.scene.Scene(root);

            // Cargar los estilos CSS
            String currentTheme = co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.ThemeManager.getInstance().getCurrentTheme();
            String mainStylesheet = getClass().getResource("/co/edu/uniquindio/poo/ProyectoFinal2025_2/Style.css").toExternalForm();
            dialogScene.getStylesheets().add(mainStylesheet);

            if ("dark".equals(currentTheme)) {
                String darkStylesheet = getClass().getResource("/co/edu/uniquindio/poo/ProyectoFinal2025_2/DarkTheme.css").toExternalForm();
                dialogScene.getStylesheets().add(darkStylesheet);
            }

            dialogStage.setScene(dialogScene);
            dialogStage.setResizable(false);
            dialogStage.showAndWait();

        } catch (Exception e) {
            Logger.error("Failed to open Add Delivery Person dialog: " + e.getMessage());
            DialogUtil.showError("Error", "No se pudo abrir el diálogo.");
        }
    }

    /**
     * Called by the dialog controller after successfully adding a delivery person.
     * Refreshes the table and statistics.
     */
    public void onDeliveryPersonAdded() {
        loadDeliveryPersons();
        updateStatistics();
    }

    /**
     * Handles updating status for a specific delivery person.
     */
    private void handleUpdateStatusForPerson(DeliveryPerson person, AvailabilityStatus newStatus) {
        boolean updated = deliveryPersonService.updateAvailability(person.getId(), newStatus);

        if (updated) {
            person.setAvailability(newStatus);
            tableDeliveryPersons.refresh();
            updateStatistics();
            DialogUtil.showSuccess("Estado actualizado correctamente.");
        } else {
            DialogUtil.showError("Error al actualizar el estado.");
        }
    }

    /**
     * Shows a dialog to assign a vehicle to a specific delivery person.
     */
    private void showAssignVehicleDialog(DeliveryPerson person) {
        List<Vehicle> availableVehicles = vehicleService.getAvailableVehicles();

        if (availableVehicles.isEmpty()) {
            DialogUtil.showWarning("Sin Vehículos", "No hay vehículos disponibles para asignar.");
            return;
        }

        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/co/edu/uniquindio/poo/ProyectoFinal2025_2/View/AssignVehicleDialog.fxml")
            );
            javafx.scene.Parent root = loader.load();

            AssignVehicleDialogController dialogController = loader.getController();
            dialogController.setData(person, availableVehicles);

            javafx.stage.Stage dialogStage = new javafx.stage.Stage();
            dialogStage.setTitle("Asignar Vehículo");
            dialogStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            dialogStage.initOwner(tableDeliveryPersons.getScene().getWindow());

            javafx.scene.Scene dialogScene = new javafx.scene.Scene(root);

            // Load stylesheets
            String currentTheme = co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.ThemeManager.getInstance().getCurrentTheme();
            String mainStylesheet = getClass().getResource("/co/edu/uniquindio/poo/ProyectoFinal2025_2/Style.css").toExternalForm();
            dialogScene.getStylesheets().add(mainStylesheet);

            if ("dark".equals(currentTheme)) {
                String darkStylesheet = getClass().getResource("/co/edu/uniquindio/poo/ProyectoFinal2025_2/DarkTheme.css").toExternalForm();
                dialogScene.getStylesheets().add(darkStylesheet);
            }

            dialogStage.setScene(dialogScene);
            dialogStage.setResizable(false);
            dialogStage.showAndWait();

            // Check if user confirmed
            if (dialogController.isConfirmed()) {
                Vehicle selectedVehicle = dialogController.getSelectedVehicle();
                person.setAssignedVehicle(selectedVehicle);
                deliveryPersonRepository.updateDeliveryPerson(person); // Persist the changes
                tableDeliveryPersons.refresh();
                DialogUtil.showSuccess("Vehículo asignado correctamente.");
            }

        } catch (Exception e) {
            Logger.error("Failed to open Assign Vehicle dialog: " + e.getMessage());
            DialogUtil.showError("Error", "No se pudo abrir el diálogo.");
        }
    }

    /**
     * Handles removing vehicle from a specific delivery person.
     */
    private void handleRemoveVehicleForPerson(DeliveryPerson person) {
        boolean confirmed = DialogUtil.showConfirmation(
                "Confirmar",
                "¿Desasignar vehículo de " + person.getName() + " " + person.getLastName() + "?"
        );

        if (!confirmed) return;

        person.setAssignedVehicle(null);
        deliveryPersonRepository.updateDeliveryPerson(person); // Persist the changes
        tableDeliveryPersons.refresh();
        DialogUtil.showSuccess("Vehículo desasignado correctamente.");
    }

    /**
     * Handles viewing vehicles for a specific delivery person.
     * Opens ManageVehicles view with the delivery person's email filter applied.
     */
    private void handleViewVehiclesForDeliveryPerson(DeliveryPerson person) {
        Logger.info("View Vehicles clicked for delivery person: " + person.getName() + " " + person.getLastName());
        if (indexController != null) {
            Logger.info("Navigating to ManageVehicles with filter: " + person.getEmail());
            indexController.loadViewWithDeliveryPersonFilter("ManageVehicles.fxml", person.getEmail(), "ManageDeliveryPersons.fxml");
        } else {
            Logger.error("IndexController is null - cannot navigate to ManageVehicles");
            DialogUtil.showError("Error", "No se puede navegar a la vista de vehículos.");
        }
    }

    /**
     * Handles removing a specific delivery person.
     */
    private void handleRemoveDeliveryPersonForPerson(DeliveryPerson person) {
        boolean confirmed = DialogUtil.showWarningConfirmation(
                "Confirmar Eliminación",
                "¿Eliminar a " + person.getName() + " " + person.getLastName() + "?",
                "Esta acción NO se puede deshacer."
        );

        if (!confirmed) return;

        boolean removed = deliveryPersonService.removeDeliveryPerson(person.getId());
        if (removed) {
            deliveryPersonsList.remove(person);
            updateStatistics();
            DialogUtil.showSuccess("Repartidor eliminado correctamente.");
        } else {
            DialogUtil.showError("Error al eliminar el repartidor.");
        }
    }

    /**
     * Handles the refresh button click.
     */
    @FXML
    private void handleRefresh() {
        loadDeliveryPersons();
        updateStatistics();
        DialogUtil.showSuccess("Actualizado", "Datos actualizados correctamente.");
    }

    // =================================================================================================================
    // Helper Methods
    // =================================================================================================================

    /**
     * Converts AvailabilityStatus to Spanish label.
     */
    private String getAvailabilityLabel(AvailabilityStatus status) {
        if (status == null) return "Desconocido";
        return switch (status) {
            case AVAILABLE -> "Disponible";
            case IN_TRANSIT -> "En Tránsito";
            case INACTIVE -> "Inactivo";
            default -> "Desconocido";
        };
    }

    /**
     * Gets the color for an availability status.
     */
    private String getAvailabilityColor(AvailabilityStatus status) {
        if (status == null) return "#000000";
        return switch (status) {
            case AVAILABLE -> "#28A745"; // Green
            case IN_TRANSIT -> "#007BFF"; // Blue
            case INACTIVE -> "#DC3545"; // Red
            default -> "#000000";
        };
    }

    /**
     * Gets a formatted string for a vehicle.
     */
    private String getVehicleLabel(Vehicle vehicle) {
        if (vehicle == null) return "Sin asignar";
        String vehicleTypeSpanish = switch (vehicle.getType()) {
            case MOTORCYCLE -> "Motocicleta";
            case CAR -> "Automóvil";
            case VAN -> "Camioneta";
            case TRUCK -> "Camión";
        };
        return vehicle.getPlate() + " - " + vehicleTypeSpanish;
    }

    /**
     * Clears the add delivery person form.
     */
    private void clearAddForm() {
        if (txtName != null) txtName.clear();
        if (txtLastName != null) txtLastName.clear();
        if (txtEmail != null) txtEmail.clear();
        if (txtPhone != null) txtPhone.clear();
        if (txtPassword != null) txtPassword.clear();
        if (txtDocumentId != null) txtDocumentId.clear();
        if (cmbCoverageArea != null) cmbCoverageArea.setValue(null);
        if (cmbInitialStatus != null) cmbInitialStatus.setValue(AvailabilityStatus.AVAILABLE);
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
            case "filters" -> filtersTabContent;
            default -> statsTabContent;
        };

        // Set active button based on restored tab
        Button buttonToActivate = switch (activeTab) {
            case "stats" -> btnTabStats;
            case "filters" -> btnTabFilters;
            default -> btnTabStats;
        };

        setActiveTab(buttonToActivate);
        showTabContent(contentToRestore, false); // false = don't auto-expand
    }
}
