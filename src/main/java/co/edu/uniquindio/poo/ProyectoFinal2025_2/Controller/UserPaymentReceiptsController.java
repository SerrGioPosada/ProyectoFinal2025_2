package co.edu.uniquindio.poo.ProyectoFinal2025_2.Controller;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.User;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.PaymentStatus;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.dto.PaymentReceiptDTO;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.AuthenticationService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.PaymentService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.DialogUtil;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;
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
    @FXML private TableColumn<PaymentReceiptDTO, Void> colActions;

    @FXML private VBox emptyStateBox;
    @FXML private Label lblTotalPayments;
    @FXML private Label lblTotalAmount;
    @FXML private ComboBox<String> cmbStatusFilter;

    private final AuthenticationService authService = AuthenticationService.getInstance();
    private final PaymentService paymentService = new PaymentService();
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
            String method = data.getValue().getPaymentProvider().toString().replace("_", " ");
            return new SimpleStringProperty(method);
        });

        colAccountNumber.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAccountNumber()));

        // Actions column
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnView = new Button("Ver Detalle");

            {
                btnView.getStyleClass().add("btn-secondary");
                btnView.setStyle("-fx-padding: 6 12; -fx-font-size: 12px;");
                btnView.setOnAction(event -> {
                    PaymentReceiptDTO receipt = getTableView().getItems().get(getIndex());
                    showReceiptDetail(receipt);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btnView);
                    setAlignment(Pos.CENTER);
                }
            }
        });
    }

    private void setupStatusFilter() {
        cmbStatusFilter.getItems().add("Todos");
        cmbStatusFilter.getItems().add("Aprobados");
        cmbStatusFilter.getItems().add("Pendientes");
        cmbStatusFilter.getItems().add("Fallidos");
        cmbStatusFilter.setValue("Todos");

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

        ButtonType closeButton = new ButtonType("Cerrar", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(closeButton);

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
        dialog.getDialogPane().setMinWidth(500);

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

    public void setIndexController(IndexController indexController) {
        this.indexController = indexController;
    }
}
