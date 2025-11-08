package co.edu.uniquindio.poo.ProyectoFinal2025_2.Controller;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.dto.ChartDataDTO;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.dto.DashboardStatsDTO;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.DashboardService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.ChartUtil;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Controller for the Admin Dashboard view (AdminDashboard.fxml).
 * <p>
 * This controller manages the admin dashboard, displaying real-time statistics,
 * charts, and key metrics about users, shipments, financial data, and delivery personnel.
 * Data is refreshed automatically at regular intervals.
 * </p>
 */
public class AdminDashboardController implements Initializable {

    // =================================================================================================================
    // FXML Fields - Statistics Cards
    // =================================================================================================================

    @FXML private javafx.scene.layout.VBox cardTotalUsers;
    @FXML private javafx.scene.layout.VBox cardTotalShipments;
    @FXML private javafx.scene.layout.VBox cardDelivered;
    @FXML private javafx.scene.layout.VBox cardInTransit;
    @FXML private javafx.scene.layout.VBox cardRevenue;
    @FXML private javafx.scene.layout.VBox cardSuccessRate;

    @FXML private Label lblTotalUsers;
    @FXML private Label lblActiveUsers;
    @FXML private Label lblNewUsersToday;

    @FXML private Label lblTotalShipments;
    @FXML private Label lblPendingShipments;
    @FXML private Label lblInTransitShipments;
    @FXML private Label lblDeliveredShipments;
    @FXML private Label lblDeliverySuccessRate;

    @FXML private Label lblTotalRevenue;
    @FXML private Label lblRevenueToday;
    @FXML private Label lblRevenueThisWeek;
    @FXML private Label lblRevenueThisMonth;
    @FXML private Label lblAverageOrderValue;

    @FXML private Label lblTotalDeliveryPersons;
    @FXML private Label lblAvailableDeliveryPersons;
    @FXML private Label lblBusyDeliveryPersons;

    @FXML private Label lblTotalIncidents;
    @FXML private Label lblUnresolvedIncidents;

    @FXML private Label lblLastUpdated;

    // =================================================================================================================
    // FXML Fields - Charts
    // =================================================================================================================

    @FXML private PieChart chartShipmentStatus;
    @FXML private BarChart<String, Number> chartDeliveryPersonPerformance;
    @FXML private LineChart<String, Number> chartRevenueTrend;
    @FXML private AreaChart<String, Number> chartShipmentsTrend;
    @FXML private PieChart chartIncidentsByType;

    // =================================================================================================================
    // Services
    // =================================================================================================================

    private final DashboardService dashboardService;
    private Timeline refreshTimeline;

    // Refresh interval in seconds
    private static final int REFRESH_INTERVAL_SECONDS = 30;

    // =================================================================================================================
    // Constructor
    // =================================================================================================================

    /**
     * Default constructor. Initializes the dashboard service.
     */
    public AdminDashboardController() {
        this.dashboardService = new DashboardService();
    }

    // =================================================================================================================
    // Initialization
    // =================================================================================================================

    /**
     * Initializes the controller. Called automatically after FXML loading.
     * <p>
     * Sets up the dashboard by loading initial data and starting the auto-refresh timer.
     * </p>
     * @param url The location used to resolve relative paths
     * @param resourceBundle The resources used to localize the root object
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Logger.info("AdminDashboardController initialized");

        // Setup card hover effects
        setupCardHoverEffects();

        // Load initial dashboard data
        loadDashboardData();

        // Start auto-refresh
        startAutoRefresh();
    }

    /**
     * Sets up hover effects for all dashboard cards.
     */
    private void setupCardHoverEffects() {
        setupCardHover(cardTotalUsers);
        setupCardHover(cardTotalShipments);
        setupCardHover(cardDelivered);
        setupCardHover(cardInTransit);
        setupCardHover(cardRevenue);
        setupCardHover(cardSuccessRate);
    }

    /**
     * Configures hover effect for a single card.
     */
    private void setupCardHover(javafx.scene.layout.VBox card) {
        if (card == null) return;

        card.setOnMouseEntered(event -> {
            card.setStyle(card.getStyle().replace(
                "dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3)",
                "dropshadow(gaussian, rgba(74, 158, 255, 0.4), 15, 0, 0, 5)"
            ));
        });

        card.setOnMouseExited(event -> {
            card.setStyle(card.getStyle().replace(
                "dropshadow(gaussian, rgba(74, 158, 255, 0.4), 15, 0, 0, 5)",
                "dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3)"
            ));
        });
    }

    // =================================================================================================================
    // Dashboard Data Loading
    // =================================================================================================================

    /**
     * Loads all dashboard data and updates the UI.
     * <p>
     * This method fetches statistics from the DashboardService and updates
     * all labels and charts with the latest data.
     * </p>
     */
    private void loadDashboardData() {
        try {
            Logger.info("Loading dashboard data...");

            // Fetch dashboard statistics
            DashboardStatsDTO stats = dashboardService.calculateDashboardStats();

            // Update UI on JavaFX thread
            Platform.runLater(() -> {
                updateStatisticsCards(stats);
                updateCharts(stats);
                updateLastUpdatedLabel();
            });

            Logger.info("Dashboard data loaded successfully");

        } catch (Exception e) {
            Logger.error("Error loading dashboard data: " + e.getMessage(), e);
            Platform.runLater(() -> showErrorState());
        }
    }

    /**
     * Updates all statistics cards with new data.
     * @param stats The dashboard statistics DTO
     */
    private void updateStatisticsCards(DashboardStatsDTO stats) {
        // User statistics
        if (lblTotalUsers != null) lblTotalUsers.setText(String.valueOf(stats.getTotalUsers()));
        if (lblActiveUsers != null) lblActiveUsers.setText(String.valueOf(stats.getActiveUsers()));
        if (lblNewUsersToday != null) lblNewUsersToday.setText(String.valueOf(stats.getNewUsersToday()));

        // Shipment statistics
        if (lblTotalShipments != null) lblTotalShipments.setText(String.valueOf(stats.getTotalShipments()));
        if (lblPendingShipments != null) lblPendingShipments.setText(String.valueOf(stats.getPendingShipments()));
        if (lblInTransitShipments != null) lblInTransitShipments.setText(String.valueOf(stats.getInTransitShipments()));
        if (lblDeliveredShipments != null) lblDeliveredShipments.setText(String.valueOf(stats.getDeliveredShipments()));
        if (lblDeliverySuccessRate != null) {
            lblDeliverySuccessRate.setText(ChartUtil.formatPercentage(stats.getDeliverySuccessRate()));
        }

        // Financial statistics
        if (lblTotalRevenue != null) lblTotalRevenue.setText(ChartUtil.formatCurrency(stats.getTotalRevenue()));
        if (lblRevenueToday != null) lblRevenueToday.setText(ChartUtil.formatCurrency(stats.getRevenueToday()));
        if (lblRevenueThisWeek != null) lblRevenueThisWeek.setText(ChartUtil.formatCurrency(stats.getRevenueThisWeek()));
        if (lblRevenueThisMonth != null) lblRevenueThisMonth.setText(ChartUtil.formatCurrency(stats.getRevenueThisMonth()));
        if (lblAverageOrderValue != null) {
            lblAverageOrderValue.setText(ChartUtil.formatCurrency(stats.getAverageOrderValue()));
        }

        // Delivery person statistics
        if (lblTotalDeliveryPersons != null) {
            lblTotalDeliveryPersons.setText(String.valueOf(stats.getTotalDeliveryPersons()));
        }
        if (lblAvailableDeliveryPersons != null) {
            lblAvailableDeliveryPersons.setText(String.valueOf(stats.getAvailableDeliveryPersons()));
        }
        if (lblBusyDeliveryPersons != null) {
            lblBusyDeliveryPersons.setText(String.valueOf(stats.getBusyDeliveryPersons()));
        }

        // Incident statistics
        if (lblTotalIncidents != null) lblTotalIncidents.setText(String.valueOf(stats.getTotalIncidents()));
        if (lblUnresolvedIncidents != null) {
            lblUnresolvedIncidents.setText(String.valueOf(stats.getUnresolvedIncidents()));
        }
    }

    /**
     * Updates all charts with new data.
     * @param stats The dashboard statistics DTO
     */
    private void updateCharts(DashboardStatsDTO stats) {
        updateShipmentStatusChart(stats);
        updateDeliveryPersonPerformanceChart(stats);
        updateRevenueTrendChart(stats);
        updateShipmentsTrendChart(stats);
        updateIncidentsByTypeChart(stats);
    }

    /**
     * Updates the shipment status pie chart.
     * @param stats The dashboard statistics DTO
     */
    private void updateShipmentStatusChart(DashboardStatsDTO stats) {
        if (chartShipmentStatus == null) return;

        Map<String, Long> shipmentsByStatus = stats.getShipmentsByStatus();
        if (shipmentsByStatus != null && !shipmentsByStatus.isEmpty()) {
            ChartUtil.populatePieChart(chartShipmentStatus, shipmentsByStatus);
        } else {
            // Show placeholder data when no shipments exist
            chartShipmentStatus.getData().clear();
            chartShipmentStatus.setTitle("Sin datos de envíos disponibles");
        }
    }

    /**
     * Updates the delivery person performance bar chart.
     * @param stats The dashboard statistics DTO
     */
    private void updateDeliveryPersonPerformanceChart(DashboardStatsDTO stats) {
        if (chartDeliveryPersonPerformance == null) return;

        Map<String, Long> shipmentsPerDP = stats.getShipmentsPerDeliveryPerson();
        if (shipmentsPerDP != null && !shipmentsPerDP.isEmpty()) {
            ChartUtil.populateBarChart(chartDeliveryPersonPerformance, shipmentsPerDP, "Envíos");
        } else {
            chartDeliveryPersonPerformance.getData().clear();
            chartDeliveryPersonPerformance.setTitle("Sin repartidores o datos disponibles");
        }
    }

    /**
     * Updates the revenue trend line chart (last 7 days).
     * @param stats The dashboard statistics DTO
     */
    private void updateRevenueTrendChart(DashboardStatsDTO stats) {
        if (chartRevenueTrend == null) return;

        Map<LocalDate, Double> revenuePerDay = stats.getRevenuePerDay();
        if (revenuePerDay != null && !revenuePerDay.isEmpty()) {
            // Get last 7 days
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(6);

            // Filter and format data
            Map<String, Double> last7Days = revenuePerDay.entrySet().stream()
                .filter(entry -> !entry.getKey().isBefore(startDate) && !entry.getKey().isAfter(endDate))
                .collect(java.util.stream.Collectors.toMap(
                    entry -> ChartUtil.formatDateForChart(entry.getKey()),
                    Map.Entry::getValue,
                    (a, b) -> a,
                    java.util.LinkedHashMap::new
                ));

            ChartUtil.populateLineChart(chartRevenueTrend, last7Days, "Ingresos");
        }
    }

    /**
     * Updates the shipments trend area chart (last 7 days).
     * @param stats The dashboard statistics DTO
     */
    private void updateShipmentsTrendChart(DashboardStatsDTO stats) {
        if (chartShipmentsTrend == null) return;

        Map<LocalDate, Long> shipmentsPerDay = stats.getShipmentsPerDay();
        if (shipmentsPerDay != null && !shipmentsPerDay.isEmpty()) {
            // Get last 7 days
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(6);

            // Filter and format data
            Map<String, Long> last7Days = shipmentsPerDay.entrySet().stream()
                .filter(entry -> !entry.getKey().isBefore(startDate) && !entry.getKey().isAfter(endDate))
                .collect(java.util.stream.Collectors.toMap(
                    entry -> ChartUtil.formatDateForChart(entry.getKey()),
                    Map.Entry::getValue,
                    (a, b) -> a,
                    java.util.LinkedHashMap::new
                ));

            // Clear and populate
            chartShipmentsTrend.getData().clear();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Envíos");

            last7Days.forEach((label, value) -> series.getData().add(new XYChart.Data<>(label, value)));

            chartShipmentsTrend.getData().add(series);
        }
    }

    /**
     * Updates the incidents by type pie chart.
     * @param stats The dashboard statistics DTO
     */
    private void updateIncidentsByTypeChart(DashboardStatsDTO stats) {
        if (chartIncidentsByType == null) return;

        Map<String, Long> incidentsByType = stats.getIncidentsByType();
        if (incidentsByType != null && !incidentsByType.isEmpty()) {
            ChartUtil.populatePieChart(chartIncidentsByType, incidentsByType);
        } else {
            chartIncidentsByType.setTitle("Sin Incidentes");
        }
    }

    /**
     * Updates the "Last Updated" label with the current timestamp.
     */
    private void updateLastUpdatedLabel() {
        if (lblLastUpdated != null) {
            String timestamp = java.time.LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            lblLastUpdated.setText("Última actualización: " + timestamp);
        }
    }

    // =================================================================================================================
    // Auto-Refresh
    // =================================================================================================================

    /**
     * Starts the auto-refresh timer to update dashboard data periodically.
     * <p>
     * The dashboard will refresh every {@link #REFRESH_INTERVAL_SECONDS} seconds.
     * </p>
     */
    private void startAutoRefresh() {
        refreshTimeline = new Timeline(new KeyFrame(
            Duration.seconds(REFRESH_INTERVAL_SECONDS),
            event -> loadDashboardData()
        ));
        refreshTimeline.setCycleCount(Timeline.INDEFINITE);
        refreshTimeline.play();

        Logger.info("Auto-refresh started with interval: " + REFRESH_INTERVAL_SECONDS + " seconds");
    }

    /**
     * Stops the auto-refresh timer.
     * <p>
     * This should be called when the controller is no longer in use to prevent memory leaks.
     * </p>
     */
    public void stopAutoRefresh() {
        if (refreshTimeline != null) {
            refreshTimeline.stop();
            Logger.info("Auto-refresh stopped");
        }
    }

    // =================================================================================================================
    // Manual Refresh
    // =================================================================================================================

    /**
     * Manually refreshes the dashboard data.
     * <p>
     * This method can be called from a "Refresh" button in the UI.
     * </p>
     */
    @FXML
    private void handleRefresh() {
        Logger.info("Manual refresh triggered");
        loadDashboardData();
    }

    // =================================================================================================================
    // Error Handling
    // =================================================================================================================

    /**
     * Shows an error state in the dashboard when data loading fails.
     */
    private void showErrorState() {
        if (lblLastUpdated != null) {
            lblLastUpdated.setText("Error al cargar datos. Reintentando...");
            lblLastUpdated.setStyle("-fx-text-fill: #dc3545;");
        }
    }

    // =================================================================================================================
    // Lifecycle
    // =================================================================================================================

    /**
     * Cleanup method called when the controller is destroyed.
     * <p>
     * Stops the auto-refresh timer to prevent resource leaks.
     * </p>
     */
    public void cleanup() {
        stopAutoRefresh();
        Logger.info("AdminDashboardController cleaned up");
    }
}
