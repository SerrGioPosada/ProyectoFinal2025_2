package co.edu.uniquindio.poo.proyectofiinal2025_2.Repositories;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Admin;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Util.UtilModel.Logger;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Util.UtilRepository.GsonProvider;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Util.UtilRepository.JsonFileHandler;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Util.UtilRepository.RepositoryPaths;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Util.UtilRepository.RepositoryValidator;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Manages the persistence and retrieval of Admin entities using HashMaps for fast lookups by ID and email.
 * <p>This class is implemented as a Singleton and saves data to a local JSON file.</p>
 */
public class AdminRepository {

    // =================================================================================================================
    // CONSTANTS AND FIELDS
    // =================================================================================================================

    private final Gson gson = GsonProvider.createGsonWithPrettyPrinting();
    private static AdminRepository instance;

    private final Map<String, Admin> adminsById;
    private final Map<String, Admin> adminsByEmail;

    // =================================================================================================================
    // CONSTRUCTOR (Singleton)
    // =================================================================================================================

    private AdminRepository() {
        this.adminsById = new HashMap<>();
        this.adminsByEmail = new HashMap<>();
        loadFromFile();
        Logger.info("AdminRepository initialized. Admins loaded: " + adminsById.size());
    }

    public static synchronized AdminRepository getInstance() {
        if (instance == null) {
            instance = new AdminRepository();
        }
        return instance;
    }

    // =================================================================================================================
    // FILE I/O OPERATIONS
    // =================================================================================================================

    private void saveToFile() {
        List<Admin> adminList = new ArrayList<>(adminsById.values());
        JsonFileHandler.saveToFile(RepositoryPaths.ADMINS_PATH, adminList, gson);
    }

    private void loadFromFile() {
        Type listType = new TypeToken<ArrayList<Admin>>() {}.getType();
        Optional<List<Admin>> loadedAdmins = JsonFileHandler.loadFromFile(
                RepositoryPaths.ADMINS_PATH,
                listType,
                gson
        );

        loadedAdmins.ifPresent(admins -> {
            Logger.info("Loading " + admins.size() + " admins from file...");
            for (Admin admin : admins) {
                if (RepositoryValidator.validateEntityWithIdAndEmail(admin, admin.getId(), admin.getEmail(), "Admin")) {
                    adminsById.put(admin.getId(), admin);
                    adminsByEmail.put(admin.getEmail().toLowerCase(), admin);
                } else {
                    Logger.warning("Warning: Skipping corrupt admin entry in JSON file");
                }
            }
            Logger.info("Successfully loaded " + adminsById.size() + " admins");
        });
    }

    // =================================================================================================================
    // CRUD OPERATIONS
    // =================================================================================================================

    public void addAdmin(Admin admin) {
        if (!RepositoryValidator.validateEntityWithIdAndEmail(admin, admin.getId(), admin.getEmail(), "Admin")) {
            return;
        }

        Logger.info("Adding admin: " + admin.getEmail() + " (ID: " + admin.getId() + ")");

        adminsById.put(admin.getId(), admin);
        adminsByEmail.put(admin.getEmail().toLowerCase(), admin);

        Logger.info("Total admins in memory: " + adminsById.size());

        saveToFile();
    }

    public void removeAdmin(String adminId) {
        if (!RepositoryValidator.validateId(adminId, "Admin")) {
            return;
        }

        Admin adminToRemove = adminsById.get(adminId);
        if (adminToRemove != null) {
            Logger.info("Removing admin: " + adminToRemove.getEmail());
            adminsById.remove(adminId);
            adminsByEmail.remove(adminToRemove.getEmail().toLowerCase());
            saveToFile();
        }
    }

    public List<Admin> getAdmins() {
        return new ArrayList<>(adminsById.values());
    }

    public Optional<Admin> findByEmail(String email) {
        if (!RepositoryValidator.validateEmail(email, "Admin")) {
            return Optional.empty();
        }
        return Optional.ofNullable(adminsByEmail.get(email.toLowerCase()));
    }

    public Optional<Admin> findById(String id) {
        if (!RepositoryValidator.validateId(id, "Admin")) {
            return Optional.empty();
        }
        return Optional.ofNullable(adminsById.get(id));
    }

    public void printAllAdmins() {
        Logger.info("=== Current Admins in Memory ===");
        adminsById.values().forEach(admin ->
                Logger.info("- " + admin.getEmail() + " (ID: " + admin.getId() + ", Level: " + admin.getPermissionLevel() + ")")
        );
        Logger.info("Total: " + adminsById.size() + " admins");
    }
}