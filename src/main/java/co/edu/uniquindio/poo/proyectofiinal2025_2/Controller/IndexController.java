package co.edu.uniquindio.poo.proyectofiinal2025_2.Controller;


import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import javafx.util.Duration;
import java.util.ResourceBundle;

public class IndexController implements Initializable {

    @FXML
    private AnchorPane slider;

    @FXML
    private ImageView exit;

    @FXML
    private Label menu;

    @FXML
    private Label menuBack;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        slider.setTranslateX(-176);

        exit.setOnMouseClicked(event -> {
            System.exit(0);
        });

        menu.setOnMouseClicked(event -> {
            TranslateTransition slide = new TranslateTransition();
            slide.setDuration(Duration.seconds(0.4));
            slide.setNode(slider);

            slide.setToX(0);
            slide.play();
            slider.setTranslateX(-176);

            slide.setOnFinished((ActionEvent e)-> {
                menu.setVisible(false);
                menuBack.setVisible(true);

            });
        });

        menuBack.setOnMouseClicked(event -> {
            TranslateTransition slide = new TranslateTransition();
            slide.setDuration(Duration.seconds(0.4));
            slide.setNode(slider);

            slide.setToX(-176);
            slide.play();
            slider.setTranslateX(0);

            slide.setOnFinished((ActionEvent e)-> {
                menu.setVisible(true);
                menuBack.setVisible(false);

            });
        });
    }
}