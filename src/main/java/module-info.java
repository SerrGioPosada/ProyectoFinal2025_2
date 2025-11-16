module co.edu.uniquindio.poo.ProyectoFinal2025_2 {
    // JavaFX
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    // Java Desktop (para abrir navegador)
    requires java.desktop;

    // Preferencias (para ThemeManager)
    requires java.prefs;

    // Seguridad
    requires jbcrypt;

    // Utilidades
    requires static lombok;
    requires com.google.gson;

    // PDF
    requires org.apache.pdfbox;

    // Email
    requires jakarta.mail;
    requires jakarta.activation;

    // Google OAuth2 - Estas dependencias pueden que no sean de 'jpro-auth',
    // si usas jpro-auth, las correctas son 'one.jpro.platform.auth.core' y 'one.jpro.platform.auth.google'
    requires com.google.api.client;
    requires com.google.api.client.auth;
    requires com.google.api.client.json.jackson2;
    requires com.google.common;
    requires sdk.java;

    // Exports y Opens
    opens co.edu.uniquindio.poo.ProyectoFinal2025_2.Controller to javafx.fxml;
    exports co.edu.uniquindio.poo.ProyectoFinal2025_2;

    // --- Permisos para Gson ---
    opens co.edu.uniquindio.poo.ProyectoFinal2025_2.Model to com.google.gson;
    opens co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.dto to com.google.gson;
    opens co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums to com.google.gson;
}