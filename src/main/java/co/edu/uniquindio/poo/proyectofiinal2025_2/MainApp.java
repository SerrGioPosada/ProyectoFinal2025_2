package co.edu.uniquindio.poo.proyectofiinal2025_2;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Util.Seeder.AdminSeeder;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Util.UtilModel.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Clase principal de la aplicación JavaFX.
 * Inicializa los datos necesarios (como el admin por defecto) y carga la interfaz inicial.
 */
public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        // ===========================================
        // INICIALIZACIÓN DE DATOS
        // ===========================================

        Logger.info("╔════════════════════════════════════════╗");
        Logger.info("║   Inicializando Mi Proyecto Final      ║");
        Logger.info("╚════════════════════════════════════════╝");

        // Crear administrador por defecto si no existe
        AdminSeeder.seedDefaultAdmin();

        Logger.info("╔════════════════════════════════════════╗");
        Logger.info("║      ¡Cargando Interfaz Gráfica!        ║");
        Logger.info("╚════════════════════════════════════════╝");

        // ===========================================
        // CARGA DE LA INTERFAZ
        // ===========================================

        FXMLLoader fxmlLoader = new FXMLLoader(
                MainApp.class.getResource("View/Index.fxml")
        );
        Scene scene = new Scene(fxmlLoader.load());

        // Configuración de la ventana principal
        stage.setTitle("Mi Proyecto Final 2025-2");
        stage.setScene(scene);
        stage.setMinWidth(710);
        stage.setMinHeight(400);

        // Mostrar ventana
        stage.show();

        Logger.info("Aplicación iniciada correctamente");
    }

    public static void main(String[] args) {
        launch(args);
    }
}

