package co.edu.uniquindio.poo.proyectofiinal2025_2.Util;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Admin;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Enums.PermissionLevel;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Repositories.AdminRepository;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Util.UtilModel.PasswordUtility;

import java.util.UUID;

/**
 * Utility class to seed the database with default administrator accounts upon application startup.
 * <p>
 * This ensures that the system is always accessible for administrative purposes, especially on the first run.
 * This is a non-instantiable utility class.
 * </p>
 */
public final class AdminSeeder {

    // =================================================================================================================
    // Constructor
    // =================================================================================================================

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private AdminSeeder() {
        // This class should not be instantiated.
    }

    // =================================================================================================================
    // Public Static API
    // =================================================================================================================

    /**
     * Creates a single default super administrator if no other admins exist in the repository.
     * <p>
     * Default credentials:
     * <ul>
     *     <li>Email: admin@sistema.com</li>
     *     <li>Password: Admin123!</li>
     * </ul>
     * </p>
     */
    public static void seedDefaultAdmin() {
        AdminRepository adminRepo = AdminRepository.getInstance();

        if (!adminRepo.getAdmins().isEmpty()) {
            System.out.println("AdminSeeder: Administrator account already exists. Skipping seed process.");
            return;
        }

        System.out.println("AdminSeeder: No administrators found. Seeding default super admin...");

        Admin superAdmin = new Admin.Builder()
                .withId(UUID.randomUUID().toString())
                .withName("Super")
                .withLastName("Administrator")
                .withEmail("admin@sistema.com")
                .withPhone("0000000000")
                .withPassword(PasswordUtility.hashPassword("Admin123!"))
                .withEmployeeId("EMP-001")
                .withPermissionLevel(PermissionLevel.SUPER_ADMIN)
                .build();

        adminRepo.addAdmin(superAdmin);

        System.out.println("AdminSeeder: Default super admin created successfully.");
        System.out.println("    > Email: admin@sistema.com");
        System.out.println("    > Password: Admin123!");
        System.out.println("    > IMPORTANT: Change this password after the first login!");
    }

    /**
     * (Alternative) Creates a set of default administrators with different permission levels.
     * This method is useful for development and testing environments.
     */
    public static void seedMultipleAdmins() {
        AdminRepository adminRepo = AdminRepository.getInstance();

        if (!adminRepo.getAdmins().isEmpty()) {
            System.out.println("AdminSeeder: Administrator accounts already exist. Skipping seed process.");
            return;
        }

        System.out.println("AdminSeeder: Seeding multiple default admin accounts...");

        // Super Admin
        Admin superAdmin = new Admin.Builder()
                .withId(UUID.randomUUID().toString())
                .withName("Super")
                .withLastName("Admin")
                .withEmail("superadmin@sistema.com")
                .withPhone("3001234567")
                .withPassword(PasswordUtility.hashPassword("SuperAdmin123!"))
                .withEmployeeId("SA-001")
                .withPermissionLevel(PermissionLevel.SUPER_ADMIN)
                .build();
        adminRepo.addAdmin(superAdmin);
        System.out.println("    > Super Admin created: superadmin@sistema.com");

        // Standard Admin
        Admin standardAdmin = new Admin.Builder()
                .withId(UUID.randomUUID().toString())
                .withName("Standard")
                .withLastName("Admin")
                .withEmail("admin@sistema.com")
                .withPhone("3009876543")
                .withPassword(PasswordUtility.hashPassword("Admin123!"))
                .withEmployeeId("A-001")
                .withPermissionLevel(PermissionLevel.STANDARD)
                .build();
        adminRepo.addAdmin(standardAdmin);
        System.out.println("    > Standard Admin created: admin@sistema.com");

        // Reports Only Admin
        Admin reportsAdmin = new Admin.Builder()
                .withId(UUID.randomUUID().toString())
                .withName("Reports")
                .withLastName("Admin")
                .withEmail("reports@sistema.com")
                .withPhone("3001112233")
                .withPassword(PasswordUtility.hashPassword("Reports123!"))
                .withEmployeeId("R-001")
                .withPermissionLevel(PermissionLevel.REPORTS_ONLY)
                .build();
        adminRepo.addAdmin(reportsAdmin);
        System.out.println("    > Reports Admin created: reports@sistema.com");

        System.out.println("\n    > IMPORTANT: Change these default passwords after the first login!");
    }
}
