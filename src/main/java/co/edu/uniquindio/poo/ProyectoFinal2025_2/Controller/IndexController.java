package co.edu.uniquindio.poo.ProyectoFinal2025_2.Controller;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.AuthenticablePerson;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Person;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.dto.NotificationDTO;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.AuthenticationService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.NotificationService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.ThemeManager;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;
import javafx.animation.TranslateTransition;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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
    private final NotificationService notificationService = NotificationService.getInstance();
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
    @FXML
    private StackPane notificationBellContainer;
    @FXML
    private Label lblNotificationCount;
    @FXML
    private Button btnNotificationBell;
    // =================================================================================================================
    // Dependencies and State
    // =================================================================================================================
    private AnchorPane sidebar;
    private BaseSidebarController sidebarController;
    private String previousViewName = null; // Track the previous view for back navigation
    private Object currentController = null; // Track the currently loaded controller

    // =================================================================================================================
    // Initialization
    // =================================================================================================================
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadSidebar();
        setupTopBarEventHandlers();
        setupNotificationBell();
        updateNotificationBellVisibility();
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
            URL fxmlUrl = getClass().getResource("/co/edu/uniquindio/poo/ProyectoFinal2025_2/View/" + fxmlName);
            if (fxmlUrl == null) {
                Logger.error("Cannot find FXML resource: " + fxmlName + ". Showing placeholder.");
                showPlaceholder(fxmlName.replace(".fxml", ""));
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Node view = loader.load();

            // Inject IndexController to child if needed
            Object controller = loader.getController();
            this.currentController = controller; // Save current controller reference
            injectIndexController(controller);

            // Update active button in sidebar
            updateSidebarActiveButton(fxmlName);

            // Track previous view (but not if loading NotificationsCenter)
            if (!fxmlName.equals("NotificationsCenter.fxml") && previousViewName != null && !previousViewName.equals(fxmlName)) {
                previousViewName = fxmlName;
            } else if (!fxmlName.equals("NotificationsCenter.fxml")) {
                previousViewName = fxmlName;
            }

            // Apply fade transition
            loadViewWithTransition(view);

        } catch (IOException e) {
            Logger.error("Failed to load view FXML: " + fxmlName, e);
        }
    }

    /**
     * Loads a view with a smooth fade transition.
     */
    private void loadViewWithTransition(Node newView) {
        if (paneIndex.getChildren().isEmpty()) {
            // No transition for first load
            paneIndex.getChildren().setAll(newView);
            return;
        }

        // Fade out old view
        Node oldView = paneIndex.getChildren().get(0);
        javafx.animation.FadeTransition fadeOut = new javafx.animation.FadeTransition(javafx.util.Duration.millis(150), oldView);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        fadeOut.setOnFinished(event -> {
            // Replace with new view
            paneIndex.getChildren().setAll(newView);
            newView.setOpacity(0.0);

            // Fade in new view
            javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(javafx.util.Duration.millis(200), newView);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });

        fadeOut.play();
    }

    /**
     * Loads a view with a user filter applied (for ShipmentManagement).
     *
     * @param fxmlName  The FXML filename to load.
     * @param userEmail The user email to filter by.
     */
    public void loadViewWithUserFilter(String fxmlName, String userEmail) {
        loadViewWithUserFilter(fxmlName, userEmail, null);
    }

    /**
     * Loads a view with a user filter applied and source view information.
     *
     * @param fxmlName   The FXML filename to load.
     * @param userEmail  The user email to filter by.
     * @param sourceView The name of the view that initiated this navigation (e.g., "ManageUsers.fxml").
     */
    public void loadViewWithUserFilter(String fxmlName, String userEmail, String sourceView) {
        Logger.info("Loading view: " + fxmlName + " with user filter: " + userEmail + " from source: " + sourceView);
        try {
            URL fxmlUrl = getClass().getResource("/co/edu/uniquindio/poo/ProyectoFinal2025_2/View/" + fxmlName);
            if (fxmlUrl == null) {
                Logger.error("Cannot find FXML resource: " + fxmlName + ". Showing placeholder.");
                showPlaceholder(fxmlName.replace(".fxml", ""));
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Node view = loader.load();

            // Inject IndexController to child if needed
            Object controller = loader.getController();
            this.currentController = controller; // Save current controller reference
            injectIndexController(controller);

            // If it's ShipmentManagementController, apply the user filter
            if (controller instanceof ShipmentManagementController shipmentController) {
                shipmentController.applyUserFilter(userEmail, sourceView);
            }

            // If it's AdminOrderManagementController, apply the user filter
            if (controller instanceof AdminOrderManagementController orderController) {
                orderController.applyUserFilter(userEmail, sourceView);
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

        // Refresh notification bell visibility and badge
        refreshNotificationBell();

        // Load the appropriate dashboard based on user role
        if (authService.isCurrentPersonAdmin()) {
            Stage stage = (Stage) paneIndex.getScene().getWindow();
            stage.setMaximized(true);
            loadView("AdminDashboard.fxml");
            return;
        }

        if (authService.getCurrentPerson() instanceof co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.DeliveryPerson) {
            Stage stage = (Stage) paneIndex.getScene().getWindow();
            stage.setMaximized(true);
            loadView("DeliveryDashboard.fxml");
            return;
        }

        loadView("UserDashboard.fxml");
    }

    /**
     * Opens the Signup window in a modal dialog.
     */
    public void openSignupWindow() {
        try {
            URL fxmlUrl = getClass().getResource("/co/edu/uniquindio/poo/ProyectoFinal2025_2/View/Signup.fxml");
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
            Scene scene = new Scene(root);

            // Apply current theme to modal window
            ThemeManager.getInstance().applyThemeToScene(scene);

            stage.setScene(scene);
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

    /**
     * Refreshes the profile image displayed in the sidebar.
     * Should be called after the user updates their profile picture.
     */
    public void refreshSidebarProfileImage() {
        if (sidebarController != null) {
            sidebarController.refreshProfileImage();
            Logger.info("Sidebar profile image refreshed.");
        } else {
            Logger.warn("Cannot refresh sidebar image: sidebarController is null.");
        }
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
        String fxmlFile = determineSidebarFxml();
        Logger.info("Loading sidebar: " + fxmlFile);
        try {
            URL fxmlUrl = getClass().getResource("/co/edu/uniquindio/poo/ProyectoFinal2025_2/View/" + fxmlFile);
            if (fxmlUrl == null) {
                Logger.error("Cannot find sidebar FXML: " + fxmlFile);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            sidebar = loader.load();

            sidebarController = loader.getController();
            if (sidebarController != null) sidebarController.setIndexController(this);

            sidebar.setTranslateX(-SIDEBAR_WIDTH);
            rootPane.setLeft(sidebar);

        } catch (IOException e) {
            Logger.error("Failed to load sidebar FXML.", e);
        }
    }

    /**
     * Determines which sidebar FXML to load based on the current user's role.
     *
     * @return The FXML filename for the appropriate sidebar.
     */
    private String determineSidebarFxml() {
        if (authService.isCurrentPersonAdmin()) {
            return "AdminSidebar.fxml";
        } else if (authService.getCurrentPerson() instanceof co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.DeliveryPerson) {
            return "DeliverySidebar.fxml";
        } else {
            return "UserSidebar.fxml";
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
        else if (controller instanceof UserDashboardController userDashboard) userDashboard.setIndexController(this);
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
            URL fxmlUrl = getClass().getResource("/co/edu/uniquindio/poo/ProyectoFinal2025_2/View/Index.fxml");
            if (fxmlUrl == null) {
                Logger.error("Cannot find Index.fxml for reload");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            // Create new scene and set it to the stage
            Scene scene = new Scene(root);

            // Apply the current theme to the new scene
            ThemeManager.getInstance().applyThemeToScene(scene);

            stage.setScene(scene);

            // Reset window state if needed
            stage.setMaximized(false);

            Logger.info("Application reloaded successfully.");

        } catch (IOException e) {
            Logger.error("Failed to reload application", e);
        }
    }

    /**
     * Updates the active button in the sidebar based on the currently loaded view.
     *
     * @param fxmlName The name of the FXML file being loaded
     */
    private void updateSidebarActiveButton(String fxmlName) {
        if (sidebarController == null) {
            return;
        }

        // Call the appropriate setActiveView method based on sidebar type
        if (sidebarController instanceof AdminSidebarController adminSidebar) {
            adminSidebar.setActiveView(fxmlName);
        } else if (sidebarController instanceof UserSidebarController userSidebar) {
            userSidebar.setActiveView(fxmlName);
        } else if (sidebarController instanceof DeliverySidebarController deliverySidebar) {
            deliverySidebar.setActiveView(fxmlName);
        }
    }

    // =================================================================================================================
    // Notification Bell Methods
    // =================================================================================================================

    /**
     * Sets up the notification bell button and its event handlers.
     */
    private void setupNotificationBell() {
        if (btnNotificationBell == null) {
            Logger.warn("Notification bell button is null, cannot setup handlers.");
            return;
        }

        btnNotificationBell.setOnAction(event -> handleNotificationBellClick());
        setupNotificationBadgeListener();
    }

    /**
     * Handles the notification bell button click.
     * Loads the NotificationsCenter view in the center pane.
     */
    private void handleNotificationBellClick() {
        Logger.info("Notification bell clicked, loading notifications view.");
        loadNotificationsCenterView();
    }

    /**
     * Loads the NotificationsCenter view with back button support.
     */
    private void loadNotificationsCenterView() {
        try {
            URL fxmlUrl = getClass().getResource("/co/edu/uniquindio/poo/ProyectoFinal2025_2/View/NotificationsCenter.fxml");
            if (fxmlUrl == null) {
                Logger.error("Cannot find NotificationsCenter.fxml");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Node view = loader.load();

            // Inject IndexController to the notifications controller
            Object controller = loader.getController();
            if (controller instanceof NotificationsCenterController notifController) {
                notifController.setIndexController(this);
            }

            // Load the view in the center pane
            loadViewWithTransition(view);

        } catch (IOException e) {
            Logger.error("Failed to load NotificationsCenter view", e);
        }
    }

    /**
     * Returns to the previous view.
     * Called by NotificationsCenterController when back button is clicked.
     */
    public void returnToPreviousView() {
        if (previousViewName != null && !previousViewName.isEmpty()) {
            Logger.info("Returning to previous view: " + previousViewName);
            loadView(previousViewName);
        } else {
            Logger.info("No previous view, loading default dashboard.");
            loadDefaultDashboard();
        }
    }

    /**
     * Loads the default dashboard based on user role.
     */
    private void loadDefaultDashboard() {
        if (authService.isCurrentPersonAdmin()) {
            loadView("AdminDashboard.fxml");
        } else if (authService.getCurrentPerson() instanceof co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.DeliveryPerson) {
            loadView("DeliveryDashboard.fxml");
        } else {
            loadView("UserDashboard.fxml");
        }
    }

    /**
     * Updates the visibility of the notification bell based on login status.
     */
    private void updateNotificationBellVisibility() {
        if (notificationBellContainer == null) return;

        boolean isLoggedIn = authService.isPersonLoggedIn();
        notificationBellContainer.setVisible(isLoggedIn);
        notificationBellContainer.setManaged(isLoggedIn);

        if (isLoggedIn) {
            updateNotificationBadge();
        }
    }

    /**
     * Updates the notification count badge.
     */
    private void updateNotificationBadge() {
        if (lblNotificationCount == null) return;

        Person person = authService.getCurrentPerson();
        if (!(person instanceof AuthenticablePerson currentPerson)) {
            lblNotificationCount.setVisible(false);
            lblNotificationCount.setManaged(false);
            return;
        }

        int unreadCount = notificationService.getUnreadCount(currentPerson.getId());

        if (unreadCount > 0) {
            lblNotificationCount.setText(unreadCount > 99 ? "99+" : String.valueOf(unreadCount));
            lblNotificationCount.setVisible(true);
            lblNotificationCount.setManaged(true);
        } else {
            lblNotificationCount.setVisible(false);
            lblNotificationCount.setManaged(false);
        }
    }

    /**
     * Sets up a listener to update the notification badge when notifications change.
     */
    private void setupNotificationBadgeListener() {
        Person person = authService.getCurrentPerson();
        if (!(person instanceof AuthenticablePerson currentPerson)) return;

        javafx.collections.ObservableList<NotificationDTO> notifications =
                notificationService.getUserNotifications(currentPerson.getId());

        notifications.addListener((ListChangeListener<NotificationDTO>) change -> {
            updateNotificationBadge();
        });

        updateNotificationBadge();
    }

    /**
     * Refreshes the notification bell visibility and badge.
     * Should be called after login/logout.
     */
    public void refreshNotificationBell() {
        updateNotificationBellVisibility();
    }

    /**
     * Gets the currently loaded controller.
     * Used by sidebar to access controller methods (e.g., clearContextualFilter).
     *
     * @return The currently loaded controller, or null if no view is loaded
     */
    public Object getCurrentController() {
        return this.currentController;
    }
}
