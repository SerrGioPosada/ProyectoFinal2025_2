package co.edu.uniquindio.poo.ProyectoFinal2025_2.Controller;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.AuthenticablePerson;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Person;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.AuthenticationService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.NotificationService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.AnimationUtil;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.DialogUtil;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.ImageUtil;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.NavigationUtil;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.ThemeManager;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.StringUtil;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.geometry.Pos;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Abstract base controller for all sidebars in the application.
 * <p>
 * Provides shared logic for sidebar-related features such as:
 * <ul>
 *     <li>Displaying and styling the user’s profile image.</li>
 *     <li>Handling open/close animations for the sidebar.</li>
 *     <li>Offering a utility method for navigation between FXML views.</li>
 *     <li>Displaying basic profile information for the logged user.</li>
 * </ul>
 * <p>
 * All sidebar controllers (e.g., Admin, Employee, Client) should extend this class.
 * </p>
 */
public abstract class BaseSidebarController implements Initializable {

    // =================================================================================================================
    // Constants
    // =================================================================================================================

    private static final double PROFILE_CLIP_RADIUS = 45;
    private static final double PROFILE_DIALOG_IMAGE_WIDTH = 80;
    private static final double PROFILE_DIALOG_IMAGE_HEIGHT = 80;

    // =================================================================================================================
    // FXML Fields
    // =================================================================================================================
    protected final AuthenticationService authService = AuthenticationService.getInstance();
    protected final ThemeManager themeManager = ThemeManager.getInstance();
    protected final NotificationService notificationService = NotificationService.getInstance();

    @FXML
    protected ImageView imgUserImage;

    @FXML
    protected javafx.scene.layout.StackPane profileImageContainer;

    @FXML
    protected javafx.scene.layout.HBox themeToggleContainer;

    protected co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.ThemeToggleSwitch themeToggle;

    // Notification badge (created programmatically)
    protected Label notificationBadge;

    // =================================================================================================================
    // Dependencies and State
    // =================================================================================================================
    @FXML
    protected AnchorPane slider;
    protected IndexController indexController;
    private Image defaultProfileImage;

    // Active button tracking
    protected Button currentActiveButton = null;

    // =================================================================================================================
    // Initialization
    // =================================================================================================================

    /**
     * Initializes the sidebar controller, configuring UI elements such as
     * the profile image and the initial animation state of the sidebar.
     *
     * @param url            The location used to resolve relative paths for the root object, or {@code null} if not known.
     * @param resourceBundle The resources used to localize the root object, or {@code null} if not used.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        log("Initializing BaseSidebarController...");
        defaultProfileImage = ImageUtil.loadDefaultImage("/co/edu/uniquindio/poo/ProyectoFinal2025_2/Images/default-userImage.png");
        setupProfileImage();
        prepareSidebarAnimation();
        setupThemeButton();
        log("BaseSidebarController initialized successfully.");
    }

    /**
     * Sets up the theme toggle switch with the current theme state.
     */
    private void setupThemeButton() {
        if (themeToggleContainer != null) {
            // Create the theme toggle switch programmatically
            themeToggle = new co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.ThemeToggleSwitch();

            // Add it to the container
            themeToggleContainer.getChildren().add(themeToggle);

            // Set initial state based on current theme
            themeToggle.setState(themeManager.isDarkTheme());

            // Set callback to handle theme changes
            themeToggle.setOnToggle(() -> {
                log("Theme toggle switch activated");
                themeManager.toggleTheme();
                log("Theme toggled to: " + themeManager.getCurrentTheme());

                // Ensure the scene reference is still valid
                if (themeToggle.getScene() != null) {
                    themeManager.setScene(themeToggle.getScene());
                }
            });
        }
    }

    // =================================================================================================================
    // Public API
    // =================================================================================================================

    /**
     * Injects the main {@link IndexController} reference, enabling
     * view navigation from within sidebar controllers.
     *
     * @param indexController The instance of the main IndexController.
     */
    public void setIndexController(IndexController indexController) {
        this.indexController = indexController;
        log("IndexController reference successfully injected into BaseSidebarController.");
    }

    /**
     * Opens the sidebar with a smooth slide-in animation.
     */
    public void openSidebar() {
        AnimationUtil.slide(slider, 0, 0.4);
    }

    /**
     * Closes the sidebar with a smooth slide-out animation.
     */
    public void closeSidebar() {
        if (slider != null) {
            AnimationUtil.slide(slider, -slider.getPrefWidth(), 0.4);
        }
    }

    /**
     * Provides a shortcut for navigating between views using {@link NavigationUtil}.
     * This method can be used by subclasses to trigger FXML view changes.
     *
     * @param viewName The name of the FXML file to load (e.g., "AdminDashboard.fxml").
     */
    protected void navigateTo(String viewName) {
        NavigationUtil.navigate(indexController, viewName, getClass());
    }

    /**
     * Sets the active button in the sidebar by applying the active style class.
     * Removes the active class from the previously active button.
     *
     * @param button The button that should be marked as active
     */
    public void setActiveButton(Button button) {
        // Remove active class from previous button
        if (currentActiveButton != null) {
            currentActiveButton.getStyleClass().remove("sidebar-button-active");
        }

        // Add active class to new button
        if (button != null && !button.getStyleClass().contains("sidebar-button-active")) {
            button.getStyleClass().add("sidebar-button-active");
            currentActiveButton = button;
        }
    }

    /**
     * Clears the active state from all sidebar buttons.
     * Called when loading views from header navigation to avoid having both header and sidebar buttons active.
     */
    public void clearActiveButton() {
        if (currentActiveButton != null) {
            currentActiveButton.getStyleClass().remove("sidebar-button-active");
            currentActiveButton = null;
        }
    }

    /**
     * Gets the currently active button.
     *
     * @return The currently active button, or null if none is active
     */
    public Button getCurrentActiveButton() {
        return currentActiveButton;
    }

    // =================================================================================================================
    // Protected Utility Methods
    // =================================================================================================================

    /**
     * Prepares the sidebar to be hidden off-screen before any animations are triggered.
     * Ensures a smooth entry when the sidebar first opens.
     */
    private void prepareSidebarAnimation() {
        if (slider != null) {
            slider.setTranslateX(-slider.getPrefWidth());
        }
    }

    // =================================================================================================================
    // Profile Image Handling
    // =================================================================================================================

    /**
     * Configures the profile image view:
     * <ul>
     *     <li>Sets the image based on the currently authenticated user.</li>
     *     <li>Applies a circular clip to the image.</li>
     *     <li>Adds hover animation effects.</li>
     *     <li>Displays a profile dialog when clicked.</li>
     * </ul>
     */
    private void setupProfileImage() {
        if (imgUserImage == null || profileImageContainer == null) {
            logError("ImageView or Container is null. Cannot set up profile image.");
            return;
        }

        log("Setting up profile image view...");
        javafx.scene.image.Image image = getPersonImage(authService.getCurrentPerson());
        imgUserImage.setImage(image);

        // Center the image using viewport for perfect circular crop
        if (image != null) {
            double imageWidth = image.getWidth();
            double imageHeight = image.getHeight();

            // Calculate the square crop area (centered)
            double size = Math.min(imageWidth, imageHeight);
            double offsetX = (imageWidth - size) / 2;
            double offsetY = (imageHeight - size) / 2;

            imgUserImage.setViewport(new javafx.geometry.Rectangle2D(offsetX, offsetY, size, size));
        }

        // Apply circular clip to the CONTAINER
        javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(45, 45, 45);
        profileImageContainer.setClip(clip);

        // Apply smooth scaling to the CONTAINER (not the ImageView)
        javafx.animation.ScaleTransition scaleUp = new javafx.animation.ScaleTransition(
            javafx.util.Duration.millis(200), profileImageContainer
        );
        scaleUp.setToX(1.08);
        scaleUp.setToY(1.08);

        javafx.animation.ScaleTransition scaleDown = new javafx.animation.ScaleTransition(
            javafx.util.Duration.millis(200), profileImageContainer
        );
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);

        profileImageContainer.setOnMouseEntered(event -> scaleUp.playFromStart());
        profileImageContainer.setOnMouseExited(event -> scaleDown.playFromStart());

        // Show profile info when clicked
        profileImageContainer.setOnMouseClicked(event -> {
            Person person = authService.getCurrentPerson();
            DialogUtil.showProfileDialog(person, defaultProfileImage,
                    PROFILE_DIALOG_IMAGE_WIDTH, PROFILE_DIALOG_IMAGE_HEIGHT);
        });

        log("Profile image successfully initialized.");
    }

    /**
     * Retrieves the correct profile image for a given person.
     * <p>
     * If the user has no profile image or cannot be authenticated, the default image is used.
     * </p>
     *
     * @param person The {@link Person} whose profile image should be loaded.
     * @return A {@link Image} representing the person's profile picture, or a default image if unavailable.
     */
    private Image getPersonImage(Person person) {
        if (!(person instanceof AuthenticablePerson authPerson)) {
            return defaultProfileImage;
        }

        String path = authPerson.getProfileImagePath();
        if (StringUtil.isNullOrEmpty(path)) return defaultProfileImage;

        // Use loadFromFile for user images (file system paths)
        Image img = ImageUtil.loadFromFile(path);
        return (img != null) ? img : defaultProfileImage;
    }

    /**
     * Refreshes the profile image in the sidebar.
     * Call this method after the user updates their profile picture.
     */
    public void refreshProfileImage() {
        if (imgUserImage == null) {
            logError("ImageView 'imgUserImage' is null. Cannot refresh profile image.");
            return;
        }

        log("Refreshing profile image...");
        Person currentPerson = authService.getCurrentPerson();
        imgUserImage.setImage(getPersonImage(currentPerson));
        log("Profile image refreshed successfully.");
    }

    // =================================================================================================================
    // Notification Badge Management
    // =================================================================================================================

    /**
     * Creates and attaches a notification badge to a button.
     * The badge displays the count of unread notifications.
     *
     * @param button The button to attach the badge to (e.g., notifications button)
     * @return The created badge Label for further customization if needed
     */
    protected Label createNotificationBadge(Button button) {
        if (button == null) {
            logError("Cannot create notification badge: button is null");
            return null;
        }

        // Create badge label
        notificationBadge = new Label();
        notificationBadge.getStyleClass().add("notification-badge");
        notificationBadge.setStyle(
            "-fx-background-color: #dc3545; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 10px; " +
            "-fx-font-weight: bold; " +
            "-fx-padding: 2 6 2 6; " +
            "-fx-background-radius: 10; " +
            "-fx-min-width: 18px; " +
            "-fx-alignment: center;"
        );
        notificationBadge.setVisible(false);  // Hidden by default
        notificationBadge.setManaged(false);  // Doesn't take space when hidden

        // Wrap button in StackPane if not already wrapped
        if (!(button.getParent() instanceof StackPane)) {
            // We'll position it absolutely, so just log that the button should be in a StackPane
            log("Note: For best results, place notification button in a StackPane in FXML");
        }

        // Try to add badge to button's parent if it's a StackPane
        if (button.getParent() instanceof StackPane stackPane) {
            stackPane.getChildren().add(notificationBadge);
            StackPane.setAlignment(notificationBadge, Pos.TOP_RIGHT);
            log("Notification badge created and added to StackPane");
        } else {
            log("Notification badge created but parent is not StackPane - manual positioning needed");
        }

        return notificationBadge;
    }

    /**
     * Updates the notification badge with the current unread count.
     * Automatically shows/hides the badge based on whether there are unread notifications.
     */
    protected void updateNotificationBadge() {
        if (notificationBadge == null) {
            return; // Badge not created yet
        }

        Person person = authService.getCurrentPerson();
        if (!(person instanceof AuthenticablePerson currentPerson)) {
            return;
        }

        int unreadCount = notificationService.getUnreadCount(currentPerson.getId());

        if (unreadCount > 0) {
            // Show badge with count
            notificationBadge.setText(unreadCount > 99 ? "99+" : String.valueOf(unreadCount));
            notificationBadge.setVisible(true);
            notificationBadge.setManaged(true);
            log("Notification badge updated: " + unreadCount + " unread");
        } else {
            // Hide badge when no unread notifications
            notificationBadge.setVisible(false);
            notificationBadge.setManaged(false);
            log("Notification badge hidden: no unread notifications");
        }
    }

    /**
     * Sets up a listener to automatically update the notification badge when notifications change.
     * Call this method after creating the badge to enable real-time updates.
     */
    protected void setupNotificationBadgeListener() {
        Person person = authService.getCurrentPerson();
        if (!(person instanceof AuthenticablePerson currentPerson)) {
            return;
        }

        // Get the observable notification list
        javafx.collections.ObservableList<co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.dto.NotificationDTO> notifications =
                notificationService.getUserNotifications(currentPerson.getId());

        // Listen for changes
        notifications.addListener((javafx.collections.ListChangeListener<co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.dto.NotificationDTO>) change -> {
            updateNotificationBadge();
        });

        // Initial update
        updateNotificationBadge();
        log("Notification badge listener set up");
    }

    // =================================================================================================================
    // Logging Utilities
    // =================================================================================================================

    /**
     * Logs informational messages from the BaseSidebarController.
     *
     * @param msg The message to be printed to the standard output.
     */
    protected void log(String msg) {
        Logger.info("[BaseSidebar] " + msg);
    }

    /**
     * Logs error messages to the standard error output.
     *
     * @param msg The error message to print.
     */
    protected void logError(String msg) {
        Logger.error("[BaseSidebar:ERROR] " + msg);
    }
}
