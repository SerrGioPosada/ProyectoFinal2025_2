package co.edu.uniquindio.poo.proyectofiinal2025_2.Model;

import javafx.scene.image.Image;

public class User {
    private String name;
    private String email;
    private String password;
    private Image profileImage;

    public User(String name, String email, String password, Image profileImage) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.profileImage = profileImage;
    }

    public String getNombre() {
        return name;
    }

    public String getCorreo() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public Image getProfileImage() {
        return profileImage;
    }
}
