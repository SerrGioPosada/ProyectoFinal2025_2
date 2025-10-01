package co.edu.uniquindio.poo.proyectofiinal2025_2.Services;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Admin;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Repositories.AdminRepository;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Util.PasswordUtility;
import co.edu.uniquindio.poo.proyectofiinal2025_2.dto.PersonCreationData;

/**
 * <p>Provides business logic services related to administrators.</p>
 * <p>This service class encapsulates the logic for administrator-specific
 * actions, such as registration, ensuring that passwords are securely hashed.</p>
 */
public class AdminService {

    private final AdminRepository adminRepository;

    /**
     * Constructs a new AdminService with a repository dependency.
     *
     * @param adminRepository The repository for managing administrator data.
     */
    public AdminService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    /**
     * Orchestrates the registration of a new admin from raw creation data.
     * <p>
     * This method handles the entire registration process:
     * 1. Validates that the email is not already in use.
     * 2. Calls the PersonFactory to create a new Admin object.
     * 3. Hashes the admin's password for secure storage.
     * 4. Persists the new admin to the repository.
     * </p>
     *
     * @param data The PersonCreationData DTO containing the admin's raw information.
     * @return true if registration is successful, false if the email already exists.

    public boolean registerAdmin(PersonCreationData data) {
        // 1. Validar que el email no exista.
        if (adminRepository.findByEmail(data.getEmail()).isPresent()) {
            return false; // Email ya registrado.
        }

        // 2. Llamar a la fábrica para crear el objeto Admin.
        Admin newAdmin = (Admin) personFactory.createPerson(PersonType.ADMIN, data);

        // 3. Hashear la contraseña del objeto recién creado.
        String hashedPassword = PasswordUtility.hashPassword(newAdmin.getPassword());
        newAdmin.setPassword(hashedPassword);

        // 4. Guardar el admin final en el repositorio.
        adminRepository.addAdmin(newAdmin);

        return true;
    }
*/

    // Other admin-specific business logic methods will go here.
}
