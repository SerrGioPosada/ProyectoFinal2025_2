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

    private final UserRepository userRepository = UserRepository.getInstance();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // Sidebar hidden initially
        slider.setTranslateX(-176);

        exit.setOnMouseClicked(event -> System.exit(0));

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

        Circle clip = new Circle(50, 50, 50); // centerX, centerY, radius
        userImage.setClip(clip);
        userImage.setLayoutX(32);
        userImage.setLayoutY(17);

        // Hover animation
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


        userImage.setOnMouseClicked(e -> {
            User currentUser = UserRepository.getInstance().getCurrentUser();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Perfil");
            alert.setHeaderText(null);

            if (currentUser != null) {
                alert.setContentText("Nombre: " + currentUser.getNombre() + "\n Correo: " + currentUser.getCorreo());
                ImageView imageView = new ImageView(currentUser.getProfileImage());
                imageView.setFitWidth(80);
                imageView.setFitHeight(80);
                imageView.setPreserveRatio(true);
                alert.setGraphic(imageView);
            } else {
                alert.setContentText("Por favor, inicie sesión para ver su perfil.");

                // 🔹 Imagen por defecto
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
