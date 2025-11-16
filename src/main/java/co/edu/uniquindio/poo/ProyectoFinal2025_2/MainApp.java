package co.edu.uniquindio.poo.ProyectoFinal2025_2;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.Seeder.AdminSeeder;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.Seeder.TariffSeeder;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.ThemeManager;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;
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
        try {
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
                    MainApp.class.getResource("/co/edu/uniquindio/poo/ProyectoFinal2025_2/View/Index.fxml")
            );
            Scene scene = new Scene(fxmlLoader.load());

            // Initialize theme manager with the scene
            ThemeManager themeManager = ThemeManager.getInstance();
            themeManager.setScene(scene);

            // Main window configuration
            stage.setTitle("Sistema de Gestión de Envíos - 2025-2");
            stage.setScene(scene);
            stage.setMinWidth(710);
            stage.setMinHeight(400);
            stage.setMaximized(true);

            // Handle application close
            stage.setOnCloseRequest(event -> {
                Logger.info("Application closing...");
            });

            // Show window
            stage.show();

            Logger.info("Application started successfully. Theme: " + themeManager.getCurrentTheme());
        } catch (Exception e) {
            Logger.error("Failed to start application: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

