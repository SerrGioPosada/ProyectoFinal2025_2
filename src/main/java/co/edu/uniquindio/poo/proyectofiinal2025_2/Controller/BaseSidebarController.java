package co.edu.uniquindio.poo.proyectofiinal2025_2.Controller;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.User;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Repositories.UserRepository;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Services.UserService;
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
 * Contains the common logic for:
 * - Handling the user's profile image.
 * - Sidebar open/close animations.
 */
public abstract class BaseSidebarController implements Initializable {

    @FXML
    protected ImageView imgUserImage;

    @FXML
    protected AnchorPane slider;

    // Use UserService instead of repository
    protected final UserService userService = new UserService(UserRepository.getInstance());

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // === Profile Image Setup ===
        String path = "/co/edu/uniquindio/poo/proyectofiinal2025_2/Images/default-userImage.png"; // cuidado con el nombre
        URL imgUrl = getClass().getResource(path);

        if (imgUrl != null) {
            imgUserImage.setImage(new Image(imgUrl.toExternalForm()));
        }

        imgUserImage.setFitWidth(100);
        imgUserImage.setFitHeight(100);
        imgUserImage.setPreserveRatio(true);

        // Circular clipping mask
        Circle clip = new Circle(50, 50, 50);
        imgUserImage.setClip(clip);

        // Hover animation
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

        // On click: show profile information
        imgUserImage.setOnMouseClicked(e -> {
            User currentUser = userService.getCurrentUser();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Profile");
            alert.setHeaderText(null);

            if (currentUser != null) {
                alert.setContentText("Name: " + currentUser.getName() + "\nEmail: " + currentUser.getEmail());
                ImageView imageView = new ImageView(currentUser.getProfileImage());
                imageView.setFitWidth(80);
                imageView.setFitHeight(80);
                imageView.setPreserveRatio(true);
                alert.setGraphic(imageView);
            } else if (imgUrl != null) {
                alert.setContentText("Please register to see user data");
                ImageView imageView = new ImageView(new Image(imgUrl.toExternalForm()));
                imageView.setFitWidth(80);
                imageView.setFitHeight(80);
                imageView.setPreserveRatio(true);
                alert.setGraphic(imageView);
            }

            alert.showAndWait();
        });

        // === Sidebar Setup ===
        if (slider != null) {
            slider.setTranslateX(-slider.getPrefWidth());
        }
    }

    /** Opens the sidebar with sliding animation */
    public void openSidebar() {
        if (slider != null) {
            TranslateTransition slide = new TranslateTransition(Duration.seconds(0.4), slider);
            slide.setToX(0);
            slide.play();
        }
    }

    /** Closes the sidebar with sliding animation */
    public void closeSidebar() {
        if (slider != null) {
            TranslateTransition slide = new TranslateTransition(Duration.seconds(0.4), slider);
            slide.setToX(-slider.getPrefWidth());
            slide.play();
        }
    }
}
