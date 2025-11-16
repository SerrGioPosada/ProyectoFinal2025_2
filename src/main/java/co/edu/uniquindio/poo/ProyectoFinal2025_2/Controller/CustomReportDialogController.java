package co.edu.uniquindio.poo.ProyectoFinal2025_2.Controller;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.ReportService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.DialogUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;
import java.time.LocalDate;

/**
 * Controller for the custom report generation dialog.
 * Allows users to customize report parameters including type, format, and date range.
 */
public class CustomReportDialogController {

    @FXML private ComboBox<String> cmbReportType;
    @FXML private RadioButton rbPDF;
    @FXML private RadioButton rbCSV;
    @FXML private ToggleGroup formatGroup;
    @FXML private DatePicker dateFrom;
    @FXML private DatePicker dateTo;

    private final ReportService reportService = ReportService.getInstance();

    /**
     * Initializes the controller after FXML elements are loaded.
     * Sets up ComboBox items, date pickers, and default values.
     */
    @FXML
    public void initialize() {
        setupReportTypeComboBox();
        setupDatePickers();
    }

    /**
     * Sets up the report type ComboBox with available report types.
     */
    private void setupReportTypeComboBox() {
        cmbReportType.getItems().addAll(
                "Reporte General",
                "Reporte Financiero",
                "Reporte de Envíos",
                "Reporte de Usuarios",
                "Reporte de Repartidores"
        );

        cmbReportType.setPromptText("Seleccionar tipo de reporte...");

        // Custom button cell to show prompt text when no selection
        cmbReportType.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Seleccionar tipo de reporte...");
                    setStyle("-fx-text-fill: #999999 !important; -fx-font-size: 13px;");
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: #032d4d !important; -fx-font-size: 13px;");
                }
            }
        });
    }

    /**
     * Sets up the date pickers with default date range (last 30 days).
     */
    private void setupDatePickers() {
        LocalDate today = LocalDate.now();
        dateTo.setValue(today);
        dateFrom.setValue(today.minusDays(30));
    }

    /**
     * Sets date range to the last week.
     */
    @FXML
    private void setLastWeek() {
        LocalDate today = LocalDate.now();
        dateTo.setValue(today);
        dateFrom.setValue(today.minusWeeks(1));
    }

    /**
     * Sets date range to the last month.
     */
    @FXML
    private void setLastMonth() {
        LocalDate today = LocalDate.now();
        dateTo.setValue(today);
        dateFrom.setValue(today.minusMonths(1));
    }

    /**
     * Sets date range to the last 3 months.
     */
    @FXML
    private void setLast3Months() {
        LocalDate today = LocalDate.now();
        dateTo.setValue(today);
        dateFrom.setValue(today.minusMonths(3));
    }

    /**
     * Handles the generate button action.
     * Validates inputs and generates the selected report.
     */
    @FXML
    private void handleGenerate() {
        // Validate inputs
        if (cmbReportType.getValue() == null) {
            DialogUtil.showError("Error de Validación",
                    "Por favor, seleccione un tipo de reporte.");
            return;
        }

        if (dateFrom.getValue() == null || dateTo.getValue() == null) {
            DialogUtil.showError("Error de Validación",
                    "Por favor, seleccione el rango de fechas.");
            return;
        }

        if (dateFrom.getValue().isAfter(dateTo.getValue())) {
            DialogUtil.showError("Error de Validación",
                    "La fecha inicial no puede ser posterior a la fecha final.");
            return;
        }

        // Determine format
        boolean isPDF = rbPDF.isSelected();

        // Generate report based on selected type
        try {
            String reportType = cmbReportType.getValue();
            File reportFile = null;

            switch (reportType) {
                case "Reporte General":
                    if (isPDF) {
                        reportFile = reportService.generateGeneralReportPDF(
                                dateFrom.getValue(), dateTo.getValue());
                    } else {
                        reportFile = reportService.generateGeneralReportCSV(
                                dateFrom.getValue(), dateTo.getValue());
                    }
                    break;

                case "Reporte Financiero":
                    if (isPDF) {
                        reportFile = reportService.generateFinancialReportPDF(
                                dateFrom.getValue(), dateTo.getValue());
                    } else {
                        reportFile = reportService.generateFinancialReportCSV(
                                dateFrom.getValue(), dateTo.getValue());
                    }
                    break;

                case "Reporte de Envíos":
                    if (isPDF) {
                        reportFile = reportService.generateShipmentsReportPDF(
                                dateFrom.getValue(), dateTo.getValue());
                    } else {
                        reportFile = reportService.generateShipmentsReportCSV(
                                dateFrom.getValue(), dateTo.getValue());
                    }
                    break;

                case "Reporte de Usuarios":
                    if (isPDF) {
                        reportFile = reportService.generateUsersReportPDF(
                                dateFrom.getValue(), dateTo.getValue());
                    } else {
                        reportFile = reportService.generateUsersReportCSV(
                                dateFrom.getValue(), dateTo.getValue());
                    }
                    break;

                case "Reporte de Repartidores":
                    if (isPDF) {
                        reportFile = reportService.generateDeliveryPersonnelReportPDF(
                                dateFrom.getValue(), dateTo.getValue());
                    } else {
                        reportFile = reportService.generateDeliveryPersonnelReportCSV(
                                dateFrom.getValue(), dateTo.getValue());
                    }
                    break;

                default:
                    DialogUtil.showError("Error", "Tipo de reporte no reconocido.");
                    return;
            }

            // Show success message
            if (reportFile != null) {
                final File finalReportFile = reportFile; // Make it effectively final for lambda
                String format = isPDF ? "PDF" : "CSV";
                String filePath = reportFile.getAbsolutePath();
                String message = String.format(
                        "Reporte generado exitosamente:\n\n%s\n\n¿Desea abrir el archivo?",
                        filePath);

                ButtonType openButton = new ButtonType("Abrir");
                ButtonType closeButton = new ButtonType("Cerrar", ButtonBar.ButtonData.CANCEL_CLOSE);

                Alert alert = new Alert(Alert.AlertType.INFORMATION, message, openButton, closeButton);
                alert.setTitle("Reporte Generado");
                alert.setHeaderText("✅ " + reportType + " (" + format + ")");

                alert.showAndWait().ifPresent(response -> {
                    if (response == openButton) {
                        openFile(finalReportFile);
                    }
                });

                // Close the dialog
                handleCancel();
            }

        } catch (Exception e) {
            DialogUtil.showError("Error al Generar Reporte",
                    "Ocurrió un error al generar el reporte: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Opens the generated report file using the system's default application.
     *
     * @param file File to open
     */
    private void openFile(File file) {
        try {
            if (file.exists()) {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file);
                } else {
                    DialogUtil.showWarning("Advertencia",
                            "No se puede abrir el archivo automáticamente. " +
                            "Por favor, ábralo manualmente desde: " + file.getAbsolutePath());
                }
            } else {
                DialogUtil.showError("Error", "El archivo no fue encontrado: " + file.getAbsolutePath());
            }
        } catch (Exception e) {
            DialogUtil.showError("Error al Abrir Archivo",
                    "No se pudo abrir el archivo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handles the cancel button action.
     * Closes the dialog window.
     */
    @FXML
    private void handleCancel() {
        Stage stage = (Stage) cmbReportType.getScene().getWindow();
        stage.close();
    }
}
