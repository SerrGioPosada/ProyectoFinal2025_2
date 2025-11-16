package co.edu.uniquindio.poo.ProyectoFinal2025_2.Controller;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.DeliveryPerson;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.ShipmentStatus;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.dto.ShipmentDTO;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.AuthenticationService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.ShipmentService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.DialogUtil;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.TabStateManager;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.DistanceCalculator;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for the Delivery History view (DeliveryHistory.fxml).
 * <p>
 * This controller manages the delivery history and earnings tracking for delivery persons,
 * showing completed deliveries, earnings statistics, and charts with a collapsible tab system.
 * </p>
 */
public class DeliveryHistoryController implements Initializable {

    // =================================================================================================================
    // FXML Fields - Tab System
    // =================================================================================================================

    @FXML private VBox collapsibleTabSection;
    @FXML private Button btnCollapseToggle;
    @FXML private Button btnTabEarnings;
    @FXML private Button btnTabDeliveryStats;
    @FXML private Button btnTabFilters;

    @FXML private VBox earningsTabContent;
    @FXML private VBox deliveryStatsTabContent;
    @FXML private VBox filtersTabContent;

    // =================================================================================================================
    // FXML Fields - Statistics
    // =================================================================================================================

    @FXML private Label lblTotalEarnings;
    @FXML private Label lblMonthEarnings;
    @FXML private Label lblWeekEarnings;
    @FXML private Label lblTodayEarnings;
    @FXML private Label lblAverageEarnings;

    @FXML private Label lblCompletedDeliveries;
    @FXML private Label lblSuccessfulDeliveries;
    @FXML private Label lblIncidentDeliveries;
    @FXML private Label lblSuccessRate;
    @FXML private Label lblLastUpdated;

    // =================================================================================================================
    // FXML Fields - Charts
    // =================================================================================================================

    @FXML private LineChart<String, Number> chartEarningsTrend;
    @FXML private PieChart chartDeliveriesByStatus;

    // =================================================================================================================
    // FXML Fields - Filters and Table
    // =================================================================================================================

    @FXML private DatePicker dateFrom;
    @FXML private DatePicker dateTo;
    @FXML private TextField searchField;

    @FXML private TableView<DeliveryHistoryEntry> historyTable;
    @FXML private TableColumn<DeliveryHistoryEntry, String> colDate;
    @FXML private TableColumn<DeliveryHistoryEntry, String> colOrderId;
    @FXML private TableColumn<DeliveryHistoryEntry, String> colRoute;
    @FXML private TableColumn<DeliveryHistoryEntry, String> colStatus;
    @FXML private TableColumn<DeliveryHistoryEntry, String> colDistance;
    @FXML private TableColumn<DeliveryHistoryEntry, String> colEarnings;
    @FXML private TableColumn<DeliveryHistoryEntry, String> colRating;

    // =================================================================================================================
    // Services and Data
    // =================================================================================================================

    private final AuthenticationService authService;
    private final ShipmentService shipmentService;

    private DeliveryPerson currentDeliveryPerson;
    private List<ShipmentDTO> completedShipments;
    private ObservableList<DeliveryHistoryEntry> historyEntries;
    private FilteredList<DeliveryHistoryEntry> filteredHistory;

    private static final String VIEW_NAME = "DeliveryHistory";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // =================================================================================================================
    // Constructor
    // =================================================================================================================

    public DeliveryHistoryController() {
        this.authService = AuthenticationService.getInstance();
        this.shipmentService = new ShipmentService();
        this.completedShipments = new ArrayList<>();
        this.historyEntries = FXCollections.observableArrayList();
    }

    // =================================================================================================================
    // Initialization
    // =================================================================================================================

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Logger.info("Initializing DeliveryHistoryController");

        this.currentDeliveryPerson = (DeliveryPerson) authService.getCurrentPerson();

        if (currentDeliveryPerson == null) {
            DialogUtil.showError("Error", "No se pudo obtener la información del repartidor.");
            return;
        }

        initializeTableColumns();
        initializeDatePickers();
        setupDynamicFilters();
        loadHistoryData();
        restoreViewState();

        Logger.info("DeliveryHistoryController initialized successfully");
    }

    private void initializeTableColumns() {
        colDate.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDate()));
        colOrderId.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getOrderId()));
        colRoute.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRoute()));
        colStatus.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));
        colDistance.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDistance()));
        colEarnings.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEarnings()));
        colRating.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRating()));

        // Apply styled cell factory for status column (badges)
        colStatus.setCellFactory(column -> new TableCell<DeliveryHistoryEntry, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    Label badge = new Label(item);
                    String backgroundColor = getStatusColor(item);
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

    private void setupContextMenu() {
        historyTable.setRowFactory(tv -> {
            TableRow<DeliveryHistoryEntry> row = new TableRow<>();
            ContextMenu contextMenu = new ContextMenu();

            MenuItem viewDetails = new MenuItem("Ver Detalles");
            viewDetails.setOnAction(event -> {
                DeliveryHistoryEntry selected = row.getItem();
                if (selected != null) {
                    showDeliveryDetails(selected);
                }
            });

            contextMenu.getItems().add(viewDetails);

            row.contextMenuProperty().bind(
                javafx.beans.binding.Bindings.when(row.emptyProperty())
                    .then((ContextMenu) null)
                    .otherwise(contextMenu)
            );

            return row;
        });
    }

    private void initializeDatePickers() {
        dateTo.setValue(LocalDate.now());
        dateFrom.setValue(LocalDate.now().minusDays(30));
    }

    private void setupDynamicFilters() {
        // Dynamic date filter
        if (dateFrom != null) {
            dateFrom.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        }
        if (dateTo != null) {
            dateTo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        }

        // Dynamic search filter
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        }
    }

    // =================================================================================================================
    // Data Loading
    // =================================================================================================================

    private void loadHistoryData() {
        try {
            completedShipments = shipmentService.listAll().stream()
                    .filter(shipment -> currentDeliveryPerson.getId().equals(shipment.getDeliveryPersonId()))
                    .filter(shipment -> shipment.getStatus() == ShipmentStatus.DELIVERED ||
                            shipment.getStatus() == ShipmentStatus.CANCELLED ||
                            shipment.getStatus() == ShipmentStatus.RETURNED)
                    .collect(Collectors.toList());

            updateHistoryEntries();
            updateStatistics();
            updateCharts();

            if (lblLastUpdated != null) {
                lblLastUpdated.setText("Última actualización: " +
                        LocalDateTime.now().format(DATETIME_FORMATTER));
            }

            Logger.info("Loaded " + completedShipments.size() + " completed shipments");

        } catch (Exception e) {
            Logger.error("Error loading history data: " + e.getMessage());
            DialogUtil.showError("Error", "No se pudieron cargar los datos del historial.");
        }
    }

    private void updateHistoryEntries() {
        historyEntries.clear();

        for (ShipmentDTO shipment : completedShipments) {
            String date = shipment.getCreationDate() != null ?
                    shipment.getCreationDate().format(DATE_FORMATTER) : "--";
            String orderId = shipment.getOrderId() != null ? shipment.getOrderId() : "--";
            String route = buildRouteString(shipment);
            String status = shipment.getStatus() != null ? getStatusSpanish(shipment.getStatus()) : "--";
            String distance = calculateDistance(shipment) + " km";
            String earnings = "$" + String.format("%.2f", shipment.getTotalCost());
            String rating = "⭐ 5.0"; // Placeholder

            historyEntries.add(new DeliveryHistoryEntry(
                    date, orderId, route, status, distance, earnings, rating, shipment
            ));
        }

        filteredHistory = new FilteredList<>(historyEntries, p -> true);
        historyTable.setItems(filteredHistory);
    }

    private void updateStatistics() {
        double totalEarnings = completedShipments.stream()
                .mapToDouble(ShipmentDTO::getTotalCost)
                .sum();

        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);
        LocalDate weekStart = now.minusDays(7);

        double monthEarnings = completedShipments.stream()
                .filter(s -> s.getCreationDate() != null && s.getCreationDate().toLocalDate().isAfter(monthStart.minusDays(1)))
                .mapToDouble(ShipmentDTO::getTotalCost)
                .sum();

        double weekEarnings = completedShipments.stream()
                .filter(s -> s.getCreationDate() != null && s.getCreationDate().toLocalDate().isAfter(weekStart.minusDays(1)))
                .mapToDouble(ShipmentDTO::getTotalCost)
                .sum();

        double todayEarnings = completedShipments.stream()
                .filter(s -> s.getCreationDate() != null && s.getCreationDate().toLocalDate().equals(now))
                .mapToDouble(ShipmentDTO::getTotalCost)
                .sum();

        double averageEarnings = completedShipments.isEmpty() ? 0.0 : totalEarnings / completedShipments.size();

        lblTotalEarnings.setText("$" + String.format("%.2f", totalEarnings));
        lblMonthEarnings.setText("$" + String.format("%.2f", monthEarnings));
        lblWeekEarnings.setText("$" + String.format("%.2f", weekEarnings));
        lblTodayEarnings.setText("$" + String.format("%.2f", todayEarnings));
        lblAverageEarnings.setText("$" + String.format("%.2f", averageEarnings));

        // Delivery statistics
        long completedCount = completedShipments.stream()
                .filter(s -> s.getStatus() == ShipmentStatus.DELIVERED)
                .count();

        long successfulCount = completedCount; // All delivered are successful for now
        long incidentCount = completedShipments.stream()
                .filter(s -> s.getStatus() == ShipmentStatus.CANCELLED || s.getStatus() == ShipmentStatus.RETURNED)
                .count();

        double successRate = completedShipments.isEmpty() ? 0.0 :
                (successfulCount * 100.0) / completedShipments.size();

        lblCompletedDeliveries.setText(String.valueOf(completedShipments.size()));
        lblSuccessfulDeliveries.setText(String.valueOf(successfulCount));
        lblIncidentDeliveries.setText(String.valueOf(incidentCount));
        lblSuccessRate.setText(String.format("%.1f%%", successRate));
    }

    private void updateCharts() {
        updateEarningsTrendChart();
        updateDeliveriesByStatusChart();
    }

    private void updateEarningsTrendChart() {
        chartEarningsTrend.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Ganancias Diarias");

        Map<LocalDate, Double> dailyEarnings = new TreeMap<>();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(29);

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            dailyEarnings.put(date, 0.0);
        }

        for (ShipmentDTO shipment : completedShipments) {
            if (shipment.getCreationDate() != null) {
                LocalDate shipmentDate = shipment.getCreationDate().toLocalDate();
                if (!shipmentDate.isBefore(startDate) && !shipmentDate.isAfter(endDate)) {
                    double cost = shipment.getTotalCost();
                    dailyEarnings.merge(shipmentDate, cost, Double::sum);
                }
            }
        }

        for (Map.Entry<LocalDate, Double> entry : dailyEarnings.entrySet()) {
            series.getData().add(new XYChart.Data<>(
                    entry.getKey().format(DateTimeFormatter.ofPattern("dd/MM")),
                    entry.getValue()
            ));
        }

        chartEarningsTrend.getData().add(series);
    }

    private void updateDeliveriesByStatusChart() {
        chartDeliveriesByStatus.getData().clear();

        Map<String, Long> statusCounts = completedShipments.stream()
                .collect(Collectors.groupingBy(
                        s -> getStatusSpanish(s.getStatus()),
                        Collectors.counting()
                ));

        for (Map.Entry<String, Long> entry : statusCounts.entrySet()) {
            PieChart.Data slice = new PieChart.Data(
                    entry.getKey() + " (" + entry.getValue() + ")",
                    entry.getValue()
            );
            chartDeliveriesByStatus.getData().add(slice);
        }
    }

    private void applyFilters() {
        if (filteredHistory == null) return;

        filteredHistory.setPredicate(entry -> {
            // Date filter
            if (dateFrom != null && dateFrom.getValue() != null &&
                dateTo != null && dateTo.getValue() != null) {

                try {
                    LocalDate entryDate = LocalDate.parse(entry.getDate(), DATE_FORMATTER);
                    if (entryDate.isBefore(dateFrom.getValue()) ||
                        entryDate.isAfter(dateTo.getValue())) {
                        return false;
                    }
                } catch (Exception e) {
                    // Skip if date parsing fails
                }
            }

            // Search filter
            if (searchField != null && searchField.getText() != null &&
                !searchField.getText().trim().isEmpty()) {
                String search = searchField.getText().toLowerCase();
                return entry.getOrderId().toLowerCase().contains(search) ||
                       entry.getRoute().toLowerCase().contains(search);
            }

            return true;
        });
    }

    // =================================================================================================================
    // Tab System Event Handlers
    // =================================================================================================================

    @FXML
    private void switchToEarningsTab() {
        setActiveTab(btnTabEarnings);
        showTabContent(earningsTabContent, true);
    }

    @FXML
    private void switchToDeliveryStatsTab() {
        setActiveTab(btnTabDeliveryStats);
        showTabContent(deliveryStatsTabContent, true);
    }

    @FXML
    private void switchToFiltersTab() {
        setActiveTab(btnTabFilters);
        showTabContent(filtersTabContent, true);
    }

    private void setActiveTab(Button activeButton) {
        btnTabEarnings.getStyleClass().remove("tab-button-active");
        btnTabDeliveryStats.getStyleClass().remove("tab-button-active");
        btnTabFilters.getStyleClass().remove("tab-button-active");

        activeButton.getStyleClass().add("tab-button-active");
    }

    private void showTabContent(javafx.scene.Node contentToShow, boolean shouldExpand) {
        earningsTabContent.setVisible(false);
        earningsTabContent.setManaged(false);
        deliveryStatsTabContent.setVisible(false);
        deliveryStatsTabContent.setManaged(false);
        filtersTabContent.setVisible(false);
        filtersTabContent.setManaged(false);

        contentToShow.setVisible(true);
        contentToShow.setManaged(true);

        if (shouldExpand && !TabStateManager.isExpanded(VIEW_NAME)) {
            TabStateManager.setExpanded(VIEW_NAME, true);
            applyCollapseState(true);
        }

        if (contentToShow == earningsTabContent) {
            TabStateManager.setActiveTab(VIEW_NAME, "earnings");
        } else if (contentToShow == deliveryStatsTabContent) {
            TabStateManager.setActiveTab(VIEW_NAME, "deliveryStats");
        } else if (contentToShow == filtersTabContent) {
            TabStateManager.setActiveTab(VIEW_NAME, "filters");
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
        boolean expanded = TabStateManager.isExpanded(VIEW_NAME);
        applyCollapseState(expanded);

        String activeTab = TabStateManager.getActiveTab(VIEW_NAME);
        if (activeTab != null) {
            switch (activeTab) {
                case "earnings":
                    setActiveTab(btnTabEarnings);
                    showTabContent(earningsTabContent, false);
                    break;
                case "deliveryStats":
                    setActiveTab(btnTabDeliveryStats);
                    showTabContent(deliveryStatsTabContent, false);
                    break;
                case "filters":
                    setActiveTab(btnTabFilters);
                    showTabContent(filtersTabContent, false);
                    break;
                default:
                    setActiveTab(btnTabEarnings);
                    showTabContent(earningsTabContent, false);
                    break;
            }
        } else {
            setActiveTab(btnTabEarnings);
            showTabContent(earningsTabContent, false);
        }
    }

    // =================================================================================================================
    // Action Handlers
    // =================================================================================================================

    @FXML
    private void handleRefresh() {
        Logger.info("Refreshing history data...");
        loadHistoryData();
        DialogUtil.showSuccess("Actualizado", "Datos actualizados correctamente.");
    }

    @FXML
    private void handleClearDateFilter() {
        dateTo.setValue(LocalDate.now());
        dateFrom.setValue(LocalDate.now().minusDays(30));
        if (searchField != null) {
            searchField.clear();
        }
    }

    @FXML
    private void handleLastMonth() {
        dateTo.setValue(LocalDate.now());
        dateFrom.setValue(LocalDate.now().minusMonths(1));
    }

    @FXML
    private void handleLastWeek() {
        dateTo.setValue(LocalDate.now());
        dateFrom.setValue(LocalDate.now().minusWeeks(1));
    }

    @FXML
    private void handleExportToPdf() {
        DialogUtil.showInfo("Próximamente", "La función de exportar a PDF estará disponible próximamente.");
    }

    private void showDeliveryDetails(DeliveryHistoryEntry entry) {
        StringBuilder details = new StringBuilder();
        details.append("Fecha: ").append(entry.getDate()).append("\n");
        details.append("ID Orden: ").append(entry.getOrderId()).append("\n");
        details.append("Ruta: ").append(entry.getRoute()).append("\n");
        details.append("Estado: ").append(entry.getStatus()).append("\n");
        details.append("Distancia: ").append(entry.getDistance()).append("\n");
        details.append("Ganancia: ").append(entry.getEarnings()).append("\n");
        details.append("Calificación: ").append(entry.getRating());

        DialogUtil.showInfo("Detalles de la Entrega", details.toString());
    }

    // =================================================================================================================
    // Helper Methods
    // =================================================================================================================

    private String buildRouteString(ShipmentDTO shipment) {
        String origin = shipment.getOriginAddressComplete() != null ? shipment.getOriginAddressComplete() : "N/A";
        String destination = shipment.getDestinationAddressComplete() != null ? shipment.getDestinationAddressComplete() : "N/A";
        return origin + " → " + destination;
    }

    private double calculateDistance(ShipmentDTO shipment) {
        return shipment.getDistanceKm();
    }

    private String getStatusSpanish(ShipmentStatus status) {
        if (status == null) return "Desconocido";
        return switch (status) {
            case DELIVERED -> "Entregado";
            case CANCELLED -> "Cancelado";
            case RETURNED -> "Devuelto";
            case IN_TRANSIT -> "En Tránsito";
            case PENDING_ASSIGNMENT -> "Pendiente";
            case READY_FOR_PICKUP -> "Listo para Recoger";
            case OUT_FOR_DELIVERY -> "En Entrega";
            default -> status.toString();
        };
    }

    private String getStatusColor(String status) {
        return switch (status.toLowerCase()) {
            case "entregado" -> "#28a745";
            case "cancelado" -> "#dc3545";
            case "devuelto" -> "#ffc107";
            case "en tránsito" -> "#17a2b8";
            case "pendiente" -> "#6c757d";
            default -> "#6c757d";
        };
    }

    // =================================================================================================================
    // Inner Class - DeliveryHistoryEntry
    // =================================================================================================================

    public static class DeliveryHistoryEntry {
        private final String date;
        private final String orderId;
        private final String route;
        private final String status;
        private final String distance;
        private final String earnings;
        private final String rating;
        private final ShipmentDTO shipment;

        public DeliveryHistoryEntry(String date, String orderId, String route, String status,
                                   String distance, String earnings, String rating, ShipmentDTO shipment) {
            this.date = date;
            this.orderId = orderId;
            this.route = route;
            this.status = status;
            this.distance = distance;
            this.earnings = earnings;
            this.rating = rating;
            this.shipment = shipment;
        }

        public String getDate() { return date; }
        public String getOrderId() { return orderId; }
        public String getRoute() { return route; }
        public String getStatus() { return status; }
        public String getDistance() { return distance; }
        public String getEarnings() { return earnings; }
        public String getRating() { return rating; }
        public ShipmentDTO getShipment() { return shipment; }
    }
}
