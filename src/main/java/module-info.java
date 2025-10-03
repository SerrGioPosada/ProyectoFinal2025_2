module co.edu.uniquindio.poo.proyectofiinal2025_2 {
    requires javafx.controls;
    requires javafx.fxml;

    // The module name for jBCrypt is derived from its JAR file name
    requires jbcrypt;

    // Lombok is a compile-time only dependency
    requires static lombok;

    // Gson is a runtime dependency (used to read/write files)
    requires com.google.gson;

    opens co.edu.uniquindio.poo.proyectofiinal2025_2.Controller to javafx.fxml;
    exports co.edu.uniquindio.poo.proyectofiinal2025_2;
}
