package co.edu.uniquindio.poo.ProyectoFinal2025_2.Controller;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.DeliveryPerson;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.ShipmentStatus;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.dto.ShipmentDTO;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.AuthenticationService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.ShipmentService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.DialogUtil;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.DistanceCalculator;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilService.DateTimeUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

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
 * showing completed deliveries, earnings statistics, and charts.
 * </p>
 */
public class DeliveryHistoryController implements Initializable {

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

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // =================================================================================================================
    // Constructor
    // =================================================================================================================

    /**
     * Default constructor. Initializes services.
     */
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

        // Get current delivery person
        this.currentDeliveryPerson = (DeliveryPerson) authService.getCurrentPerson();

        if (currentDeliveryPerson == null) {
            DialogUtil.showError("Error", "No se pudo obtener la información del repartidor.");
            return;
        }

        // Initialize table columns
        initializeTableColumns();

        // Initialize date pickers
        initializeDatePickers();

        // Load history data
        loadHistoryData();

        // Set up search functionality
        setupSearchFilter();

        Logger.info("DeliveryHistoryController initialized successfully");
    }

    /**
     * Initializes the table columns with cell value factories.
     */
    private void initializeTableColumns() {
        colDate.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDate()));
        colOrderId.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getOrderId()));
        colRoute.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRoute()));
        colStatus.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));
        colDistance.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDistance()));
        colEarnings.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEarnings()));
        colRating.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRating()));
    }

    /**
     * Initializes date pickers with default values.
     */
    private void initializeDatePickers() {
        // Set default date range to last 30 days
        dateTo.setValue(LocalDate.now());
        dateFrom.setValue(LocalDate.now().minusDays(30));
    }

    /**
     * Sets up the search filter functionality.
     */
    private void setupSearchFilter() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterHistoryTable(newValue);
        });
    }

    // =================================================================================================================
    // Data Loading
    // =================================================================================================================

    /**
     * Loads the complete history data.
     */
    private void loadHistoryData() {
        try {
            // Get all completed shipments for this delivery person
            completedShipments = shipmentService.listAll().stream()
                    .filter(shipment -> currentDeliveryPerson.getId().equals(shipment.getDeliveryPersonId()))
                    .filter(shipment -> shipment.getStatus() == ShipmentStatus.DELIVERED ||
                            shipment.getStatus() == ShipmentStatus.CANCELLED ||
                            shipment.getStatus() == ShipmentStatus.RETURNED)
                    .collect(Collectors.toList());

            // Convert to history entries
            updateHistoryEntries();

            // Update statistics
            updateStatistics();

            // Update charts
            updateCharts();

            // Update last updated label
            if (lblLastUpdated != null) {
                lblLastUpdated.setText("Última actualización: " +
                        LocalDateTime.now().format(DATETIME_FORMATTER));
            }

            Logger.info("History data loaded successfully: " + completedShipments.size() + " completed shipments");

        } catch (Exception e) {
            Logger.error("Error loading history data: " + e.getMessage());
            DialogUtil.showError("Error", "No se pudo cargar el historial de entregas.");
        }
    }

    /**
     * Updates the history entries list from completed orders.
     */
    private void updateHistoryEntries() {
        historyEntries.clear();

        for (ShipmentDTO shipment : completedShipments) {
            LocalDateTime dateTime = shipment.getActualDeliveryDate() != null ?
                    shipment.getActualDeliveryDate() : shipment.getCreationDate();
            String date = dateTime != null ? dateTime.format(DATETIME_FORMATTER) : "--";

            String route = String.format("%s → %s",
                    shipment.getOriginAddressComplete() != null ? shipment.getOriginZone() : "N/A",
                    shipment.getDestinationAddressComplete() != null ? shipment.getDestinationZone() : "N/A");

            double distance = shipment.getDistanceKm();

            String status = translateStatus(shipment.getStatus());
            String earnings = String.format("$%.2f", shipment.getTotalCost());
            String rating = "N/A"; // ShipmentDTO doesn't have rating field

            DeliveryHistoryEntry entry = new DeliveryHistoryEntry(
                    date,
                    shipment.getId(),
                    route,
                    status,
                    String.format("%.2f km", distance),
                    earnings,
                    rating
            );

            historyEntries.add(entry);
        }

        historyTable.setItems(historyEntries);
    }

    /**
     * Updates the statistics labels.
     */
    private void updateStatistics() {
        double totalEarnings = 0.0;
        double monthEarnings = 0.0;
        double weekEarnings = 0.0;
        double todayEarnings = 0.0;

        int completedCount = 0;
        int successfulCount = 0;
        int incidentCount = 0;

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime startOfWeek = now.minusDays(7);
        LocalDateTime startOfDay = now.withHour(0).withMinute(0).withSecond(0);

        for (ShipmentDTO shipment : completedShipments) {
            double cost = shipment.getTotalCost();
            totalEarnings += cost;

            LocalDateTime dateTime = shipment.getActualDeliveryDate() != null ?
                    shipment.getActualDeliveryDate() : shipment.getCreationDate();

            if (dateTime != null) {
                if (dateTime.isAfter(startOfMonth)) {
                    monthEarnings += cost;
                }
                if (dateTime.isAfter(startOfWeek)) {
                    weekEarnings += cost;
                }
                if (dateTime.isAfter(startOfDay)) {
                    todayEarnings += cost;
                }
            }

            completedCount++;
            if (shipment.getStatus() == ShipmentStatus.DELIVERED) {
                successfulCount++;
            } else {
                incidentCount++;
            }
        }

        double averageEarnings = completedCount > 0 ? totalEarnings / completedCount : 0.0;
        double successRate = completedCount > 0 ? (successfulCount * 100.0 / completedCount) : 0.0;

        // Update labels
        lblTotalEarnings.setText(String.format("$%.2f", totalEarnings));
        lblMonthEarnings.setText(String.format("$%.2f", monthEarnings));
        lblWeekEarnings.setText(String.format("$%.2f", weekEarnings));
        lblTodayEarnings.setText(String.format("$%.2f", todayEarnings));
        lblAverageEarnings.setText(String.format("$%.2f", averageEarnings));

        lblCompletedDeliveries.setText(String.valueOf(completedCount));
        lblSuccessfulDeliveries.setText(String.valueOf(successfulCount));
        lblIncidentDeliveries.setText(String.valueOf(incidentCount));
        lblSuccessRate.setText(String.format("%.1f%%", successRate));
    }

    /**
     * Updates the charts with current data.
     */
    private void updateCharts() {
        updateEarningsTrendChart();
        updateDeliveriesByStatusChart();
    }

    /**
     * Updates the earnings trend line chart.
     */
    private void updateEarningsTrendChart() {
        chartEarningsTrend.getData().clear();

        // Group earnings by date for last 30 days
        Map<LocalDate, Double> earningsByDate = new TreeMap<>();

        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();

        // Initialize all dates with 0
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            earningsByDate.put(date, 0.0);
        }

        // Sum up earnings by date
        for (ShipmentDTO shipment : completedShipments) {
            LocalDateTime dateTime = shipment.getActualDeliveryDate() != null ?
                    shipment.getActualDeliveryDate() : shipment.getCreationDate();

            if (dateTime != null) {
                LocalDate date = dateTime.toLocalDate();
                if (!date.isBefore(startDate) && !date.isAfter(endDate)) {
                    earningsByDate.put(date, earningsByDate.getOrDefault(date, 0.0) + shipment.getTotalCost());
                }
            }
        }

        // Create series
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Ganancias");

        for (Map.Entry<LocalDate, Double> entry : earningsByDate.entrySet()) {
            series.getData().add(new XYChart.Data<>(
                    entry.getKey().format(DATE_FORMATTER),
                    entry.getValue()
            ));
        }

        chartEarningsTrend.getData().add(series);
    }

    /**
     * Updates the deliveries by status pie chart.
     */
    private void updateDeliveriesByStatusChart() {
        chartDeliveriesByStatus.getData().clear();

        Map<String, Long> statusCounts = completedShipments.stream()
                .collect(Collectors.groupingBy(
                        shipment -> translateStatus(shipment.getStatus()),
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

    // =================================================================================================================
    // Event Handlers
    // =================================================================================================================

    /**
     * Handles the refresh button action.
     */
    @FXML
    private void handleRefresh() {
        Logger.info("Refreshing history data");
        loadHistoryData();
        DialogUtil.showInfo("Éxito", "Historial actualizado exitosamente");
    }

    /**
     * Handles the apply date filter button action.
     */
    @FXML
    private void handleApplyDateFilter() {
        LocalDate from = dateFrom.getValue();
        LocalDate to = dateTo.getValue();

        if (from == null || to == null) {
            DialogUtil.showWarning("Fechas Inválidas", "Por favor selecciona ambas fechas.");
            return;
        }

        if (from.isAfter(to)) {
            DialogUtil.showWarning("Fechas Inválidas", "La fecha inicial no puede ser posterior a la fecha final.");
            return;
        }

        // Filter completed shipments by date range
        List<ShipmentDTO> filteredShipments = completedShipments.stream()
                .filter(shipment -> {
                    LocalDateTime dateTime = shipment.getActualDeliveryDate() != null ?
                            shipment.getActualDeliveryDate() : shipment.getCreationDate();
                    if (dateTime == null) return false;

                    LocalDate deliveryDate = dateTime.toLocalDate();
                    return !deliveryDate.isBefore(from) && !deliveryDate.isAfter(to);
                })
                .collect(Collectors.toList());

        completedShipments = filteredShipments;
        updateHistoryEntries();
        updateStatistics();
        updateCharts();

        Logger.info("Date filter applied: " + from + " to " + to);
        DialogUtil.showInfo("Filtro Aplicado", "Se encontraron " + filteredShipments.size() + " entregas");
    }

    /**
     * Handles the clear date filter button action.
     */
    @FXML
    private void handleClearDateFilter() {
        dateFrom.setValue(LocalDate.now().minusDays(30));
        dateTo.setValue(LocalDate.now());
        loadHistoryData();
        Logger.info("Date filter cleared");
    }

    /**
     * Handles the last month quick filter.
     */
    @FXML
    private void handleLastMonth() {
        dateTo.setValue(LocalDate.now());
        dateFrom.setValue(LocalDate.now().minusMonths(1));
        handleApplyDateFilter();
    }

    /**
     * Handles the last week quick filter.
     */
    @FXML
    private void handleLastWeek() {
        dateTo.setValue(LocalDate.now());
        dateFrom.setValue(LocalDate.now().minusDays(7));
        handleApplyDateFilter();
    }

    /**
     * Handles the export to PDF button action.
     */
    @FXML
    private void handleExportToPdf() {
        // This would integrate with a PDF library like iText or Apache PDFBox
        DialogUtil.showInfo("Función Pendiente",
                "La exportación a PDF estará disponible próximamente.");
        Logger.info("Export to PDF requested");
    }

    // =================================================================================================================
    // Helper Methods
    // =================================================================================================================

    /**
     * Filters the history table based on search text.
     */
    private void filterHistoryTable(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            historyTable.setItems(historyEntries);
            return;
        }

        String lowerCaseFilter = searchText.toLowerCase().trim();

        ObservableList<DeliveryHistoryEntry> filteredList = historyEntries.stream()
                .filter(entry ->
                        entry.getOrderId().toLowerCase().contains(lowerCaseFilter) ||
                                entry.getRoute().toLowerCase().contains(lowerCaseFilter) ||
                                entry.getStatus().toLowerCase().contains(lowerCaseFilter)
                )
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        historyTable.setItems(filteredList);
    }

    /**
     * Translates ShipmentStatus enum to Spanish text.
     */
    private String translateStatus(ShipmentStatus status) {
        return switch (status) {
            case DELIVERED -> "Entregado";
            case RETURNED -> "Devuelto";
            case CANCELLED -> "Cancelado";
            default -> status.getDisplayName();
        };
    }

    // =================================================================================================================
    // Inner Class - DeliveryHistoryEntry
    // =================================================================================================================

    /**
     * Represents a row in the delivery history table.
     */
    public static class DeliveryHistoryEntry {
        private final String date;
        private final String orderId;
        private final String route;
        private final String status;
        private final String distance;
        private final String earnings;
        private final String rating;

        public DeliveryHistoryEntry(String date, String orderId, String route, String status,
                                    String distance, String earnings, String rating) {
            this.date = date;
            this.orderId = orderId;
            this.route = route;
            this.status = status;
            this.distance = distance;
            this.earnings = earnings;
            this.rating = rating;
        }

        public String getDate() { return date; }
        public String getOrderId() { return orderId; }
        public String getRoute() { return route; }
        public String getStatus() { return status; }
        public String getDistance() { return distance; }
        public String getEarnings() { return earnings; }
        public String getRating() { return rating; }
    }
}
