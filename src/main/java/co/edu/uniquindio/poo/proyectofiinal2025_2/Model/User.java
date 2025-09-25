package co.edu.uniquindio.poo.proyectofiinal2025_2.Model;

import javafx.scene.image.Image;

/**
 * Represents a system user.
 * <p>
 * A user contains personal information such as first name, last name,
 * email, password, and an optional profile image.
 * </p>
 */
public class User {

    private String name;
    private String lastName;
    private String email;
    private String password;
    private Image profileImage;

    /**
     * Constructs a new user with the provided data.
     *
     * @param name         first name of the user
     * @param lastName     last name of the user
     * @param email        email address of the user
     * @param password     user's password
     * @param profileImage profile image of the user
     */
    public User(String name, String lastName, String email, String password, Image profileImage) {
        this.name = name;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.profileImage = profileImage;
    }

    // ======================
    // Getters
    // ======================

    public String getNombre() {
        return name;
    }

    public String getApellido() {
        return lastName;
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

    // ======================
    // Setters
    // ======================

    public void setNombre(String name) {
        this.name = name;
    }

    public void setApellido(String lastName) {
        this.lastName = lastName;
    }

    public void setCorreo(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setProfileImage(Image profileImage) {
        this.profileImage = profileImage;
    }
}
