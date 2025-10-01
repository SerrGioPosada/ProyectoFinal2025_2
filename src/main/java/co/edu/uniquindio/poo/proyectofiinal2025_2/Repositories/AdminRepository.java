package co.edu.uniquindio.poo.proyectofiinal2025_2.Repositories;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Admin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * <p>Manages the persistence and retrieval of Admin entities.</p>
 * <p>This class is implemented as a Singleton to ensure that there is only one
 * instance managing all administrator data in the application.</p>
 */
public class AdminRepository {

    private static AdminRepository instance;
    private final List<Admin> admins;

    /**
     * Private constructor to initialize the repository. Part of the Singleton pattern.
     */
    private AdminRepository() {
        this.admins = new ArrayList<>();
    }

    /**
     * Returns the single instance of the repository.
     *
     * @return The singleton instance of AdminRepository.
     */
    public static synchronized AdminRepository getInstance() {
        if (instance == null) {
            instance = new AdminRepository();
        }
        return instance;
    }

    /**
     * Adds a new admin to the repository.
     *
     * @param admin The Admin to add.
     */
    public void addAdmin(Admin admin) {
        admins.add(admin);
    }


    /**
     * Retrieves all admins from the repository.
     *
     * @return A list of all admins.
     */
    public List<Admin> getAdmins() {
        return new ArrayList<>(admins);
    }

    /**
     * Finds an admin by their email address.
     *
     * @param email The email of the admin to find.
     * @return An Optional containing the found Admin, or empty if not found.
     */
    public Optional<Admin> findByEmail(String email) {
        return admins.stream()
                .filter(admin -> admin.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }
}
