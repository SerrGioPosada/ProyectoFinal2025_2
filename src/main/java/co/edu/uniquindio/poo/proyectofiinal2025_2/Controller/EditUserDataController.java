package co.edu.uniquindio.poo.proyectofiinal2025_2.Controller;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.AuthenticablePerson;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Services.AuthenticationService;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Util.UtilController.DialogUtil;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Util.UtilModel.Logger;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

/**
 * Controller for the Edit User Data view (EditUserData.fxml).
 * <p>
 * Allows users to edit their personal information including profile photo.
 * </p>
 */
public class EditUserDataController implements Initializable {

    // =================================================================================================================
    // FXML Fields
    // =================================================================================================================

    @FXML private ImageView imgProfilePreview;
    @FXML private Label lblPhotoName;

    @FXML private TextField txtFirstName;
    @FXML private TextField txtLastName;
    @FXML private TextField txtEmail;
    @FXML private TextField txtPhone;
    @FXML private TextField txtId;
    @FXML private DatePicker dateBirthDate;
    @FXML private TextArea txtAdditionalInfo;

    @FXML private Label lblFirstNameError;
    @FXML private Label lblLastNameError;
    @FXML private Label lblPhoneError;

    // =================================================================================================================
    // Services and Data
    // =================================================================================================================

    private final AuthenticationService authService = AuthenticationService.getInstance();
    private AuthenticablePerson currentUser;
    private File selectedPhotoFile;
    private IndexController indexController;

    // =================================================================================================================
    // Initialization
    // =================================================================================================================

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        currentUser = authService.getAuthenticatedUser();

        if (currentUser == null) {
            Logger.error("No user logged in. Cannot edit data.");
            DialogUtil.showError("Error", "No hay usuario autenticado");
            return;
        }

        setupTextAreaAutoResize();
        loadUserData();
        Logger.info("EditUserDataController initialized for user: " + currentUser.getId());
    }

    /**
     * Configures the TextArea to auto-resize based on content up to a maximum height.
     */
    private void setupTextAreaAutoResize() {
        if (txtAdditionalInfo == null) return;

        // Set initial properties
        txtAdditionalInfo.setWrapText(true);

        // Add listener to adjust height based on content
        txtAdditionalInfo.textProperty().addListener((observable, oldValue, newValue) -> {
            // Calculate approximate height needed (rough estimate: 20px per line)
            String text = newValue == null ? "" : newValue;
            int lineCount = 1;

            // Count lines based on newline characters
            for (char c : text.toCharArray()) {
                if (c == '\n') lineCount++;
            }

            // Estimate wrapped lines (assuming ~60 chars per line at default width)
            int estimatedWrappedLines = (int) Math.ceil(text.length() / 60.0);
            lineCount = Math.max(lineCount, estimatedWrappedLines);

            // Calculate height (20px per line + some padding)
            double calculatedHeight = Math.max(60, Math.min(200, lineCount * 20 + 10));

            txtAdditionalInfo.setPrefHeight(calculatedHeight);
        });
    }

    // =================================================================================================================
    // Data Loading
    // =================================================================================================================

    /**
     * Loads current user data into form fields.
     */
    private void loadUserData() {
        txtFirstName.setText(currentUser.getName());
        txtLastName.setText(currentUser.getLastName());
        txtEmail.setText(currentUser.getEmail());
        txtPhone.setText(currentUser.getPhone() != null ? currentUser.getPhone() : "");
        txtId.setText(currentUser.getId());

        // Load birth date if available
        // dateBirthDate.setValue(currentUser.getBirthDate());

        // Load additional info if available
        // txtAdditionalInfo.setText(currentUser.getAdditionalInfo());

        loadProfilePhoto();
    }

    /**
     * Loads the current profile photo into the preview.
     */
    private void loadProfilePhoto() {
        try {
            String imagePath = currentUser.getProfileImagePath();

            if (imagePath != null && !imagePath.isEmpty()) {
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString());
                    imgProfilePreview.setImage(image);
                    lblPhotoName.setText(imageFile.getName());
                    return;
                }
            }

            // Load default image
            Image defaultImage = new Image(getClass().getResourceAsStream("/co/edu/uniquindio/poo/proyectofiinal2025_2/Images/default-profile.png"));
            imgProfilePreview.setImage(defaultImage);
            lblPhotoName.setText("Sin foto seleccionada");

        } catch (Exception e) {
            Logger.error("Failed to load profile photo: " + e.getMessage());
            imgProfilePreview.setImage(null);
            lblPhotoName.setText("Error al cargar foto");
        }
    }

    // =================================================================================================================
    // Event Handlers
    // =================================================================================================================

    /**
     * Handles the Select Photo button click.
     */
    @FXML
    private void handleSelectPhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Foto de Perfil");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Imagenes", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );

        selectedPhotoFile = fileChooser.showOpenDialog(imgProfilePreview.getScene().getWindow());

        if (selectedPhotoFile != null) {
            try {
                Image image = new Image(selectedPhotoFile.toURI().toString());
                imgProfilePreview.setImage(image);
                lblPhotoName.setText(selectedPhotoFile.getName());
                Logger.info("Photo selected: " + selectedPhotoFile.getAbsolutePath());
            } catch (Exception e) {
                Logger.error("Failed to load selected photo: " + e.getMessage());
                DialogUtil.showError("Error", "No se pudo cargar la foto seleccionada");
            }
        }
    }

    /**
     * Handles the Remove Photo button click.
     */
    @FXML
    private void handleRemovePhoto() {
        selectedPhotoFile = null;
        currentUser.setProfileImagePath(null);

        try {
            Image defaultImage = new Image(getClass().getResourceAsStream("/co/edu/uniquindio/poo/proyectofiinal2025_2/Images/default-profile.png"));
            imgProfilePreview.setImage(defaultImage);
            lblPhotoName.setText("Sin foto seleccionada");
        } catch (Exception e) {
            imgProfilePreview.setImage(null);
            lblPhotoName.setText("Sin foto");
        }
    }

    /**
     * Handles the Save button click.
     */
    @FXML
    private void handleSave() {
        clearErrors();

        if (!validateInputs()) {
            return;
        }

        try {
            // Update user data
            currentUser.setName(txtFirstName.getText().trim());
            currentUser.setLastName(txtLastName.getText().trim());
            currentUser.setPhone(txtPhone.getText().trim());

            // Update birth date if set
            LocalDate birthDate = dateBirthDate.getValue();
            // currentUser.setBirthDate(birthDate);

            // Update additional info
            String additionalInfo = txtAdditionalInfo.getText();
            // currentUser.setAdditionalInfo(additionalInfo);

            // Update photo if a new one was selected
            if (selectedPhotoFile != null) {
                currentUser.setProfileImagePath(selectedPhotoFile.getAbsolutePath());
            }

            // TODO: Save to repository using UserService
            // userService.updateUser(currentUser);

            DialogUtil.showSuccess("Exito", "Datos actualizados correctamente");
            Logger.info("User data updated for: " + currentUser.getId());

            navigateBack();

        } catch (Exception e) {
            Logger.error("Failed to save user data: " + e.getMessage());
            DialogUtil.showError("Error", "No se pudieron guardar los cambios: " + e.getMessage());
        }
    }

    /**
     * Handles the Cancel button click.
     */
    @FXML
    private void handleCancel() {
        Logger.info("Edit user data cancelled");
        navigateBack();
    }

    /**
     * Handles the Back button click.
     */
    @FXML
    private void handleBack() {
        Logger.info("Back button clicked from EditUserData");
        navigateBack();
    }

    /**
     * Sets the IndexController reference for navigation.
     */
    public void setIndexController(IndexController indexController) {
        this.indexController = indexController;
    }

    // =================================================================================================================
    // Validation
    // =================================================================================================================

    /**
     * Validates all input fields.
     */
    private boolean validateInputs() {
        boolean isValid = true;

        if (txtFirstName.getText().trim().isEmpty()) {
            showError(lblFirstNameError, "El nombre es requerido");
            isValid = false;
        }

        if (txtLastName.getText().trim().isEmpty()) {
            showError(lblLastNameError, "El apellido es requerido");
            isValid = false;
        }

        String phone = txtPhone.getText().trim();
        if (!phone.isEmpty() && !phone.matches("\\d{7,15}")) {
            showError(lblPhoneError, "Ingresa un tel�fono v�lido (7-15 d�gitos)");
            isValid = false;
        }

        return isValid;
    }

    // =================================================================================================================
    // Helper Methods
    // =================================================================================================================

    private void showError(Label label, String message) {
        label.setText(message);
        label.setVisible(true);
        label.setManaged(true);
    }

    private void clearErrors() {
        lblFirstNameError.setVisible(false);
        lblFirstNameError.setManaged(false);
        lblLastNameError.setVisible(false);
        lblLastNameError.setManaged(false);
        lblPhoneError.setVisible(false);
        lblPhoneError.setManaged(false);
    }

    private void closeWindow() {
        Stage stage = (Stage) txtFirstName.getScene().getWindow();
        stage.close();
    }

    /**
     * Navigates back to the profile view.
     */
    private void navigateBack() {
        if (indexController != null) {
            // Check if user is admin or regular user and load appropriate profile
            if (authService.isCurrentPersonAdmin()) {
                indexController.loadView("AdminProfile.fxml");
            } else {
                indexController.loadView("UserProfile.fxml");
            }
        } else {
            // Fallback: close window if opened as modal
            closeWindow();
        }
    }
}
