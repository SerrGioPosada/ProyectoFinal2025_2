package co.edu.uniquindio.poo.ProyectoFinal2025_2.Controller;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.PaymentMethod;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.User;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.PaymentMethodType;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.PaymentProvider;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.AuthenticationService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.PaymentMethodService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.DialogUtil;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for managing user's payment methods.
 */
public class ManagePaymentMethodsController implements Initializable {

    @FXML private VBox paymentMethodsContainer;
    @FXML private VBox emptyStateBox;
    @FXML private Label lblMethodCount;

    private final AuthenticationService authService = AuthenticationService.getInstance();
    private final PaymentMethodService paymentMethodService = new PaymentMethodService();
    private User currentUser;
    private IndexController indexController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        currentUser = (User) authService.getCurrentPerson();

        if (currentUser == null) {
            Logger.error("No user logged in");
            DialogUtil.showError("Error", "No hay usuario autenticado");
            return;
        }

        loadPaymentMethods();
        Logger.info("ManagePaymentMethodsController initialized for user: " + currentUser.getId());
    }

    private void loadPaymentMethods() {
        List<PaymentMethod> methods = paymentMethodService.getPaymentMethodsByUserId(currentUser.getId());

        paymentMethodsContainer.getChildren().clear();

        if (methods.isEmpty()) {
            showEmptyState();
        } else {
            hideEmptyState();
            for (PaymentMethod method : methods) {
                paymentMethodsContainer.getChildren().add(createPaymentMethodCard(method));
            }
        }

        lblMethodCount.setText(String.valueOf(methods.size()));
    }

    private HBox createPaymentMethodCard(PaymentMethod method) {
        HBox card = new HBox(20);
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().add("payment-method-card");
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 8;");

        // Icon/Type indicator
        VBox iconBox = new VBox(5);
        iconBox.setAlignment(Pos.CENTER);
        iconBox.setPrefWidth(80);
        Label iconLabel = new Label(getIconForType(method.getType()));
        iconLabel.setStyle("-fx-font-size: 32px;");
        Label typeLabel = new Label(getTypeLabel(method.getType()));
        typeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6c757d;");
        iconBox.getChildren().addAll(iconLabel, typeLabel);

        // Payment method details
        VBox detailsBox = new VBox(8);
        detailsBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(detailsBox, Priority.ALWAYS);

        Label providerLabel = new Label(method.getProvider().toString().replace("_", " "));
        providerLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #032d4d;");

        Label accountLabel = new Label("Cuenta: " + method.getAccountNumber());
        accountLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6c757d;");

        detailsBox.getChildren().addAll(providerLabel, accountLabel);

        // Action buttons
        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER_RIGHT);

        Button btnEdit = new Button("Editar");
        btnEdit.getStyleClass().add("btn-secondary");
        btnEdit.setStyle("-fx-padding: 8 16;");
        btnEdit.setOnAction(e -> handleEditPaymentMethod(method));

        Button btnDelete = new Button("Eliminar");
        btnDelete.getStyleClass().add("btn-danger");
        btnDelete.setStyle("-fx-padding: 8 16;");
        btnDelete.setOnAction(e -> handleDeletePaymentMethod(method));

        buttonsBox.getChildren().addAll(btnEdit, btnDelete);

        card.getChildren().addAll(iconBox, detailsBox, buttonsBox);
        return card;
    }

    private String getIconForType(PaymentMethodType type) {
        return switch (type) {
            case CREDIT_CARD -> "💳";
            case DEBIT_CARD -> "💳";
            case CASH -> "💵";
            case DIGITAL_WALLET -> "📱";
            case PAYPAL -> "🅿️";
            default -> "💰";
        };
    }

    private String getTypeLabel(PaymentMethodType type) {
        return switch (type) {
            case CREDIT_CARD -> "Crédito";
            case DEBIT_CARD -> "Débito";
            case CASH -> "Efectivo";
            case DIGITAL_WALLET -> "Wallet";
            case PAYPAL -> "PayPal";
            default -> "Otro";
        };
    }

    @FXML
    private void handleAddPaymentMethod() {
        Dialog<PaymentMethod> dialog = createPaymentMethodDialog(null);
        Optional<PaymentMethod> result = dialog.showAndWait();

        result.ifPresent(method -> {
            try {
                paymentMethodService.createPaymentMethod(
                    currentUser.getId(),
                    method.getType(),
                    method.getProvider(),
                    method.getAccountNumber()
                );
                DialogUtil.showSuccess("Éxito", "Método de pago agregado correctamente");
                loadPaymentMethods();
            } catch (Exception e) {
                Logger.error("Failed to add payment method: " + e.getMessage());
                DialogUtil.showError("Error", "No se pudo agregar el método de pago");
            }
        });
    }

    private void handleEditPaymentMethod(PaymentMethod method) {
        Dialog<PaymentMethod> dialog = createPaymentMethodDialog(method);
        Optional<PaymentMethod> result = dialog.showAndWait();

        result.ifPresent(updatedMethod -> {
            try {
                paymentMethodService.updatePaymentMethod(
                    method.getId(),
                    updatedMethod.getType(),
                    updatedMethod.getProvider(),
                    updatedMethod.getAccountNumber()
                );
                DialogUtil.showSuccess("Éxito", "Método de pago actualizado correctamente");
                loadPaymentMethods();
            } catch (Exception e) {
                Logger.error("Failed to update payment method: " + e.getMessage());
                DialogUtil.showError("Error", "No se pudo actualizar el método de pago");
            }
        });
    }

    private void handleDeletePaymentMethod(PaymentMethod method) {
        boolean confirmed = DialogUtil.showConfirmation(
            "Confirmar Eliminación",
            "¿Está seguro que desea eliminar este método de pago?\n\n" +
            method.getProvider() + " - " + method.getAccountNumber()
        );

        if (confirmed) {
            try {
                paymentMethodService.deletePaymentMethod(method.getId());
                DialogUtil.showSuccess("Éxito", "Método de pago eliminado correctamente");
                loadPaymentMethods();
            } catch (Exception e) {
                Logger.error("Failed to delete payment method: " + e.getMessage());
                DialogUtil.showError("Error", "No se pudo eliminar el método de pago");
            }
        }
    }

    private Dialog<PaymentMethod> createPaymentMethodDialog(PaymentMethod existingMethod) {
        Dialog<PaymentMethod> dialog = new Dialog<>();
        dialog.setTitle(existingMethod == null ? "Agregar Método de Pago" : "Editar Método de Pago");
        dialog.setHeaderText(existingMethod == null ?
            "Ingrese los datos del nuevo método de pago" :
            "Modifique los datos del método de pago");

        ButtonType saveButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);

        // Style the dialog pane
        dialog.getDialogPane().setStyle(
            "-fx-background-color: white;" +
            "-fx-padding: 20;" +
            "-fx-font-family: 'Segoe UI';"
        );

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20, 20, 10, 20));
        grid.setStyle("-fx-background-color: white;");

        // Style labels
        Label lblType = new Label("Tipo:");
        lblType.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label lblProvider = new Label("Proveedor:");
        lblProvider.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label lblNumber = new Label("Número:");
        lblNumber.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        ComboBox<PaymentMethodType> cmbType = new ComboBox<>();
        cmbType.getItems().addAll(PaymentMethodType.values());
        cmbType.setPromptText("Seleccione tipo");
        cmbType.setPrefWidth(250);

        // Configure cell factory for PaymentMethodType with Spanish labels
        cmbType.setCellFactory(lv -> new ListCell<PaymentMethodType>() {
            @Override
            protected void updateItem(PaymentMethodType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(getTypeLabel(item));
                }
            }
        });

        cmbType.setButtonCell(new ListCell<PaymentMethodType>() {
            @Override
            protected void updateItem(PaymentMethodType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Seleccione tipo");
                } else {
                    setText(getTypeLabel(item));
                }
            }
        });

        ComboBox<PaymentProvider> cmbProvider = new ComboBox<>();
        cmbProvider.getItems().addAll(PaymentProvider.values());
        cmbProvider.setPromptText("Seleccione proveedor");
        cmbProvider.setPrefWidth(250);

        // Configure cell factory for PaymentProvider with Spanish labels
        cmbProvider.setCellFactory(lv -> new ListCell<PaymentProvider>() {
            @Override
            protected void updateItem(PaymentProvider item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(getProviderLabel(item));
                }
            }
        });

        cmbProvider.setButtonCell(new ListCell<PaymentProvider>() {
            @Override
            protected void updateItem(PaymentProvider item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Seleccione proveedor");
                } else {
                    setText(getProviderLabel(item));
                }
            }
        });

        TextField txtAccountNumber = new TextField();
        txtAccountNumber.setPromptText("Número de cuenta/tarjeta");
        txtAccountNumber.setPrefWidth(250);
        txtAccountNumber.setStyle("-fx-font-size: 14px; -fx-padding: 8;");

        if (existingMethod != null) {
            cmbType.setValue(existingMethod.getType());
            cmbProvider.setValue(existingMethod.getProvider());
            txtAccountNumber.setText(existingMethod.getAccountNumber());
        }

        grid.add(lblType, 0, 0);
        grid.add(cmbType, 1, 0);
        grid.add(lblProvider, 0, 1);
        grid.add(cmbProvider, 1, 1);
        grid.add(lblNumber, 0, 2);
        grid.add(txtAccountNumber, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (cmbType.getValue() == null || cmbProvider.getValue() == null ||
                    txtAccountNumber.getText().trim().isEmpty()) {
                    DialogUtil.showError("Error", "Todos los campos son obligatorios");
                    return null;
                }

                PaymentMethod method = new PaymentMethod();
                method.setType(cmbType.getValue());
                method.setProvider(cmbProvider.getValue());
                method.setAccountNumber(txtAccountNumber.getText().trim());
                return method;
            }
            return null;
        });

        return dialog;
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
        paymentMethodsContainer.setVisible(false);
        paymentMethodsContainer.setManaged(false);
    }

    private void hideEmptyState() {
        emptyStateBox.setVisible(false);
        emptyStateBox.setManaged(false);
        paymentMethodsContainer.setVisible(true);
        paymentMethodsContainer.setManaged(true);
    }

    public void setIndexController(IndexController indexController) {
        this.indexController = indexController;
    }
}
