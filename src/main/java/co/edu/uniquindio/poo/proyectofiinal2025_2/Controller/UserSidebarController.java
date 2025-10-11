package co.edu.uniquindio.poo.proyectofiinal2025_2.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the User Sidebar (UserSidebar.fxml).
 * <p>Inherits profile image and sidebar animation from BaseSidebarController.
 * This controller is responsible for setting the visibility of buttons
 * based on the user's authentication status.</p>
 */
public class UserSidebarController extends BaseSidebarController {

    @FXML
    private Button btnLogin;

    @FXML
    private Button btnLogout;

    @FXML
    private Button btnProfile;

    @FXML
    private Button btnSignup;

    @FXML
    private Button btnNewShipment;

    @FXML
    private Button btnOrders;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);

        // Ask the central authentication service (from BaseSidebarController) if a person is logged in
        boolean isLoggedIn = authService.isPersonLoggedIn();

        // Set visibility of buttons based on the authentication status
        setButtonVisibility(btnLogin, !isLoggedIn);
        setButtonVisibility(btnSignup, !isLoggedIn);

        setButtonVisibility(btnProfile, isLoggedIn);
        setButtonVisibility(btnNewShipment, isLoggedIn);
        setButtonVisibility(btnOrders, isLoggedIn);
        setButtonVisibility(btnLogout, isLoggedIn);

        // Add actions to navigate
        btnLogin.setOnAction(event -> {
            if (indexController != null) {
                indexController.loadView("Login.fxml");
            }
        });

        // CAMBIO AQUÍ: Ahora abre ventana modal en lugar de cargar la vista
        btnSignup.setOnAction(event -> {
            if (indexController != null) {
                indexController.openSignupWindow();
            }
        });
    }

    /**
     * Utility method to control both the visibility and layout management of a button.
     *
     * @param button  The button to modify.
     * @param visible true to make the button visible and managed, false to hide it.
     */
    private void setButtonVisibility(Button button, boolean visible) {
        button.setVisible(visible);
        button.setManaged(visible);
    }
}