package co.edu.uniquindio.poo.proyectofiinal2025_2.Services;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Admin;
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
     * Registers a new admin, hashing their password for secure storage.
     *
     * @param admin The admin object containing plain text password to register.
     * @return true if registration is successful, false if the email already exists.
     */
    public boolean registerAdmin(Admin admin) {
        if (adminRepository.findByEmail(admin.getEmail()).isPresent()) {
            return false; // Email already registered
        }

        // Security: Hash the plain text password before saving the admin.
        String hashedPassword = PasswordUtility.hashPassword(admin.getPassword());
        admin.setPassword(hashedPassword);

        adminRepository.addAdmin(admin);
        return true;
    }

    // Other admin-specific business logic methods will go here.
}
