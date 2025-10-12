package co.edu.uniquindio.poo.proyectofiinal2025_2.Controller;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Services.AuthenticationService;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * The main controller for the application's primary stage (Index.fxml).
 * <p>
 * Orchestrates the main layout (top bar, sidebar, content area) and handles:
 * <ul>
 *     <li>Loading the correct sidebar based on user role.</li>
 *     <li>Loading views dynamically into the main content area.</li>
 *     <li>Top-level UI events (menu toggle, exit).</li>
 *     <li>Login success handling.</li>
 * </ul>
 * </p>
 */

public class IndexController implements Initializable {

    // =================================================================================================================
    // FXML Fields
    // =================================================================================================================

    private static final double SIDEBAR_WIDTH = 176.0;
    private final AuthenticationService authService = AuthenticationService.getInstance();
    @FXML
    private ImageView exit;
    @FXML
    private Label lblMenu;
    @FXML
    private Label lblMenuBack;
    @FXML
    private BorderPane rootPane;
    @FXML
    private StackPane paneIndex;
    // =================================================================================================================
    // Dependencies and State
    // =================================================================================================================
    private AnchorPane sidebar;

    // =================================================================================================================
    // Initialization
    // =================================================================================================================
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadSidebar();
        setupTopBarEventHandlers();
    }

    // =================================================================================================================
    // Public API for Child Controllers
    // =================================================================================================================

    /**
     * Loads a view into the central content pane.
     *
     * @param fxmlName The FXML filename to load (e.g., "Login.fxml").
     */
    public void loadView(String fxmlName) {
        System.out.println("Loading view: " + fxmlName);
        try {
            URL fxmlUrl = getClass().getResource("/co/edu/uniquindio/poo/proyectofiinal2025_2/View/" + fxmlName);
            if (fxmlUrl == null) {
                System.err.println("Cannot find FXML resource: " + fxmlName + ". Showing placeholder.");
                showPlaceholder(fxmlName.replace(".fxml", ""));
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Node view = loader.load();

            // Inject IndexController to child if needed
            Object controller = loader.getController();
            injectIndexController(controller);

            paneIndex.getChildren().setAll(view);

        } catch (IOException e) {
            System.err.println("Failed to load view FXML: " + fxmlName);
            e.printStackTrace();
        }
    }

    /**
     * Called after successful login.
     * Reloads the sidebar and opens the appropriate dashboard.
     */
    public void onLoginSuccess() {
        loadSidebar();

        if (authService.isCurrentPersonAdmin()) {
            Stage stage = (Stage) paneIndex.getScene().getWindow();
            stage.setMaximized(true);
            loadView("AdminDashboard.fxml");
            return;
        }

        loadView("UserDashboard.fxml");
    }

    /**
     * Opens the Signup window in a modal dialog.
     */
    public void openSignupWindow() {
        try {
            URL fxmlUrl = getClass().getResource("/co/edu/uniquindio/poo/proyectofiinal2025_2/View/Signup.fxml");
            if (fxmlUrl == null) {
                System.err.println("Cannot find FXML file: Signup.fxml");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            SignupController signupController = loader.getController();
            signupController.setIndexController(this);

            Stage stage = new Stage();
            stage.setTitle("Create New Account");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);

            closeSidebar();
            lblMenu.setVisible(true);
            lblMenuBack.setVisible(false);

            stage.showAndWait();

        } catch (IOException e) {
            System.err.println("Failed to load Signup window.");
            e.printStackTrace();
        }
    }

    /**
     * Opens the sidebar with slide-in animation.
     */
    public void openSidebar() {
        toggleSidebar(true);
    }

    /**
     * Closes the sidebar with slide-out animation.
     */
    public void closeSidebar() {
        toggleSidebar(false);
    }

    // =================================================================================================================
    // Private Helper Methods
    // =================================================================================================================

    /**
     * Toggles the sidebar animation based on open/close state.
     *
     * @param open true to open, false to close
     */
    private void toggleSidebar(boolean open) {
        if (sidebar == null) return;
        TranslateTransition slide = new TranslateTransition(Duration.seconds(0.35), sidebar);
        slide.setToX(open ? 0 : -SIDEBAR_WIDTH);
        slide.play();
    }

    /**
     * Sets up the top bar button event handlers.
     */
    private void setupTopBarEventHandlers() {
        exit.setOnMouseClicked(event -> {
            System.out.println("Exit clicked, closing app.");
            System.exit(0);
        });

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
     * Loads the appropriate sidebar based on user role.
     */
    private void loadSidebar() {
        String fxmlFile = authService.isCurrentPersonAdmin() ? "AdminSidebar.fxml" : "UserSidebar.fxml";
        System.out.println("Loading sidebar: " + fxmlFile);
        try {
            URL fxmlUrl = getClass().getResource("/co/edu/uniquindio/poo/proyectofiinal2025_2/View/" + fxmlFile);
            if (fxmlUrl == null) {
                System.err.println("Cannot find sidebar FXML: " + fxmlFile);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            sidebar = loader.load();

            BaseSidebarController sidebarController = loader.getController();
            if (sidebarController != null) sidebarController.setIndexController(this);

            sidebar.setTranslateX(-SIDEBAR_WIDTH);
            rootPane.setLeft(sidebar);

        } catch (IOException e) {
            System.err.println("Failed to load sidebar FXML.");
            e.printStackTrace();
        }
    }

    /**
     * Injects this IndexController into child controllers if they support it.
     *
     * @param controller the child controller
     */
    private void injectIndexController(Object controller) {
        if (controller instanceof LoginController login) login.setIndexController(this);
        else if (controller instanceof SignupController signup) signup.setIndexController(this);
        else if (controller instanceof ManageUsersController manage) manage.setIndexController(this);
    }

    /**
     * Shows a placeholder for unimplemented views.
     *
     * @param viewName Name of the view
     */
    private void showPlaceholder(String viewName) {
        Label placeholder = new Label("View: " + viewName + "\n\n(Not yet implemented)");
        placeholder.setStyle("-fx-font-size: 24px; -fx-text-fill: #6c757d; -fx-alignment: center;");
        StackPane container = new StackPane(placeholder);
        container.setStyle("-fx-background-color: #f8f9fa;");
        paneIndex.getChildren().setAll(container);
    }

    /**
     * Replaces the center content of the main BorderPane with the given view.
     *
     * @param content The new content to display in the center of the main layout.
     */
    public void setCenterContent(Parent content) {
        if (paneIndex == null || content == null) return;
        paneIndex.getChildren().setAll(content);
    }
}
