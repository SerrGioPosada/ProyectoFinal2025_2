package co.edu.uniquindio.poo.ProyectoFinal2025_2.Controller;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.DeliveryPerson;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Vehicle;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.AuthenticationService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Repositories.DeliveryPersonRepository;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.DialogUtil;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
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

    // =================================================================================================================
    // Services and State
    // =================================================================================================================

    private final AuthenticationService authService = AuthenticationService.getInstance();
    private final DeliveryPersonRepository deliveryPersonRepository = DeliveryPersonRepository.getInstance();
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

        // Availability status with colored styling
        lblAvailability.setText(getAvailabilityLabel(currentDeliveryPerson.getAvailability()));
        applyAvailabilityStyle(currentDeliveryPerson.getAvailability());

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
                // Update delivery person profile image path
                currentDeliveryPerson.setProfileImagePath(selectedFile.getAbsolutePath());

                // Save to repository to persist changes
                deliveryPersonRepository.updateDeliveryPerson(currentDeliveryPerson);
                Logger.info("Delivery person profile image saved to repository");

                // Reload the image in the view
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
            Logger.error("IndexController not set - cannot navigate to EditUserData");
            DialogUtil.showError("Error", "No se pudo navegar a la vista de edición de datos");
        }
    }

    /**
     * Handles the change password button action.
     * Navigates to the change password view.
     */
    @FXML
    private void handleChangePassword() {
        Logger.info("Navigating to Change Password view");
        if (indexController != null) {
            indexController.loadView("ChangePassword.fxml");
        } else {
            Logger.error("IndexController not set - cannot navigate to ChangePassword");
            DialogUtil.showError("Error", "No se pudo navegar a la vista de cambio de contraseña");
        }
    }

    /**
     * Handles the delete account button action.
     * Shows a confirmation dialog and deletes the account if confirmed.
     */
    @FXML
    private void handleDeleteAccount() {
        Logger.info("Delete account requested for delivery person: " + currentDeliveryPerson.getId());

        boolean confirmed = DialogUtil.showConfirmation(
            "Eliminar Cuenta",
            "¿Estás seguro de que deseas eliminar tu cuenta?",
            "Esta acción es irreversible y se perderán todos tus datos permanentemente."
        );

        if (confirmed) {
            try {
                // Delete the delivery person from repository
                deliveryPersonRepository.removeDeliveryPerson(currentDeliveryPerson.getId());
                Logger.info("Delivery person account deleted successfully: " + currentDeliveryPerson.getId());

                // Logout the user
                authService.logout();
                Logger.info("User logged out after account deletion");

                // Show success message
                DialogUtil.showSuccess("Cuenta Eliminada", "Tu cuenta ha sido eliminada exitosamente.");

                // Navigate to index/login
                if (indexController != null) {
                    indexController.loadView("Login.fxml");
                } else {
                    Logger.error("IndexController not set - cannot navigate to Login");
                }

            } catch (Exception e) {
                Logger.error("Failed to delete delivery person account: " + e.getMessage());
                DialogUtil.showError("Error", "No se pudo eliminar la cuenta. Por favor, intenta de nuevo.");
            }
        } else {
            Logger.info("Account deletion cancelled by user");
        }
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

    /**
     * Applies color styling to the availability status label based on the status.
     *
     * @param status The availability status.
     */
    private void applyAvailabilityStyle(co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.AvailabilityStatus status) {
        if (status == null) {
            lblAvailability.setStyle("-fx-text-fill: #6c757d; -fx-font-weight: bold;");
            return;
        }

        String color = switch (status) {
            case AVAILABLE -> "#28a745"; // Green for available
            case IN_TRANSIT -> "#ffc107"; // Yellow/Orange for in transit
            case INACTIVE -> "#dc3545"; // Red for inactive
            default -> "#6c757d"; // Gray for unknown
        };

        lblAvailability.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
    }
}
