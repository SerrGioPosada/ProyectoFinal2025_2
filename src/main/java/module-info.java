module co.edu.uniquindio.poo.proyectofiinal2025_2 {
    requires javafx.controls;
    requires javafx.fxml;

    // The module name for jBCrypt is derived from its JAR file name
    requires jbcrypt;

    opens co.edu.uniquindio.poo.proyectofiinal2025_2.Controller to javafx.fxml;
    exports co.edu.uniquindio.poo.proyectofiinal2025_2;
}
