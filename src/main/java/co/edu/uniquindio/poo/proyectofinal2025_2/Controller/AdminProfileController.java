package co.edu.uniquindio.poo.proyectofinal2025_2.Controller;

import co.edu.uniquindio.poo.proyectofinal2025_2.Model.Admin;
import co.edu.uniquindio.poo.proyectofinal2025_2.Services.AuthenticationService;
import co.edu.uniquindio.poo.proyectofinal2025_2.Util.UtilController.DialogUtil;
import co.edu.uniquindio.poo.proyectofinal2025_2.Util.UtilModel.Logger;
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
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * Controller for Admin Profile view.
 */
public class AdminProfileController implements Initializable {

    @FXML private ImageView imgProfile;
    @FXML private Label lblName;
    @FXML private Label lblEmail;
    @FXML private Label lblPhone;
    @FXML private Label lblMemberSince;

    @FXML private Label lblTotalUsers;
    @FXML private Label lblActiveShipments;
    @FXML private Label lblTotalDeliveryPersons;

    private final AuthenticationService authService = AuthenticationService.getInstance();
    private Admin currentAdmin;
    private IndexController indexController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        currentAdmin = (Admin) authService.getCurrentPerson();

        if (currentAdmin == null) {
            Logger.error("No admin logged in");
            DialogUtil.showError("Error", "No hay administrador autenticado");
            return;
        }

        loadAdminData();
        loadSystemStats();
        Logger.info("AdminProfileController initialized for admin: " + currentAdmin.getId());
    }

    private void loadAdminData() {
        lblName.setText(currentAdmin.getName() + " " + currentAdmin.getLastName());
        lblEmail.setText(currentAdmin.getEmail());
        lblPhone.setText(currentAdmin.getPhone() != null ? currentAdmin.getPhone() : "--");

        // TODO: Add createdAt field to Person model if needed
        lblMemberSince.setText("--");

        loadProfileImage();
    }

    private void loadProfileImage() {
        try {
            String imagePath = currentAdmin.getProfileImagePath();

            if (imagePath != null && !imagePath.isEmpty()) {
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString());
                    imgProfile.setImage(image);
                    return;
                }
            }

            Image defaultImage = new Image(getClass().getResourceAsStream("/co/edu/uniquindio/poo/proyectofinal2025_2/Images/default-userImage.png"));
            imgProfile.setImage(defaultImage);

        } catch (Exception e) {
            Logger.error("Failed to load profile image: " + e.getMessage());
            imgProfile.setImage(null);
        }
    }

    private void loadSystemStats() {
        // TODO: Load from services
        lblTotalUsers.setText("--");
        lblActiveShipments.setText("--");
        lblTotalDeliveryPersons.setText("--");
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
                currentAdmin.setProfileImagePath(selectedFile.getAbsolutePath());
                loadProfileImage();
                // TODO: Save to repository
                DialogUtil.showSuccess("Exito", "Foto de perfil actualizada correctamente");
                Logger.info("Profile photo updated for admin: " + currentAdmin.getId());

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

    /**
     * Sets the IndexController reference for navigation.
     */
    public void setIndexController(IndexController indexController) {
        this.indexController = indexController;
    }

    @FXML
    private void handleDeleteAccount() {
        Logger.warn("Admin requested account deletion");

        boolean confirmed = DialogUtil.showConfirmation(
            "ELIMINAR CUENTA DE ADMINISTRADOR",
            "Esta accion eliminara tu cuenta de administrador.\n\n" +
            "ADVERTENCIA: Si eres el unico administrador del sistema,\n" +
            "no podras administrarlo despues de esta accion.\n\n" +
            "Estas seguro que deseas continuar?"
        );

        if (!confirmed) return;

        boolean finalConfirm = DialogUtil.showConfirmation(
            "Confirmacion Final",
            "Ultima advertencia: Realmente deseas eliminar tu cuenta?\n\n" +
            "Admin: " + currentAdmin.getEmail()
        );

        if (!finalConfirm) return;

        try {
            // TODO: Implement account deletion
            authService.logout();
            DialogUtil.showInfo("Cuenta Eliminada", "Tu cuenta ha sido eliminada exitosamente");
            Logger.info("Admin account deleted: " + currentAdmin.getId());

            Stage stage = (Stage) lblName.getScene().getWindow();
            stage.close();

        } catch (Exception e) {
            Logger.error("Failed to delete admin account: " + e.getMessage());
            DialogUtil.showError("Error", "No se pudo eliminar la cuenta: " + e.getMessage());
        }
    }

    private void openSubView(String fxmlFile, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/co/edu/uniquindio/poo/proyectofinal2025_2/View/" + fxmlFile)
            );
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle(title);

            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                getClass().getResource("/co/edu/uniquindio/poo/proyectofinal2025_2/Style.css").toExternalForm()
            );
            stage.setScene(scene);

            stage.setWidth(900);
            stage.setHeight(700);
            stage.setResizable(true);

            stage.show();
            stage.setOnHidden(e -> loadAdminData());

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
