package co.edu.uniquindio.poo.proyectofiinal2025_2.Controller;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.AuthenticablePerson;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Person;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Services.AuthenticationService;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Abstract base controller for all sidebars in the application.
 * <p>
 * This class provides core functionalities shared across different sidebars, such as:
 * <ul>
 *     <li>Displaying and styling the user's profile image.</li>
 *     <li>Handling animations for opening and closing the sidebar.</li>
 *     <li>Providing a mechanism for communication with the main {@link IndexController}.</li>
 * </ul>
 * Subclasses are expected to handle their own specific button actions.
 * </p>
 */
public abstract class BaseSidebarController implements Initializable {

    // =================================================================================================================
    // FXML Fields
    // =================================================================================================================

    @FXML
    protected ImageView imgUserImage;

    @FXML
    protected AnchorPane slider;

    // =================================================================================================================
    // Dependencies and State
    // =================================================================================================================

    protected final AuthenticationService authService = AuthenticationService.getInstance();
    protected IndexController indexController;
    private Image defaultProfileImage;

    // =================================================================================================================
    // Initialization
    // =================================================================================================================

    /**
     * Initializes the controller, setting up all UI components and event handlers.
     *
     * @param url The location used to resolve relative paths for the root object, or {@code null} if not known.
     * @param resourceBundle The resources used to localize the root object, or {@code null} if not used.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("Initializing BaseSidebarController...");
        loadDefaultImage();
        setupProfileImage();
        setupSidebarAnimation();
        System.out.println("BaseSidebarController initialized successfully.");
    }

    // =================================================================================================================
    // Public API Methods
    // =================================================================================================================

    /**
     * Sets the reference to the main IndexController to enable cross-controller communication.
     *
     * @param indexController The instance of the main application controller.
     */
    public void setIndexController(IndexController indexController) {
        System.out.println("IndexController has been set in BaseSidebarController.");
        this.indexController = indexController;
    }

    /**
     * Opens the sidebar with a sliding animation.
     */
    public void openSidebar() {
        if (slider == null) {
            System.err.println("Cannot open sidebar: slider component is null.");
            return;
        }
        System.out.println("Starting open sidebar animation.");
        TranslateTransition slide = new TranslateTransition(Duration.seconds(0.4), slider);
        slide.setToX(0);
        slide.play();
    }

    /**
     * Closes the sidebar with a sliding animation.
     */
    public void closeSidebar() {
        if (slider == null) {
            System.err.println("Cannot close sidebar: slider component is null.");
            return;
        }
        System.out.println("Starting close sidebar animation.");
        TranslateTransition slide = new TranslateTransition(Duration.seconds(0.4), slider);
        slide.setToX(-slider.getPrefWidth());
        slide.play();
    }

    // =================================================================================================================
    // Private Helper Methods
    // =================================================================================================================

    /**
     * Loads and caches the default profile image from the resources.
     */
    private void loadDefaultImage() {
        String defaultImagePath = "/co/edu/uniquindio/poo/proyectofiinal2025_2/Images/default-userImage.png";
        System.out.println("Loading default profile image from: " + defaultImagePath);
        try {
            this.defaultProfileImage = new Image(getClass().getResourceAsStream(defaultImagePath));
            if (this.defaultProfileImage.isError()) {
                System.err.println("CRITICAL: Default profile image failed to load. Check the path.");
            }
        } catch (Exception e) {
            System.err.println("CRITICAL: Exception while loading default profile image.");
            e.printStackTrace();
        }
    }

    /**
     * Configures the profile image view, including loading the user-specific image,
     * applying a circular clip, and setting up event handlers.
     */
    private void setupProfileImage() {
        System.out.println("Setting up profile image view...");
        imgUserImage.setImage(getPersonImage(authService.getCurrentPerson()));
        Circle clip = new Circle(50, 50, 50); // Assuming the image view is 100x100
        imgUserImage.setClip(clip);
        addHoverAnimation(imgUserImage);
        imgUserImage.setOnMouseClicked(event -> {
            System.out.println("Profile image clicked.");
            showProfileInformationDialog();
        });
        System.out.println("Profile image setup complete.");
    }

    /**
     * Prepares the initial state of the sidebar for the slide-in animation.
     */
    private void setupSidebarAnimation() {
        if (slider != null) {
            slider.setTranslateX(-slider.getPrefWidth());
        }
    }

    /**
     * Adds a scale-on-hover animation to the profile image.
     *
     * @param imageView The ImageView to animate.
     */
    private void addHoverAnimation(ImageView imageView) {
        ScaleTransition stEnter = new ScaleTransition(Duration.millis(200), imageView);
        stEnter.setToX(1.1);
        stEnter.setToY(1.1);
        ScaleTransition stExit = new ScaleTransition(Duration.millis(200), imageView);
        stExit.setToX(1.0);
        stExit.setToY(1.0);
        imageView.setOnMouseEntered(e -> stEnter.play());
        imageView.setOnMouseExited(e -> stExit.play());
    }

    /**
     * Creates and displays an information dialog with the current user's profile details.
     */
    private void showProfileInformationDialog() {
        System.out.println("Showing profile information dialog...");
        Person currentPerson = authService.getCurrentPerson();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Profile Information");
        alert.setHeaderText(null);

        if (currentPerson == null) {
            System.out.println("No user logged in. Showing default info in dialog.");
            alert.setContentText("Please log in to view your profile.");
            alert.setGraphic(new ImageView(defaultProfileImage));
            alert.showAndWait();
            return;
        }

        alert.setContentText("Name: " + currentPerson.getName() + "\nEmail: " + currentPerson.getEmail());
        ImageView alertImageView = new ImageView(getPersonImage(currentPerson));
        alertImageView.setFitWidth(80);
        alertImageView.setFitHeight(80);
        alertImageView.setPreserveRatio(true);
        alert.setGraphic(alertImageView);
        alert.showAndWait();
    }

    /**
     * Safely retrieves the profile image for a given person.
     *
     * @param person The person whose image is to be loaded.
     * @return The loaded {@link Image} object, or the default profile image if not found or if the person is not authenticable.
     */
    private Image getPersonImage(Person person) {
        System.out.println("Attempting to get image for a person...");

        if (person == null) {
            System.out.println("Person is null. Using default image.");
            return defaultProfileImage;
        }

        if (!(person instanceof AuthenticablePerson)) {
            System.out.println("Person is not an AuthenticablePerson. Using default image.");
            return defaultProfileImage;
        }

        AuthenticablePerson authPerson = (AuthenticablePerson) person;
        String imagePath = authPerson.getProfileImagePath();
        System.out.println("Person is Authenticable. Profile image path: '" + imagePath + "'");

        if (imagePath == null || imagePath.isEmpty()) {
            System.out.println("Image path is null or empty. Using default image.");
            return defaultProfileImage;
        }

        try {
            InputStream stream = getClass().getResourceAsStream(imagePath);
            if (stream == null) {
                System.err.println("Could not find resource stream for path: " + imagePath + ". Falling back to default.");
                return defaultProfileImage;
            }

            System.out.println("Image stream found. Loading image from path.");
            Image loadedImage = new Image(stream);

            if (loadedImage.isError()) {
                System.err.println("Error loading image from stream for path: " + imagePath + ". Falling back to default.");
                return defaultProfileImage;
            }

            return loadedImage;

        } catch (Exception e) {
            System.err.println("Exception while loading profile image from path: " + imagePath);
            e.printStackTrace();
            return defaultProfileImage; // Return default on any exception
        }
    }
}
