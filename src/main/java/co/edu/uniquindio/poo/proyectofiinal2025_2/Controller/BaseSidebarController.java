package co.edu.uniquindio.poo.proyectofiinal2025_2.Controller;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Person;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.User;
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

    // Use the central authentication service
    protected final AuthenticationService authService = AuthenticationService.getInstance();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // === Profile Image Setup ===
        String path = "/co/edu/uniquindio/poo/proyectofiinal2025_2/Images/default-userImage.png";
        URL imgUrl = getClass().getResource(path);

        // Set a default image initially
        if (imgUrl != null) {
            imgUserImage.setImage(new Image(imgUrl.toExternalForm()));
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
            Person currentPerson = authService.getCurrentPerson();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Profile Information");
            alert.setHeaderText(null);

            if (currentPerson != null) {
                alert.setContentText("Name: " + currentPerson.getName() + "\nEmail: " + currentPerson.getEmail());
                ImageView imageView = new ImageView();
                imageView.setFitWidth(80);
                imageView.setFitHeight(80);
                imageView.setPreserveRatio(true);

                // A User has a profile image, an Admin does not
                if (currentPerson instanceof User) {
                    User currentUser = (User) currentPerson;
                    if (currentUser.getProfileImage() != null) {
                        imageView.setImage(currentUser.getProfileImage());
                    } else if (imgUrl != null) {
                        imageView.setImage(new Image(imgUrl.toExternalForm())); // Fallback to default
                    }
                } else if (imgUrl != null) {
                    // For Admins or other types, use the default image
                    imageView.setImage(new Image(imgUrl.toExternalForm()));
                }
                alert.setGraphic(imageView);

            } else {
                // Case where no one is logged in
                alert.setContentText("Please log in to view your profile.");
                if (imgUrl != null) {
                    ImageView imageView = new ImageView(new Image(imgUrl.toExternalForm()));
                    imageView.setFitWidth(80);
                    imageView.setFitHeight(80);
                    imageView.setPreserveRatio(true);
                    alert.setGraphic(imageView);
                }
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
