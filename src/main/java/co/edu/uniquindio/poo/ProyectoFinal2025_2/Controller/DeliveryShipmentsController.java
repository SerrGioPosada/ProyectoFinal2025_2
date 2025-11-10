package co.edu.uniquindio.poo.ProyectoFinal2025_2.Controller;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.DeliveryPerson;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.ShipmentStatus;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Invoice;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Order;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Shipment;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Repositories.InvoiceRepository;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Repositories.OrderRepository;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.AuthenticationService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.OrderService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.ShipmentService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.DialogUtil;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.TabStateManager;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller for the Delivery Person Shipments view (DeliveryShipments.fxml).
 * <p>
 * This controller manages the view where delivery persons can see their assigned shipments
 * organized by tabs (All, Pending, In Transit, Delivered) with collapsible sections.
 * </p>
 */
public class DeliveryShipmentsController implements Initializable {

    private static final String VIEW_NAME = "DeliveryShipments";

    // =================================================================================================================
    // FXML Fields - Tab Buttons
    // =================================================================================================================

    @FXML private Button btnTabAll;
    @FXML private Button btnTabPending;
    @FXML private Button btnTabInTransit;
    @FXML private Button btnTabDelivered;
    @FXML private Button btnCollapseToggle;

    // =================================================================================================================
    // FXML Fields - Tab Content Areas
    // =================================================================================================================

    @FXML private VBox tabContentAll;
    @FXML private VBox tabContentPending;
    @FXML private VBox tabContentInTransit;
    @FXML private VBox tabContentDelivered;
    @FXML private VBox collapsibleSection;

    // =================================================================================================================
    // FXML Fields - Tables (One per tab)
    // =================================================================================================================

    // All Shipments Table
    @FXML private TableView<Shipment> shipmentsTableAll;
    @FXML private TableColumn<Shipment, String> colShipmentId;
    @FXML private TableColumn<Shipment, String> colOrigin;
    @FXML private TableColumn<Shipment, String> colDestination;
    @FXML private TableColumn<Shipment, String> colStatus;
    @FXML private TableColumn<Shipment, String> colDate;
    @FXML private TableColumn<Shipment, String> colCost;

    // Pending Shipments Table
    @FXML private TableView<Shipment> shipmentsTablePending;
    @FXML private TableColumn<Shipment, String> colPendingId;
    @FXML private TableColumn<Shipment, String> colPendingOrigin;
    @FXML private TableColumn<Shipment, String> colPendingDestination;
    @FXML private TableColumn<Shipment, String> colPendingDate;
    @FXML private TableColumn<Shipment, String> colPendingCost;
    @FXML private TableColumn<Shipment, String> colPendingPriority;

    // In Transit Shipments Table
    @FXML private TableView<Shipment> shipmentsTableInTransit;
    @FXML private TableColumn<Shipment, String> colTransitId;
    @FXML private TableColumn<Shipment, String> colTransitOrigin;
    @FXML private TableColumn<Shipment, String> colTransitDestination;
    @FXML private TableColumn<Shipment, String> colTransitPickupDate;
    @FXML private TableColumn<Shipment, String> colTransitEstimated;
    @FXML private TableColumn<Shipment, String> colTransitCost;

    // Delivered Shipments Table
    @FXML private TableView<Shipment> shipmentsTableDelivered;
    @FXML private TableColumn<Shipment, String> colDeliveredId;
    @FXML private TableColumn<Shipment, String> colDeliveredOrigin;
    @FXML private TableColumn<Shipment, String> colDeliveredDestination;
    @FXML private TableColumn<Shipment, String> colDeliveredDate;
    @FXML private TableColumn<Shipment, String> colDeliveredCost;
    @FXML private TableColumn<Shipment, String> colDeliveredRating;

    // =================================================================================================================
    // FXML Fields - Filters and Search
    // =================================================================================================================

    @FXML private ComboBox<ShipmentStatus> filterStatus;
    @FXML private TextField searchField;

    // =================================================================================================================
    // FXML Fields - Statistics
    // =================================================================================================================

    @FXML private Label lblTotal;
    @FXML private Label lblPending;
    @FXML private Label lblInTransit;
    @FXML private Label lblDelivered;

    // =================================================================================================================
    // FXML Fields - Actions
    // =================================================================================================================

    @FXML private ComboBox<ShipmentStatus> cmbNewStatus;
    @FXML private Button btnUpdateStatus;

    // =================================================================================================================
    // Services and State
    // =================================================================================================================

    private final ShipmentService shipmentService = new ShipmentService();
    private final AuthenticationService authService = AuthenticationService.getInstance();
    private final OrderService orderService = new OrderService();
    private final OrderRepository orderRepository = OrderRepository.getInstance();
    private final InvoiceRepository invoiceRepository = InvoiceRepository.getInstance();
    private DeliveryPerson currentDeliveryPerson;
    private ObservableList<Shipment> allShipments;
    private String currentTab = "All"; // Track current active tab

    // =================================================================================================================
    // Initialization
    // =================================================================================================================

    /**
     * Initializes the controller. Loads assigned shipments for the current delivery person.
     *
     * @param url            The location used to resolve relative paths.
     * @param resourceBundle The resources used to localize the root object.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        currentDeliveryPerson = (DeliveryPerson) authService.getCurrentPerson();

        if (currentDeliveryPerson == null) {
            Logger.error("No delivery person logged in");
            DialogUtil.showError("Error", "No hay repartidor autenticado");
            return;
        }

        setupTables();
        setupFilters();
        setupTabButtons();
        loadShipments();
        updateStatistics();
        restoreTabState();

        Logger.info("DeliveryShipmentsController initialized for delivery person: " + currentDeliveryPerson.getId());
    }

    // =================================================================================================================
    // Setup Methods
    // =================================================================================================================

    /**
     * Sets up all table columns with cell value factories.
     */
    private void setupTables() {
        setupAllShipmentsTable();
        setupPendingShipmentsTable();
        setupInTransitShipmentsTable();
        setupDeliveredShipmentsTable();
    }

    /**
     * Sets up the "All Shipments" table.
     */
    private void setupAllShipmentsTable() {
        colShipmentId.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getId()));
        colOrigin.setCellValueFactory(data -> new SimpleStringProperty(formatAddress(data.getValue().getOrigin())));
        colDestination.setCellValueFactory(data -> new SimpleStringProperty(formatAddress(data.getValue().getDestination())));
        colStatus.setCellValueFactory(data -> new SimpleStringProperty(getStatusLabel(data.getValue().getStatus())));
        colDate.setCellValueFactory(data -> new SimpleStringProperty(formatDate(data.getValue())));
        colCost.setCellValueFactory(data -> new SimpleStringProperty(String.format("$%,.2f", data.getValue().getTotalCost())));

        applyStatusBadgeStyle(colStatus);
        shipmentsTableAll.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        setupContextMenu(shipmentsTableAll);
    }

    /**
     * Sets up the "Pending Shipments" table.
     */
    private void setupPendingShipmentsTable() {
        colPendingId.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getId()));
        colPendingOrigin.setCellValueFactory(data -> new SimpleStringProperty(formatAddress(data.getValue().getOrigin())));
        colPendingDestination.setCellValueFactory(data -> new SimpleStringProperty(formatAddress(data.getValue().getDestination())));
        colPendingDate.setCellValueFactory(data -> new SimpleStringProperty(formatDate(data.getValue())));
        colPendingCost.setCellValueFactory(data -> new SimpleStringProperty(String.format("$%,.2f", data.getValue().getTotalCost())));
        colPendingPriority.setCellValueFactory(data -> new SimpleStringProperty(getPriorityLabel(data.getValue())));

        shipmentsTablePending.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        setupContextMenu(shipmentsTablePending);
    }

    /**
     * Sets up the "In Transit Shipments" table.
     */
    private void setupInTransitShipmentsTable() {
        colTransitId.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getId()));
        colTransitOrigin.setCellValueFactory(data -> new SimpleStringProperty(formatAddress(data.getValue().getOrigin())));
        colTransitDestination.setCellValueFactory(data -> new SimpleStringProperty(formatAddress(data.getValue().getDestination())));
        colTransitPickupDate.setCellValueFactory(data -> new SimpleStringProperty(formatDate(data.getValue())));
        colTransitEstimated.setCellValueFactory(data -> new SimpleStringProperty(formatEstimatedDelivery(data.getValue())));
        colTransitCost.setCellValueFactory(data -> new SimpleStringProperty(String.format("$%,.2f", data.getValue().getTotalCost())));

        shipmentsTableInTransit.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        setupContextMenu(shipmentsTableInTransit);
    }

    /**
     * Sets up the "Delivered Shipments" table.
     */
    private void setupDeliveredShipmentsTable() {
        colDeliveredId.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getId()));
        colDeliveredOrigin.setCellValueFactory(data -> new SimpleStringProperty(formatAddress(data.getValue().getOrigin())));
        colDeliveredDestination.setCellValueFactory(data -> new SimpleStringProperty(formatAddress(data.getValue().getDestination())));
        colDeliveredDate.setCellValueFactory(data -> new SimpleStringProperty(formatDeliveryDate(data.getValue())));
        colDeliveredCost.setCellValueFactory(data -> new SimpleStringProperty(String.format("$%,.2f", data.getValue().getTotalCost())));
        colDeliveredRating.setCellValueFactory(data -> new SimpleStringProperty("N/A")); // Placeholder

        shipmentsTableDelivered.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        setupContextMenu(shipmentsTableDelivered);
    }

    /**
     * Sets up context menu (right-click) for a shipment table.
     */
    private void setupContextMenu(TableView<Shipment> table) {
        ContextMenu contextMenu = new ContextMenu();

        // Ver Detalles
        MenuItem viewDetails = new MenuItem("Ver Detalles");
        viewDetails.setOnAction(event -> {
            Shipment selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showShipmentDetails(selected);
            }
        });

        // Ver Orden Asociada
        MenuItem viewOrder = new MenuItem("Ver Orden Asociada");
        viewOrder.setOnAction(event -> {
            Shipment selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showAssociatedOrder(selected);
            }
        });

        // Marcar En Tránsito
        MenuItem markInTransit = new MenuItem("Marcar En Tránsito");
        markInTransit.setOnAction(event -> {
            Shipment selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                updateShipmentStatus(selected, ShipmentStatus.IN_TRANSIT);
            }
        });

        // Marcar En Camino
        MenuItem markOutForDelivery = new MenuItem("Marcar En Camino");
        markOutForDelivery.setOnAction(event -> {
            Shipment selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                updateShipmentStatus(selected, ShipmentStatus.OUT_FOR_DELIVERY);
            }
        });

        // Marcar Entregado
        MenuItem markDelivered = new MenuItem("Marcar Entregado");
        markDelivered.setOnAction(event -> {
            Shipment selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                updateShipmentStatus(selected, ShipmentStatus.DELIVERED);
            }
        });

        // Marcar Devuelto
        MenuItem markReturned = new MenuItem("Marcar Devuelto");
        markReturned.setOnAction(event -> {
            Shipment selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                updateShipmentStatus(selected, ShipmentStatus.RETURNED);
            }
        });

        // Reportar Incidencia
        MenuItem reportIncident = new MenuItem("⚠️ Reportar Incidencia");
        reportIncident.setOnAction(event -> {
            Shipment selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                handleReportIncident(selected);
            }
        });

        contextMenu.getItems().addAll(
                viewDetails,
                viewOrder,
                new SeparatorMenuItem(),
                markInTransit,
                markOutForDelivery,
                markDelivered,
                markReturned,
                new SeparatorMenuItem(),
                reportIncident
        );

        // Show context menu only on rows with data
        table.setRowFactory(tv -> {
            TableRow<Shipment> row = new TableRow<>();
            row.setOnContextMenuRequested(event -> {
                if (!row.isEmpty()) {
                    table.getSelectionModel().select(row.getItem());
                    contextMenu.show(row, event.getScreenX(), event.getScreenY());
                }
            });
            return row;
        });
    }

    /**
     * Applies status badge styling to a status column.
     */
    private void applyStatusBadgeStyle(TableColumn<Shipment, String> column) {
        column.setCellFactory(col -> new TableCell<Shipment, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                    return;
                }

                Shipment shipment = getTableView().getItems().get(getIndex());
                if (shipment == null) return;

                Label badge = new Label(item);
                String color = getStatusColor(shipment.getStatus());
                badge.setStyle(
                    "-fx-background-color: " + color + ";" +
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
    }

    /**
     * Sets up filter combo boxes and search field listeners.
     */
    private void setupFilters() {
        // Status filter - Spanish translations
        filterStatus.getItems().clear();
        filterStatus.getItems().add(null); // "All" option
        filterStatus.getItems().addAll(ShipmentStatus.values());

        // Custom cell factory for Spanish translations
        filterStatus.setCellFactory(lv -> new ListCell<ShipmentStatus>() {
            @Override
            protected void updateItem(ShipmentStatus item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Todos los Estados" : getStatusLabel(item));
            }
        });

        filterStatus.setButtonCell(new ListCell<ShipmentStatus>() {
            @Override
            protected void updateItem(ShipmentStatus item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Todos los Estados");
                    setStyle("-fx-text-fill: #999999;");
                } else {
                    setText(getStatusLabel(item));
                    setStyle("-fx-text-fill: #032d4d;");
                }
            }
        });

        filterStatus.setOnAction(event -> applyFilters());
        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());

        // New status combo box for updates - Spanish translations
        // Only initialize if the element exists in FXML
        if (cmbNewStatus != null) {
            cmbNewStatus.getItems().clear();
            cmbNewStatus.getItems().addAll(
                    ShipmentStatus.IN_TRANSIT,
                    ShipmentStatus.OUT_FOR_DELIVERY,
                    ShipmentStatus.DELIVERED,
                    ShipmentStatus.RETURNED
            );

            cmbNewStatus.setCellFactory(lv -> new ListCell<ShipmentStatus>() {
                @Override
                protected void updateItem(ShipmentStatus item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : getStatusLabel(item));
                }
            });

            cmbNewStatus.setButtonCell(new ListCell<ShipmentStatus>() {
                @Override
                protected void updateItem(ShipmentStatus item, boolean empty) {
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
        }
    }

    /**
     * Sets up tab button styles and tracking.
     */
    private void setupTabButtons() {
        // Initially, "All" tab is active
        setActiveTabButton(btnTabAll);
    }

    // =================================================================================================================
    // Data Loading Methods
    // =================================================================================================================

    /**
     * Loads all shipments assigned to the current delivery person.
     */
    private void loadShipments() {
        List<Shipment> shipments = currentDeliveryPerson.getAssignedShipments();
        if (shipments == null) {
            shipments = List.of();
        }

        allShipments = FXCollections.observableArrayList(shipments);

        // Populate all tables
        loadAllShipmentsTable();
        loadPendingShipmentsTable();
        loadInTransitShipmentsTable();
        loadDeliveredShipmentsTable();

        Logger.info("Loaded " + shipments.size() + " shipments for delivery person");
    }

    /**
     * Loads data into the "All Shipments" table.
     */
    private void loadAllShipmentsTable() {
        shipmentsTableAll.setItems(allShipments);
    }

    /**
     * Loads data into the "Pending Shipments" table (READY_FOR_PICKUP).
     */
    private void loadPendingShipmentsTable() {
        List<Shipment> pending = allShipments.stream()
                .filter(s -> s.getStatus() == ShipmentStatus.READY_FOR_PICKUP ||
                            s.getStatus() == ShipmentStatus.PENDING_ASSIGNMENT)
                .collect(Collectors.toList());
        shipmentsTablePending.setItems(FXCollections.observableArrayList(pending));
    }

    /**
     * Loads data into the "In Transit Shipments" table.
     */
    private void loadInTransitShipmentsTable() {
        List<Shipment> inTransit = allShipments.stream()
                .filter(s -> s.getStatus() == ShipmentStatus.IN_TRANSIT ||
                            s.getStatus() == ShipmentStatus.OUT_FOR_DELIVERY)
                .collect(Collectors.toList());
        shipmentsTableInTransit.setItems(FXCollections.observableArrayList(inTransit));
    }

    /**
     * Loads data into the "Delivered Shipments" table.
     */
    private void loadDeliveredShipmentsTable() {
        List<Shipment> delivered = allShipments.stream()
                .filter(s -> s.getStatus() == ShipmentStatus.DELIVERED)
                .collect(Collectors.toList());
        shipmentsTableDelivered.setItems(FXCollections.observableArrayList(delivered));
    }

    /**
     * Updates the statistics labels based on current shipments.
     */
    private void updateStatistics() {
        if (allShipments == null) return;

        int total = allShipments.size();
        long pending = allShipments.stream()
                .filter(s -> s.getStatus() == ShipmentStatus.READY_FOR_PICKUP ||
                            s.getStatus() == ShipmentStatus.PENDING_ASSIGNMENT)
                .count();
        long inTransit = allShipments.stream()
                .filter(s -> s.getStatus() == ShipmentStatus.IN_TRANSIT ||
                            s.getStatus() == ShipmentStatus.OUT_FOR_DELIVERY)
                .count();
        long delivered = allShipments.stream()
                .filter(s -> s.getStatus() == ShipmentStatus.DELIVERED)
                .count();

        lblTotal.setText(String.valueOf(total));
        lblPending.setText(String.valueOf(pending));
        lblInTransit.setText(String.valueOf(inTransit));
        lblDelivered.setText(String.valueOf(delivered));
    }

    /**
     * Applies filters based on status and search text to the current tab's table.
     */
    private void applyFilters() {
        if (allShipments == null) return;

        List<Shipment> filtered = allShipments.stream()
                .filter(this::matchesStatusFilter)
                .filter(this::matchesSearchFilter)
                .collect(Collectors.toList());

        // Apply filters to current visible table only
        switch (currentTab) {
            case "All" -> shipmentsTableAll.setItems(FXCollections.observableArrayList(filtered));
            case "Pending" -> {
                List<Shipment> pendingFiltered = filtered.stream()
                        .filter(s -> s.getStatus() == ShipmentStatus.READY_FOR_PICKUP ||
                                    s.getStatus() == ShipmentStatus.PENDING_ASSIGNMENT)
                        .collect(Collectors.toList());
                shipmentsTablePending.setItems(FXCollections.observableArrayList(pendingFiltered));
            }
            case "InTransit" -> {
                List<Shipment> transitFiltered = filtered.stream()
                        .filter(s -> s.getStatus() == ShipmentStatus.IN_TRANSIT ||
                                    s.getStatus() == ShipmentStatus.OUT_FOR_DELIVERY)
                        .collect(Collectors.toList());
                shipmentsTableInTransit.setItems(FXCollections.observableArrayList(transitFiltered));
            }
            case "Delivered" -> {
                List<Shipment> deliveredFiltered = filtered.stream()
                        .filter(s -> s.getStatus() == ShipmentStatus.DELIVERED)
                        .collect(Collectors.toList());
                shipmentsTableDelivered.setItems(FXCollections.observableArrayList(deliveredFiltered));
            }
        }
    }

    /**
     * Checks if a shipment matches the selected status filter.
     */
    private boolean matchesStatusFilter(Shipment shipment) {
        ShipmentStatus selectedStatus = filterStatus.getValue();
        return selectedStatus == null || shipment.getStatus() == selectedStatus;
    }

    /**
     * Checks if a shipment matches the search text.
     */
    private boolean matchesSearchFilter(Shipment shipment) {
        String searchText = searchField.getText();
        if (searchText == null || searchText.trim().isEmpty()) {
            return true;
        }

        String lowerSearch = searchText.toLowerCase();
        return shipment.getId().toLowerCase().contains(lowerSearch) ||
               formatAddress(shipment.getOrigin()).toLowerCase().contains(lowerSearch) ||
               formatAddress(shipment.getDestination()).toLowerCase().contains(lowerSearch);
    }

    // =================================================================================================================
    // Tab Management and State Persistence
    // =================================================================================================================

    /**
     * Restores the last active tab and collapsed state from preferences.
     */
    private void restoreTabState() {
        String activeTab = TabStateManager.getActiveTab(VIEW_NAME);
        if (activeTab == null) {
            activeTab = "All"; // Default tab
        }
        boolean isExpanded = TabStateManager.isExpanded(VIEW_NAME);

        // Restore tab
        switch (activeTab) {
            case "Pending" -> showTabContent(tabContentPending, btnTabPending, "Pending", false);
            case "InTransit" -> showTabContent(tabContentInTransit, btnTabInTransit, "InTransit", false);
            case "Delivered" -> showTabContent(tabContentDelivered, btnTabDelivered, "Delivered", false);
            default -> showTabContent(tabContentAll, btnTabAll, "All", false);
        }

        // Restore collapsed state
        applyCollapseState(isExpanded);
    }

    /**
     * Shows the specified tab content and hides others.
     *
     * @param contentToShow The VBox tab content to show
     * @param tabButton The button that was clicked
     * @param tabName The internal tab name
     * @param expandIfCollapsed Whether to expand if currently collapsed
     */
    private void showTabContent(VBox contentToShow, Button tabButton, String tabName, boolean expandIfCollapsed) {
        // Handle expand if collapsed
        if (expandIfCollapsed) {
            boolean isExpanded = TabStateManager.isExpanded(VIEW_NAME);
            if (!isExpanded) {
                TabStateManager.setExpanded(VIEW_NAME, true);
                applyCollapseState(true);
            }
        }

        // Hide all tab contents
        tabContentAll.setVisible(false);
        tabContentAll.setManaged(false);
        tabContentPending.setVisible(false);
        tabContentPending.setManaged(false);
        tabContentInTransit.setVisible(false);
        tabContentInTransit.setManaged(false);
        tabContentDelivered.setVisible(false);
        tabContentDelivered.setManaged(false);

        // Show selected content
        contentToShow.setVisible(true);
        contentToShow.setManaged(true);

        // Update active tab button styling
        setActiveTabButton(tabButton);

        // Update current tab tracker
        currentTab = tabName;

        // Save tab state
        TabStateManager.setActiveTab(VIEW_NAME, tabName);

        Logger.info("Switched to tab: " + tabName);
    }

    /**
     * Sets the active tab button style.
     */
    private void setActiveTabButton(Button activeButton) {
        // Remove active style from all buttons
        btnTabAll.getStyleClass().removeAll("tab-button-active");
        btnTabPending.getStyleClass().removeAll("tab-button-active");
        btnTabInTransit.getStyleClass().removeAll("tab-button-active");
        btnTabDelivered.getStyleClass().removeAll("tab-button-active");

        btnTabAll.getStyleClass().add("tab-button");
        btnTabPending.getStyleClass().add("tab-button");
        btnTabInTransit.getStyleClass().add("tab-button");
        btnTabDelivered.getStyleClass().add("tab-button");

        // Add active style to selected button
        activeButton.getStyleClass().removeAll("tab-button");
        activeButton.getStyleClass().add("tab-button-active");
    }

    /**
     * Applies the collapse/expand state to the collapsible section.
     */
    private void applyCollapseState(boolean isExpanded) {
        collapsibleSection.setVisible(isExpanded);
        collapsibleSection.setManaged(isExpanded);
        btnCollapseToggle.setText(isExpanded ? "▲" : "▼");
    }

    /**
     * Toggles the collapsed/expanded state of the table section.
     */
    @FXML
    private void toggleCollapse() {
        boolean isCurrentlyExpanded = TabStateManager.isExpanded(VIEW_NAME);
        boolean newState = !isCurrentlyExpanded;

        TabStateManager.setExpanded(VIEW_NAME, newState);
        applyCollapseState(newState);

        Logger.info("Table section " + (newState ? "expanded" : "collapsed"));
    }

    // =================================================================================================================
    // Tab Event Handlers
    // =================================================================================================================

    @FXML
    private void handleShowAllShipments() {
        showTabContent(tabContentAll, btnTabAll, "All", true);
    }

    @FXML
    private void handleShowPendingShipments() {
        showTabContent(tabContentPending, btnTabPending, "Pending", true);
    }

    @FXML
    private void handleShowInTransitShipments() {
        showTabContent(tabContentInTransit, btnTabInTransit, "InTransit", true);
    }

    @FXML
    private void handleShowDeliveredShipments() {
        showTabContent(tabContentDelivered, btnTabDelivered, "Delivered", true);
    }

    // =================================================================================================================
    // Action Event Handlers
    // =================================================================================================================

    /**
     * Handles the update status button click.
     */
    @FXML
    private void handleUpdateStatus() {
        // Check if cmbNewStatus exists in the FXML
        if (cmbNewStatus == null) {
            Logger.warning("cmbNewStatus not found in FXML");
            DialogUtil.showError("Error", "El componente de actualización de estado no está disponible.");
            return;
        }

        // Get the selected shipment from the currently visible table
        Shipment selectedShipment = getSelectedShipmentFromCurrentTab();

        if (selectedShipment == null) {
            DialogUtil.showWarning("Advertencia", "Por favor selecciona un envío de la tabla.");
            return;
        }

        ShipmentStatus newStatus = cmbNewStatus.getValue();
        if (newStatus == null) {
            DialogUtil.showWarning("Advertencia", "Por favor selecciona un nuevo estado.");
            return;
        }

        boolean updated = shipmentService.changeStatus(
                selectedShipment.getId(),
                newStatus,
                "Updated by delivery person",
                currentDeliveryPerson.getId()
        );

        if (updated) {
            selectedShipment.setStatus(newStatus);
            loadShipments(); // Reload all tables
            updateStatistics();
            DialogUtil.showSuccess("Éxito", "Estado del envío actualizado correctamente.");
            Logger.info("Shipment " + selectedShipment.getId() + " updated to " + newStatus);
        } else {
            DialogUtil.showError("Error", "No se pudo actualizar el estado del envío.");
        }
    }

    /**
     * Gets the selected shipment from the currently visible tab.
     */
    private Shipment getSelectedShipmentFromCurrentTab() {
        return switch (currentTab) {
            case "All" -> shipmentsTableAll.getSelectionModel().getSelectedItem();
            case "Pending" -> shipmentsTablePending.getSelectionModel().getSelectedItem();
            case "InTransit" -> shipmentsTableInTransit.getSelectionModel().getSelectedItem();
            case "Delivered" -> shipmentsTableDelivered.getSelectionModel().getSelectedItem();
            default -> null;
        };
    }

    /**
     * Handles the view details button click.
     */
    @FXML
    private void handleViewDetails() {
        Shipment selectedShipment = getSelectedShipmentFromCurrentTab();

        if (selectedShipment == null) {
            DialogUtil.showWarning("Advertencia", "Por favor selecciona un envío de la tabla.");
            return;
        }

        showShipmentDetails(selectedShipment);
    }

    /**
     * Shows detailed information about a shipment.
     */
    private void showShipmentDetails(Shipment shipment) {
        DialogUtil.showInfo("Detalles del Envío",
                "ID: " + shipment.getId() + "\n" +
                "Estado: " + getStatusLabel(shipment.getStatus()) + "\n" +
                "Origen: " + formatAddress(shipment.getOrigin()) + "\n" +
                "Destino: " + formatAddress(shipment.getDestination()) + "\n" +
                "Costo: $" + String.format("%,.2f", shipment.getTotalCost()) + "\n" +
                "Fecha Creación: " + formatDate(shipment) + "\n" +
                "Entrega Estimada: " + formatEstimatedDelivery(shipment)
        );
    }

    /**
     * Shows the associated order for a shipment.
     */
    private void showAssociatedOrder(Shipment shipment) {
        if (shipment == null || shipment.getOrderId() == null) {
            DialogUtil.showWarning("Sin Orden Asociada", "Este envío no tiene una orden asociada.");
            return;
        }

        try {
            Optional<Order> orderOpt = orderRepository.findById(shipment.getOrderId());
            if (!orderOpt.isPresent()) {
                DialogUtil.showError("Error", "No se pudo encontrar la orden asociada.");
                return;
            }

            Order order = orderOpt.get();
            StringBuilder details = new StringBuilder();
            details.append("===== ORDEN ASOCIADA =====\n\n");
            details.append("ID Orden: ").append(order.getId()).append("\n");
            details.append("Estado: ").append(order.getStatus().getDisplayName()).append("\n");
            details.append("Fecha Creación: ").append(order.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n");

            // Get cost from invoice
            String costStr = "N/A";
            if (order.getInvoiceId() != null) {
                Optional<Invoice> invoiceOpt = invoiceRepository.findById(order.getInvoiceId());
                if (invoiceOpt.isPresent()) {
                    costStr = String.format("%.2f", invoiceOpt.get().getTotalAmount());
                }
            }
            details.append("Costo Total: $").append(costStr).append("\n");
            details.append("\n--- Origen ---\n");
            if (order.getOrigin() != null) {
                details.append(order.getOrigin().getStreet()).append(", ")
                       .append(order.getOrigin().getCity()).append("\n");
            }
            details.append("\n--- Destino ---\n");
            if (order.getDestination() != null) {
                details.append(order.getDestination().getStreet()).append(", ")
                       .append(order.getDestination().getCity()).append("\n");
            }
            if (order.getPaymentId() != null) {
                details.append("\nID Pago: ").append(order.getPaymentId()).append("\n");
            }
            if (order.getInvoiceId() != null) {
                details.append("ID Factura: ").append(order.getInvoiceId()).append("\n");
            }

            DialogUtil.showInfo("Orden Asociada al Envío", details.toString());
            Logger.info("Delivery person viewed associated order " + order.getId() + " for shipment " + shipment.getId());

        } catch (Exception e) {
            Logger.error("Failed to view associated order: " + e.getMessage());
            DialogUtil.showError("Error", "No se pudo mostrar la orden asociada: " + e.getMessage());
        }
    }

    /**
     * Updates a shipment's status via context menu.
     */
    private void updateShipmentStatus(Shipment shipment, ShipmentStatus newStatus) {
        if (shipment.getStatus() == newStatus) {
            DialogUtil.showInfo("Información", "El envío ya tiene este estado.");
            return;
        }

        boolean updated = shipmentService.changeStatus(
                shipment.getId(),
                newStatus,
                "Updated by delivery person via context menu",
                currentDeliveryPerson.getId()
        );

        if (updated) {
            shipment.setStatus(newStatus);
            loadShipments();
            updateStatistics();
            DialogUtil.showSuccess("Éxito",
                    "Estado actualizado a: " + getStatusLabel(newStatus));
            Logger.info("Shipment " + shipment.getId() + " updated to " + newStatus + " via context menu");
        } else {
            DialogUtil.showError("Error", "No se pudo actualizar el estado del envío.");
        }
    }

    /**
     * Handles the refresh button click.
     */
    @FXML
    private void handleRefresh() {
        loadShipments();
        updateStatistics();
        applyFilters();
        DialogUtil.showSuccess("Actualizado", "Lista de envíos actualizada.");
    }

    /**
     * Handles the clear filters button click.
     */
    @FXML
    private void handleClearFilters() {
        filterStatus.setValue(null);
        searchField.clear();
        loadShipments();
        Logger.info("Filters cleared");
    }

    // =================================================================================================================
    // Helper Methods
    // =================================================================================================================

    /**
     * Formats an address object to a display string.
     */
    private String formatAddress(co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Address address) {
        if (address == null) return "--";
        return address.getCity() + ", " + address.getState();
    }

    /**
     * Formats the creation date from a shipment.
     */
    private String formatDate(Shipment shipment) {
        if (shipment.getCreatedAt() == null) return "--";
        return shipment.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    /**
     * Formats the delivery date (if delivered).
     */
    private String formatDeliveryDate(Shipment shipment) {
        if (shipment.getDeliveredDate() == null) return "--";
        return shipment.getDeliveredDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    /**
     * Formats the estimated delivery date.
     */
    private String formatEstimatedDelivery(Shipment shipment) {
        if (shipment.getEstimatedDate() == null) return "--";
        return shipment.getEstimatedDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    /**
     * Gets a priority label for a shipment (based on additional services).
     */
    private String getPriorityLabel(Shipment shipment) {
        if (shipment.getAdditionalServices() == null || shipment.getAdditionalServices().isEmpty()) {
            return "Normal";
        }

        boolean hasPriority = shipment.getAdditionalServices().stream()
                .anyMatch(service -> service.getType() != null &&
                         (service.getType().name().contains("PRIORITY") ||
                          service.getType().name().contains("EXPRESS")));

        return hasPriority ? "Alta" : "Normal";
    }

    /**
     * Converts a ShipmentStatus to a user-friendly Spanish label.
     */
    private String getStatusLabel(ShipmentStatus status) {
        if (status == null) return "Desconocido";
        return status.getDisplayName();
    }

    /**
     * Gets the color for a shipment status.
     */
    private String getStatusColor(ShipmentStatus status) {
        if (status == null) return "#000000";
        return switch (status) {
            case PENDING_ASSIGNMENT -> "#FF9800"; // Orange
            case READY_FOR_PICKUP -> "#FFA500"; // Orange
            case IN_TRANSIT -> "#007BFF"; // Blue
            case OUT_FOR_DELIVERY -> "#66BB6A"; // Light Green
            case DELIVERED -> "#28A745"; // Green
            case RETURNED -> "#FF9800"; // Orange
            case CANCELLED -> "#DC3545"; // Red
            default -> "#6C757D"; // Gray
        };
    }

    /**
     * Handles reporting an incident for a shipment.
     * Opens the Report Incident Dialog.
     */
    private void handleReportIncident(Shipment shipment) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/co/edu/uniquindio/poo/ProyectoFinal2025_2/View/ReportIncidentDialog.fxml")
            );
            javafx.scene.Parent root = loader.load();

            ReportIncidentDialogController controller = loader.getController();

            // Set the shipment for the incident report
            controller.setShipment(shipment);

            javafx.stage.Stage dialogStage = new javafx.stage.Stage();
            dialogStage.setTitle("Reportar Incidencia");
            dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialogStage.setScene(new javafx.scene.Scene(root));
            dialogStage.setResizable(false);

            dialogStage.showAndWait();

            // If incident was reported, refresh the view
            if (controller.isIncidentReported()) {
                loadShipments();
                updateStatistics();
                DialogUtil.showSuccess("La incidencia ha sido reportada exitosamente.");
            }

        } catch (Exception e) {
            Logger.error("Error opening Report Incident Dialog: " + e.getMessage());
            DialogUtil.showError("Error", "No se pudo abrir el diálogo de reporte de incidencias.");
        }
    }
}
