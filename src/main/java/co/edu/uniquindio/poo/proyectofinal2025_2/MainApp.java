package co.edu.uniquindio.poo.proyectofinal2025_2;

import co.edu.uniquindio.poo.proyectofinal2025_2.Util.Seeder.AdminSeeder;
import co.edu.uniquindio.poo.proyectofinal2025_2.Util.Seeder.TariffSeeder;
import co.edu.uniquindio.poo.proyectofinal2025_2.Util.UtilModel.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main application class for JavaFX.
 * Initializes necessary data (such as default admin) and loads the initial interface.
 */
public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        // ===========================================
        // DATA INITIALIZATION
        // ===========================================

        Logger.info("╔════════════════════════════════════════╗");
        Logger.info("║   Initializing My Final Project        ║");
        Logger.info("╚════════════════════════════════════════╝");

        // Create default administrator if it doesn't exist
        AdminSeeder.seedDefaultAdmin();

        // Create default tariff if it doesn't exist
        TariffSeeder.seedDefaultTariff();

        Logger.info("==========================================");
        Logger.info("     Loading Graphical Interface!        ");
        Logger.info("==========================================");

        // ===========================================
        // INTERFACE LOADING
        // ===========================================

        FXMLLoader fxmlLoader = new FXMLLoader(
                MainApp.class.getResource("View/Index.fxml")
        );
        Scene scene = new Scene(fxmlLoader.load());

        // Main window configuration
        stage.setTitle("My Final Project 2025-2");
        stage.setScene(scene);
        stage.setMinWidth(710);
        stage.setMinHeight(400);

        // Show window
        stage.show();

        Logger.info("Application started successfully");
    }

    public static void main(String[] args) {
        launch(args);
    }
}

