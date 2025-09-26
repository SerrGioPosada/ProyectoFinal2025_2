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
 * Handles:
 *  - Profile image (circular + hover animation).
 *  - Sidebar open/close animations.
 */
public abstract class BaseSidebarController implements Initializable {

    @FXML
    protected ImageView imgUserImage;

    @FXML
    protected AnchorPane slider;

    // Service injection
    protected final UserService userService = new UserService(UserRepository.getInstance());

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // === Profile Image Setup ===
        String path = "/co/edu/uniquindio/poo/proyectofiinal2025_2/Images/default-userImage.png";
        URL imgUrl = getClass().getResource(path);

        if (imgUrl != null) {
            imgUserImage.setImage(new Image(imgUrl.toExternalForm()));
        }

        // Circular clipping mask (esto no puede ir en CSS)
        Circle clip = new Circle(50, 50, 50);
        imgUserImage.setClip(clip);

        // Hover animation (no se puede hacer en CSS)
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

        // On click: show profile info
        imgUserImage.setOnMouseClicked(e -> {
            User currentUser = userService.getCurrentUser();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Perfil");
            alert.setHeaderText(null);

            if (currentUser != null) {
                alert.setContentText("Nombre: " + currentUser.getName() + "\nEmail: " + currentUser.getEmail());
                ImageView imageView = new ImageView(currentUser.getProfileImage());
                imageView.setFitWidth(80);
                imageView.setFitHeight(80);
                imageView.setPreserveRatio(true);
                alert.setGraphic(imageView);
            } else if (imgUrl != null) {
                alert.setContentText("Por favor regístrate para ver los datos.");
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
