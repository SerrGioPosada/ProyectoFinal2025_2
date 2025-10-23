package co.edu.uniquindio.poo.proyectofinal2025_2.Util.UtilController;

import co.edu.uniquindio.poo.proyectofinal2025_2.Model.dto.ChartDataDTO;
import javafx.scene.chart.*;
import javafx.scene.paint.Color;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Utility class for chart operations and formatting.
 * <p>
 * This class provides reusable methods for creating and populating JavaFX charts,
 * formatting chart data, and applying consistent styling across dashboard charts.
 * </p>
 */
public class ChartUtil {

    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,##0.00");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM");

    // Chart colors matching the application theme
    private static final String PRIMARY_COLOR = "#4a9eff";
    private static final String SUCCESS_COLOR = "#28a745";
    private static final String WARNING_COLOR = "#ffc107";
    private static final String DANGER_COLOR = "#dc3545";
    private static final String INFO_COLOR = "#17a2b8";
    private static final String SECONDARY_COLOR = "#6c757d";

    /**
     * Creates a styled PieChart from ChartDataDTO list.
     * @param data List of chart data points
     * @param title Chart title
     * @return Configured PieChart
     */
    public static PieChart createPieChart(List<ChartDataDTO> data, String title) {
        PieChart chart = new PieChart();
        chart.setTitle(title);
        chart.setLegendVisible(true);
        chart.setLabelsVisible(true);

        for (ChartDataDTO point : data) {
            PieChart.Data slice = new PieChart.Data(
                point.getLabel() + " (" + (int) point.getValue() + ")",
                point.getValue()
            );
            chart.getData().add(slice);
        }

        return chart;
    }

    /**
     * Creates a styled BarChart from ChartDataDTO list.
     * @param data List of chart data points
     * @param title Chart title
     * @param xAxisLabel X-axis label
     * @param yAxisLabel Y-axis label
     * @return Configured BarChart
     */
    public static BarChart<String, Number> createBarChart(List<ChartDataDTO> data, String title,
                                                          String xAxisLabel, String yAxisLabel) {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel(xAxisLabel);

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(yAxisLabel);

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle(title);
        chart.setLegendVisible(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (ChartDataDTO point : data) {
            series.getData().add(new XYChart.Data<>(point.getLabel(), point.getValue()));
        }

        chart.getData().add(series);
        return chart;
    }

    /**
     * Creates a styled LineChart from ChartDataDTO list.
     * @param data List of chart data points
     * @param title Chart title
     * @param xAxisLabel X-axis label
     * @param yAxisLabel Y-axis label
     * @return Configured LineChart
     */
    public static LineChart<String, Number> createLineChart(List<ChartDataDTO> data, String title,
                                                            String xAxisLabel, String yAxisLabel) {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel(xAxisLabel);

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(yAxisLabel);

        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle(title);
        chart.setLegendVisible(false);
        chart.setCreateSymbols(true);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (ChartDataDTO point : data) {
            series.getData().add(new XYChart.Data<>(point.getLabel(), point.getValue()));
        }

        chart.getData().add(series);
        return chart;
    }

    /**
     * Creates a styled AreaChart from ChartDataDTO list.
     * @param data List of chart data points
     * @param title Chart title
     * @param xAxisLabel X-axis label
     * @param yAxisLabel Y-axis label
     * @return Configured AreaChart
     */
    public static AreaChart<String, Number> createAreaChart(List<ChartDataDTO> data, String title,
                                                            String xAxisLabel, String yAxisLabel) {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel(xAxisLabel);

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(yAxisLabel);

        AreaChart<String, Number> chart = new AreaChart<>(xAxis, yAxis);
        chart.setTitle(title);
        chart.setLegendVisible(false);
        chart.setCreateSymbols(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (ChartDataDTO point : data) {
            series.getData().add(new XYChart.Data<>(point.getLabel(), point.getValue()));
        }

        chart.getData().add(series);
        return chart;
    }

    /**
     * Populates a PieChart with data from a map.
     * @param chart The PieChart to populate
     * @param data Map with labels as keys and values as data
     */
    public static void populatePieChart(PieChart chart, Map<String, ? extends Number> data) {
        chart.getData().clear();
        data.forEach((label, value) -> {
            PieChart.Data slice = new PieChart.Data(
                label + " (" + value.intValue() + ")",
                value.doubleValue()
            );
            chart.getData().add(slice);
        });
    }

    /**
     * Populates a BarChart with data from a map.
     * @param chart The BarChart to populate
     * @param data Map with labels as keys and values as data
     * @param seriesName Name for the data series
     */
    public static void populateBarChart(BarChart<String, Number> chart, Map<String, ? extends Number> data,
                                       String seriesName) {
        chart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(seriesName);

        data.forEach((label, value) -> series.getData().add(new XYChart.Data<>(label, value)));

        chart.getData().add(series);
    }

    /**
     * Populates a LineChart with data from a map.
     * @param chart The LineChart to populate
     * @param data Map with labels as keys and values as data
     * @param seriesName Name for the data series
     */
    public static void populateLineChart(LineChart<String, Number> chart, Map<String, ? extends Number> data,
                                        String seriesName) {
        chart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(seriesName);

        data.forEach((label, value) -> series.getData().add(new XYChart.Data<>(label, value)));

        chart.getData().add(series);
    }

    /**
     * Formats a date for chart labels.
     * @param date The date to format
     * @return Formatted date string (dd/MM)
     */
    public static String formatDateForChart(LocalDate date) {
        return date.format(DATE_FORMATTER);
    }

    /**
     * Formats a currency value for display.
     * @param amount The amount to format
     * @return Formatted currency string
     */
    public static String formatCurrency(double amount) {
        return CURRENCY_FORMAT.format(amount);
    }

    /**
     * Formats a decimal number for display.
     * @param number The number to format
     * @return Formatted number string
     */
    public static String formatNumber(double number) {
        return DECIMAL_FORMAT.format(number);
    }

    /**
     * Formats a percentage for display.
     * @param percentage The percentage to format (0-100)
     * @return Formatted percentage string
     */
    public static String formatPercentage(double percentage) {
        return String.format("%.1f%%", percentage);
    }

    /**
     * Applies a color theme to a chart based on index.
     * @param index The index to determine color
     * @return Color string
     */
    public static String getChartColor(int index) {
        String[] colors = {PRIMARY_COLOR, SUCCESS_COLOR, WARNING_COLOR, DANGER_COLOR, INFO_COLOR, SECONDARY_COLOR};
        return colors[index % colors.length];
    }

    /**
     * Gets a color for a specific status type.
     * @param status The status string
     * @return Color string
     */
    public static String getStatusColor(String status) {
        if (status == null) return SECONDARY_COLOR;

        switch (status.toUpperCase()) {
            case "DELIVERED":
            case "COMPLETED":
            case "SUCCESS":
                return SUCCESS_COLOR;

            case "PENDING":
            case "PENDING_ASSIGNMENT":
                return WARNING_COLOR;

            case "CANCELLED":
            case "FAILED":
            case "ERROR":
                return DANGER_COLOR;

            case "IN_TRANSIT":
            case "OUT_FOR_DELIVERY":
            case "PROCESSING":
                return INFO_COLOR;

            default:
                return SECONDARY_COLOR;
        }
    }

    /**
     * Formats a large number with K/M suffix.
     * @param number The number to format
     * @return Formatted number with suffix
     */
    public static String formatLargeNumber(double number) {
        if (number >= 1_000_000) {
            return String.format("%.1fM", number / 1_000_000);
        } else if (number >= 1_000) {
            return String.format("%.1fK", number / 1_000);
        } else {
            return String.format("%.0f", number);
        }
    }

    /**
     * Converts hours to a human-readable format.
     * @param hours Number of hours
     * @return Formatted string (e.g., "2d 5h" or "15h")
     */
    public static String formatHours(double hours) {
        if (hours >= 24) {
            int days = (int) (hours / 24);
            int remainingHours = (int) (hours % 24);
            return days + "d " + remainingHours + "h";
        } else {
            return String.format("%.0fh", hours);
        }
    }

    /**
     * Creates tooltip text for chart data points.
     * @param label The data point label
     * @param value The data point value
     * @param isCurrency Whether the value is currency
     * @return Formatted tooltip text
     */
    public static String createTooltip(String label, double value, boolean isCurrency) {
        String formattedValue = isCurrency ? formatCurrency(value) : formatNumber(value);
        return label + ": " + formattedValue;
    }
}
