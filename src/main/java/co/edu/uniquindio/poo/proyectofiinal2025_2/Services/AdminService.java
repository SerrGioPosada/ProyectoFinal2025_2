package co.edu.uniquindio.poo.proyectofiinal2025_2.Services;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Admin;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Enums.PersonType;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Factory.PersonFactory;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.dto.PersonCreationData;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Repositories.AdminRepository;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Util.PasswordUtility;

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
     */
    public boolean registerAdmin(PersonCreationData data) {
        // 1. Validate that the email doesn't already exist.
        if (adminRepository.findByEmail(data.getEmail()).isPresent()) {
            return false; // Email is already registered.
        }

        // 2. Call the factory to create the Admin object.
        Admin newAdmin = (Admin) PersonFactory.createPerson(PersonType.ADMIN, data);

        // 3. Hash the password of the newly created object.
        String hashedPassword = PasswordUtility.hashPassword(newAdmin.getPassword());
        newAdmin.setPassword(hashedPassword);

        // 4. Save the final admin to the repository.
        adminRepository.addAdmin(newAdmin);

        return true;
    }


    // Other admin-specific business logic methods will go here.
}
