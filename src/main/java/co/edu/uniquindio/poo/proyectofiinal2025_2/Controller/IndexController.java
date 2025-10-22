package co.edu.uniquindio.poo.proyectofiinal2025_2.Controller;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Services.AuthenticationService;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Util.UtilModel.Logger;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * The main controller for the application's primary stage (Index.fxml).
 * <p>
 * Orchestrates the main layout (top bar, sidebar, content area) and handles:
 * <ul>
 *     <li>Loading the correct sidebar based on user role.</li>
 *     <li>Loading views dynamically into the main content area.</li>
 *     <li>Top-level UI events (menu toggle, exit).</li>
 *     <li>Login success handling.</li>
 * </ul>
 * </p>
 */

public class IndexController implements Initializable {

    // =================================================================================================================
    // FXML Fields
    // =================================================================================================================

    private static final double SIDEBAR_WIDTH = 176.0;
    private final AuthenticationService authService = AuthenticationService.getInstance();
    @FXML
    private ImageView exit;
    @FXML
    private Label lblMenu;
    @FXML
    private Label lblMenuBack;
    @FXML
    private BorderPane rootPane;
    @FXML
    private StackPane paneIndex;
    // =================================================================================================================
    // Dependencies and State
    // =================================================================================================================
    private AnchorPane sidebar;

    // =================================================================================================================
    // Initialization
    // =================================================================================================================
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadSidebar();
        setupTopBarEventHandlers();
    }

    // =================================================================================================================
    // Public API for Child Controllers
    // =================================================================================================================

    /**
     * Loads a view into the central content pane.
     *
     * @param fxmlName The FXML filename to load (e.g., "Login.fxml").
     */
    public void loadView(String fxmlName) {
        Logger.info("Loading view: " + fxmlName);
        try {
            URL fxmlUrl = getClass().getResource("/co/edu/uniquindio/poo/proyectofiinal2025_2/View/" + fxmlName);
            if (fxmlUrl == null) {
                Logger.error("Cannot find FXML resource: " + fxmlName + ". Showing placeholder.");
                showPlaceholder(fxmlName.replace(".fxml", ""));
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Node view = loader.load();

            // Inject IndexController to child if needed
            Object controller = loader.getController();
            injectIndexController(controller);

            paneIndex.getChildren().setAll(view);

        } catch (IOException e) {
            Logger.error("Failed to load view FXML: " + fxmlName, e);
        }
    }

    /**
     * Loads a view with a user filter applied (for ShipmentManagement).
     *
     * @param fxmlName  The FXML filename to load.
     * @param userEmail The user email to filter by.
     */
    public void loadViewWithUserFilter(String fxmlName, String userEmail) {
        Logger.info("Loading view: " + fxmlName + " with user filter: " + userEmail);
        try {
            URL fxmlUrl = getClass().getResource("/co/edu/uniquindio/poo/proyectofiinal2025_2/View/" + fxmlName);
            if (fxmlUrl == null) {
                Logger.error("Cannot find FXML resource: " + fxmlName + ". Showing placeholder.");
                showPlaceholder(fxmlName.replace(".fxml", ""));
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Node view = loader.load();

            // Inject IndexController to child if needed
            Object controller = loader.getController();
            injectIndexController(controller);

            // If it's ShipmentManagementController, apply the user filter
            if (controller instanceof ShipmentManagementController shipmentController) {
                shipmentController.applyUserFilter(userEmail);
            }

            paneIndex.getChildren().setAll(view);

        } catch (IOException e) {
            Logger.error("Failed to load view FXML: " + fxmlName, e);
        }
    }

    /**
     * Called after successful login.
     * Reloads the sidebar and opens the appropriate dashboard.
     */
    public void onLoginSuccess() {
        loadSidebar();

        // Reinitialize menu button states after loading sidebar
        if (lblMenuBack != null && lblMenu != null) {
            lblMenu.setVisible(true);
            lblMenu.setMouseTransparent(false);
            lblMenuBack.setVisible(false);
            lblMenuBack.setMouseTransparent(true);
        }

        if (authService.isCurrentPersonAdmin()) {
            Stage stage = (Stage) paneIndex.getScene().getWindow();
            stage.setMaximized(true);
            loadView("AdminDashboard.fxml");
            return;
        }

        loadView("UserDashboard.fxml");
    }

    /**
     * Opens the Signup window in a modal dialog.
     */
    public void openSignupWindow() {
        try {
            URL fxmlUrl = getClass().getResource("/co/edu/uniquindio/poo/proyectofiinal2025_2/View/Signup.fxml");
            if (fxmlUrl == null) {
                Logger.error("Cannot find FXML file: Signup.fxml");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            SignupController signupController = loader.getController();
            signupController.setIndexController(this);

            Stage stage = new Stage();
            stage.setTitle("Create New Account");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);

            closeSidebar();
            lblMenu.setVisible(true);
            lblMenu.setMouseTransparent(false);
            lblMenuBack.setVisible(false);
            lblMenuBack.setMouseTransparent(true);

            stage.showAndWait();

        } catch (IOException e) {
            Logger.error("Failed to load Signup window.", e);
        }
    }

    /**
     * Opens the sidebar with slide-in animation.
     */
    public void openSidebar() {
        toggleSidebar(true);
    }

    /**
     * Closes the sidebar with slide-out animation.
     */
    public void closeSidebar() {
        toggleSidebar(false);
    }

    // =================================================================================================================
    // Private Helper Methods
    // =================================================================================================================

    /**
     * Toggles the sidebar animation based on open/close state.
     *
     * @param open true to open, false to close
     */
    private void toggleSidebar(boolean open) {
        if (sidebar == null) return;
        TranslateTransition slide = new TranslateTransition(Duration.seconds(0.35), sidebar);
        slide.setToX(open ? 0 : -SIDEBAR_WIDTH);
        slide.play();
    }

    /**
     * Sets up the top bar button event handlers.
     */
    private void setupTopBarEventHandlers() {
        exit.setOnMouseClicked(event -> {
            Logger.info("Exit clicked, closing app.");
            System.exit(0);
        });

        // Initialize menu buttons - lblMenuBack should be invisible and non-interactive
        lblMenuBack.setVisible(false);
        lblMenuBack.setMouseTransparent(true);
        lblMenu.setMouseTransparent(false);

        lblMenu.setOnMouseClicked(event -> {
            lblMenu.setVisible(false);
            lblMenu.setMouseTransparent(true);
            lblMenuBack.setVisible(true);
            lblMenuBack.setMouseTransparent(false);
            openSidebar();
        });

        lblMenuBack.setOnMouseClicked(event -> {
            lblMenu.setVisible(true);
            lblMenu.setMouseTransparent(false);
            lblMenuBack.setVisible(false);
            lblMenuBack.setMouseTransparent(true);
            closeSidebar();
        });
    }

    /**
     * Loads the appropriate sidebar based on user role.
     */
    private void loadSidebar() {
        String fxmlFile = authService.isCurrentPersonAdmin() ? "AdminSidebar.fxml" : "UserSidebar.fxml";
        Logger.info("Loading sidebar: " + fxmlFile);
        try {
            URL fxmlUrl = getClass().getResource("/co/edu/uniquindio/poo/proyectofiinal2025_2/View/" + fxmlFile);
            if (fxmlUrl == null) {
                Logger.error("Cannot find sidebar FXML: " + fxmlFile);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            sidebar = loader.load();

            BaseSidebarController sidebarController = loader.getController();
            if (sidebarController != null) sidebarController.setIndexController(this);

            sidebar.setTranslateX(-SIDEBAR_WIDTH);
            rootPane.setLeft(sidebar);

        } catch (IOException e) {
            Logger.error("Failed to load sidebar FXML.", e);
        }
    }

    /**
     * Injects this IndexController into child controllers if they support it.
     *
     * @param controller the child controller
     */
    private void injectIndexController(Object controller) {
        if (controller instanceof LoginController login) login.setIndexController(this);
        else if (controller instanceof SignupController signup) signup.setIndexController(this);
        else if (controller instanceof ForgotPasswordController forgotPassword) forgotPassword.setIndexController(this);
        else if (controller instanceof ManageUsersController manage) manage.setIndexController(this);
        else if (controller instanceof AdminProfileController adminProfile) adminProfile.setIndexController(this);
        else if (controller instanceof UserProfileController userProfile) userProfile.setIndexController(this);
        else if (controller instanceof EditUserDataController editUserData) editUserData.setIndexController(this);
        else if (controller instanceof ChangePasswordController changePassword) changePassword.setIndexController(this);
        else if (controller instanceof ManageAddressesController manageAddresses) manageAddresses.setIndexController(this);
    }

    /**
     * Shows a placeholder for unimplemented views.
     *
     * @param viewName Name of the view
     */
    private void showPlaceholder(String viewName) {
        Label placeholder = new Label("View: " + viewName + "\n\n(Not yet implemented)");
        placeholder.setStyle("-fx-font-size: 24px; -fx-text-fill: #6c757d; -fx-alignment: center;");
        StackPane container = new StackPane(placeholder);
        container.setStyle("-fx-background-color: #f8f9fa;");
        paneIndex.getChildren().setAll(container);
    }

    /**
     * Replaces the center content of the main BorderPane with the given view.
     *
     * @param content The new content to display in the center of the main layout.
     */
    public void setCenterContent(Parent content) {
        if (paneIndex == null || content == null) return;
        paneIndex.getChildren().setAll(content);
    }

    /**
     * Reloads the entire application to its initial state.
     * This method should be called after logout to avoid stacking instances.
     */
    public void reloadApplication() {
        try {
            Logger.info("Reloading application to initial state after logout.");

            // Get the current stage
            Stage stage = (Stage) paneIndex.getScene().getWindow();

            // Load Index.fxml fresh
            URL fxmlUrl = getClass().getResource("/co/edu/uniquindio/poo/proyectofiinal2025_2/View/Index.fxml");
            if (fxmlUrl == null) {
                Logger.error("Cannot find Index.fxml for reload");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            // Create new scene and set it to the stage
            Scene scene = new Scene(root);
            stage.setScene(scene);

            // Reset window state if needed
            stage.setMaximized(false);

            Logger.info("Application reloaded successfully.");

        } catch (IOException e) {
            Logger.error("Failed to reload application", e);
        }
    }
}
