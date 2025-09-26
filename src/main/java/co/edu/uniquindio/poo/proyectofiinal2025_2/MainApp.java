package co.edu.uniquindio.poo.proyectofiinal2025_2;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(
                MainApp.class.getResource("View/Index.fxml")
        );
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Mi Proyecto Final 2025-2");
        stage.setScene(scene);
        stage.setMinWidth(710);
        stage.setMinHeight(400);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}


