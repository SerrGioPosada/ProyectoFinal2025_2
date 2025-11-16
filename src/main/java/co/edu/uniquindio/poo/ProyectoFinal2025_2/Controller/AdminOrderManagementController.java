package co.edu.uniquindio.poo.ProyectoFinal2025_2.Controller;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.DeliveryPerson;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Order;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Shipment;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.User;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.AvailabilityStatus;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.CoverageArea;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.OrderStatus;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Repositories.DeliveryPersonRepository;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Repositories.OrderRepository;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Repositories.ShipmentRepository;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Repositories.UserRepository;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.OrderService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.ReportService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.DialogUtil;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.TabStateManager;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;
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
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller for order management (Admin view).
 * Allows admins to manage ALL orders in the system.
 */
public class AdminOrderManagementController implements Initializable {

    // Root pane
    @FXML private VBox rootPane;

    // Back button (for contextual navigation)
    @FXML private Button btnBack;

    // Table and Columns
    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, String> colId;
    @FXML private TableColumn<Order, String> colUser;
    @FXML private TableColumn<Order, String> colOrigin;
    @FXML private TableColumn<Order, String> colDestination;
    @FXML private TableColumn<Order, String> colStatus;
    @FXML private TableColumn<Order, String> colCreatedDate;
    @FXML private TableColumn<Order, String> colShipmentId;
    @FXML private TableColumn<Order, String> colPaymentId;

    // Filters
    @FXML private ComboBox<OrderStatus> filterStatus;
    @FXML private TextField searchField;

    // Counter Labels
    @FXML private Label lblTotalOrders;
    @FXML private Label lblPending;
    @FXML private Label lblProcessing;
    @FXML private Label lblReadyForShipment;
    @FXML private Label lblCompleted;

    // Tab System Elements
    @FXML private VBox collapsibleTabSection;
    @FXML private Button btnCollapseToggle;
    @FXML private Button btnTabStats;
    @FXML private Button btnTabFilters;

    @FXML private javafx.scene.layout.HBox statsTabContent;
    @FXML private VBox filtersTabContent;

    private static final String VIEW_NAME = "AdminOrderManagement";

    // Repositories and Services
    private final OrderRepository orderRepository = OrderRepository.getInstance();
    private final UserRepository userRepository = UserRepository.getInstance();
    private final DeliveryPersonRepository deliveryPersonRepository = DeliveryPersonRepository.getInstance();
    private final ShipmentRepository shipmentRepository = ShipmentRepository.getInstance();
    private final OrderService orderService = new OrderService();
    private final ReportService reportService = ReportService.getInstance();

    // Data
    private ObservableList<Order> ordersData;

    // Navigation context
    private String sourceView = null; // The view that navigated to this view (e.g., "ManageUsers.fxml")
    private IndexController indexController;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        setupFilters();
        loadAllOrders();
        updateCounters();
        restoreViewState();

        Logger.info("AdminOrderManagementController initialized");
    }

    /**
     * Setup the table columns with proper cell value factories.
     */
    private void setupTable() {
        colId.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getId()));
        colUser.setCellValueFactory(data -> {
            String userId = data.getValue().getUserId();
            return userRepository.findById(userId)
                    .map(user -> new SimpleStringProperty(user.getEmail()))
                    .orElse(new SimpleStringProperty("N/A"));
        });
        colOrigin.setCellValueFactory(data -> {
            var addr = data.getValue().getOrigin();
            return new SimpleStringProperty(addr != null ? addr.getCity() : "N/A");
        });
        colDestination.setCellValueFactory(data -> {
            var addr = data.getValue().getDestination();
            return new SimpleStringProperty(addr != null ? addr.getCity() : "N/A");
        });
        colStatus.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getStatus() != null
                ? data.getValue().getStatus().getDisplayName()
                : "N/A"));

        // Apply styled cell factory for status column
        colStatus.setCellFactory(column -> new javafx.scene.control.TableCell<Order, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    Order order = getTableView().getItems().get(getIndex());
                    OrderStatus status = order.getStatus();

                    // Create styled label for status
                    javafx.scene.control.Label badge = new javafx.scene.control.Label(item);
                    badge.setStyle(
                        "-fx-background-color: " + status.getColor() + ";" +
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
        colCreatedDate.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getCreatedAt() != null
                ? data.getValue().getCreatedAt().format(DATE_FORMATTER)
                : "N/A"));
        colShipmentId.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getShipmentId() != null
                ? data.getValue().getShipmentId()
                : "N/A"));
        colPaymentId.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getPaymentId() != null
                ? data.getValue().getPaymentId()
                : "N/A"));

        // Context menu for actions
        ordersTable.setRowFactory(tv -> {
            TableRow<Order> row = new TableRow<>();
            ContextMenu contextMenu = new ContextMenu();

            MenuItem viewDetails = new MenuItem("Ver Detalles");
            viewDetails.setOnAction(e -> viewOrderDetails(row.getItem()));

            MenuItem viewHistory = new MenuItem("Ver Historial Completo");
            viewHistory.setOnAction(e -> showOrderHistory(row.getItem()));

            MenuItem approveOrder = new MenuItem("Aprobar y Crear Envío");
            approveOrder.setOnAction(e -> approveOrderAndCreateShipment(row.getItem()));

            // Only show approve button if order status is PENDING_APPROVAL
            approveOrder.visibleProperty().bind(
                javafx.beans.binding.Bindings.createBooleanBinding(() -> {
                    Order order = row.getItem();
                    return order != null && order.getStatus() == OrderStatus.PENDING_APPROVAL;
                }, row.itemProperty())
            );

            MenuItem rejectOrder = new MenuItem("Rechazar Orden");
            rejectOrder.setOnAction(e -> rejectOrder(row.getItem()));

            // Only show reject button if order status is NOT CANCELLED
            rejectOrder.visibleProperty().bind(
                javafx.beans.binding.Bindings.createBooleanBinding(() -> {
                    Order order = row.getItem();
                    return order != null && order.getStatus() != OrderStatus.CANCELLED;
                }, row.itemProperty())
            );

            contextMenu.getItems().addAll(viewDetails, viewHistory, approveOrder, rejectOrder);

            row.contextMenuProperty().bind(
                javafx.beans.binding.Bindings.when(row.emptyProperty())
                    .then((ContextMenu) null)
                    .otherwise(contextMenu)
            );

            return row;
        });
    }

    /**
     * Setup filters and search functionality.
     */
    private void setupFilters() {
        // Initialize status filter with ButtonCell
        if (filterStatus != null) {
            ObservableList<OrderStatus> statusList = FXCollections.observableArrayList();
            statusList.add(null); // "All" option
            statusList.addAll(OrderStatus.values());
            filterStatus.setItems(statusList);

            filterStatus.setCellFactory(lv -> new javafx.scene.control.ListCell<OrderStatus>() {
                @Override
                protected void updateItem(OrderStatus item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                    } else if (item == null) {
                        setText("Todos los estados");
                    } else {
                        setText(item.getDisplayName());
                    }
                }
            });

            filterStatus.setValue(null);

            filterStatus.setButtonCell(new javafx.scene.control.ListCell<OrderStatus>() {
                @Override
                protected void updateItem(OrderStatus item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText("Todos los estados");
                        setStyle("-fx-text-fill: #6c757d;");
                    } else {
                        setText(item.getDisplayName());
                        setStyle("-fx-text-fill: #495057;");
                    }
                }
            });

            filterStatus.setOnAction(e -> applyFilters());
        }

        // Search field
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        }
    }

    /**
     * Load all orders from repository.
     * Shows only orders with PENDING_APPROVAL or CANCELLED status.
     * Once approved, orders become shipments and are managed separately.
     */
    private void loadAllOrders() {
        List<Order> allOrders = orderRepository.findAll().stream()
                .filter(order -> order.getStatus() == OrderStatus.PENDING_APPROVAL ||
                               order.getStatus() == OrderStatus.CANCELLED ||
                               order.getStatus() == OrderStatus.AWAITING_PAYMENT ||
                               order.getStatus() == OrderStatus.PAID)
                .collect(Collectors.toList());
        ordersData = FXCollections.observableArrayList(allOrders);
        ordersTable.setItems(ordersData);
        ordersTable.refresh(); // Force table refresh to update UI
    }

    /**
     * Apply filters to the orders table.
     */
    @FXML
    private void applyFilters() {
        List<Order> allOrders = orderRepository.findAll().stream()
                .filter(order -> order.getStatus() == OrderStatus.PENDING_APPROVAL ||
                               order.getStatus() == OrderStatus.CANCELLED ||
                               order.getStatus() == OrderStatus.AWAITING_PAYMENT ||
                               order.getStatus() == OrderStatus.PAID)
                .collect(Collectors.toList());

        // Filter by status
        if (filterStatus != null && filterStatus.getValue() != null) {
            allOrders = allOrders.stream()
                    .filter(order -> order.getStatus() == filterStatus.getValue())
                    .collect(Collectors.toList());
        }

        // Filter by search text (searches by order ID, user email, and shipment ID)
        if (searchField != null && searchField.getText() != null && !searchField.getText().isEmpty()) {
            String searchText = searchField.getText().toLowerCase();
            allOrders = allOrders.stream()
                    .filter(order -> {
                        // Search by order ID
                        if (order.getId().toLowerCase().contains(searchText)) {
                            return true;
                        }

                        // Search by user email
                        if (order.getUserId() != null) {
                            User user = userRepository.findById(order.getUserId()).orElse(null);
                            if (user != null && user.getEmail().toLowerCase().contains(searchText)) {
                                return true;
                            }
                        }

                        // Search by shipment ID associated with this order
                        Optional<Shipment> shipmentOpt = shipmentRepository.findAll().stream()
                                .filter(s -> order.getId().equals(s.getOrderId()))
                                .findFirst();
                        if (shipmentOpt.isPresent() && shipmentOpt.get().getId().toLowerCase().contains(searchText)) {
                            return true;
                        }

                        return false;
                    })
                    .collect(Collectors.toList());
        }

        ordersData = FXCollections.observableArrayList(allOrders);
        ordersTable.setItems(ordersData);
        updateCounters();
    }

    /**
     * Clear all filters.
     */
    @FXML
    private void clearFilters() {
        if (filterStatus != null) filterStatus.setValue(null);
        if (searchField != null) searchField.clear();
        loadAllOrders();
        updateCounters();
    }

    /**
     * Refresh the table data.
     */
    @FXML
    private void refresh() {
        loadAllOrders();
        updateCounters();
        Logger.info("Orders table refreshed");
    }

    /**
     * Update counter labels.
     */
    private void updateCounters() {
        List<Order> allOrders = orderRepository.findAll();

        if (lblTotalOrders != null) lblTotalOrders.setText(String.valueOf(allOrders.size()));
        if (lblPending != null) lblPending.setText(String.valueOf(
            allOrders.stream().filter(o -> o.getStatus() == OrderStatus.AWAITING_PAYMENT).count()));
        if (lblProcessing != null) lblProcessing.setText(String.valueOf(
            allOrders.stream().filter(o -> o.getStatus() == OrderStatus.PENDING_APPROVAL).count()));
        if (lblReadyForShipment != null) lblReadyForShipment.setText(String.valueOf(
            allOrders.stream().filter(o -> o.getStatus() == OrderStatus.APPROVED).count()));
        if (lblCompleted != null) lblCompleted.setText(String.valueOf(
            allOrders.stream().filter(o -> o.getStatus() == OrderStatus.CANCELLED).count()));
    }

    /**
     * View order details.
     */
    private void viewOrderDetails(Order order) {
        if (order == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/co/edu/uniquindio/poo/ProyectoFinal2025_2/View/OrderDetail.fxml")
            );
            Parent root = loader.load();

            OrderDetailController controller = loader.getController();
            controller.loadOrderDetails(order.getId());

            Stage stage = new Stage();
            stage.setTitle("Detalles de la Orden - " + order.getId());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(true);
            stage.setMinWidth(600);
            stage.setMinHeight(700);

            // Create scene and apply stylesheet (more vertical layout)
            Scene scene = new Scene(root, 650, 800);
            String stylesheet = getClass().getResource("/co/edu/uniquindio/poo/proyectofinal2025_2/Style.css").toExternalForm();
            scene.getStylesheets().add(stylesheet);

            stage.setScene(scene);
            stage.showAndWait();

        } catch (Exception e) {
            Logger.error("Error loading order details: " + e.getMessage());
            DialogUtil.showError("Error", "No se pudo cargar los detalles de la orden: " + e.getMessage());
        }
    }

    /**
     * Approve an order.
     */
    private void approveOrder(Order order) {
        if (order == null) return;

        if (order.getStatus() == OrderStatus.AWAITING_PAYMENT) {
            DialogUtil.showWarning("Orden Pendiente de Pago",
                "La orden está esperando el pago. No se puede aprobar aún.");
            return;
        }

        boolean confirm = DialogUtil.showConfirmation(
            "Aprobar Orden",
            "¿Está seguro de que desea aprobar la orden " + order.getId() + "?");

        if (confirm) {
            // Change status to APPROVED
            order.setStatus(OrderStatus.APPROVED);
            orderRepository.update(order);
            refresh();
            DialogUtil.showSuccess("Éxito", "Orden aprobada correctamente");
            Logger.info("Order approved: " + order.getId());
        }
    }

    /**
     * Reject an order.
     */
    private void rejectOrder(Order order) {
        if (order == null) return;

        boolean confirm = DialogUtil.showConfirmation(
            "Rechazar Orden",
            "¿Está seguro de que desea rechazar la orden " + order.getId() + "?");

        if (confirm) {
            // Change status to CANCELLED
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.update(order);
            refresh();
            DialogUtil.showSuccess("Éxito", "Orden rechazada correctamente");
            Logger.info("Order rejected: " + order.getId());
        }
    }

    /**
     * Assign a delivery person to an order.
     */
    private void assignDeliveryPersonToOrder(Order order) {
        if (order == null) return;

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
        dialog.setHeaderText("Asignar repartidor a la orden " + order.getId());
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
                boolean success = orderService.assignDeliveryPerson(order.getId(), dp.getId());
                if (success) {
                    DialogUtil.showSuccess("Éxito", "Repartidor asignado correctamente");
                    refresh();
                    Logger.info("Delivery person " + dp.getId() + " assigned to order " + order.getId());
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
     * Approve an order and create the corresponding shipment.
     * In the new workflow, delivery person assignment happens at the shipment level.
     */
    private void approveOrderAndCreateShipment(Order order) {
        if (order == null) return;

        // Validate order status
        if (order.getStatus() == OrderStatus.AWAITING_PAYMENT) {
            DialogUtil.showWarning("Orden Pendiente de Pago",
                "La orden está esperando el pago. No se puede aprobar aún.");
            return;
        }

        if (order.getStatus() == OrderStatus.APPROVED) {
            DialogUtil.showWarning("Orden Ya Procesada",
                "Esta orden ya ha sido procesada y convertida en envío.");
            return;
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            DialogUtil.showWarning("Orden Cancelada",
                "Esta orden ha sido cancelada y no se puede aprobar.");
            return;
        }

        // Confirm approval
        boolean confirm = DialogUtil.showConfirmation(
            "Aprobar Orden y Crear Envío",
            "¿Está seguro de que desea aprobar la orden " + order.getId() + " y crear el envío?\n\n" +
            "El envío se creará con estado PENDING_ASSIGNMENT (pendiente de asignar repartidor).\n" +
            "Podrá asignar el repartidor desde la sección de Gestión de Envíos.");

        if (confirm) {
            try {
                // Use OrderService to approve and create shipment
                boolean success = orderService.approveOrderAndCreateShipment(order.getId());

                if (success) {
                    refresh();
                    DialogUtil.showSuccess("Éxito",
                        "Orden aprobada y envío creado correctamente.\n" +
                        "El envío ahora aparecerá en la sección de Gestión de Envíos con estado PENDING_ASSIGNMENT.\n" +
                        "Asigne un repartidor desde allí para continuar con el proceso.");
                    Logger.info("Order approved and shipment created: " + order.getId());
                } else {
                    DialogUtil.showError("Error", "No se pudo aprobar la orden y crear el envío");
                }
            } catch (Exception e) {
                Logger.error("Failed to approve order and create shipment: " + e.getMessage());
                DialogUtil.showError("Error", "Error al aprobar orden: " + e.getMessage());
            }
        }
    }

    /**
     * Auto-assign has been moved to ShipmentManagementController.
     * Assignment now happens at the shipment level, not order level.
     */

    /**
     * Handles the assign delivery person button click.
     * Assigns a delivery person to the selected order.
     */
    @FXML
    private void handleAssignDeliveryPerson() {
        Order selectedOrder = ordersTable.getSelectionModel().getSelectedItem();

        if (selectedOrder == null) {
            DialogUtil.showWarning("Seleccione una Orden",
                "Por favor, seleccione una orden de la tabla para asignar un repartidor.");
            return;
        }

        assignDeliveryPersonToOrder(selectedOrder);
    }

    /**
     * Show the complete history of an order (unified Order + Shipment timeline).
     * Shows order events always, and includes shipment events if available.
     */
    private void showOrderHistory(Order order) {
        if (order == null) return;

        try {
            // Load the ShipmentHistoryDialog
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "/co/edu/uniquindio/poo/ProyectoFinal2025_2/View/ShipmentHistoryDialog.fxml"));
            Parent root = loader.load();

            // Get controller and load history from order
            ShipmentHistoryDialogController controller = loader.getController();
            controller.loadHistoryFromOrder(order.getId());

            // Create and show modal dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Historial Completo - Orden " + order.getId());
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(ordersTable.getScene().getWindow());
            dialogStage.setResizable(true);
            dialogStage.setMinWidth(600);
            dialogStage.setMinHeight(500);

            // Create scene and apply stylesheet
            Scene scene = new Scene(root, 650, 550);
            String stylesheet = getClass().getResource("/co/edu/uniquindio/poo/proyectofinal2025_2/Style.css").toExternalForm();
            scene.getStylesheets().add(stylesheet);

            dialogStage.setScene(scene);
            dialogStage.showAndWait();

        } catch (Exception e) {
            Logger.error("Failed to show order history: " + e.getMessage());
            DialogUtil.showError("Error", "No se pudo cargar el historial: " + e.getMessage());
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

    // =================================================================================================================
    // Public API Methods (for navigation from other views)
    // =================================================================================================================

    /**
     * Applies a user filter when navigating from ManageUsers view.
     * Sets the search field to the user's email and ensures filters tab is open.
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

        // Ensure the filters tab is open and expanded
        javafx.application.Platform.runLater(() -> {
            setActiveTab(btnTabFilters);
            showTabContent(filtersTabContent, "filters", true);

            // Set the email in the search field
            searchField.setText(userEmail);

            // This will trigger the listener and apply the filter
            applyFilters();

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

        // Clear search field
        if (searchField != null) {
            searchField.clear();
        }

        // Reset filters
        applyFilters();

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
     * Handles export button click - shows format selection dialog and exports orders.
     */
    @FXML
    private void handleExport() {
        if (ordersData == null || ordersData.isEmpty()) {
            DialogUtil.showWarning("Sin Datos", "No hay órdenes para exportar.");
            return;
        }

        // Show dialog to choose export format
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exportar Órdenes");
        alert.setHeaderText("Seleccione el formato de exportación");
        alert.setContentText(String.format("Se exportarán %d órdenes (filtradas).", ordersData.size()));

        ButtonType btnCSV = new ButtonType("CSV");
        ButtonType btnPDF = new ButtonType("PDF");
        ButtonType btnCancel = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(btnCSV, btnPDF, btnCancel);

        alert.showAndWait().ifPresent(response -> {
            if (response == btnCSV) {
                exportToCSV();
            } else if (response == btnPDF) {
                exportToPDF();
            }
        });
    }

    /**
     * Exports orders to CSV format.
     */
    private void exportToCSV() {
        try {
            List<Order> orders = ordersData.stream().collect(Collectors.toList());

            File file = reportService.exportOrdersToCSV(orders);

            if (file != null && file.exists()) {
                DialogUtil.showSuccess("Exportación Exitosa",
                        "Archivo exportado a: " + file.getAbsolutePath());
                Logger.info("Orders exported to CSV: " + file.getAbsolutePath());
            } else {
                DialogUtil.showError("Error", "No se pudo crear el archivo de exportación.");
            }
        } catch (Exception e) {
            Logger.error("Error exporting orders to CSV: " + e.getMessage());
            DialogUtil.showError("Error", "Error al exportar: " + e.getMessage());
        }
    }

    /**
     * Exports orders to PDF format.
     */
    private void exportToPDF() {
        try {
            List<Order> orders = ordersData.stream().collect(Collectors.toList());

            File file = reportService.exportOrdersToPDF(orders);

            if (file != null && file.exists()) {
                DialogUtil.showSuccess("Exportación Exitosa",
                        "Archivo exportado a: " + file.getAbsolutePath());
                Logger.info("Orders exported to PDF: " + file.getAbsolutePath());
            } else {
                DialogUtil.showError("Error", "No se pudo crear el archivo de exportación.");
            }
        } catch (Exception e) {
            Logger.error("Error exporting orders to PDF: " + e.getMessage());
            DialogUtil.showError("Error", "Error al exportar: " + e.getMessage());
        }
    }

    /**
     * Handles the back button click - returns to the source view.
     */
    @FXML
    private void handleBack() {
        Logger.info("handleBack called - sourceView: " + sourceView + ", indexController: " + (indexController != null ? "set" : "null"));

        if (sourceView == null) {
            Logger.warning("sourceView is null, cannot navigate back");
            return;
        }

        if (indexController == null) {
            Logger.error("indexController is null, cannot navigate back");
            return;
        }

        Logger.info("Navigating back to: " + sourceView);
        indexController.loadView(sourceView);
    }
}
