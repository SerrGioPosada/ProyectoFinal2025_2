package co.edu.uniquindio.poo.ProyectoFinal2025_2.Controller;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.ReportService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.DialogUtil;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.*;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Controller for the Reports view.
 * Handles report generation, visualization, and analytics display.
 */
public class ReportsController implements Initializable {

    // =================================================================================================================
    // FXML Fields - Stats
    // =================================================================================================================

    @FXML private Label lblLastGenerated;
    @FXML private Label lblTotalRevenue;
    @FXML private Label lblTotalShipments;
    @FXML private Label lblSuccessRate;
    @FXML private Label lblActiveUsers;
    @FXML private Label lblAvgDeliveryTime;

    // =================================================================================================================
    // FXML Fields - Date Pickers
    // =================================================================================================================

    @FXML private DatePicker dateFrom;
    @FXML private DatePicker dateTo;

    // =================================================================================================================
    // FXML Fields - Charts
    // =================================================================================================================

    @FXML private LineChart<String, Number> chartRevenueTrend;
    @FXML private PieChart chartShipmentsByStatus;
    @FXML private BarChart<String, Number> chartTopDeliveryPersonnel;
    @FXML private BarChart<String, Number> chartShipmentsByArea;

    // =================================================================================================================
    // FXML Fields - Activity Table
    // =================================================================================================================

    @FXML private TableView<ActivityRecord> tableRecentActivity;
    @FXML private TableColumn<ActivityRecord, String> colActivityDate;
    @FXML private TableColumn<ActivityRecord, String> colActivityType;
    @FXML private TableColumn<ActivityRecord, String> colActivityDescription;
    @FXML private TableColumn<ActivityRecord, String> colActivityUser;
    @FXML private TableColumn<ActivityRecord, String> colActivityStatus;

    // =================================================================================================================
    // Services
    // =================================================================================================================

    private final ReportService reportService = new ReportService();
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // =================================================================================================================
    // Initialization
    // =================================================================================================================

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Set default date range (last 30 days)
        dateTo.setValue(LocalDate.now());
        dateFrom.setValue(LocalDate.now().minusDays(30));

        // Initialize table
        setupActivityTable();

        // Load initial data
        handleRefresh();

        Logger.info("ReportsController initialized");
    }

    // =================================================================================================================
    // Event Handlers - Refresh & Date Range
    // =================================================================================================================

    @FXML
    private void handleRefresh() {
        Logger.info("Refreshing reports data");

        LocalDate from = dateFrom.getValue();
        LocalDate to = dateTo.getValue();

        if (from == null || to == null) {
            DialogUtil.showWarning("Seleccione un rango de fechas", "Por favor seleccione fecha de inicio y fin.");
            return;
        }

        if (from.isAfter(to)) {
            DialogUtil.showWarning("Rango de fechas inválido", "La fecha de inicio debe ser anterior a la fecha de fin.");
            return;
        }

        loadStatistics(from, to);
        loadCharts(from, to);

        lblLastGenerated.setText("Última generación: " + LocalDateTime.now().format(DATETIME_FORMATTER));
    }

    @FXML
    private void handleLastMonth() {
        dateTo.setValue(LocalDate.now());
        dateFrom.setValue(LocalDate.now().minusMonths(1));
        handleRefresh();
    }

    @FXML
    private void handleLastQuarter() {
        dateTo.setValue(LocalDate.now());
        dateFrom.setValue(LocalDate.now().minusMonths(3));
        handleRefresh();
    }

    @FXML
    private void handleLastYear() {
        dateTo.setValue(LocalDate.now());
        dateFrom.setValue(LocalDate.now().minusYears(1));
        handleRefresh();
    }

    // =================================================================================================================
    // Event Handlers - General Report
    // =================================================================================================================

    @FXML
    private void handleGeneralReportPDF() {
        generateAndOpenReport(() ->
            reportService.generateGeneralReportPDF(dateFrom.getValue(), dateTo.getValue()),
            "Reporte General PDF"
        );
    }

    @FXML
    private void handleGeneralReportCSV() {
        generateAndOpenReport(() ->
            reportService.generateGeneralReportCSV(dateFrom.getValue(), dateTo.getValue()),
            "Reporte General CSV"
        );
    }

    // =================================================================================================================
    // Event Handlers - Financial Report
    // =================================================================================================================

    @FXML
    private void handleFinancialReportPDF() {
        generateAndOpenReport(() ->
            reportService.generateFinancialReportPDF(dateFrom.getValue(), dateTo.getValue()),
            "Reporte Financiero PDF"
        );
    }

    @FXML
    private void handleFinancialReportCSV() {
        generateAndOpenReport(() ->
            reportService.generateFinancialReportCSV(dateFrom.getValue(), dateTo.getValue()),
            "Reporte Financiero CSV"
        );
    }

    // =================================================================================================================
    // Event Handlers - Shipments Report
    // =================================================================================================================

    @FXML
    private void handleShipmentsReportPDF() {
        generateAndOpenReport(() ->
            reportService.generateShipmentsReportPDF(dateFrom.getValue(), dateTo.getValue()),
            "Reporte de Envíos PDF"
        );
    }

    @FXML
    private void handleShipmentsReportCSV() {
        generateAndOpenReport(() ->
            reportService.generateShipmentsReportCSV(dateFrom.getValue(), dateTo.getValue()),
            "Reporte de Envíos CSV"
        );
    }

    // =================================================================================================================
    // Event Handlers - Users Report
    // =================================================================================================================

    @FXML
    private void handleUsersReportPDF() {
        generateAndOpenReport(() ->
            reportService.generateUsersReportPDF(dateFrom.getValue(), dateTo.getValue()),
            "Reporte de Usuarios PDF"
        );
    }

    @FXML
    private void handleUsersReportCSV() {
        generateAndOpenReport(() ->
            reportService.generateUsersReportCSV(dateFrom.getValue(), dateTo.getValue()),
            "Reporte de Usuarios CSV"
        );
    }

    // =================================================================================================================
    // Event Handlers - Delivery Personnel Report
    // =================================================================================================================

    @FXML
    private void handleDeliveryPersonnelReportPDF() {
        generateAndOpenReport(() ->
            reportService.generateDeliveryPersonnelReportPDF(dateFrom.getValue(), dateTo.getValue()),
            "Reporte de Repartidores PDF"
        );
    }

    @FXML
    private void handleDeliveryPersonnelReportCSV() {
        generateAndOpenReport(() ->
            reportService.generateDeliveryPersonnelReportCSV(dateFrom.getValue(), dateTo.getValue()),
            "Reporte de Repartidores CSV"
        );
    }

    // =================================================================================================================
    // Event Handlers - Custom Report & Export All
    // =================================================================================================================

    @FXML
    private void handleCustomReport() {
        DialogUtil.showInfo("Función en Desarrollo",
            "La funcionalidad de reportes personalizados estará disponible próximamente.");
    }

    @FXML
    private void handleExportAll() {
        boolean confirm = DialogUtil.showConfirmation(
            "Exportar Todos los Reportes",
            "¿Desea generar un paquete con todos los reportes disponibles?",
            "Esto puede tardar unos momentos."
        );

        if (!confirm) return;

        try {
            LocalDate from = dateFrom.getValue();
            LocalDate to = dateTo.getValue();

            // Generate all reports
            reportService.generateGeneralReportPDF(from, to);
            reportService.generateGeneralReportCSV(from, to);
            reportService.generateFinancialReportPDF(from, to);
            reportService.generateFinancialReportCSV(from, to);
            reportService.generateShipmentsReportPDF(from, to);
            reportService.generateShipmentsReportCSV(from, to);
            reportService.generateUsersReportPDF(from, to);
            reportService.generateUsersReportCSV(from, to);
            reportService.generateDeliveryPersonnelReportPDF(from, to);
            reportService.generateDeliveryPersonnelReportCSV(from, to);

            DialogUtil.showSuccess("Todos los reportes han sido generados exitosamente en la carpeta 'reportes'.");

            // Open reports folder
            File reportsFolder = new File("reportes");
            if (reportsFolder.exists() && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(reportsFolder);
            }

        } catch (Exception e) {
            Logger.error("Error exporting all reports: " + e.getMessage());
            DialogUtil.showError("Error al generar los reportes", e.getMessage());
        }
    }

    // =================================================================================================================
    // Private Methods - Data Loading
    // =================================================================================================================

    /**
     * Loads and displays statistics for the given date range.
     */
    private void loadStatistics(LocalDate from, LocalDate to) {
        try {
            // Load statistics
            double revenue = reportService.calculateTotalRevenue(from, to);
            long shipments = reportService.countTotalShipments(from, to);
            double successRate = reportService.calculateSuccessRate(from, to);
            long activeUsers = reportService.countActiveUsers(from, to);
            double avgTime = reportService.calculateAverageDeliveryTime(from, to);

            // Update labels
            lblTotalRevenue.setText(String.format("$%,.2f", revenue));
            lblTotalShipments.setText(String.valueOf(shipments));
            lblSuccessRate.setText(String.format("%.1f%%", successRate));
            lblActiveUsers.setText(String.valueOf(activeUsers));
            lblAvgDeliveryTime.setText(String.format("%.0fh", avgTime));

        } catch (Exception e) {
            Logger.error("Error loading statistics: " + e.getMessage());
        }
    }

    /**
     * Loads and displays all charts.
     */
    private void loadCharts(LocalDate from, LocalDate to) {
        loadRevenueTrendChart();
        loadShipmentsByStatusChart(from, to);
        loadTopDeliveryPersonnelChart(from, to);
        loadShipmentsByAreaChart(from, to);
    }

    /**
     * Loads revenue trend chart (last 14 days).
     */
    private void loadRevenueTrendChart() {
        try {
            chartRevenueTrend.getData().clear();

            Map<String, Double> dailyRevenue = reportService.getDailyRevenue(14);

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Ingresos Diarios");

            dailyRevenue.forEach((date, revenue) ->
                series.getData().add(new XYChart.Data<>(date, revenue))
            );

            chartRevenueTrend.getData().add(series);

        } catch (Exception e) {
            Logger.error("Error loading revenue trend chart: " + e.getMessage());
        }
    }

    /**
     * Loads shipments by status pie chart.
     */
    private void loadShipmentsByStatusChart(LocalDate from, LocalDate to) {
        try {
            chartShipmentsByStatus.getData().clear();

            Map<String, Long> byStatus = reportService.getShipmentsByStatus(from, to);

            byStatus.forEach((status, count) ->
                chartShipmentsByStatus.getData().add(new PieChart.Data(status, count))
            );

        } catch (Exception e) {
            Logger.error("Error loading shipments by status chart: " + e.getMessage());
        }
    }

    /**
     * Loads top delivery personnel bar chart.
     */
    private void loadTopDeliveryPersonnelChart(LocalDate from, LocalDate to) {
        try {
            chartTopDeliveryPersonnel.getData().clear();

            Map<String, Long> topDelivery = reportService.getTopDeliveryPersonnel(from, to, 10);

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Entregas Completadas");

            topDelivery.forEach((name, count) ->
                series.getData().add(new XYChart.Data<>(name, count))
            );

            chartTopDeliveryPersonnel.getData().add(series);

        } catch (Exception e) {
            Logger.error("Error loading top delivery personnel chart: " + e.getMessage());
        }
    }

    /**
     * Loads shipments by coverage area bar chart.
     */
    private void loadShipmentsByAreaChart(LocalDate from, LocalDate to) {
        try {
            chartShipmentsByArea.getData().clear();

            Map<String, Long> byArea = reportService.getShipmentsByCoverageArea(from, to);

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Envíos por Zona");

            byArea.forEach((area, count) ->
                series.getData().add(new XYChart.Data<>(area, count))
            );

            chartShipmentsByArea.getData().add(series);

        } catch (Exception e) {
            Logger.error("Error loading shipments by area chart: " + e.getMessage());
        }
    }

    /**
     * Sets up the activity table columns.
     */
    private void setupActivityTable() {
        colActivityDate.setCellValueFactory(data -> data.getValue().dateProperty());
        colActivityType.setCellValueFactory(data -> data.getValue().typeProperty());
        colActivityDescription.setCellValueFactory(data -> data.getValue().descriptionProperty());
        colActivityUser.setCellValueFactory(data -> data.getValue().userProperty());
        colActivityStatus.setCellValueFactory(data -> data.getValue().statusProperty());

        // Sample data for demonstration
        tableRecentActivity.setItems(FXCollections.observableArrayList());
    }

    // =================================================================================================================
    // Utility Methods
    // =================================================================================================================

    /**
     * Generic method to generate and open a report.
     */
    private void generateAndOpenReport(ReportGenerator generator, String reportName) {
        try {
            Logger.info("Generating " + reportName);

            File report = generator.generate();

            if (report == null || !report.exists()) {
                DialogUtil.showError("Error al generar el reporte", "No se pudo crear el archivo del reporte.");
                return;
            }

            // Show success message
            boolean open = DialogUtil.showConfirmation(
                "Reporte Generado",
                "El reporte ha sido generado exitosamente.",
                "¿Desea abrir el archivo ahora?"
            );

            if (open && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(report);
            }

        } catch (IOException e) {
            Logger.error("Error opening report: " + e.getMessage());
            DialogUtil.showError("Error al abrir el reporte", e.getMessage());
        } catch (Exception e) {
            Logger.error("Error generating report: " + e.getMessage());
            DialogUtil.showError("Error al generar el reporte", e.getMessage());
        }
    }

    /**
     * Functional interface for report generation.
     */
    @FunctionalInterface
    private interface ReportGenerator {
        File generate();
    }

    // =================================================================================================================
    // Inner Class - Activity Record (for demonstration)
    // =================================================================================================================

    public static class ActivityRecord {
        private final SimpleStringProperty date;
        private final SimpleStringProperty type;
        private final SimpleStringProperty description;
        private final SimpleStringProperty user;
        private final SimpleStringProperty status;

        public ActivityRecord(String date, String type, String description, String user, String status) {
            this.date = new SimpleStringProperty(date);
            this.type = new SimpleStringProperty(type);
            this.description = new SimpleStringProperty(description);
            this.user = new SimpleStringProperty(user);
            this.status = new SimpleStringProperty(status);
        }

        public SimpleStringProperty dateProperty() { return date; }
        public SimpleStringProperty typeProperty() { return type; }
        public SimpleStringProperty descriptionProperty() { return description; }
        public SimpleStringProperty userProperty() { return user; }
        public SimpleStringProperty statusProperty() { return status; }
    }
}
