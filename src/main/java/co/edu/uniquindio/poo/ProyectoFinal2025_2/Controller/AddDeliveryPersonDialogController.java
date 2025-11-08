package co.edu.uniquindio.poo.ProyectoFinal2025_2.Controller;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.AvailabilityStatus;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.CoverageArea;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.PersonType;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.dto.PersonCreationData;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.DeliveryPersonService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.DialogUtil;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the Add Delivery Person Dialog (AddDeliveryPersonDialog.fxml).
 * <p>
 * This controller manages a modal dialog for creating new delivery persons.
 * </p>
 */
public class AddDeliveryPersonDialogController implements Initializable {

    // =================================================================================================================
    // FXML Fields
    // =================================================================================================================

    @FXML private TextField txtName;
    @FXML private TextField txtLastName;
    @FXML private TextField txtEmail;
    @FXML private TextField txtPhone;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtPasswordVisible;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private TextField txtConfirmPasswordVisible;
    @FXML private TextField txtDocumentId;
    @FXML private ComboBox<CoverageArea> cmbCoverageArea;
    @FXML private Button btnTogglePassword;
    @FXML private Button btnToggleConfirmPassword;

    // Floating labels
    @FXML private Label lblNameFloat;
    @FXML private Label lblLastNameFloat;
    @FXML private Label lblEmailFloat;
    @FXML private Label lblPhoneFloat;
    @FXML private Label lblPasswordFloat;
    @FXML private Label lblConfirmPasswordFloat;
    @FXML private Label lblDocumentIdFloat;
    @FXML private Label lblCoverageAreaFloat;

    // Error labels
    @FXML private Label lblNameError;
    @FXML private Label lblLastNameError;
    @FXML private Label lblEmailError;
    @FXML private Label lblPhoneError;
    @FXML private Label lblPasswordError;
    @FXML private Label lblConfirmPasswordError;
    @FXML private Label lblDocumentIdError;
    @FXML private Label lblCoverageAreaError;
    @FXML private Label lblError;

    // =================================================================================================================
    // Services and State
    // =================================================================================================================

    private final DeliveryPersonService deliveryPersonService = DeliveryPersonService.getInstance();
    private ManageDeliveryPersonsController parentController;

    // =================================================================================================================
    // Initialization
    // =================================================================================================================

    /**
     * Initializes the controller. Sets up combo boxes.
     *
     * @param url            The location used to resolve relative paths.
     * @param resourceBundle The resources used to localize the root object.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupComboBoxes();
        setupFloatingLabels();
        setupEnterKeyNavigation();
        setupPasswordFieldSync();
        setupPasswordToggleIcons();
        Logger.info("AddDeliveryPersonDialogController initialized.");
    }

    /**
     * Sets up the eye icons for password toggle buttons.
     */
    private void setupPasswordToggleIcons() {
        try {
            // Load eye open image for password field
            Image eyeOpenImage = new Image(getClass().getResourceAsStream("/co/edu/uniquindio/poo/ProyectoFinal2025_2/Images/eye-open.png"));
            ImageView eyeOpenView = new ImageView(eyeOpenImage);
            eyeOpenView.setFitWidth(20);
            eyeOpenView.setFitHeight(20);
            eyeOpenView.setPreserveRatio(true);
            btnTogglePassword.setGraphic(eyeOpenView);
            btnTogglePassword.setText("");

            // Load eye open image for confirm password field
            ImageView eyeOpenView2 = new ImageView(eyeOpenImage);
            eyeOpenView2.setFitWidth(20);
            eyeOpenView2.setFitHeight(20);
            eyeOpenView2.setPreserveRatio(true);
            btnToggleConfirmPassword.setGraphic(eyeOpenView2);
            btnToggleConfirmPassword.setText("");
        } catch (Exception e) {
            Logger.error("Error loading eye icons: " + e.getMessage());
        }
    }

    /**
     * Sets up synchronization between password fields and their visible counterparts.
     */
    private void setupPasswordFieldSync() {
        // Sync password fields
        txtPassword.textProperty().addListener((obs, oldVal, newVal) -> {
            if (txtPassword.isVisible()) {
                txtPasswordVisible.setText(newVal);
            }
        });
        txtPasswordVisible.textProperty().addListener((obs, oldVal, newVal) -> {
            if (txtPasswordVisible.isVisible()) {
                txtPassword.setText(newVal);
            }
        });

        // Sync confirm password fields
        txtConfirmPassword.textProperty().addListener((obs, oldVal, newVal) -> {
            if (txtConfirmPassword.isVisible()) {
                txtConfirmPasswordVisible.setText(newVal);
            }
        });
        txtConfirmPasswordVisible.textProperty().addListener((obs, oldVal, newVal) -> {
            if (txtConfirmPasswordVisible.isVisible()) {
                txtConfirmPassword.setText(newVal);
            }
        });
    }

    /**
     * Sets up combo boxes with values.
     */
    private void setupComboBoxes() {
        // Setup Coverage Area ComboBox
        cmbCoverageArea.getItems().addAll(CoverageArea.values());

        // Custom cell factory to display Spanish names in the dropdown
        cmbCoverageArea.setCellFactory(lv -> new javafx.scene.control.ListCell<CoverageArea>() {
            @Override
            protected void updateItem(CoverageArea item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getDisplayName());
            }
        });

        // Custom button cell to show placeholder when empty
        cmbCoverageArea.setButtonCell(new javafx.scene.control.ListCell<CoverageArea>() {
            @Override
            protected void updateItem(CoverageArea item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Seleccionar Área");
                    setStyle("-fx-text-fill: #999999;");
                } else {
                    setText(item.getDisplayName());
                    setStyle("-fx-text-fill: #032d4d;");
                }
            }
        });
    }

    /**
     * Sets up floating label animations for all text fields.
     */
    private void setupFloatingLabels() {
        setupFieldListeners(txtName, lblNameFloat, "NOMBRE");
        setupFieldListeners(txtLastName, lblLastNameFloat, "APELLIDO");
        setupFieldListeners(txtEmail, lblEmailFloat, "CORREO ELECTRÓNICO");
        setupFieldListeners(txtPhone, lblPhoneFloat, "TELÉFONO");
        setupFieldListeners(txtPassword, lblPasswordFloat, "CONTRASEÑA");
        setupFieldListeners(txtDocumentId, lblDocumentIdFloat, "DOCUMENTO DE IDENTIDAD");
        setupComboBoxListeners(cmbCoverageArea, lblCoverageAreaFloat, "ÁREA DE COBERTURA");
    }

    /**
     * Sets up Enter key navigation between fields.
     */
    private void setupEnterKeyNavigation() {
        if (txtName != null) {
            txtName.setOnKeyPressed(event -> {
                if (event.getCode().toString().equals("ENTER") && !txtName.getText().isEmpty()) {
                    txtLastName.requestFocus();
                }
            });
        }
        if (txtLastName != null) {
            txtLastName.setOnKeyPressed(event -> {
                if (event.getCode().toString().equals("ENTER") && !txtLastName.getText().isEmpty()) {
                    txtEmail.requestFocus();
                }
            });
        }
        if (txtEmail != null) {
            txtEmail.setOnKeyPressed(event -> {
                if (event.getCode().toString().equals("ENTER") && !txtEmail.getText().isEmpty()) {
                    txtPhone.requestFocus();
                }
            });
        }
        if (txtPhone != null) {
            txtPhone.setOnKeyPressed(event -> {
                if (event.getCode().toString().equals("ENTER") && !txtPhone.getText().isEmpty()) {
                    txtPassword.requestFocus();
                }
            });
        }
        if (txtPassword != null) {
            txtPassword.setOnKeyPressed(event -> {
                if (event.getCode().toString().equals("ENTER") && !txtPassword.getText().isEmpty()) {
                    txtDocumentId.requestFocus();
                }
            });
        }
        if (txtDocumentId != null) {
            txtDocumentId.setOnKeyPressed(event -> {
                if (event.getCode().toString().equals("ENTER") && !txtDocumentId.getText().isEmpty()) {
                    cmbCoverageArea.requestFocus();
                }
            });
        }
        if (cmbCoverageArea != null) {
            cmbCoverageArea.setOnKeyPressed(event -> {
                if (event.getCode().toString().equals("ENTER") && cmbCoverageArea.getValue() != null) {
                    handleSave();
                }
            });
        }
    }

    /**
     * Attaches focus and text listeners to a field to create the floating label effect.
     * The label stays visible when the field contains text, even after losing focus.
     *
     * @param field  The text input control (TextField or PasswordField).
     * @param label  The floating label associated with the field.
     * @param prompt The prompt text to restore when the field is empty and unfocused.
     */
    private void setupFieldListeners(Control field, Label label, String prompt) {
        label.setOpacity(0);
        label.setTranslateY(35);

        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            String text = (field instanceof TextField) ? ((TextField) field).getText() : ((PasswordField) field).getText();
            if (newVal) {
                // On focus: always animate label up
                animateFloatingLabel(label, field, true, prompt);
            } else if (text.isEmpty()) {
                // On blur: only hide label if field is empty
                animateFloatingLabel(label, field, false, prompt);
            }
            // If field has text, label stays visible (don't animate down)
        });

        TextInputControl textInput = (TextInputControl) field;
        textInput.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty() && label.getOpacity() < 0.5) {
                // If text is added and label is hidden, show it
                animateFloatingLabel(label, field, true, prompt);
            } else if (newVal.isEmpty() && !field.isFocused()) {
                // If text is cleared and field is not focused, hide label
                animateFloatingLabel(label, field, false, prompt);
            }
        });
    }

    /**
     * Attaches focus and value listeners to a ComboBox to create the floating label effect.
     * The label stays visible when the ComboBox has a selected value, even after losing focus.
     *
     * @param comboBox The ComboBox control.
     * @param label    The floating label associated with the ComboBox.
     * @param prompt   The prompt text to restore when the ComboBox is empty and unfocused.
     */
    private void setupComboBoxListeners(ComboBox<?> comboBox, Label label, String prompt) {
        label.setOpacity(0);
        label.setTranslateY(35);

        comboBox.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                // On focus: always animate label up
                animateFloatingLabel(label, comboBox, true, prompt);
            } else if (comboBox.getValue() == null) {
                // On blur: only hide label if no value is selected
                animateFloatingLabel(label, comboBox, false, prompt);
            }
            // If combo has value, label stays visible (don't animate down)
        });

        comboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && label.getOpacity() < 0.5) {
                // If value is selected and label is hidden, show it
                animateFloatingLabel(label, comboBox, true, prompt);
            } else if (newVal == null && !comboBox.isFocused()) {
                // If value is cleared and combo is not focused, hide label
                animateFloatingLabel(label, comboBox, false, prompt);
            }
        });
    }

    /**
     * Animates a floating label up or down based on the focus state.
     *
     * @param label      The label to animate.
     * @param field      The associated control.
     * @param moveUp     True to move the label up, false to move it down.
     * @param promptText The original prompt text to restore.
     */
    private void animateFloatingLabel(Label label, Control field, boolean moveUp, String promptText) {
        TranslateTransition translate = new TranslateTransition(Duration.millis(200), label);
        FadeTransition fade = new FadeTransition(Duration.millis(200), label);

        if (moveUp) {
            translate.setToY(0);
            fade.setToValue(1.0);
            if (field instanceof TextInputControl) {
                ((TextInputControl) field).setPromptText(null);
            } else if (field instanceof ComboBox) {
                ((ComboBox<?>) field).setPromptText(null);
            }
        } else {
            translate.setToY(35);
            fade.setToValue(0.0);
            fade.setOnFinished(e -> {
                if (field instanceof TextInputControl) {
                    ((TextInputControl) field).setPromptText(promptText);
                } else if (field instanceof ComboBox) {
                    ((ComboBox<?>) field).setPromptText(promptText);
                }
            });
        }
        translate.play();
        fade.play();
    }

    /**
     * Sets the parent controller reference.
     *
     * @param parentController The ManageDeliveryPersonsController instance.
     */
    public void setParentController(ManageDeliveryPersonsController parentController) {
        this.parentController = parentController;
    }

    // =================================================================================================================
    // Event Handlers
    // =================================================================================================================

    /**
     * Handles the save button click.
     * Validates inputs and creates the delivery person.
     */
    @FXML
    private void handleSave() {
        // Validate inputs
        String name = txtName.getText();
        String lastName = txtLastName.getText();
        String email = txtEmail.getText();
        String phone = txtPhone.getText();
        String password = txtPassword.isVisible() ? txtPassword.getText() : txtPasswordVisible.getText();
        String confirmPassword = txtConfirmPassword.isVisible() ? txtConfirmPassword.getText() : txtConfirmPasswordVisible.getText();
        String documentId = txtDocumentId.getText();
        CoverageArea coverageArea = cmbCoverageArea.getValue();

        if (name == null || name.trim().isEmpty()) {
            DialogUtil.showWarning("Advertencia", "Por favor ingresa el nombre.");
            return;
        }

        if (lastName == null || lastName.trim().isEmpty()) {
            DialogUtil.showWarning("Advertencia", "Por favor ingresa el apellido.");
            return;
        }

        if (email == null || email.trim().isEmpty()) {
            DialogUtil.showWarning("Advertencia", "Por favor ingresa el email.");
            return;
        }

        if (password == null || password.trim().isEmpty()) {
            DialogUtil.showWarning("Advertencia", "Por favor ingresa la contraseña.");
            return;
        }

        if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
            DialogUtil.showWarning("Advertencia", "Por favor confirma la contraseña.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            DialogUtil.showWarning("Advertencia", "Las contraseñas no coinciden.");
            return;
        }

        if (documentId == null || documentId.trim().isEmpty()) {
            DialogUtil.showWarning("Advertencia", "Por favor ingresa el documento de identidad.");
            return;
        }

        if (coverageArea == null) {
            DialogUtil.showWarning("Advertencia", "Por favor selecciona un área de cobertura.");
            return;
        }

        // Create PersonCreationData using Builder
        // Initial status is always AVAILABLE
        PersonCreationData data = new PersonCreationData.Builder()
                .withName(name)
                .withLastName(lastName)
                .withEmail(email)
                .withPhone(phone)
                .withPassword(password)
                .withDocumentId(documentId)
                .withCoverageArea(coverageArea)
                .withAvailability(AvailabilityStatus.AVAILABLE)
                .build();

        // Register delivery person
        boolean registered = deliveryPersonService.registerDeliveryPerson(data);

        if (registered) {
            DialogUtil.showSuccess("Éxito", "Repartidor agregado correctamente.");
            Logger.info("New delivery person registered: " + email);

            // Notify parent controller
            if (parentController != null) {
                parentController.onDeliveryPersonAdded();
            }

            // Close dialog
            closeDialog();
        } else {
            DialogUtil.showError("Error", "Ya existe un repartidor con ese email.");
        }
    }

    /**
     * Toggles password field visibility between PasswordField and TextField.
     */
    @FXML
    private void togglePasswordVisibility() {
        try {
            if (txtPassword.isVisible()) {
                // Switch to visible - show eye-closed icon
                txtPasswordVisible.setText(txtPassword.getText());
                txtPassword.setVisible(false);
                txtPassword.setManaged(false);
                txtPasswordVisible.setVisible(true);
                txtPasswordVisible.setManaged(true);

                Image eyeClosedImage = new Image(getClass().getResourceAsStream("/co/edu/uniquindio/poo/ProyectoFinal2025_2/Images/eye-closed.png"));
                ImageView eyeClosedView = new ImageView(eyeClosedImage);
                eyeClosedView.setFitWidth(20);
                eyeClosedView.setFitHeight(20);
                eyeClosedView.setPreserveRatio(true);
                btnTogglePassword.setGraphic(eyeClosedView);
            } else {
                // Switch to hidden - show eye-open icon
                txtPassword.setText(txtPasswordVisible.getText());
                txtPasswordVisible.setVisible(false);
                txtPasswordVisible.setManaged(false);
                txtPassword.setVisible(true);
                txtPassword.setManaged(true);

                Image eyeOpenImage = new Image(getClass().getResourceAsStream("/co/edu/uniquindio/poo/ProyectoFinal2025_2/Images/eye-open.png"));
                ImageView eyeOpenView = new ImageView(eyeOpenImage);
                eyeOpenView.setFitWidth(20);
                eyeOpenView.setFitHeight(20);
                eyeOpenView.setPreserveRatio(true);
                btnTogglePassword.setGraphic(eyeOpenView);
            }
        } catch (Exception e) {
            Logger.error("Error toggling password visibility: " + e.getMessage());
        }
    }

    @FXML
    private void toggleConfirmPasswordVisibility() {
        try {
            if (txtConfirmPassword.isVisible()) {
                // Switch to visible - show eye-closed icon
                txtConfirmPasswordVisible.setText(txtConfirmPassword.getText());
                txtConfirmPassword.setVisible(false);
                txtConfirmPassword.setManaged(false);
                txtConfirmPasswordVisible.setVisible(true);
                txtConfirmPasswordVisible.setManaged(true);

                Image eyeClosedImage = new Image(getClass().getResourceAsStream("/co/edu/uniquindio/poo/ProyectoFinal2025_2/Images/eye-closed.png"));
                ImageView eyeClosedView = new ImageView(eyeClosedImage);
                eyeClosedView.setFitWidth(20);
                eyeClosedView.setFitHeight(20);
                eyeClosedView.setPreserveRatio(true);
                btnToggleConfirmPassword.setGraphic(eyeClosedView);
            } else {
                // Switch to hidden - show eye-open icon
                txtConfirmPassword.setText(txtConfirmPasswordVisible.getText());
                txtConfirmPasswordVisible.setVisible(false);
                txtConfirmPasswordVisible.setManaged(false);
                txtConfirmPassword.setVisible(true);
                txtConfirmPassword.setManaged(true);

                Image eyeOpenImage = new Image(getClass().getResourceAsStream("/co/edu/uniquindio/poo/ProyectoFinal2025_2/Images/eye-open.png"));
                ImageView eyeOpenView = new ImageView(eyeOpenImage);
                eyeOpenView.setFitWidth(20);
                eyeOpenView.setFitHeight(20);
                eyeOpenView.setPreserveRatio(true);
                btnToggleConfirmPassword.setGraphic(eyeOpenView);
            }
        } catch (Exception e) {
            Logger.error("Error toggling confirm password visibility: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    /**
     * Closes the dialog window.
     */
    private void closeDialog() {
        Stage stage = (Stage) txtName.getScene().getWindow();
        stage.close();
    }
}
