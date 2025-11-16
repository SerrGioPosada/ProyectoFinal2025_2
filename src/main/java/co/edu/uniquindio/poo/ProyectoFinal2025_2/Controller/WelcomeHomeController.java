package co.edu.uniquindio.poo.ProyectoFinal2025_2.Controller;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the Welcome Home landing page.
 * Handles navigation to Login and Signup views.
 */
public class WelcomeHomeController implements Initializable {

    @FXML
    private Button btnLogin;

    @FXML
    private Button btnSignup;

    private IndexController indexController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Logger.info("WelcomeHomeController initialized");
    }

    /**
     * Sets the IndexController reference.
     * This is called by IndexController after loading the view.
     *
     * @param indexController The main IndexController
     */
    public void setIndexController(IndexController indexController) {
        this.indexController = indexController;
    }

    /**
     * Handles the Login button click.
     * Loads the Login view.
     */
    @FXML
    private void handleLogin() {
        Logger.info("Login button clicked from WelcomeHome");
        if (indexController != null) {
            indexController.loadView("Login.fxml");
        } else {
            Logger.error("IndexController is null, cannot load Login view");
        }
    }

    /**
     * Handles the Signup button click.
     * Opens the Signup modal window.
     */
    @FXML
    private void handleSignup() {
        Logger.info("Signup button clicked from WelcomeHome");
        if (indexController != null) {
            indexController.openSignupWindow();
        } else {
            Logger.error("IndexController is null, cannot open Signup window");
        }
    }
}
