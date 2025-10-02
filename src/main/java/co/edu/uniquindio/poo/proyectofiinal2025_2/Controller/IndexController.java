package co.edu.uniquindio.poo.proyectofiinal2025_2.Controller;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Services.AuthenticationService;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane; // Necessary import
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the main Index view (Index.fxml).
 * <p>Handles top-bar interactions, dynamically loads sidebars, and manages the main content area
 * by loading different views (like Login, Signup, Dashboards) into it.</p>
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
    @FXML
    private AnchorPane paneIndex;
    @FXML
    private StackPane stackIndex; // ADDED: Reference to the StackPane defined in Index.fxml

    private AnchorPane sidebar;
    private final AuthenticationService authService = AuthenticationService.getInstance();

    private static final double SIDEBAR_WIDTH = 176.0; // keep in sync with FXML/sidebar width

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadSidebar();

        exit.setOnMouseClicked(event -> System.exit(0));
        lblMenuBack.setVisible(false);

        lblMenu.setOnMouseClicked(event -> {
            lblMenu.setVisible(false);
            lblMenuBack.setVisible(true);
            openSidebar();
        });

        lblMenuBack.setOnMouseClicked(event -> {
            lblMenu.setVisible(true);
            lblMenuBack.setVisible(false);
            closeSidebar();
        });
    }

    /**
     * Loads the correct sidebar, gets its controller, and passes a reference to this IndexController.
     */
    private void loadSidebar() {
        try {
            String fxmlFile = authService.isCurrentPersonAdmin() ? "AdminSidebar.fxml" : "UserSidebar.fxml";
            URL fxmlUrl = getClass().getResource("/co/edu/uniquindio/poo/proyectofiinal2025_2/View/" + fxmlFile);
            if (fxmlUrl == null) {
                System.err.println("Cannot find FXML file: " + fxmlFile);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            sidebar = loader.load();

            BaseSidebarController sidebarController = loader.getController();
            sidebarController.setIndexController(this);

            sidebar.setTranslateX(-SIDEBAR_WIDTH);
            rootPane.setLeft(sidebar);

        } catch (IOException e) {
            System.err.println("Failed to load sidebar FXML.");
            e.printStackTrace();
        }
    }

    /**
     * Loads a new view into the central content pane (stackIndex).
     * It also passes a reference of this controller to the new view's controller if applicable.
     *
     * @param fxmlName The name of the FXML file to load (e.g., "Login.fxml").
     */
    public void loadView(String fxmlName) {
        try {
            URL fxmlUrl = getClass().getResource("/co/edu/uniquindio/poo/proyectofiinal2025_2/View/" + fxmlName);
            if (fxmlUrl == null) {
                System.err.println("Cannot find FXML file: " + fxmlName);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Node view = loader.load();

            Object controller = loader.getController();
            if (controller instanceof LoginController) {
                ((LoginController) controller).setIndexController(this);
            }
            // Add more 'instanceof' checks here for other controllers like SignupController

            // --- START OF RESPONSIVE CENTERING LOGIC ---

            // 1. Clear the StackPane.
            stackIndex.getChildren().clear();

            // 2. Add the loaded view. StackPane automatically centers its children.
            stackIndex.getChildren().add(view);

            // --- END OF RESPONSIVE CENTERING LOGIC ---

        } catch (IOException e) {
            System.err.println("Failed to load view FXML: " + fxmlName);
            e.printStackTrace();
        }
    }

    /**
     * Called by child controllers (like LoginController) after a successful login.
     * This method reloads the sidebar and loads the user's main dashboard.
     */
    public void onLoginSuccess() {
        loadSidebar();
        loadView("UserDashboard.fxml"); // Ensure UserDashboard.fxml exists
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