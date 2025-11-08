package co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.Seeder;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Admin;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.PermissionLevel;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Repositories.AdminRepository;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilService.IdGenerationUtil;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.PasswordUtility;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;

/**
 * Seeder utility class for initializing default administrator accounts on application startup.
 *
 * <p>This seeder ensures that the system has at least one super administrator account available
 * for initial setup and administrative access. It prevents database chicken-and-egg problems
 * where you need admin access to create the first admin.</p>
 *
 * <p><b>Core Functionality:</b></p>
 * <ul>
 *     <li><b>Single Admin Seed:</b> {@link #seedDefaultAdmin()} - Creates one super admin</li>
 *     <li><b>Multi-Admin Seed:</b> {@link #seedMultipleAdmins()} - Creates admins with different permission levels (for testing)</li>
 * </ul>
 *
 * <p><b>Usage:</b></p>
 * <pre>
 * // In MainApp.start():
 * public void start(Stage stage) throws Exception {
 *     AdminSeeder.seedDefaultAdmin();  // Seed admin before loading UI
 *     // ... load FXML and show stage
 * }
 * </pre>
 *
 * <p><b>Default Credentials (MUST BE CHANGED AFTER FIRST LOGIN):</b></p>
 * <ul>
 *     <li><b>Email:</b> admin@sistema.com</li>
 *     <li><b>Password:</b> Admin123!</li>
 *     <li><b>Permission Level:</b> SUPER_ADMIN</li>
 * </ul>
 *
 * <p><b>Design Benefits:</b></p>
 * <ul>
 *     <li>Eliminates manual database setup for first admin</li>
 *     <li>Idempotent: Safe to call multiple times (checks if admin exists)</li>
 *     <li>Uses {@link IdGenerationUtil} for consistent ID generation</li>
 *     <li>Uses {@link Logger} for proper logging instead of System.out.println</li>
 *     <li>Provides multi-admin seed for development/testing environments</li>
 * </ul>
 *
 * <p><b>Security Considerations:</b></p>
 * <ul>
 *     <li>Default password MUST be changed immediately after first login</li>
 *     <li>Passwords are hashed using BCrypt via {@link PasswordUtility}</li>
 *     <li>Only seeds if no admins exist (won't override existing accounts)</li>
 * </ul>
 *
 * @author Sistema de Gestión de Envíos
 * @version 1.0
 * @since 2025
 * @see AdminRepository
 * @see PasswordUtility
 * @see IdGenerationUtil
 */
public final class AdminSeeder {

    // =================================================================================================================
    // CONSTRUCTOR
    // =================================================================================================================

    /**
     * Private constructor to prevent instantiation.
     * This is a seeder utility class with only static methods.
     *
     * @throws UnsupportedOperationException if instantiation is attempted
     */
    private AdminSeeder() {
        throw new UnsupportedOperationException("Seeder class cannot be instantiated");
    }

    // =================================================================================================================
    // SINGLE ADMIN SEEDER (Production Use)
    // =================================================================================================================

    /**
     * Creates a single default super administrator if no other admins exist in the repository.
     *
     * <p>This is the primary seeding method for production environments. It creates exactly one
     * super admin with full system access, allowing you to bootstrap the application.</p>
     *
     * <p><b>Default Credentials (CHANGE IMMEDIATELY AFTER FIRST LOGIN):</b></p>
     * <ul>
     *     <li><b>Email:</b> admin@sistema.com</li>
     *     <li><b>Password:</b> Admin123!</li>
     *     <li><b>Name:</b> Super Administrator</li>
     *     <li><b>Employee ID:</b> EMP-001</li>
     *     <li><b>Permission Level:</b> {@link PermissionLevel#SUPER_ADMIN}</li>
     * </ul>
     *
     * <p><b>Behavior:</b></p>
     * <ul>
     *     <li>If admins already exist: Logs message and returns immediately (idempotent)</li>
     *     <li>If no admins exist: Creates super admin, saves to repository, logs credentials</li>
     *     <li>ID is generated using {@link IdGenerationUtil#generateId()}</li>
     *     <li>Password is hashed using {@link PasswordUtility#hashPassword(String)}</li>
     * </ul>
     *
     * <p><b>Example Output:</b></p>
     * <pre>
     * AdminSeeder: No administrators found. Seeding default super admin...
     * AdminSeeder: Default super admin created successfully.
     *     > Email: admin@sistema.com
     *     > Password: Admin123!
     *     > IMPORTANT: Change this password after the first login!
     * </pre>
     *
     * @see #seedMultipleAdmins() for development/testing environments
     */
    public static void seedDefaultAdmin() {
        AdminRepository adminRepo = AdminRepository.getInstance();

        // Check if admins already exist (idempotent behavior)
        if (!adminRepo.getAdmins().isEmpty()) {
            Logger.info("AdminSeeder: Administrator account already exists. Skipping seed process.");
            return;
        }

        Logger.info("AdminSeeder: No administrators found. Seeding default super admin...");

        // Build super admin with default credentials
        Admin superAdmin = new Admin.Builder()
                .withId(IdGenerationUtil.generateId())
                .withName("Super")
                .withLastName("Administrator")
                .withEmail("admin@sistema.com")
                .withPhone("0000000000")
                .withPassword(PasswordUtility.hashPassword("Admin123!"))
                .withEmployeeId("EMP-001")
                .withPermissionLevel(PermissionLevel.SUPER_ADMIN)
                .build();

        // Persist to repository
        adminRepo.addAdmin(superAdmin);

        // Log success with credentials reminder
        Logger.info("AdminSeeder: Default super admin created successfully.");
        Logger.info("    > Email: admin@sistema.com");
        Logger.info("    > Password: Admin123!");
        Logger.warning("    > IMPORTANT: Change this password after the first login!");
    }

    // =================================================================================================================
    // MULTI-ADMIN SEEDER (Development/Testing Use)
    // =================================================================================================================

    /**
     * Creates a set of default administrators with different permission levels.
     *
     * <p>This method is useful for development and testing environments where you need multiple
     * admin accounts with different permission levels to test authorization logic.</p>
     *
     * <p><b>Created Accounts:</b></p>
     * <ol>
     *     <li><b>Super Admin:</b> superadmin@sistema.com / SuperAdmin123! (Full access)</li>
     *     <li><b>Standard Admin:</b> admin@sistema.com / Admin123! (Standard operations)</li>
     *     <li><b>Reports Admin:</b> reports@sistema.com / Reports123! (Read-only access)</li>
     * </ol>
     *
     * <p><b>Behavior:</b></p>
     * <ul>
     *     <li>If admins already exist: Logs message and returns immediately (idempotent)</li>
     *     <li>If no admins exist: Creates 3 admins with different {@link PermissionLevel}s</li>
     *     <li>Each admin has unique employee ID, email, and permission level</li>
     *     <li>All passwords are hashed using {@link PasswordUtility#hashPassword(String)}</li>
     * </ul>
     *
     * <p><b>Example Usage (in MainApp for testing):</b></p>
     * <pre>
     * // In development mode:
     * AdminSeeder.seedMultipleAdmins();  // Creates 3 admins for testing
     * </pre>
     *
     * <p><b>Security Warning:</b></p>
     * <ul>
     *     <li>This method is intended for development/testing ONLY</li>
     *     <li>DO NOT use in production (creates multiple default accounts)</li>
     *     <li>All default passwords MUST be changed before deployment</li>
     * </ul>
     *
     * @see PermissionLevel for available permission levels
     * @see #seedDefaultAdmin() for production use
     */
    public static void seedMultipleAdmins() {
        AdminRepository adminRepo = AdminRepository.getInstance();

        // Check if admins already exist (idempotent behavior)
        if (!adminRepo.getAdmins().isEmpty()) {
            Logger.info("AdminSeeder: Administrator accounts already exist. Skipping seed process.");
            return;
        }

        Logger.info("AdminSeeder: Seeding multiple default admin accounts...");

        // =================================================================================================================
        // 1. Super Admin (Full System Access)
        // =================================================================================================================

        Admin superAdmin = new Admin.Builder()
                .withId(IdGenerationUtil.generateId())
                .withName("Super")
                .withLastName("Admin")
                .withEmail("superadmin@sistema.com")
                .withPhone("3001234567")
                .withPassword(PasswordUtility.hashPassword("SuperAdmin123!"))
                .withEmployeeId("SA-001")
                .withPermissionLevel(PermissionLevel.SUPER_ADMIN)
                .build();
        adminRepo.addAdmin(superAdmin);
        Logger.info("    > Super Admin created: superadmin@sistema.com");

        // =================================================================================================================
        // 2. Standard Admin (Regular Administrative Operations)
        // =================================================================================================================

        Admin standardAdmin = new Admin.Builder()
                .withId(IdGenerationUtil.generateId())
                .withName("Standard")
                .withLastName("Admin")
                .withEmail("admin@sistema.com")
                .withPhone("3009876543")
                .withPassword(PasswordUtility.hashPassword("Admin123!"))
                .withEmployeeId("A-001")
                .withPermissionLevel(PermissionLevel.STANDARD)
                .build();
        adminRepo.addAdmin(standardAdmin);
        Logger.info("    > Standard Admin created: admin@sistema.com");

        // =================================================================================================================
        // 3. Reports-Only Admin (Read-Only Access)
        // =================================================================================================================

        Admin reportsAdmin = new Admin.Builder()
                .withId(IdGenerationUtil.generateId())
                .withName("Reports")
                .withLastName("Admin")
                .withEmail("reports@sistema.com")
                .withPhone("3001112233")
                .withPassword(PasswordUtility.hashPassword("Reports123!"))
                .withEmployeeId("R-001")
                .withPermissionLevel(PermissionLevel.REPORTS_ONLY)
                .build();
        adminRepo.addAdmin(reportsAdmin);
        Logger.info("    > Reports Admin created: reports@sistema.com");

        // Log security warning
        Logger.warning("\n    > IMPORTANT: Change these default passwords after the first login!");
    }
}
