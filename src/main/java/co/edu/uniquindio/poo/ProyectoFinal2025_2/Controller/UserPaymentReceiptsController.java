package co.edu.uniquindio.poo.ProyectoFinal2025_2.Controller;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Invoice;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.User;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.PaymentStatus;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.OrderStatus;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.PaymentProvider;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.PaymentMethodType;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.dto.PaymentReceiptDTO;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.AuthenticationService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.InvoiceService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.PaymentService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.DialogUtil;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.PdfUtility;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for displaying user's payment receipts.
 */
public class UserPaymentReceiptsController implements Initializable {

    @FXML private TableView<PaymentReceiptDTO> receiptsTable;
    @FXML private TableColumn<PaymentReceiptDTO, String> colInvoiceNumber;
    @FXML private TableColumn<PaymentReceiptDTO, String> colPaymentDate;
    @FXML private TableColumn<PaymentReceiptDTO, String> colAmount;
    @FXML private TableColumn<PaymentReceiptDTO, String> colStatus;
    @FXML private TableColumn<PaymentReceiptDTO, String> colPaymentMethod;
    @FXML private TableColumn<PaymentReceiptDTO, String> colAccountNumber;

    @FXML private VBox emptyStateBox;
    @FXML private Label lblTotalPayments;
    @FXML private Label lblTotalAmount;
    @FXML private ComboBox<String> cmbStatusFilter;

    private final AuthenticationService authService = AuthenticationService.getInstance();
    private final PaymentService paymentService = new PaymentService();
    private final InvoiceService invoiceService = new InvoiceService();
    private User currentUser;
    private ObservableList<PaymentReceiptDTO> receiptsData;
    private FilteredList<PaymentReceiptDTO> filteredReceipts;
    private IndexController indexController;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        currentUser = (User) authService.getCurrentPerson();

        if (currentUser == null) {
            Logger.error("No user logged in");
            DialogUtil.showError("Error", "No hay usuario autenticado");
            return;
        }

        setupTable();
        setupStatusFilter();
        loadReceipts();
        Logger.info("UserPaymentReceiptsController initialized for user: " + currentUser.getId());
    }

    private void setupTable() {
        // Configure columns
        colInvoiceNumber.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getInvoiceNumber()));

        colPaymentDate.setCellValueFactory(data -> {
            String formattedDate = data.getValue().getPaymentDate().format(DATE_FORMATTER);
            return new SimpleStringProperty(formattedDate);
        });

        colAmount.setCellValueFactory(data -> {
            String formattedAmount = String.format("$%.2f", data.getValue().getAmount());
            return new SimpleStringProperty(formattedAmount);
        });

        colStatus.setCellValueFactory(data -> new SimpleStringProperty(getStatusLabel(data.getValue().getStatus())));
        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    PaymentReceiptDTO receipt = getTableView().getItems().get(getIndex());
                    setStyle(getStatusStyle(receipt.getStatus()));
                }
            }
        });

        colPaymentMethod.setCellValueFactory(data -> {
            String method = getProviderLabel(data.getValue().getPaymentProvider());
            return new SimpleStringProperty(method);
        });

        colAccountNumber.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAccountNumber()));

        // Setup context menu for table rows
        setupContextMenu();
    }

    /**
     * Sets up the context menu for table rows.
     */
    private void setupContextMenu() {
        receiptsTable.setRowFactory(tv -> {
            TableRow<PaymentReceiptDTO> row = new TableRow<>();

            ContextMenu contextMenu = new ContextMenu();

            // Ver Detalles del Comprobante
            MenuItem viewDetailsItem = new MenuItem("Ver Detalles");
            viewDetailsItem.setOnAction(event -> {
                PaymentReceiptDTO selected = row.getItem();
                if (selected != null) {
                    showReceiptDetail(selected);
                }
            });

            // Ver Orden Asociada
            MenuItem viewOrderItem = new MenuItem("Ver Orden Asociada");
            viewOrderItem.setOnAction(event -> {
                PaymentReceiptDTO selected = row.getItem();
                if (selected != null) {
                    showAssociatedOrder(selected);
                }
            });

            contextMenu.getItems().addAll(viewDetailsItem, viewOrderItem);

            // Only show context menu on non-empty rows
            row.setOnContextMenuRequested(event -> {
                if (!row.isEmpty()) {
                    contextMenu.show(row, event.getScreenX(), event.getScreenY());
                }
            });

            // Also allow double-click to view details
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showReceiptDetail(row.getItem());
                }
            });

            return row;
        });
    }

    /**
     * Shows the associated order details for a payment receipt.
     */
    private void showAssociatedOrder(PaymentReceiptDTO receipt) {
        // Get the order
        co.edu.uniquindio.poo.ProyectoFinal2025_2.Repositories.OrderRepository orderRepo =
            co.edu.uniquindio.poo.ProyectoFinal2025_2.Repositories.OrderRepository.getInstance();

        Optional<co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Order> orderOpt = orderRepo.findById(receipt.getOrderId());
        if (!orderOpt.isPresent()) {
            DialogUtil.showError("Error", "No se pudo encontrar la orden asociada");
            return;
        }

        co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Order order = orderOpt.get();

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Orden Asociada");
        dialog.setHeaderText("Orden #" + order.getId());

        // Apply stylesheet
        dialog.getDialogPane().getStylesheets().add(
            getClass().getResource("/co/edu/uniquindio/poo/ProyectoFinal2025_2/Style.css").toExternalForm()
        );
        dialog.getDialogPane().getStyleClass().add("dialog-pane");

        ButtonType closeButton = new ButtonType("Cerrar", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(closeButton);

        // Style the close button
        Button closeBtn = (Button) dialog.getDialogPane().lookupButton(closeButton);
        if (closeBtn != null) {
            closeBtn.getStyleClass().add("btn-secondary");
        }

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color: white;");

        int row = 0;

        addDetailRow(grid, row++, "ID Orden:", order.getId());
        addDetailRow(grid, row++, "Estado:", translateOrderStatus(order.getStatus()));
        addDetailRow(grid, row++, "Origen:", order.getOrigin().getCity() + ", " + order.getOrigin().getStreet());
        addDetailRow(grid, row++, "Destino:", order.getDestination().getCity() + ", " + order.getDestination().getStreet());
        addDetailRow(grid, row++, "Fecha Creación:", order.getCreatedAt().format(DATE_FORMATTER));
        addDetailRow(grid, row++, "Costo Total:", String.format("$%.2f", order.getTotalCost()));

        if (order.getShipmentId() != null && !order.getShipmentId().isEmpty()) {
            addDetailRow(grid, row++, "ID Envío:", order.getShipmentId());
        }
        if (order.getInvoiceId() != null && !order.getInvoiceId().isEmpty()) {
            addDetailRow(grid, row++, "ID Factura:", order.getInvoiceId());
        }

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setMinWidth(550);
        dialog.getDialogPane().setMinHeight(400);

        dialog.showAndWait();
    }

    private String translateOrderStatus(OrderStatus status) {
        return switch (status) {
            case AWAITING_PAYMENT -> "Esperando Pago";
            case PAID -> "Pagado";
            case PENDING_APPROVAL -> "Pendiente de Aprobación";
            case APPROVED -> "Aprobado";
            case CANCELLED -> "Cancelado";
        };
    }

    private void setupStatusFilter() {
        cmbStatusFilter.getItems().add("Todos");
        cmbStatusFilter.getItems().add("Aprobados");
        cmbStatusFilter.getItems().add("Pendientes");
        cmbStatusFilter.getItems().add("Fallidos");
        cmbStatusFilter.setPromptText("Filtrar por estado");
        cmbStatusFilter.setValue("Todos");

        // Configure button cell to show selected value or prompt text
        cmbStatusFilter.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                // Show prompt text only when there's no value selected
                if (cmbStatusFilter.getValue() == null) {
                    setText(cmbStatusFilter.getPromptText());
                } else {
                    setText(cmbStatusFilter.getValue());
                }
            }
        });

        cmbStatusFilter.setOnAction(e -> applyFilter());
    }

    private void loadReceipts() {
        List<PaymentReceiptDTO> receipts = paymentService.getUserPaymentReceipts(currentUser.getId());

        receiptsData = FXCollections.observableArrayList(receipts);
        filteredReceipts = new FilteredList<>(receiptsData, p -> true);
        receiptsTable.setItems(filteredReceipts);

        updateStats();
        updateUI();
    }

    private void applyFilter() {
        String selectedFilter = cmbStatusFilter.getValue();

        filteredReceipts.setPredicate(receipt -> {
            if (selectedFilter == null || selectedFilter.equals("Todos")) {
                return true;
            }

            return switch (selectedFilter) {
                case "Aprobados" -> receipt.getStatus() == PaymentStatus.APPROVED;
                case "Pendientes" -> receipt.getStatus() == PaymentStatus.PENDING;
                case "Fallidos" -> receipt.getStatus() == PaymentStatus.FAILED;
                default -> true;
            };
        });

        updateStats();
    }

    private void updateStats() {
        int totalPayments = filteredReceipts.size();
        double totalAmount = filteredReceipts.stream()
                .filter(r -> r.getStatus() == PaymentStatus.APPROVED)
                .mapToDouble(PaymentReceiptDTO::getAmount)
                .sum();

        lblTotalPayments.setText(String.valueOf(totalPayments));
        lblTotalAmount.setText(String.format("$%.2f", totalAmount));
    }

    private void updateUI() {
        if (receiptsData.isEmpty()) {
            showEmptyState();
        } else {
            hideEmptyState();
        }
    }

    private void showReceiptDetail(PaymentReceiptDTO receipt) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Detalle del Comprobante");
        dialog.setHeaderText("Comprobante de Pago - " + receipt.getInvoiceNumber());

        // Apply stylesheet to dialog
        dialog.getDialogPane().getStylesheets().add(
            getClass().getResource("/co/edu/uniquindio/poo/ProyectoFinal2025_2/Style.css").toExternalForm()
        );
        dialog.getDialogPane().getStyleClass().add("dialog-pane");

        ButtonType downloadPdfButton = new ButtonType("Descargar PDF", ButtonBar.ButtonData.LEFT);
        ButtonType closeButton = new ButtonType("Cerrar", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(downloadPdfButton, closeButton);

        // Style the buttons
        Button pdfBtn = (Button) dialog.getDialogPane().lookupButton(downloadPdfButton);
        if (pdfBtn != null) {
            pdfBtn.getStyleClass().add("btn-primary");
        }

        Button closeBtn = (Button) dialog.getDialogPane().lookupButton(closeButton);
        if (closeBtn != null) {
            closeBtn.getStyleClass().add("btn-secondary");
        }

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color: white;");

        int row = 0;

        addDetailRow(grid, row++, "# Pago:", receipt.getPaymentId());
        addDetailRow(grid, row++, "# Factura:", receipt.getInvoiceNumber());
        addDetailRow(grid, row++, "# Orden:", receipt.getOrderId());
        addDetailRow(grid, row++, "Fecha:", receipt.getPaymentDate().format(DATE_FORMATTER));
        addDetailRow(grid, row++, "Monto:", String.format("$%.2f", receipt.getAmount()));
        addDetailRow(grid, row++, "Estado:", getStatusLabel(receipt.getStatus()));
        addDetailRow(grid, row++, "Método de Pago:", receipt.getPaymentMethodType().toString().replace("_", " "));
        addDetailRow(grid, row++, "Proveedor:", receipt.getPaymentProvider().toString().replace("_", " "));
        addDetailRow(grid, row++, "Cuenta:", receipt.getAccountNumber());

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setMinWidth(550);
        dialog.getDialogPane().setMinHeight(400);

        // Handle PDF download button
        if (pdfBtn != null) {
            pdfBtn.setOnAction(event -> {
                event.consume(); // Prevent dialog from closing
                handleDownloadReceiptPDF(receipt);
            });
        }

        dialog.showAndWait();
    }

    private void addDetailRow(GridPane grid, int row, String label, String value) {
        Label lblLabel = new Label(label);
        lblLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #032d4d; -fx-font-size: 14px;");

        Label lblValue = new Label(value);
        lblValue.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 14px;");

        grid.add(lblLabel, 0, row);
        grid.add(lblValue, 1, row);
    }

    private String getStatusLabel(PaymentStatus status) {
        return switch (status) {
            case APPROVED -> "Aprobado";
            case PENDING -> "Pendiente";
            case FAILED -> "Fallido";
            case REFUNDED -> "Reembolsado";
        };
    }

    private String getStatusStyle(PaymentStatus status) {
        return switch (status) {
            case APPROVED -> "-fx-text-fill: #28a745; -fx-font-weight: bold;";
            case PENDING -> "-fx-text-fill: #ffc107; -fx-font-weight: bold;";
            case FAILED -> "-fx-text-fill: #dc3545; -fx-font-weight: bold;";
            case REFUNDED -> "-fx-text-fill: #6c757d; -fx-font-weight: bold;";
        };
    }

    private String getProviderLabel(PaymentProvider provider) {
        return switch (provider) {
            case VISA -> "Visa";
            case MASTERCARD -> "Mastercard";
            case AMERICAN_EXPRESS -> "American Express";
            case PAYPAL -> "PayPal";
            case STRIPE -> "Stripe";
            case MERCADO_PAGO -> "Mercado Pago";
            case CASH -> "Efectivo";
            case OTHER -> "Otro";
        };
    }

    private void showEmptyState() {
        emptyStateBox.setVisible(true);
        emptyStateBox.setManaged(true);
        receiptsTable.setVisible(false);
        receiptsTable.setManaged(false);
    }

    private void hideEmptyState() {
        emptyStateBox.setVisible(false);
        emptyStateBox.setManaged(false);
        receiptsTable.setVisible(true);
        receiptsTable.setManaged(true);
    }

    /**
     * Handles downloading the receipt PDF.
     */
    private void handleDownloadReceiptPDF(PaymentReceiptDTO receipt) {
        try {
            // Get the invoice repository
            co.edu.uniquindio.poo.ProyectoFinal2025_2.Repositories.InvoiceRepository invoiceRepository =
                co.edu.uniquindio.poo.ProyectoFinal2025_2.Repositories.InvoiceRepository.getInstance();

            // Get the invoice by ID
            Optional<Invoice> invoiceOpt = invoiceRepository.findById(receipt.getInvoiceId());
            if (!invoiceOpt.isPresent()) {
                DialogUtil.showError("Error", "No se pudo encontrar la factura");
                return;
            }

            Invoice invoice = invoiceOpt.get();

            // Generate PDF
            java.io.File pdfFile = PdfUtility.generateInvoicePDF(invoice, currentUser);

            // Show success message with file location
            DialogUtil.showInfo("PDF Generado",
                "El comprobante se guardó exitosamente en:\n" + pdfFile.getAbsolutePath());

            Logger.info("Generated PDF receipt for invoice: " + receipt.getInvoiceNumber());
        } catch (java.io.IOException e) {
            Logger.error("Error generating PDF: " + e.getMessage());
            DialogUtil.showError("Error", "Error al generar el PDF: " + e.getMessage());
        }
    }

    public void setIndexController(IndexController indexController) {
        this.indexController = indexController;
    }
}
