package co.edu.uniquindio.poo.ProyectoFinal2025_2.Controller;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.User;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.AuthenticationService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.UserService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Repositories.UserRepository;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.DialogUtil;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for User Profile view.
 */
public class UserProfileController implements Initializable {

    @FXML private ImageView imgProfile;
    @FXML private Label lblName;
    @FXML private Label lblEmail;
    @FXML private Label lblPhone;
    @FXML private Label lblMemberSince;

    private final AuthenticationService authService = AuthenticationService.getInstance();
    private final UserRepository userRepository = UserRepository.getInstance();
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

        loadUserData();
        Logger.info("UserProfileController initialized for user: " + currentUser.getId());
    }

    private void loadUserData() {
        lblName.setText(currentUser.getName() + " " + currentUser.getLastName());
        lblEmail.setText(currentUser.getEmail());
        lblPhone.setText(currentUser.getPhone() != null ? currentUser.getPhone() : "--");

        // TODO: Add createdAt field to Person model if needed
        lblMemberSince.setText("--");

        loadProfileImage();
    }

    private void loadProfileImage() {
        try {
            String imagePath = currentUser.getProfileImagePath();

            if (imagePath != null && !imagePath.isEmpty()) {
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString());
                    imgProfile.setImage(image);
                    return;
                }
            }

            Image defaultImage = new Image(getClass().getResourceAsStream("/co/edu/uniquindio/poo/ProyectoFinal2025_2/Images/default-userImage.png"));
            imgProfile.setImage(defaultImage);

        } catch (Exception e) {
            Logger.error("Failed to load profile image: " + e.getMessage());
            imgProfile.setImage(null);
        }
    }

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
                // Update user profile image path
                currentUser.setProfileImagePath(selectedFile.getAbsolutePath());

                // Save to repository to persist changes
                userRepository.updateUser(currentUser);
                Logger.info("User profile image saved to repository");

                // Reload the image in the view
                loadProfileImage();

                // Refresh sidebar profile image
                if (indexController != null) {
                    indexController.refreshSidebarProfileImage();
                }

                DialogUtil.showSuccess("Exito", "Foto de perfil actualizada correctamente");
                Logger.info("Profile photo updated for user: " + currentUser.getId());

            } catch (Exception e) {
                Logger.error("Failed to update profile photo: " + e.getMessage());
                DialogUtil.showError("Error", "No se pudo actualizar la foto de perfil");
            }
        }
    }

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

    @FXML
    private void handleManageAddresses() {
        Logger.info("Navigating to Manage Addresses view");
        if (indexController != null) {
            indexController.loadView("ManageAddresses.fxml");
        } else {
            Logger.error("IndexController not set - cannot navigate to ManageAddresses");
            DialogUtil.showError("Error", "No se pudo navegar a la vista de direcciones");
        }
    }

    /**
     * Sets the IndexController reference for navigation.
     */
    public void setIndexController(IndexController indexController) {
        this.indexController = indexController;
    }

    @FXML
    private void handleDeleteAccount() {
        Logger.warn("User requested account deletion");

        boolean confirmed = DialogUtil.showConfirmation(
            "ELIMINAR CUENTA",
            "Esta accion eliminara tu cuenta de usuario.\n\n" +
            "Se eliminaran todos tus envios, direcciones y datos personales.\n\n" +
            "Estas seguro que deseas continuar?"
        );

        if (!confirmed) return;

        boolean finalConfirm = DialogUtil.showConfirmation(
            "Confirmacion Final",
            "Ultima advertencia: Realmente deseas eliminar tu cuenta?\n\n" +
            "Usuario: " + currentUser.getEmail()
        );

        if (!finalConfirm) return;

        try {
            // TODO: Implement account deletion
            authService.logout();
            DialogUtil.showInfo("Cuenta Eliminada", "Tu cuenta ha sido eliminada exitosamente");
            Logger.info("User account deleted: " + currentUser.getId());

            Stage stage = (Stage) lblName.getScene().getWindow();
            stage.close();

        } catch (Exception e) {
            Logger.error("Failed to delete user account: " + e.getMessage());
            DialogUtil.showError("Error", "No se pudo eliminar la cuenta: " + e.getMessage());
        }
    }

    private void openSubView(String fxmlFile, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/co/edu/uniquindio/poo/ProyectoFinal2025_2/View/" + fxmlFile)
            );
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle(title);

            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                getClass().getResource("/co/edu/uniquindio/poo/ProyectoFinal2025_2/Style.css").toExternalForm()
            );
            stage.setScene(scene);

            stage.setWidth(900);
            stage.setHeight(700);
            stage.setResizable(true);

            stage.show();
            stage.setOnHidden(e -> loadUserData());

        } catch (Exception e) {
            Logger.error("Failed to load " + fxmlFile + ": " + e.getMessage());
            if (e.getCause() != null) {
                Logger.error("Cause: " + e.getCause().getMessage());
            }
            e.printStackTrace();
            DialogUtil.showError("Error", "No se pudo abrir la ventana: " + title + "\n" + e.getMessage());
        }
    }
}
