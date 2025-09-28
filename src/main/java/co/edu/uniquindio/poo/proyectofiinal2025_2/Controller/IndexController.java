package co.edu.uniquindio.poo.proyectofiinal2025_2.Controller;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Services.AuthenticationService;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the main Index view (Index.fxml).
 * <p>Handles top-bar interactions and dynamically loads the appropriate sidebar
 * (Admin or User) based on the logged-in person's type.</p>
 */
public class IndexController implements Initializable {

    @FXML
    private ImageView exit;
    @FXML
    private Label lblMenu;
    @FXML
    private Label lblMenuBack;
    @FXML
    private BorderPane rootPane;

    private AnchorPane sidebar;
    private final AuthenticationService authService = AuthenticationService.getInstance();

    private static final double SIDEBAR_WIDTH = 176.0; // keep in sync with FXML/sidebar width

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Dynamically load the correct sidebar based on the person's type (Admin or User)
        loadSidebar();

        // Configure sidebar animation and initial state
        if (sidebar != null) {
            sidebar.setTranslateX(-SIDEBAR_WIDTH);
            rootPane.setLeft(sidebar);
        }

        // Exit button closes the application
        exit.setOnMouseClicked(event -> System.exit(0));

        // Hide back-menu button initially
        lblMenuBack.setVisible(false);

        // Open sidebar action
        lblMenu.setOnMouseClicked(event -> {
            lblMenu.setVisible(false);
            lblMenuBack.setVisible(true);
            openSidebar();
        });

        // Close sidebar action
        lblMenuBack.setOnMouseClicked(event -> {
            lblMenu.setVisible(true);
            lblMenuBack.setVisible(false);
            closeSidebar();
        });
    }

    /**
     * Loads the appropriate sidebar FXML (AdminSidebar.fxml or UserSidebar.fxml)
     * into the view based on the current person's type, determined by the AuthenticationService.
     */
    private void loadSidebar() {
        try {
            // Ask the authentication service if the current person is an admin
            String fxmlFile = authService.isCurrentPersonAdmin() ? "AdminSidebar.fxml" : "UserSidebar.fxml";
            URL fxmlUrl = getClass().getResource("/co/edu/uniquindio/poo/proyectofiinal2025_2/View/" + fxmlFile);
            if (fxmlUrl == null) {
                System.err.println("Cannot find FXML file: " + fxmlFile);
                return;
            }
            sidebar = FXMLLoader.load(fxmlUrl);
        } catch (IOException e) {
            System.err.println("Failed to load sidebar FXML.");
            e.printStackTrace();
        }
    }

    /**
     * Plays a slide-in animation to show the sidebar.
     */
    private void openSidebar() {
        if (sidebar == null) return;
        TranslateTransition slide = new TranslateTransition(Duration.seconds(0.35), sidebar);
        slide.setToX(0);
        slide.play();
    }

    /**
     * Plays a slide-out animation to hide the sidebar.
     */
    private void closeSidebar() {
        if (sidebar == null) return;
        TranslateTransition slide = new TranslateTransition(Duration.seconds(0.35), sidebar);
        slide.setToX(-SIDEBAR_WIDTH);
        slide.play();
    }
}
