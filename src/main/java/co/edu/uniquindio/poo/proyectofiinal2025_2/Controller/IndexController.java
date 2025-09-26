package co.edu.uniquindio.poo.proyectofiinal2025_2.Controller;

import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the main Index view (Index.fxml).
 * Handles top-bar interactions and opens/closes the included sidebar node.
 */
public class IndexController implements Initializable {

    @FXML
    private ImageView exit;

    @FXML
    private Label menu;

    @FXML
    private Label menuBack;

    /**
     * Root node of the included sidebar (UserSidebar.fxml or AdminSidebar.fxml).
     * This is the AnchorPane returned by the fx:include (fx:id="userSidebar").
     */
    @FXML
    private AnchorPane userSidebar;

    private static final double SIDEBAR_WIDTH = 176.0; // keep in sync with FXML/sidebar width

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Exit button closes the application
        exit.setOnMouseClicked(event -> System.exit(0));

        // Hide menuBack initially (assuming menu visible at start)
        menuBack.setVisible(false);

        // If the sidebar was included, position it hidden to the left
        if (userSidebar != null) {
            userSidebar.setTranslateX(-SIDEBAR_WIDTH);
        }

        // Open sidebar
        menu.setOnMouseClicked(event -> {
            menu.setVisible(false);
            menuBack.setVisible(true);
            openSidebar();
        });

        // Close sidebar
        menuBack.setOnMouseClicked(event -> {
            menu.setVisible(true);
            menuBack.setVisible(false);
            closeSidebar();
        });
    }

    private void openSidebar() {
        if (userSidebar == null) return;
        TranslateTransition slide = new TranslateTransition(Duration.seconds(0.35), userSidebar);
        slide.setToX(0);
        slide.play();
    }

    private void closeSidebar() {
        if (userSidebar == null) return;
        TranslateTransition slide = new TranslateTransition(Duration.seconds(0.35), userSidebar);
        slide.setToX(-SIDEBAR_WIDTH);
        slide.play();
    }
}
