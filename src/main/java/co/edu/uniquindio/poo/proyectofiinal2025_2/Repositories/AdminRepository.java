package co.edu.uniquindio.poo.proyectofiinal2025_2.Repositories;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Admin;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Util.Adapter.LocalDateTimeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
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

    private static final String FILE_PATH = "data/admins.json";
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .setPrettyPrinting()
            .create();

    private static AdminRepository instance;

    private final Map<String, Admin> adminsById;
    private final Map<String, Admin> adminsByEmail;

    private AdminRepository() {
        this.adminsById = new HashMap<>();
        this.adminsByEmail = new HashMap<>();
        loadFromFile();
        System.out.println("AdminRepository initialized. Admins loaded: " + adminsById.size());
    }

    public static synchronized AdminRepository getInstance() {
        if (instance == null) {
            instance = new AdminRepository();
        }
        return instance;
    }

    private void saveToFile() {
        try {
            File file = new File(FILE_PATH);
            File parentDir = file.getParentFile();

            if (parentDir != null && !parentDir.exists()) {
                if (!parentDir.mkdirs()) {
                    System.err.println("Failed to create directory: " + parentDir.getAbsolutePath());
                    return;
                }
            }

            List<Admin> adminList = new ArrayList<>(adminsById.values());
            System.out.println("Saving " + adminList.size() + " admins to file...");

            try (FileWriter writer = new FileWriter(file, false)) {
                gson.toJson(adminList, writer);
                writer.flush();
            }

            System.out.println("Admins saved successfully to " + FILE_PATH);

        } catch (IOException e) {
            System.err.println("Error saving admins to file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadFromFile() {
        File file = new File(FILE_PATH);

        if (!file.exists()) {
            System.out.println("No admins.json file found. Starting with empty repository.");
            return;
        }

        if (file.length() == 0) {
            System.out.println("admins.json file is empty. Starting with empty repository.");
            return;
        }

        try (Reader reader = new FileReader(file)) {
            Type listType = new TypeToken<ArrayList<Admin>>() {}.getType();
            List<Admin> loadedAdmins = gson.fromJson(reader, listType);

            if (loadedAdmins != null && !loadedAdmins.isEmpty()) {
                System.out.println("Loading " + loadedAdmins.size() + " admins from file...");

                for (Admin admin : loadedAdmins) {
                    if (admin != null && admin.getId() != null && admin.getEmail() != null) {
                        adminsById.put(admin.getId(), admin);
                        adminsByEmail.put(admin.getEmail().toLowerCase(), admin);
                    } else {
                        System.err.println("Warning: Skipping corrupt admin entry in JSON file");
                    }
                }

                System.out.println("Successfully loaded " + adminsById.size() + " admins");
            } else {
                System.out.println("No valid admins found in file");
            }
        } catch (Exception e) {
            System.err.println("Error loading or parsing admins from file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void addAdmin(Admin admin) {
        if (admin == null) {
            System.err.println("ERROR: Cannot add null admin");
            return;
        }

        if (admin.getId() == null) {
            System.err.println("ERROR: Admin ID is null for email: " + admin.getEmail());
            return;
        }

        if (admin.getEmail() == null) {
            System.err.println("ERROR: Admin email is null for ID: " + admin.getId());
            return;
        }

        System.out.println("Adding admin: " + admin.getEmail() + " (ID: " + admin.getId() + ")");

        adminsById.put(admin.getId(), admin);
        adminsByEmail.put(admin.getEmail().toLowerCase(), admin);

        System.out.println("Total admins in memory: " + adminsById.size());

        saveToFile();
    }

    public void removeAdmin(String adminId) {
        Admin adminToRemove = adminsById.get(adminId);
        if (adminToRemove != null) {
            System.out.println("Removing admin: " + adminToRemove.getEmail());
            adminsById.remove(adminId);
            adminsByEmail.remove(adminToRemove.getEmail().toLowerCase());
            saveToFile();
        }
    }

    public List<Admin> getAdmins() {
        return new ArrayList<>(adminsById.values());
    }

    public Optional<Admin> findByEmail(String email) {
        return Optional.ofNullable(adminsByEmail.get(email.toLowerCase()));
    }

    public Optional<Admin> findById(String id) {
        return Optional.ofNullable(adminsById.get(id));
    }

    public void printAllAdmins() {
        System.out.println("=== Current Admins in Memory ===");
        adminsById.values().forEach(admin ->
                System.out.println("- " + admin.getEmail() + " (ID: " + admin.getId() + ", Level: " + admin.getPermissionLevel() + ")")
        );
        System.out.println("Total: " + adminsById.size() + " admins");
    }
}