package co.edu.uniquindio.poo.proyectofiinal2025_2.Services;
import co.edu.uniquindio.poo.proyectofiinal2025_2.dto.PersonCreationData;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.User;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Repositories.UserRepository;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Util.PasswordUtility;

/**
 * <p>Service that handles business logic related to customers (Users).</p>
 * <p>Uses UserRepository for persistence and provides higher-level
 * operations such as registration, ensuring that passwords are securely hashed.</p>
 */
public class UserService {

    private final UserRepository userRepository;

    /**
     * Constructs a UserService with the given repository.
     *
     * @param userRepository repository instance
     */
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ======================
    // User management
    // ======================


    /**
     * Orchestrates the registration of a new user from raw creation data.
     * <p>
     * This method handles the entire registration process:
     * 1. Validates that the email is not already in use.
     * 2. Calls the PersonFactory to create a new User object.
     * 3. Hashes the user's password for secure storage.
     * 4. Persists the new user to the repository.
     * </p>
     *
     * @param data The PersonCreationData DTO containing the user's raw information.
     * @return true if registration is successful, false if the email already exists.

    public boolean registerUser(PersonCreationData data) {
        // 1. Validar que el email no exista antes de crear nada.
        if (userRepository.findByEmail(data.getEmail()).isPresent()) {
            return false; // Email ya registrado.
        }

        // 2. Llamar a la fábrica para crear el objeto User.
        User newUser = (User) personFactory.createPerson(PersonType.USER, data);

        // 3. Hashear la contraseña del objeto recién creado.
        String hashedPassword = PasswordUtility.hashPassword(newUser.getPassword());
        newUser.setPassword(hashedPassword);

        // 4. Guardar el usuario final en el repositorio.
        userRepository.addUser(newUser);

        return true;
    }

 */

    public User signup(){
        return null;
    }

    // Other user-specific business logic methods will go here.
}
