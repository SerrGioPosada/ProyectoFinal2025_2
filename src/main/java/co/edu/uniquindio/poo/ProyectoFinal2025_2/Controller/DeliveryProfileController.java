package co.edu.uniquindio.poo.ProyectoFinal2025_2.Controller;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.DeliveryPerson;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Vehicle;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.AuthenticationService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.PasswordResetService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.DialogUtil;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.NavigationUtil;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.PasswordUtility;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.PasswordValidator;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for Delivery Person Profile view (DeliveryProfile.fxml).
 * <p>
 * This controller manages the profile view for delivery persons,
 * displaying their personal information, assigned vehicle, coverage area,
 * and availability status. It also provides options to update profile data.
 * </p>
 */
public class DeliveryProfileController implements Initializable {

    // =================================================================================================================
    // FXML Fields
    // =================================================================================================================

    @FXML private ImageView imgProfile;
    @FXML private Label lblName;
    @FXML private Label lblEmail;
    @FXML private Label lblPhone;
    @FXML private Label lblDocumentId;
    @FXML private Label lblAvailability;
    @FXML private Label lblCoverageArea;
    @FXML private Label lblVehiclePlate;
    @FXML private Label lblVehicleType;
    @FXML private Label lblVehicleCapacity;
    @FXML private Label lblTotalShipments;

    // Password change fields
    @FXML private PasswordField txtCurrentPassword;
    @FXML private PasswordField txtNewPassword;
    @FXML private PasswordField txtConfirmPassword;

    // =================================================================================================================
    // Services and State
    // =================================================================================================================

    private final AuthenticationService authService = AuthenticationService.getInstance();
    private final PasswordResetService passwordResetService = new PasswordResetService();
    private DeliveryPerson currentDeliveryPerson;
    private IndexController indexController;

    // =================================================================================================================
    // Initialization
    // =================================================================================================================

    /**
     * Initializes the controller. Loads the current delivery person's data.
     *
     * @param url            The location used to resolve relative paths.
     * @param resourceBundle The resources used to localize the root object.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        currentDeliveryPerson = (DeliveryPerson) authService.getCurrentPerson();

        if (currentDeliveryPerson == null) {
            Logger.error("No delivery person logged in");
            DialogUtil.showError("Error", "No hay repartidor autenticado");
            return;
        }

        loadDeliveryPersonData();
        Logger.info("DeliveryProfileController initialized for delivery person: " + currentDeliveryPerson.getId());
    }

    /**
     * Injects the IndexController reference for navigation purposes.
     *
     * @param indexController The main IndexController instance.
     */
    public void setIndexController(IndexController indexController) {
        this.indexController = indexController;
    }

    // =================================================================================================================
    // Data Loading Methods
    // =================================================================================================================

    /**
     * Loads all delivery person data into the UI components.
     */
    private void loadDeliveryPersonData() {
        lblName.setText(currentDeliveryPerson.getName() + " " + currentDeliveryPerson.getLastName());
        lblEmail.setText(currentDeliveryPerson.getEmail());
        lblPhone.setText(currentDeliveryPerson.getPhone() != null ? currentDeliveryPerson.getPhone() : "--");
        lblDocumentId.setText(currentDeliveryPerson.getDocumentId() != null ? currentDeliveryPerson.getDocumentId() : "--");

        // Availability status
        lblAvailability.setText(getAvailabilityLabel(currentDeliveryPerson.getAvailability()));

        // Coverage area
        lblCoverageArea.setText(currentDeliveryPerson.getCoverageArea() != null
            ? currentDeliveryPerson.getCoverageArea().toString()
            : "--");

        // Total shipments
        int totalShipments = currentDeliveryPerson.getAssignedShipments() != null
            ? currentDeliveryPerson.getAssignedShipments().size()
            : 0;
        lblTotalShipments.setText(String.valueOf(totalShipments));

        // Vehicle information
        loadVehicleInfo();

        // Profile image
        loadProfileImage();
    }

    /**
     * Loads the vehicle information for the delivery person.
     */
    private void loadVehicleInfo() {
        Vehicle vehicle = currentDeliveryPerson.getAssignedVehicle();
        if (vehicle != null) {
            lblVehiclePlate.setText(vehicle.getPlate());
            lblVehicleType.setText(vehicle.getType().toString());
            lblVehicleCapacity.setText(vehicle.getCapacity() + " kg");
        } else {
            lblVehiclePlate.setText("Sin asignar");
            lblVehicleType.setText("--");
            lblVehicleCapacity.setText("--");
        }
    }

    /**
     * Loads the profile image from the delivery person's profile image path.
     */
    private void loadProfileImage() {
        try {
            String imagePath = currentDeliveryPerson.getProfileImagePath();

            if (imagePath != null && !imagePath.isEmpty()) {
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString());
                    imgProfile.setImage(image);
                    return;
                }
            }

            // Load default image
            Image defaultImage = new Image(getClass().getResourceAsStream("/co/edu/uniquindio/poo/ProyectoFinal2025_2/Images/default-userImage.png"));
            imgProfile.setImage(defaultImage);

        } catch (Exception e) {
            Logger.error("Failed to load profile image: " + e.getMessage());
            imgProfile.setImage(null);
        }
    }

    // =================================================================================================================
    // Event Handlers
    // =================================================================================================================

    /**
     * Handles the change photo button action.
     * Opens a file chooser to select a new profile picture.
     */
    @FXML
    private void handleChangePhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Foto de Perfil");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Imagenes", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );

        File selectedFile = fileChooser.showOpenDialog(imgProfile.getScene().getWindow());

        if (selectedFile != null) {
            try {
                currentDeliveryPerson.setProfileImagePath(selectedFile.getAbsolutePath());
                loadProfileImage();

                // Refresh sidebar profile image
                if (indexController != null) {
                    indexController.refreshSidebarProfileImage();
                }

                DialogUtil.showSuccess("Éxito", "Foto de perfil actualizada correctamente");
                Logger.info("Profile photo updated for delivery person: " + currentDeliveryPerson.getId());

            } catch (Exception e) {
                Logger.error("Failed to update profile photo: " + e.getMessage());
                DialogUtil.showError("Error", "No se pudo actualizar la foto de perfil");
            }
        }
    }

    /**
     * Handles the edit personal data button action.
     * Navigates to the edit user data view.
     */
    @FXML
    private void handleEditPersonalData() {
        Logger.info("Navigating to Edit Personal Data view");
        if (indexController != null) {
            indexController.loadView("EditUserData.fxml");
        } else {
            NavigationUtil.navigate(indexController, "EditUserData.fxml", getClass());
        }
    }

    /**
     * Handles the update password button action.
     * Validates and updates the delivery person's password.
     */
    @FXML
    private void handleUpdatePassword() {
        String currentPassword = txtCurrentPassword.getText();
        String newPassword = txtNewPassword.getText();
        String confirmPassword = txtConfirmPassword.getText();

        // Validate inputs
        if (currentPassword == null || currentPassword.trim().isEmpty()) {
            DialogUtil.showWarning("Advertencia", "Por favor ingresa tu contraseña actual.");
            return;
        }

        if (newPassword == null || newPassword.trim().isEmpty()) {
            DialogUtil.showWarning("Advertencia", "Por favor ingresa tu nueva contraseña.");
            return;
        }

        if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
            DialogUtil.showWarning("Advertencia", "Por favor confirma tu nueva contraseña.");
            return;
        }

        // Check if new passwords match
        if (!newPassword.equals(confirmPassword)) {
            DialogUtil.showError("Error", "Las contraseñas no coinciden.");
            return;
        }

        // Validate new password strength
        List<String> passwordErrors = PasswordValidator.validatePassword(newPassword);
        if (!passwordErrors.isEmpty()) {
            DialogUtil.showError("Error",
                "La nueva contraseña no cumple con los requisitos:\n" +
                String.join("\n", passwordErrors));
            return;
        }

        // Verify current password
        if (!PasswordUtility.checkPassword(currentPassword, currentDeliveryPerson.getPassword())) {
            DialogUtil.showError("Error", "La contraseña actual es incorrecta.");
            return;
        }

        // Check if new password is different from current
        if (PasswordUtility.checkPassword(newPassword, currentDeliveryPerson.getPassword())) {
            DialogUtil.showWarning("Advertencia", "La nueva contraseña debe ser diferente a la actual.");
            return;
        }

        // Update password using PasswordUtility directly
        String hashedPassword = PasswordUtility.hashPassword(newPassword);
        currentDeliveryPerson.setPassword(hashedPassword);

        // Save the updated person - note: in a real system, this would persist to repository
        DialogUtil.showSuccess("Éxito", "Contraseña actualizada correctamente.");
        Logger.info("Password updated for delivery person: " + currentDeliveryPerson.getId());

        // Clear fields
        clearPasswordFields();
    }

    /**
     * Handles the cancel password change button action.
     * Clears all password fields.
     */
    @FXML
    private void handleCancelPasswordChange() {
        clearPasswordFields();
        DialogUtil.showInfo("Cancelado", "Cambio de contraseña cancelado.");
    }

    /**
     * Clears all password input fields.
     */
    private void clearPasswordFields() {
        if (txtCurrentPassword != null) txtCurrentPassword.clear();
        if (txtNewPassword != null) txtNewPassword.clear();
        if (txtConfirmPassword != null) txtConfirmPassword.clear();
    }

    // =================================================================================================================
    // Helper Methods
    // =================================================================================================================

    /**
     * Converts an AvailabilityStatus to a user-friendly Spanish label.
     *
     * @param status The availability status.
     * @return A formatted Spanish string.
     */
    private String getAvailabilityLabel(co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.AvailabilityStatus status) {
        if (status == null) return "Desconocido";
        return switch (status) {
            case AVAILABLE -> "Disponible";
            case IN_TRANSIT -> "En Tránsito";
            case INACTIVE -> "Inactivo";
            default -> "Desconocido";
        };
    }
}
