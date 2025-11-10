package co.edu.uniquindio.poo.ProyectoFinal2025_2.Controller;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.DeliveryPerson;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.AvailabilityStatus;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.CoverageArea;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.IncidentType;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.ShipmentStatus;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Order;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.User;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.dto.ShipmentDTO;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.dto.ShipmentFilterDTO;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Repositories.DeliveryPersonRepository;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Repositories.OrderRepository;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Repositories.UserRepository;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.AuthenticationService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.ShipmentService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.DialogUtil;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.TabStateManager;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller for shipment management (Admin view).
 * Allows admins to manage ALL shipments in the system.
 */
public class ShipmentManagementController implements Initializable {

    // Root pane
    @FXML private VBox rootPane;

    // Back button (for contextual navigation)
    @FXML private Button btnBack;

    // Table and Columns
    @FXML private TableView<ShipmentDTO> shipmentsTable;
    @FXML private TableColumn<ShipmentDTO, String> colId;
    @FXML private TableColumn<ShipmentDTO, String> colOrderId;
    @FXML private TableColumn<ShipmentDTO, String> colUser;
    @FXML private TableColumn<ShipmentDTO, String> colRoute;
    @FXML private TableColumn<ShipmentDTO, Double> colWeight;
    @FXML private TableColumn<ShipmentDTO, String> colStatus;
    @FXML private TableColumn<ShipmentDTO, String> colDeliveryPerson;
    @FXML private TableColumn<ShipmentDTO, String> colCreationDate;
    @FXML private TableColumn<ShipmentDTO, String> colEstimatedDate;
    @FXML private TableColumn<ShipmentDTO, Double> colCost;
    @FXML private TableColumn<ShipmentDTO, Integer> colPriority;

    // Filters
    @FXML private ComboBox<ShipmentStatus> filterStatus;
    @FXML private ComboBox<CoverageArea> filterZone;
    @FXML private ComboBox<String> filterUser;
    @FXML private ComboBox<String> filterDeliveryPerson;
    @FXML private DatePicker filterDateFrom;
    @FXML private DatePicker filterDateTo;
    @FXML private TextField searchField;
    @FXML private CheckBox chkDelayed;
    @FXML private CheckBox chkIncidents;

    // Counter Labels
    @FXML private Label lblTotalShipments;
    @FXML private Label lblPending;
    @FXML private Label lblInRoute;
    @FXML private Label lblDelivered;
    @FXML private Label lblIncidents;

    // Tab System Elements
    @FXML private VBox collapsibleTabSection;
    @FXML private Button btnCollapseToggle;
    @FXML private Button btnTabStats;
    @FXML private Button btnTabFilters;

    @FXML private javafx.scene.layout.HBox statsTabContent;
    @FXML private VBox filtersTabContent;

    private static final String VIEW_NAME = "ShipmentManagement";

    // Services and Repositories
    private final ShipmentService shipmentService = new ShipmentService();
    private final UserRepository userRepository = UserRepository.getInstance();
    private final DeliveryPersonRepository deliveryPersonRepository = DeliveryPersonRepository.getInstance();
    private final AuthenticationService authService = AuthenticationService.getInstance();

    // Data
    private ObservableList<ShipmentDTO> shipmentsData;

    // Navigation context
    private String sourceView = null; // The view that navigated to this view (e.g., "ManageUsers.fxml")
    private IndexController indexController;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        setupContextMenu();
        setupFilters();
        loadAllShipments();
        updateCounters();
        restoreViewState();

        Logger.info("ShipmentManagementController initialized");
    }

    /**
     * Sets up table columns with cell value factories.
     */
    private void setupTable() {
        colId.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getId()));
        colOrderId.setCellValueFactory(data -> {
            String orderId = data.getValue().getOrderId();
            return new SimpleStringProperty(orderId != null ? orderId : "N/A");
        });
        colUser.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUserName()));

        colRoute.setCellValueFactory(data -> {
            String origin = data.getValue().getOriginAddressComplete();
            String destination = data.getValue().getDestinationAddressComplete();
            if (origin == null) origin = "N/A";
            if (destination == null) destination = "N/A";
            return new SimpleStringProperty(origin + " → " + destination);
        });

        colWeight.setCellValueFactory(data ->
            new SimpleDoubleProperty(data.getValue().getWeightKg()).asObject());

        colStatus.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getStatusDisplayName()));

        colDeliveryPerson.setCellValueFactory(data -> {
            String name = data.getValue().getDeliveryPersonName();
            return new SimpleStringProperty(name != null ? name : "Unassigned");
        });

        colCreationDate.setCellValueFactory(data -> {
            if (data.getValue().getCreationDate() == null) return new SimpleStringProperty("--");
            return new SimpleStringProperty(
                data.getValue().getCreationDate().format(DATE_FORMATTER)
            );
        });

        colEstimatedDate.setCellValueFactory(data -> {
            if (data.getValue().getEstimatedDeliveryDate() == null) return new SimpleStringProperty("--");
            return new SimpleStringProperty(
                data.getValue().getEstimatedDeliveryDate().format(DATE_FORMATTER)
            );
        });

        colCost.setCellValueFactory(data ->
            new SimpleDoubleProperty(data.getValue().getTotalCost()).asObject());

        colPriority.setCellValueFactory(data ->
            new SimpleIntegerProperty(data.getValue().getPriority()).asObject());

        // Format currency
        colCost.setCellFactory(column -> new TableCell<ShipmentDTO, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                setText(String.format("$%,.2f", item));
            }
        });

        // Color status column with badge style
        colStatus.setCellFactory(column -> new TableCell<ShipmentDTO, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                    return;
                }

                ShipmentDTO shipment = getTableView().getItems().get(getIndex());
                if (shipment == null) return;

                // Create styled label for status badge
                javafx.scene.control.Label badge = new javafx.scene.control.Label(item);
                badge.setStyle(
                    "-fx-background-color: " + shipment.getStatusColor() + ";" +
                    "-fx-text-fill: white;" +
                    "-fx-padding: 5 10 5 10;" +
                    "-fx-background-radius: 12;" +
                    "-fx-font-weight: bold;" +
                    "-fx-font-size: 11px;"
                );

                setText(null);
                setGraphic(badge);
            }
        });

        // Enable multiple selection
        shipmentsTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    /**
     * Sets up the context menu for right-click actions on table rows.
     */
    private void setupContextMenu() {
        shipmentsTable.setRowFactory(tv -> {
            TableRow<ShipmentDTO> row = new TableRow<>() {
                @Override
                protected void updateItem(ShipmentDTO item, boolean empty) {
                    super.updateItem(item, empty);
                }
            };

            ContextMenu contextMenu = new ContextMenu();

            // Ver Detalles
            MenuItem viewDetailsItem = new MenuItem("Ver Detalles");
            viewDetailsItem.setOnAction(event -> {
                ShipmentDTO selected = row.getItem();
                if (selected != null) {
                    openShipmentDetails(selected.getId());
                }
            });

            // Ver Orden Asociada
            MenuItem viewOrderItem = new MenuItem("Ver Orden Asociada");
            viewOrderItem.setOnAction(event -> {
                ShipmentDTO selected = row.getItem();
                if (selected != null) {
                    viewAssociatedOrder(selected);
                }
            });

            // Asignar Repartidor
            MenuItem assignDeliveryItem = new MenuItem("Asignar Repartidor");
            assignDeliveryItem.setOnAction(event -> {
                ShipmentDTO selected = row.getItem();
                if (selected != null) {
                    showAssignDeliveryPersonDialog(selected);
                }
            });

            // Cambiar Estado
            MenuItem changeStatusItem = new MenuItem("Cambiar Estado");
            changeStatusItem.setOnAction(event -> {
                ShipmentDTO selected = row.getItem();
                if (selected != null) {
                    showChangeStatusDialog(selected);
                }
            });

            // Registrar Incidente
            MenuItem registerIncidentItem = new MenuItem("Registrar Incidente");
            registerIncidentItem.setOnAction(event -> {
                ShipmentDTO selected = row.getItem();
                if (selected != null) {
                    showRegisterIncidentDialog(selected);
                }
            });

            // Eliminar
            MenuItem deleteItem = new MenuItem("Eliminar");
            deleteItem.setOnAction(event -> {
                ShipmentDTO selected = row.getItem();
                if (selected != null) {
                    handleDeleteForShipment(selected);
                }
            });
            deleteItem.setStyle("-fx-text-fill: #dc3545;");

            // Ver Historial Completo
            MenuItem viewHistoryItem = new MenuItem("Ver Historial Completo");
            viewHistoryItem.setOnAction(event -> {
                ShipmentDTO selected = row.getItem();
                if (selected != null) {
                    showShipmentHistory(selected);
                }
            });

            contextMenu.getItems().addAll(
                    viewDetailsItem,
                    viewOrderItem,
                    viewHistoryItem,
                    new SeparatorMenuItem(),
                    assignDeliveryItem,
                    changeStatusItem,
                    registerIncidentItem,
                    new SeparatorMenuItem(),
                    deleteItem
            );

            // Only show context menu on non-empty rows
            row.setOnContextMenuRequested(event -> {
                if (!row.isEmpty()) {
                    contextMenu.show(row, event.getScreenX(), event.getScreenY());
                }
            });

            return row;
        });
    }

    /**
     * Sets up filter combo boxes with data.
     * Each ComboBox includes a special "All" option and uses a custom converter
     * to display the appropriate text.
     */
    private void setupFilters() {
        // Status filter - add "All" option at the beginning
        ObservableList<ShipmentStatus> statusList = FXCollections.observableArrayList();
        statusList.add(null); // Null represents "All"
        statusList.addAll(ShipmentStatus.values());
        filterStatus.setItems(statusList);
        filterStatus.setCellFactory(lv -> new ListCell<ShipmentStatus>() {
            @Override
            protected void updateItem(ShipmentStatus item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else if (item == null) {
                    setText("Todos los Estados");
                } else {
                    setText(item.getDisplayName());
                }
            }
        });
        filterStatus.setValue(null);
        filterStatus.setButtonCell(new ListCell<ShipmentStatus>() {
            @Override
            protected void updateItem(ShipmentStatus item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText("Todos los Estados");
                    setStyle("-fx-text-fill: #6c757d;"); // Gray color for placeholder
                } else {
                    setText(item.getDisplayName());
                    setStyle("-fx-text-fill: #495057;"); // Normal text color
                }
            }
        });

        // Zone filter - add "All" option at the beginning
        ObservableList<CoverageArea> zoneList = FXCollections.observableArrayList();
        zoneList.add(null); // Null represents "All"
        zoneList.addAll(CoverageArea.values());
        filterZone.setItems(zoneList);
        filterZone.setCellFactory(lv -> new ListCell<CoverageArea>() {
            @Override
            protected void updateItem(CoverageArea item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else if (item == null) {
                    setText("Todas las Zonas");
                } else {
                    setText(item.getDisplayName());
                }
            }
        });
        filterZone.setValue(null);
        filterZone.setButtonCell(new ListCell<CoverageArea>() {
            @Override
            protected void updateItem(CoverageArea item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText("Todas las Zonas");
                    setStyle("-fx-text-fill: #6c757d;");
                } else {
                    setText(item.getDisplayName());
                    setStyle("-fx-text-fill: #495057;");
                }
            }
        });

        // User filter - add "All" option at the beginning
        ObservableList<String> userList = FXCollections.observableArrayList();
        userList.add(null); // Null represents "All"
        userList.addAll(userRepository.getUsers().stream()
            .map(User::getEmail)
            .collect(Collectors.toList()));
        filterUser.setItems(userList);
        filterUser.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else if (item == null) {
                    setText("Todos los Usuarios");
                } else {
                    setText(item);
                }
            }
        });
        filterUser.setValue(null);
        filterUser.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText("Todos los Usuarios");
                    setStyle("-fx-text-fill: #6c757d;");
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: #495057;");
                }
            }
        });

        // Delivery person filter - add "All" option at the beginning
        ObservableList<String> dpList = FXCollections.observableArrayList();
        dpList.add(null); // Null represents "All"
        dpList.addAll(deliveryPersonRepository.getAllDeliveryPersons().stream()
            .map(DeliveryPerson::getEmail)
            .collect(Collectors.toList()));
        filterDeliveryPerson.setItems(dpList);
        filterDeliveryPerson.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else if (item == null) {
                    setText("Todos los Repartidores");
                } else {
                    setText(item);
                }
            }
        });
        filterDeliveryPerson.setValue(null);
        filterDeliveryPerson.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText("Todos los Repartidores");
                    setStyle("-fx-text-fill: #6c757d;");
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: #495057;");
                }
            }
        });

        // Setup dynamic filters (apply automatically without "Aplicar" button)
        setupDynamicFilters();
    }

    /**
     * Setup dynamic filters that apply automatically when changed.
     */
    private void setupDynamicFilters() {
        if (filterStatus != null) {
            filterStatus.setOnAction(e -> handleFilter());
        }
        if (filterZone != null) {
            filterZone.setOnAction(e -> handleFilter());
        }
        if (filterUser != null) {
            filterUser.setOnAction(e -> handleFilter());
        }
        if (filterDeliveryPerson != null) {
            filterDeliveryPerson.setOnAction(e -> handleFilter());
        }
        if (filterDateFrom != null) {
            filterDateFrom.setOnAction(e -> handleFilter());
        }
        if (filterDateTo != null) {
            filterDateTo.setOnAction(e -> handleFilter());
        }
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> handleFilter());
        }
        if (chkDelayed != null) {
            chkDelayed.setOnAction(e -> handleFilter());
        }
        if (chkIncidents != null) {
            chkIncidents.setOnAction(e -> handleFilter());
        }
    }

    /**
     * Loads all shipments in the system.
     */
    private void loadAllShipments() {
        List<ShipmentDTO> shipments = shipmentService.listAll();
        shipmentsData = FXCollections.observableArrayList(shipments);
        shipmentsTable.setItems(shipmentsData);

        Logger.info("Loaded " + shipments.size() + " total shipments");
    }

    /**
     * Updates counter labels.
     */
    private void updateCounters() {
        if (shipmentsData == null) return;

        long total = shipmentsData.size();
        long pending = shipmentsData.stream()
            .filter(s -> s.getStatus() == ShipmentStatus.READY_FOR_PICKUP)
            .count();
        long inRoute = shipmentsData.stream()
            .filter(s -> s.getStatus() == ShipmentStatus.IN_TRANSIT ||
                        s.getStatus() == ShipmentStatus.OUT_FOR_DELIVERY)
            .count();
        long delivered = shipmentsData.stream()
            .filter(s -> s.getStatus() == ShipmentStatus.DELIVERED)
            .count();
        long incidents = shipmentsData.stream()
            .filter(s -> s.getIncident() != null)
            .count();

        lblTotalShipments.setText(String.valueOf(total));
        lblPending.setText(String.valueOf(pending));
        lblInRoute.setText(String.valueOf(inRoute));
        lblDelivered.setText(String.valueOf(delivered));
        lblIncidents.setText(String.valueOf(incidents));
    }

    // ===========================
    // Button Handlers
    // ===========================

    @FXML
    private void handleCreate() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/co/edu/uniquindio/poo/ProyectoFinal2025_2/View/CreateShipment.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Create New Shipment");

            // Create scene with stylesheet
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/co/edu/uniquindio/poo/ProyectoFinal2025_2/Style.css").toExternalForm());
            stage.setScene(scene);

            // Set window size
            stage.setWidth(1300);
            stage.setHeight(700);
            stage.setResizable(true);

            stage.show();

            stage.setOnHidden(e -> handleRefresh());

        } catch (IOException e) {
            Logger.error("Failed to load CreateShipment view: " + e.getMessage(), e);
            e.printStackTrace();
            DialogUtil.showError("Error", "Could not open create shipment form: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        loadAllShipments();
        updateCounters();
        DialogUtil.showInfo("Refreshed", "Shipment list has been updated");
    }

    @FXML
    private void handleFilter() {
        ShipmentFilterDTO filter = new ShipmentFilterDTO();
        filter.setStatus(filterStatus.getValue());
        filter.setDateFrom(filterDateFrom.getValue());
        filter.setDateTo(filterDateTo.getValue());
        filter.setZone(filterZone.getValue());
        filter.setOnlyDelayed(chkDelayed.isSelected());
        filter.setOnlyWithIncidents(chkIncidents.isSelected());

        String searchText = searchField.getText();
        if (searchText != null && !searchText.trim().isEmpty()) {
            filter.setSearchText(searchText.trim());
        }

        // Extract user ID from selection (now it's just the email)
        if (filterUser.getValue() != null) {
            String email = filterUser.getValue();
            Optional<User> userOpt = userRepository.findByEmail(email);
            userOpt.ifPresent(user -> filter.setUserId(user.getId()));
        }

        // Extract delivery person ID from selection (now it's just the email)
        if (filterDeliveryPerson.getValue() != null) {
            String email = filterDeliveryPerson.getValue();
            Optional<DeliveryPerson> dpOpt = deliveryPersonRepository.findDeliveryPersonByEmail(email);
            dpOpt.ifPresent(dp -> filter.setDeliveryPersonId(dp.getId()));
        }

        List<ShipmentDTO> filtered = shipmentService.filterShipments(filter);
        shipmentsData = FXCollections.observableArrayList(filtered);
        shipmentsTable.setItems(shipmentsData);
        updateCounters();
    }

    @FXML
    private void handleClearFilter() {
        filterStatus.setValue(null);
        filterZone.setValue(null);
        filterUser.setValue(null);
        filterDeliveryPerson.setValue(null);
        filterDateFrom.setValue(null);
        filterDateTo.setValue(null);
        searchField.clear();
        chkDelayed.setSelected(false);
        chkIncidents.setSelected(false);
        loadAllShipments();
        updateCounters();
    }

    @FXML
    private void handleViewDetails() {
        ShipmentDTO selected = shipmentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtil.showWarning("No Selection", "Please select a shipment to view details");
            return;
        }

        openShipmentDetails(selected.getId());
    }

    // NOTE: Assignment methods have been moved to AdminOrderManagementController
    // Shipments already have delivery persons assigned when they are created from orders

    @FXML
    private void handleAssign() {
        DialogUtil.showInfo("Función No Disponible",
                "La asignación de repartidores se realiza desde la vista de 'Gestión de Órdenes'.\n\n" +
                        "Los envíos ya tienen repartidores asignados cuando se crean desde las órdenes.");
    }

    @FXML
    private void handleBulkAssign() {
        DialogUtil.showInfo("Función No Disponible",
                "La asignación masiva de repartidores se realiza desde la vista de 'Gestión de Órdenes'.\n\n" +
                        "Los envíos ya tienen repartidores asignados cuando se crean desde las órdenes.");
    }

    @FXML
    private void handleChangeStatus() {
        ShipmentDTO selected = shipmentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtil.showWarning("No Selection", "Please select a shipment");
            return;
        }

        showChangeStatusDialog(selected);
    }

    @FXML
    private void handleBulkStatusUpdate() {
        List<ShipmentDTO> selected = shipmentsTable.getSelectionModel().getSelectedItems();
        if (selected.isEmpty()) {
            DialogUtil.showWarning("No Selection", "Please select at least one shipment");
            return;
        }

        showBulkStatusUpdateDialog(selected);
    }

    @FXML
    private void handleRegisterIncident() {
        ShipmentDTO selected = shipmentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtil.showWarning("No Selection", "Please select a shipment");
            return;
        }

        showRegisterIncidentDialog(selected);
    }

    @FXML
    private void handleDelete() {
        ShipmentDTO selected = shipmentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtil.showWarning("No Selection", "Please select a shipment to delete");
            return;
        }
        handleDeleteForShipment(selected);
    }

    /**
     * Handles deleting a specific shipment.
     */
    private void handleDeleteForShipment(ShipmentDTO selected) {
        boolean confirmed = DialogUtil.showWarningConfirmation(
            "Eliminar Envío",
            "¿Está seguro de eliminar el envío " + selected.getId() + "?",
            "Esta acción NO se puede deshacer."
        );

        if (!confirmed) return;

        try {
            boolean success = shipmentService.cancelShipment(selected.getId());
            if (success) {
                DialogUtil.showSuccess("Envío eliminado correctamente.");
                handleRefresh();
            }
        } catch (Exception e) {
            Logger.error("Failed to delete shipment: " + e.getMessage());
            DialogUtil.showError("Error", "No se pudo eliminar el envío: " + e.getMessage());
        }
    }

    @FXML
    private void handleExport() {
        DialogUtil.showInfo("Export", "Export functionality will be implemented soon");
        // TODO: Implement CSV/PDF export
    }

    // ===========================
    // Helper Methods
    // ===========================

    /**
     * Opens shipment details view.
     */
    private void openShipmentDetails(String shipmentId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/co/edu/uniquindio/poo/ProyectoFinal2025_2/View/ShipmentDetail.fxml"));
            Parent root = loader.load();

            ShipmentDetailController controller = loader.getController();
            controller.loadShipmentDetails(shipmentId);

            Stage stage = new Stage();
            stage.setTitle("Shipment Details - " + shipmentId);
            Scene scene = new Scene(root);
            co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.ThemeManager.getInstance().applyThemeToScene(scene);
            stage.setScene(scene);

            // Set window size
            stage.setWidth(1000);
            stage.setHeight(700);
            stage.setResizable(true);

            stage.show();

        } catch (IOException e) {
            Logger.error("Failed to load ShipmentDetail view: " + e.getMessage());
            DialogUtil.showError("Error", "Could not open shipment details");
        }
    }

    // NOTE: showAssignDialog and showBulkAssignDialog have been removed
    // Assignment is now handled in AdminOrderManagementController for orders

    /**
     * Shows dialog to change shipment status.
     */
    private void showChangeStatusDialog(ShipmentDTO shipment) {
        ChoiceDialog<ShipmentStatus> dialog = new ChoiceDialog<>(shipment.getStatus(), ShipmentStatus.values());
        dialog.setTitle("Change Status");
        dialog.setHeaderText("Change status for shipment " + shipment.getId());
        dialog.setContentText("Select new status:");

        Optional<ShipmentStatus> result = dialog.showAndWait();
        result.ifPresent(newStatus -> {
            if (newStatus == shipment.getStatus()) {
                DialogUtil.showInfo("No Change", "Status is already " + newStatus.getDisplayName());
                return;
            }

            TextInputDialog reasonDialog = new TextInputDialog();
            reasonDialog.setTitle("Status Change Reason");
            reasonDialog.setHeaderText("Provide reason for status change");
            reasonDialog.setContentText("Reason:");

            Optional<String> reason = reasonDialog.showAndWait();
            reason.ifPresent(r -> {
                try {
                    String adminId = authService.getCurrentPerson().getId();
                    boolean success = shipmentService.changeStatus(shipment.getId(), newStatus, r, adminId);
                    if (success) {
                        DialogUtil.showSuccess("Status Changed", "Status updated successfully");
                        handleRefresh();
                    }
                } catch (Exception e) {
                    Logger.error("Status change failed: " + e.getMessage());
                    DialogUtil.showError("Error", "Status change failed: " + e.getMessage());
                }
            });
        });
    }

    /**
     * Shows dialog for bulk status update.
     */
    private void showBulkStatusUpdateDialog(List<ShipmentDTO> shipments) {
        ChoiceDialog<ShipmentStatus> dialog = new ChoiceDialog<>(ShipmentStatus.IN_TRANSIT, ShipmentStatus.values());
        dialog.setTitle("Bulk Status Update");
        dialog.setHeaderText("Update status for " + shipments.size() + " shipments");
        dialog.setContentText("Select new status:");

        Optional<ShipmentStatus> result = dialog.showAndWait();
        result.ifPresent(newStatus -> {
            TextInputDialog reasonDialog = new TextInputDialog();
            reasonDialog.setTitle("Status Change Reason");
            reasonDialog.setHeaderText("Provide reason for bulk status change");
            reasonDialog.setContentText("Reason:");

            Optional<String> reason = reasonDialog.showAndWait();
            reason.ifPresent(r -> {
                int successCount = 0;
                String adminId = authService.getCurrentPerson().getId();

                for (ShipmentDTO shipment : shipments) {
                    try {
                        boolean success = shipmentService.changeStatus(shipment.getId(), newStatus, r, adminId);
                        if (success) successCount++;
                    } catch (Exception e) {
                        Logger.error("Failed to update shipment " + shipment.getId() + ": " + e.getMessage());
                    }
                }

                DialogUtil.showSuccess("Bulk Update Complete",
                    "Successfully updated " + successCount + " out of " + shipments.size() + " shipments");
                handleRefresh();
            });
        });
    }

    /**
     * Shows dialog to register an incident.
     */
    private void showRegisterIncidentDialog(ShipmentDTO shipment) {
        ChoiceDialog<IncidentType> typeDialog = new ChoiceDialog<>(IncidentType.DELAY, IncidentType.values());
        typeDialog.setTitle("Register Incident");
        typeDialog.setHeaderText("Register incident for shipment " + shipment.getId());
        typeDialog.setContentText("Select incident type:");

        Optional<IncidentType> typeResult = typeDialog.showAndWait();
        typeResult.ifPresent(type -> {
            TextInputDialog descDialog = new TextInputDialog();
            descDialog.setTitle("Incident Description");
            descDialog.setHeaderText("Describe the incident");
            descDialog.setContentText("Description:");

            Optional<String> descResult = descDialog.showAndWait();
            descResult.ifPresent(description -> {
                try {
                    String adminId = authService.getCurrentPerson().getId();
                    boolean success = shipmentService.registerIncident(shipment.getId(), type, description, adminId);
                    if (success) {
                        DialogUtil.showSuccess("Incident Registered", "Incident has been registered successfully");
                        handleRefresh();
                    }
                } catch (Exception e) {
                    Logger.error("Failed to register incident: " + e.getMessage());
                    DialogUtil.showError("Error", "Failed to register incident: " + e.getMessage());
                }
            });
        });
    }

    /**
     * Extracts email from combo box selection text.
     */
    private String extractEmail(String selection) {
        if (selection == null) return null;
        int start = selection.indexOf('(');
        int end = selection.indexOf(')');
        if (start == -1 || end == -1) return null;
        return selection.substring(start + 1, end);
    }

    /**
     * Shows details of the order associated with a shipment.
     */
    private void viewAssociatedOrder(ShipmentDTO shipment) {
        if (shipment == null || shipment.getOrderId() == null) {
            DialogUtil.showWarning("Sin Orden Asociada",
                "Este envío no tiene una orden asociada.");
            return;
        }

        OrderRepository orderRepository = OrderRepository.getInstance();
        Optional<Order> orderOpt = orderRepository.findById(shipment.getOrderId());

        if (orderOpt.isEmpty()) {
            DialogUtil.showError("Error",
                "No se encontró la orden asociada con ID: " + shipment.getOrderId());
            return;
        }

        Order order = orderOpt.get();
        StringBuilder details = new StringBuilder();
        details.append("═══════════════════════════════════════\n");
        details.append("         DETALLES DE LA ORDEN\n");
        details.append("═══════════════════════════════════════\n\n");
        details.append("ID de Orden: ").append(order.getId()).append("\n");
        details.append("Usuario: ").append(order.getUserId()).append("\n");
        details.append("Estado: ").append(order.getStatus().getDisplayName()).append("\n");
        details.append("Fecha de Creación: ").append(order.getCreatedAt().format(DATE_FORMATTER)).append("\n\n");

        details.append("------- Direcciones -------\n");
        details.append("Origen:\n");
        if (order.getOrigin() != null) {
            details.append("  ").append(order.getOrigin().getAlias()).append("\n");
            details.append("  ").append(order.getOrigin().getStreet()).append("\n");
            details.append("  ").append(order.getOrigin().getCity()).append(", ");
            details.append(order.getOrigin().getState()).append("\n");
        } else {
            details.append("  N/A\n");
        }

        details.append("\nDestino:\n");
        if (order.getDestination() != null) {
            details.append("  ").append(order.getDestination().getAlias()).append("\n");
            details.append("  ").append(order.getDestination().getStreet()).append("\n");
            details.append("  ").append(order.getDestination().getCity()).append(", ");
            details.append(order.getDestination().getState()).append("\n");
        } else {
            details.append("  N/A\n");
        }

        details.append("\n------- IDs Relacionados -------\n");
        details.append("ID de Envío: ").append(order.getShipmentId() != null ? order.getShipmentId() : "N/A").append("\n");
        details.append("ID de Pago: ").append(order.getPaymentId() != null ? order.getPaymentId() : "N/A").append("\n");
        details.append("ID de Factura: ").append(order.getInvoiceId() != null ? order.getInvoiceId() : "N/A").append("\n");

        DialogUtil.showInfo("Detalles de la Orden Asociada", details.toString());
    }

    /**
     * Shows dialog to assign a delivery person to a shipment.
     */
    private void showAssignDeliveryPersonDialog(ShipmentDTO shipment) {
        if (shipment == null) return;

        // Check if already assigned
        if (shipment.getDeliveryPersonName() != null && !shipment.getDeliveryPersonName().equals("Unassigned")) {
            boolean reassign = DialogUtil.showConfirmation(
                "Repartidor Ya Asignado",
                "Este envío ya tiene asignado a: " + shipment.getDeliveryPersonName() + "\n\n" +
                "¿Desea reasignar a un repartidor diferente?");

            if (!reassign) return;
        }

        // Get available delivery persons
        List<DeliveryPerson> available = deliveryPersonRepository.getAllDeliveryPersons().stream()
            .filter(dp -> dp.getAvailability() == AvailabilityStatus.AVAILABLE)
            .collect(Collectors.toList());

        if (available.isEmpty()) {
            DialogUtil.showError("No hay repartidores disponibles",
                "No hay repartidores disponibles en este momento.");
            return;
        }

        // Show dialog to select delivery person
        ChoiceDialog<DeliveryPerson> dialog = new ChoiceDialog<>(available.get(0), available);
        dialog.setTitle("Asignar Repartidor");
        dialog.setHeaderText("Asignar repartidor al envío " + shipment.getId());
        dialog.setContentText("Seleccione un repartidor:");

        // Custom cell factory for better display
        dialog.getDialogPane().lookupAll(".combo-box").forEach(node -> {
            if (node instanceof ComboBox) {
                @SuppressWarnings("unchecked")
                ComboBox<DeliveryPerson> comboBox = (ComboBox<DeliveryPerson>) node;
                comboBox.setCellFactory(lv -> new ListCell<DeliveryPerson>() {
                    @Override
                    protected void updateItem(DeliveryPerson dp, boolean empty) {
                        super.updateItem(dp, empty);
                        if (empty || dp == null) {
                            setText(null);
                        } else {
                            setText(dp.getName() + " - " + dp.getCoverageArea().getDisplayName());
                        }
                    }
                });
                comboBox.setButtonCell(new ListCell<DeliveryPerson>() {
                    @Override
                    protected void updateItem(DeliveryPerson dp, boolean empty) {
                        super.updateItem(dp, empty);
                        if (empty || dp == null) {
                            setText(null);
                        } else {
                            setText(dp.getName() + " - " + dp.getCoverageArea().getDisplayName());
                        }
                    }
                });
            }
        });

        Optional<DeliveryPerson> result = dialog.showAndWait();
        result.ifPresent(dp -> {
            try {
                boolean success = shipmentService.assignDeliveryPerson(shipment.getId(), dp.getId());
                if (success) {
                    DialogUtil.showSuccess("Éxito",
                        "Repartidor asignado correctamente.\n" +
                        "El estado del envío ha cambiado a READY_FOR_PICKUP.");
                    handleRefresh();
                    Logger.info("Delivery person " + dp.getId() + " assigned to shipment " + shipment.getId());
                } else {
                    DialogUtil.showError("Error", "No se pudo asignar el repartidor");
                }
            } catch (Exception e) {
                Logger.error("Failed to assign delivery person: " + e.getMessage());
                DialogUtil.showError("Error", "Error al asignar repartidor: " + e.getMessage());
            }
        });
    }

    /**
     * Shows the unified shipment history in a modal dialog.
     */
    private void showShipmentHistory(ShipmentDTO shipment) {
        if (shipment == null) return;

        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/co/edu/uniquindio/poo/ProyectoFinal2025_2/View/ShipmentHistoryDialog.fxml")
            );
            javafx.scene.Parent root = loader.load();

            ShipmentHistoryDialogController controller = loader.getController();
            controller.loadHistory(shipment.getId());

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Historial Completo - " + shipment.getId());
            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.ThemeManager.getInstance().applyThemeToScene(scene);
            stage.setScene(scene);
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.show();

        } catch (Exception e) {
            Logger.error("Failed to load ShipmentHistoryDialog: " + e.getMessage());
            DialogUtil.showError("Error", "No se pudo abrir el historial del envío");
        }
    }

    /**
     * Auto-assigns available delivery persons to pending shipments.
     */
    @FXML
    private void handleAutoAssign() {
        boolean confirmed = DialogUtil.showConfirmation(
            "Asignación Automática",
            "¿Desea asignar automáticamente repartidores a los envíos pendientes de asignación?");

        if (!confirmed) return;

        try {
            int assigned = shipmentService.autoAssignShipments(null);
            DialogUtil.showSuccess("Asignación Automática Completada",
                "Se asignaron exitosamente " + assigned + " envíos a repartidores disponibles.");
            handleRefresh();
            Logger.info("Auto-assigned " + assigned + " shipments");
        } catch (Exception e) {
            Logger.error("Auto-assignment failed: " + e.getMessage());
            DialogUtil.showError("Error", "La asignación automática falló: " + e.getMessage());
        }
    }

    /**
     * Applies a user filter to show only shipments for a specific user.
     * Called when navigating from ManageUsers.
     *
     * @param userEmail The email of the user to filter by
     */
    public void applyUserFilter(String userEmail) {
        applyUserFilter(userEmail, null);
    }

    /**
     * Applies a user filter with source view information.
     *
     * @param userEmail  The email of the user to filter by
     * @param sourceView The view that initiated this navigation
     */
    public void applyUserFilter(String userEmail, String sourceView) {
        if (userEmail == null || userEmail.isEmpty()) {
            Logger.warning("applyUserFilter called with null or empty email");
            return;
        }

        this.sourceView = sourceView;
        Logger.info("Applying user filter for email: " + userEmail + " from source: " + sourceView);

        // Show back button if we came from another view
        if (sourceView != null && btnBack != null) {
            btnBack.setVisible(true);
            btnBack.setManaged(true);
        }

        // Verify the user exists
        Optional<User> userOpt = userRepository.findByEmail(userEmail);
        if (userOpt.isEmpty()) {
            Logger.error("User not found for email: " + userEmail);
            DialogUtil.showError("Error", "No se encontró el usuario con email: " + userEmail);
            return;
        }

        // Ensure the filters tab is open and expanded
        Platform.runLater(() -> {
            switchToFiltersTab();

            // Set the email directly in the combo box (now we show only emails)
            filterUser.setValue(userEmail);

            // Apply the filter
            handleFilter();

            Logger.info("User filter applied successfully for: " + userEmail);
        });
    }

    /**
     * Clears any active filters and hides the back button.
     * Called when navigating directly from the sidebar.
     */
    public void clearContextualFilter() {
        this.sourceView = null;

        // Hide back button
        if (btnBack != null) {
            btnBack.setVisible(false);
            btnBack.setManaged(false);
        }

        // Clear filters
        if (filterUser != null) {
            filterUser.setValue(null);
        }

        // Reset filters
        handleFilter();

        Logger.info("Contextual filter cleared");
    }

    /**
     * Sets the IndexController reference for navigation.
     *
     * @param indexController The IndexController instance
     */
    public void setIndexController(IndexController indexController) {
        this.indexController = indexController;
    }

    /**
     * Handles the back button click - returns to the source view.
     */
    @FXML
    private void handleBack() {
        if (sourceView != null && indexController != null) {
            Logger.info("Navigating back to: " + sourceView);
            indexController.loadView(sourceView);
        }
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
        showTabContent(statsTabContent, "stats", true);
    }

    /**
     * Switches to the Filters tab.
     * Expands the section if collapsed (user interaction).
     */
    @FXML
    private void switchToFiltersTab() {
        setActiveTab(btnTabFilters);
        showTabContent(filtersTabContent, "filters", true);
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
     * @param tabId The ID of the tab for state persistence
     * @param expandIfCollapsed If true, expands the section if it's currently collapsed (for user clicks).
     *                          If false, respects the current collapsed state (for restoring view state).
     */
    private void showTabContent(javafx.scene.layout.Pane contentToShow, String tabId, boolean expandIfCollapsed) {
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

        // Save state
        TabStateManager.setActiveTab(VIEW_NAME, tabId);
    }

    // =================================================================================================================
    // Collapse/Expand Methods
    // =================================================================================================================

    /**
     * Toggles the collapse/expand state of the tab section.
     */
    @FXML
    private void toggleCollapse() {
        boolean currentlyExpanded = TabStateManager.isExpanded(VIEW_NAME);
        boolean newExpanded = !currentlyExpanded;
        TabStateManager.setExpanded(VIEW_NAME, newExpanded);
        applyCollapseState(newExpanded);
    }

    /**
     * Applies the collapse/expand visual state.
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
     * Restores the saved view state (collapse state and active tab).
     */
    private void restoreViewState() {
        // Restore collapse state
        boolean expanded = TabStateManager.isExpanded(VIEW_NAME);
        applyCollapseState(expanded);

        // Restore active tab (without auto-expanding)
        String activeTab = TabStateManager.getActiveTab(VIEW_NAME);
        javafx.scene.layout.Pane contentToRestore;
        String tabIdToRestore;

        if ("filters".equals(activeTab)) {
            contentToRestore = filtersTabContent;
            tabIdToRestore = "filters";
            setActiveTab(btnTabFilters);
        } else {
            contentToRestore = statsTabContent;
            tabIdToRestore = "stats";
            setActiveTab(btnTabStats);
        }

        showTabContent(contentToRestore, tabIdToRestore, false); // false = don't auto-expand
    }

}
