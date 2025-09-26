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
    private Label lblMenu;

    @FXML
    private Label lblMenuBack;

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

        // Hide lblMenuBack initially (assuming lblMenu visible at start)
        lblMenuBack.setVisible(false);

        // If the sidebar was included, position it hidden to the left
        if (userSidebar != null) {
            userSidebar.setTranslateX(-SIDEBAR_WIDTH);
        }

        // Open sidebar
        lblMenu.setOnMouseClicked(event -> {
            lblMenu.setVisible(false);
            lblMenuBack.setVisible(true);
            openSidebar();
        });

        // Close sidebar
        lblMenuBack.setOnMouseClicked(event -> {
            lblMenu.setVisible(true);
            lblMenuBack.setVisible(false);
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
