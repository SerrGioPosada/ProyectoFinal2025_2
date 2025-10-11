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
 * Base controller for sidebars (Admin and User).
 * Handles:
 *  - Profile image (circular + hover animation).
 *  - Sidebar open/close animations.
 */
public abstract class BaseSidebarController implements Initializable {

    @FXML
    protected ImageView imgUserImage;

    @FXML
    protected AnchorPane slider;

    protected final AuthenticationService authService = AuthenticationService.getInstance();
    protected IndexController indexController;

    public void setIndexController(IndexController indexController) {
        this.indexController = indexController;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // === Profile Image Setup ===
        String defaultImagePath = "/co/edu/uniquindio/poo/proyectofiinal2025_2/Images/default-userImage.png";
        Image defaultImage = new Image(getClass().getResourceAsStream(defaultImagePath));

        // Set a default image initially
        imgUserImage.setImage(defaultImage);

        // Check if an authenticable person is logged in and set their specific profile image
        Person currentPerson = authService.getCurrentPerson();
        if (currentPerson instanceof AuthenticablePerson) {
            AuthenticablePerson currentAuthPerson = (AuthenticablePerson) currentPerson;
            String userImagePath = currentAuthPerson.getProfileImagePath();
            if (userImagePath != null && !userImagePath.isEmpty()) {
                try {
                    InputStream userImageStream = getClass().getResourceAsStream(userImagePath);
                    if (userImageStream != null) {
                        imgUserImage.setImage(new Image(userImageStream));
                    } else {
                        System.err.println("Could not find user profile image at path: " + userImagePath);
                    }
                } catch (Exception e) {
                    System.err.println("Failed to load user profile image from path: " + userImagePath);
                }
            }
        }

        // Apply a circular clipping mask
        Circle clip = new Circle(50, 50, 50);
        imgUserImage.setClip(clip);

        // Apply a hover animation
        imgUserImage.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), imgUserImage);
            st.setToX(1.1);
            st.setToY(1.1);
            st.play();
        });

        imgUserImage.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), imgUserImage);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });

        // On click: show profile info from the authenticated person
        imgUserImage.setOnMouseClicked(e -> {
            Person personOnClick = authService.getCurrentPerson();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Profile Information");
            alert.setHeaderText(null);

            if (personOnClick != null) {
                alert.setContentText("Name: " + personOnClick.getName() + "\nEmail: " + personOnClick.getEmail());
                ImageView alertImageView = new ImageView();
                alertImageView.setFitWidth(80);
                alertImageView.setFitHeight(80);
                alertImageView.setPreserveRatio(true);

                Image imageToShow = defaultImage;

                if (personOnClick instanceof AuthenticablePerson) {
                    AuthenticablePerson clickedAuthPerson = (AuthenticablePerson) personOnClick;
                    String imagePath = clickedAuthPerson.getProfileImagePath();
                    if (imagePath != null && !imagePath.isEmpty()) {
                        try {
                            InputStream stream = getClass().getResourceAsStream(imagePath);
                            if (stream != null) {
                                imageToShow = new Image(stream);
                            }
                        } catch (Exception ex) {
                            System.err.println("Failed to load image for alert dialog: " + imagePath);
                        }
                    }
                }
                alertImageView.setImage(imageToShow);
                alert.setGraphic(alertImageView);

            } else {
                alert.setContentText("Please log in to view your profile.");
                ImageView alertImageView = new ImageView(defaultImage);
                alertImageView.setFitWidth(80);
                alertImageView.setFitHeight(80);
                alertImageView.setPreserveRatio(true);
                alert.setGraphic(alertImageView);
            }

            alert.showAndWait();
        });

        // === Sidebar Setup ===
        if (slider != null) {
            slider.setTranslateX(-slider.getPrefWidth());
        }
    }

    /** Opens the sidebar with a sliding animation */
    public void openSidebar() {
        if (slider != null) {
            TranslateTransition slide = new TranslateTransition(Duration.seconds(0.4), slider);
            slide.setToX(0);
            slide.play();
        }
    }

    /** Closes the sidebar with a sliding animation */
    public void closeSidebar() {
        if (slider != null) {
            TranslateTransition slide = new TranslateTransition(Duration.seconds(0.4), slider);
            slide.setToX(-slider.getPrefWidth());
            slide.play();
        }
    }
}
