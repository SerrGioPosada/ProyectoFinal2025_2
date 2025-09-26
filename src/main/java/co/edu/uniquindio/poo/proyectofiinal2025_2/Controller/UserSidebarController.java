package co.edu.uniquindio.poo.proyectofiinal2025_2.Controller;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.User;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Repositories.UserRepository;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Services.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the User Sidebar (UserSidebar.fxml).
 * Inherits profile image and sidebar animation from BaseSidebarController.
 * Contains only user-specific button logic.
 */
public class UserSidebarController extends BaseSidebarController {

    @FXML
    private AnchorPane slider;

    @FXML
    private ImageView imgUserImage;

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

    // Service to manage users
    private final UserService userService = new UserService(UserRepository.getInstance());

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);

        // Get current user from service
        User currentUser = userService.getCurrentUser();

        if (currentUser == null) {
            // No user logged in → show Login and Signup buttons
            setButtonVisibility(btnLogin, true);
            setButtonVisibility(btnSignup, true);

            // Hide profile-related options
            setButtonVisibility(btnProfile, false);
            setButtonVisibility(btnNewShipment, false);
            setButtonVisibility(btnOrders, false);
            setButtonVisibility(btnLogout, false);

        } else {
            // User logged in → show profile-related buttons
            setButtonVisibility(btnLogin, false);
            setButtonVisibility(btnSignup, false);

            setButtonVisibility(btnProfile, true);
            setButtonVisibility(btnNewShipment, true);
            setButtonVisibility(btnOrders, true);
            setButtonVisibility(btnLogout, true);
        }
    }

    /**
     * Utility method to control both visibility and layout management of buttons.
     */
    private void setButtonVisibility(Button button, boolean visible) {
        button.setVisible(visible);
        button.setManaged(visible);
    }
}
