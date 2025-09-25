package co.edu.uniquindio.poo.proyectofiinal2025_2.Controller;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.User;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Repositories.UserRepository;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller class for the main index view.
 * <p>
 * This controller handles the sidebar animations,
 * user profile image interactions (hover, click),
 * and profile alert display.
 * </p>
 */
public class IndexController implements Initializable {

    @FXML
    private AnchorPane slider;

    @FXML
    private ImageView exit;

    @FXML
    private Label menu;

    @FXML
    private ImageView userImage;

    @FXML
    private Label menuBack;

    /**
     * Singleton instance of the UserRepository to manage the current user.
     */
    private final UserRepository userRepository = UserRepository.getInstance();

    /**
     * Initializes the controller class.
     * <p>
     * - Configures sidebar animations (open/close). <br>
     * - Sets the default profile image. <br>
     * - Adds hover animation on the profile image. <br>
     * - Displays user information in an alert when the image is clicked. <br>
     * </p>
     *
     * @param url            not used in this implementation
     * @param resourceBundle not used in this implementation
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // Sidebar hidden initially
        slider.setTranslateX(-176);

        // Exit button closes the application
        exit.setOnMouseClicked(event -> System.exit(0));

        // Open sidebar animation
        menu.setOnMouseClicked(event -> {
            TranslateTransition slide = new TranslateTransition(Duration.seconds(0.4), slider);
            slide.setToX(0);
            slide.play();
            slider.setTranslateX(-176);

            slide.setOnFinished((ActionEvent e) -> {
                menu.setVisible(false);
                menuBack.setVisible(true);
            });
        });

        // Close sidebar animation
        menuBack.setOnMouseClicked(event -> {
            TranslateTransition slide = new TranslateTransition(Duration.seconds(0.4), slider);
            slide.setToX(-176);
            slide.play();
            slider.setTranslateX(0);

            slide.setOnFinished((ActionEvent e) -> {
                menu.setVisible(true);
                menuBack.setVisible(false);
            });
        });

        // Default user image
        userImage.setImage(new Image(
                getClass().getResource("/co/edu/uniquindio/poo/proyectofiinal2025_2/Images/default-userImage.png").toExternalForm()
        ));
        userImage.setFitWidth(100);
        userImage.setFitHeight(100);
        userImage.setPreserveRatio(false);

        // Circular clipping mask for profile image
        Circle clip = new Circle(50, 50, 50); // centerX, centerY, radius
        userImage.setClip(clip);
        userImage.setLayoutX(32);
        userImage.setLayoutY(17);

        // Hover animation (zoom effect)
        userImage.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), userImage);
            st.setToX(1.1);
            st.setToY(1.1);
            st.play();
        });

        userImage.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), userImage);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });

        // Show user profile information in alert when clicked
        userImage.setOnMouseClicked(e -> {
            User currentUser = userRepository.getCurrentUser();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Perfil");
            alert.setHeaderText(null);

            if (currentUser != null) {
                alert.setContentText("Nombre: " + currentUser.getNombre() + "\nCorreo: " + currentUser.getCorreo());
                ImageView imageView = new ImageView(currentUser.getProfileImage());
                imageView.setFitWidth(80);
                imageView.setFitHeight(80);
                imageView.setPreserveRatio(true);
                alert.setGraphic(imageView);
            } else {
                alert.setContentText("Por favor, inicie sesión para ver su perfil.");

                // Default profile image in alert
                Image defaultImage = new Image(
                        getClass().getResource("/co/edu/uniquindio/poo/proyectofiinal2025_2/Images/default-userImage.png").toExternalForm()
                );
                ImageView imageView = new ImageView(defaultImage);
                imageView.setFitWidth(80);
                imageView.setFitHeight(80);
                imageView.setPreserveRatio(true);
                alert.setGraphic(imageView);
            }

            alert.showAndWait();
        });
    }
}
