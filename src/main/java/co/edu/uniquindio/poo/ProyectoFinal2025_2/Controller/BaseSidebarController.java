package co.edu.uniquindio.poo.ProyectoFinal2025_2.Controller;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.AuthenticablePerson;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Person;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.AuthenticationService;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

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

    private static final double PROFILE_CLIP_RADIUS = 50;
    private static final double PROFILE_DIALOG_IMAGE_WIDTH = 80;
    private static final double PROFILE_DIALOG_IMAGE_HEIGHT = 80;

    // =================================================================================================================
    // FXML Fields
    // =================================================================================================================
    protected final AuthenticationService authService = AuthenticationService.getInstance();
    protected final ThemeManager themeManager = ThemeManager.getInstance();

    @FXML
    protected ImageView imgUserImage;

    @FXML
    protected Button btnToggleTheme;

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
     * Sets up the theme toggle button with the current theme icon.
     */
    private void setupThemeButton() {
        if (btnToggleTheme != null) {
            updateThemeButtonIcon();
        }
    }

    /**
     * Updates the theme button icon based on current theme.
     */
    private void updateThemeButtonIcon() {
        if (btnToggleTheme == null) return;

        if (themeManager.isDarkTheme()) {
            btnToggleTheme.setText("☀");  // Sun for light mode
        } else {
            btnToggleTheme.setText("🌙");  // Moon for dark mode
        }
    }

    /**
     * Handles theme toggle button click.
     */
    @FXML
    protected void handleToggleTheme() {
        log("Theme toggle button clicked");
        themeManager.toggleTheme();
        updateThemeButtonIcon();
        log("Theme toggled to: " + themeManager.getCurrentTheme());

        // Ensure the scene reference is still valid
        if (btnToggleTheme != null && btnToggleTheme.getScene() != null) {
            themeManager.setScene(btnToggleTheme.getScene());
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
        if (imgUserImage == null) {
            logError("ImageView 'imgUserImage' is null. Cannot set up profile image.");
            return;
        }

        log("Setting up profile image view...");
        imgUserImage.setImage(getPersonImage(authService.getCurrentPerson()));
        ImageUtil.applyCircularClip(imgUserImage, PROFILE_CLIP_RADIUS);
        AnimationUtil.addHoverScale(imgUserImage, 1.1, 200);

        // Show profile info when clicked, delegated to DialogUtil
        imgUserImage.setOnMouseClicked(event -> {
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
