package co.edu.uniquindio.poo.proyectofiinal2025_2.Repositories;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.User;
import javafx.scene.image.Image;

import java.util.LinkedList;
import java.util.List;

public class UserRepository {

    private static UserRepository instance;
    private final List<User> users;
    private User currentUser;

    private UserRepository() {
        users = new LinkedList<>();

        // 🔹 Usuario de prueba (Admin)
        users.add(new User(
                "Admin",
                "admin@email.com",
                "1234",
                new Image(getClass().getResource(
                        "/co/edu/uniquindio/poo/proyectofiinal2025_2/Images/default-userImage.png"
                ).toExternalForm())
        ));
    }

    public static UserRepository getInstance() {
        if (instance == null) {
            instance = new UserRepository();
        }
        return instance;
    }

    // 🔹 Registrar un usuario
    public void addUser(User user) {
        users.add(user);
    }

    // 🔹 Obtener todos los usuarios
    public List<User> getUsers() {
        return users;
    }

    // 🔹 Login: valida email y password
    public boolean login(String email, String password) {
        for (User user : users) {
            if (user.getCorreo().equals(email) && user.getPassword().equals(password)) {
                currentUser = user;
                return true;
            }
        }
        return false;
    }

    // 🔹 Logout: limpia el usuario actual
    public void logout() {
        currentUser = null;
    }

    // 🔹 Obtener usuario actual
    public User getCurrentUser() {
        return currentUser;
    }
}
