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
 * This controller acts as an orchestrator, managing the overall layout which includes a top bar,
 * a dynamic sidebar, and a central content area. It is responsible for:
 * <ul>
 *     <li>Loading the appropriate sidebar (Admin or User) based on authentication status.</li>
 *     <li>Loading different views (e.g., Login, Dashboard, ManageUsers) into the central content pane.</li>
 *     <li>Handling top-level UI events, such as the sidebar menu toggle and application exit.</li>
 *     <li>Coordinating actions after major events, like a successful login.</li>
 * </ul>
 * </p>
 */

public class IndexController implements Initializable {

    // =================================================================================================================
    // FXML Fields
    // =================================================================================================================

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
    private final AuthenticationService authService = AuthenticationService.getInstance();
    private static final double SIDEBAR_WIDTH = 176.0;

    // =================================================================================================================
    // Initialization
    // =================================================================================================================

    /**
     * Initializes the controller, setting up the initial state of the UI.
     *
     * @param url The location used to resolve relative paths for the root object.
     * @param resourceBundle The resources used to localize the root object.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadSidebar();
        setupTopBarEventHandlers();
    }

    // =================================================================================================================
    // Public API for Child Controllers
    // =================================================================================================================

    /**
     * Loads a new view into the central content pane.
     * This is the primary method for navigating between different sections of the application.
     *
     * @param fxmlName The name of the FXML file to load (e.g., "Login.fxml").
     */
    public void loadView(String fxmlName) {
        System.out.println("Attempting to load view: " + fxmlName);
        try {
            URL fxmlUrl = getClass().getResource("/co/edu/uniquindio/poo/proyectofiinal2025_2/View/" + fxmlName);
            if (fxmlUrl == null) {
                System.err.println("CRITICAL: Cannot find FXML resource: " + fxmlName + ". Displaying placeholder.");
                showPlaceholder(fxmlName.replace(".fxml", ""));
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Node view = loader.load();

            // Pass this IndexController instance to the new controller if it needs it.
            Object controller = loader.getController();
            injectIndexController(controller);

            paneIndex.getChildren().setAll(view);

        } catch (IOException e) {
            System.err.println("CRITICAL: Failed to load view FXML: " + fxmlName);
            e.printStackTrace();
        }
    }

    /**
     * Handles the application state change after a user successfully logs in.
     * This method reloads the sidebar to reflect the new user's role and loads the appropriate dashboard.
     */
    public void onLoginSuccess() {
        loadSidebar();

        if (authService.isCurrentPersonAdmin()) {

            Stage stage = (Stage) paneIndex.getScene().getWindow();
            stage.setMaximized(true);
            loadView("AdminDashboard.fxml");
            return; // Guard clause: exit after handling admin case
        }

        // Default case for non-admin users
        loadView("UserDashboard.fxml");
    }

    /**
     * Opens the Signup window as a separate, modal dialog.
     * This is typically used for initial user registration from the login screen.
     */
    public void openSignupWindow() {
        try {
            URL fxmlUrl = getClass().getResource("/co/edu/uniquindio/poo/proyectofiinal2025_2/View/Signup.fxml");
            if (fxmlUrl == null) {
                System.err.println("CRITICAL: Cannot find FXML file: Signup.fxml");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            // Pass a reference of this controller to the signup controller
            SignupController signupController = loader.getController();
            signupController.setIndexController(this);

            Stage stage = new Stage();
            stage.setTitle("Create New Account");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);

            // Ensure the main sidebar is closed when the modal is open
            closeSidebar();
            lblMenu.setVisible(true);
            lblMenuBack.setVisible(false);

            stage.showAndWait();

        } catch (IOException e) {
            System.err.println("CRITICAL: Failed to load Signup window.");
            e.printStackTrace();
        }
    }

    // =================================================================================================================
    // Private Helper Methods
    // =================================================================================================================

    /**
     * Sets up the event handlers for the main application controls, such as the exit and menu buttons.
     */
    private void setupTopBarEventHandlers() {
        exit.setOnMouseClicked(event -> {
            System.out.println("Exit button clicked. Closing application.");
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
     * Loads the correct sidebar (Admin or User) based on the current authentication state.
     * It also injects a reference of this IndexController into the sidebar's controller.
     */
    private void loadSidebar() {
        String fxmlFile = authService.isCurrentPersonAdmin() ? "AdminSidebar.fxml" : "UserSidebar.fxml";
        System.out.println("Attempting to load sidebar: " + fxmlFile);
        try {
            URL fxmlUrl = getClass().getResource("/co/edu/uniquindio/poo/proyectofiinal2025_2/View/" + fxmlFile);
            if (fxmlUrl == null) {
                System.err.println("CRITICAL: Cannot find sidebar FXML file: " + fxmlFile);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            sidebar = loader.load();

            BaseSidebarController sidebarController = loader.getController();
            if (sidebarController != null) {
                sidebarController.setIndexController(this);
            }

            sidebar.setTranslateX(-SIDEBAR_WIDTH);
            rootPane.setLeft(sidebar);

        } catch (IOException e) {
            System.err.println("CRITICAL: Failed to load sidebar FXML.");
            e.printStackTrace();
        }
    }

    /**
     * Opens the sidebar with a slide-in animation.
     */
    private void openSidebar() {
        if (sidebar == null) return;
        TranslateTransition slide = new TranslateTransition(Duration.seconds(0.35), sidebar);
        slide.setToX(0);
        slide.play();
    }

    /**
     * Closes the sidebar with a slide-out animation.
     */
    private void closeSidebar() {
        if (sidebar == null) return;
        TranslateTransition slide = new TranslateTransition(Duration.seconds(0.35), sidebar);
        slide.setToX(-SIDEBAR_WIDTH);
        slide.play();
    }

    /**
     * Injects this IndexController instance into a newly loaded controller if it supports it.
     * This allows child views to communicate back to the main controller.
     *
     * @param controller The controller instance of the newly loaded view.
     */
    private void injectIndexController(Object controller) {

        if (controller instanceof LoginController) {
            ((LoginController) controller).setIndexController(this);
            return;
        }
        if (controller instanceof SignupController) {
            ((SignupController) controller).setIndexController(this);
            return;
        }
        if (controller instanceof ManageUsersController) {
            ((ManageUsersController) controller).setIndexController(this);
        }
    }

    /**
     * Displays a placeholder in the main content area for views that are not yet implemented.
     *
     * @param viewName The name of the view to display in the placeholder text.
     */
    private void showPlaceholder(String viewName) {
        Label placeholder = new Label("View: " + viewName + "\n\n(Not yet implemented)");
        placeholder.setStyle("-fx-font-size: 24px; -fx-text-fill: #6c757d; -fx-alignment: center;");
        StackPane container = new StackPane(placeholder);
        container.setStyle("-fx-background-color: #f8f9fa;");
        paneIndex.getChildren().setAll(container);
    }
}
