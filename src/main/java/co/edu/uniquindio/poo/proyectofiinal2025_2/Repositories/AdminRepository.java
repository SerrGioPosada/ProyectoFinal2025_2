package co.edu.uniquindio.poo.proyectofiinal2025_2.Repositories;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Admin;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Manages the persistence and retrieval of Admin entities using HashMaps for fast lookups.
 * <p>
 * Implements the Singleton pattern and saves data to a local JSON file.
 * </p>
 */
public class AdminRepository {

    private static final String FILE_PATH = "data/admins.json";
    private final Gson gson = new Gson();

    private static AdminRepository instance;
    private final Map<String, Admin> adminsById;
    private final Map<String, Admin> adminsByEmail;

    /**
     * Private constructor that initializes the maps and loads data from the file.
     */
    private AdminRepository() {
        this.adminsById = new HashMap<>();
        this.adminsByEmail = new HashMap<>();
        loadFromFile();
    }

    /**
     * Returns the singleton instance of the repository.
     *
     * @return the unique instance of AdminRepository
     */
    public static synchronized AdminRepository getInstance() {
        if (instance == null) {
            instance = new AdminRepository();
        }
        return instance;
    }

    // ======================
    // Private file handling
    // ======================

    private void saveToFile() {
        try (Writer writer = new FileWriter(FILE_PATH)) {

            gson.toJson(adminsById.values(), writer);
        } catch (IOException e) {
            System.err.println("Error saving admins to file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadFromFile() {
        File file = new File(FILE_PATH);
        if (file.exists() && file.length() > 0) {
            try (Reader reader = new FileReader(file)) {
                Type listType = new TypeToken<ArrayList<Admin>>() {}.getType();
                List<Admin> loadedAdmins = gson.fromJson(reader, listType);
                if (loadedAdmins != null) {

                    for (Admin admin : loadedAdmins) {
                        adminsById.put(admin.getId(), admin);
                        adminsByEmail.put(admin.getEmail().toLowerCase(), admin);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error loading admins from file: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // ======================
    // Handling methods
    // ======================

    public void addAdmin(Admin admin) {

        adminsById.put(admin.getId(), admin);
        adminsByEmail.put(admin.getEmail().toLowerCase(), admin);
        saveToFile();
    }

    // ======================
    // Query methods
    // ======================

    public List<Admin> getAdmins() {
        return new ArrayList<>(adminsById.values());
    }

    public Optional<Admin> findByEmail(String email) {
        return Optional.ofNullable(adminsByEmail.get(email.toLowerCase()));
    }
}
