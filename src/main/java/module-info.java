module co.edu.uniquindio.poo.proyectofiinal2025_2 {
    // JavaFX
    requires javafx.controls;
    requires javafx.fxml;

    // Java Desktop (para abrir navegador)
    requires java.desktop;

    // Seguridad
    requires jbcrypt;

    // Utilidades
    requires static lombok;
    requires com.google.gson;

    // Google OAuth2
    requires com.google.api.client;
    requires com.google.api.client.auth;
    requires com.google.api.client.json.jackson2;
    requires com.google.common;

    // Exports y Opens
    opens co.edu.uniquindio.poo.proyectofiinal2025_2.Controller to javafx.fxml;
    exports co.edu.uniquindio.poo.proyectofiinal2025_2;

    opens co.edu.uniquindio.poo.proyectofiinal2025_2.Services to com.google.gson;
    opens co.edu.uniquindio.poo.proyectofiinal2025_2.Model to com.google.gson;
    opens co.edu.uniquindio.poo.proyectofiinal2025_2.Model.dto to com.google.gson;
}