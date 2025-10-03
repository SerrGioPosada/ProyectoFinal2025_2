package co.edu.uniquindio.poo.proyectofiinal2025_2.Repositories;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Vehicle;
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
 * Manages the persistence and retrieval of Vehicle entities using a HashMap for fast lookups by plate.
 * <p>This class is implemented as a Singleton and saves data to a local JSON file.</p>
 */
public class VehicleRepository {

    // --- Attributes for Persistence ---
    private static final String FILE_PATH = "data/vehicles.json";
    private final Gson gson = new Gson();

    private static VehicleRepository instance;
    private final Map<String, Vehicle> vehiclesByPlate;

    /**
     * Private constructor that loads data from the file upon initialization.
     */
    private VehicleRepository() {
        this.vehiclesByPlate = new HashMap<>();
        loadFromFile();
    }

    public static synchronized VehicleRepository getInstance() {
        if (instance == null) {
            instance = new VehicleRepository();
        }
        return instance;
    }

    // ======================
    // Private file handling methods
    // ======================

    private void saveToFile() {
        try (Writer writer = new FileWriter(FILE_PATH)) {

            gson.toJson(vehiclesByPlate.values(), writer);
        } catch (IOException e) {
            System.err.println("Error saving vehicles to file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadFromFile() {
        File file = new File(FILE_PATH);
        if (file.exists() && file.length() > 0) {
            try (Reader reader = new FileReader(file)) {
                Type listType = new TypeToken<ArrayList<Vehicle>>() {}.getType();
                List<Vehicle> loadedVehicles = gson.fromJson(reader, listType);
                if (loadedVehicles != null) {

                    for (Vehicle vehicle : loadedVehicles) {
                        vehiclesByPlate.put(vehicle.getPlate().toLowerCase(), vehicle);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error loading vehicles from file: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // ======================
    // Handling methods
    // ======================

    public void addVehicle(Vehicle vehicle) {
        vehiclesByPlate.put(vehicle.getPlate().toLowerCase(), vehicle);
        saveToFile();
    }

    // ======================
    // Query Methods
    // ======================

    /**
     * Finds a vehicle by its license plate with O(1) complexity.
     *
     * @param plate the license plate to search for
     * @return an {@link Optional} containing the vehicle if found, or an empty Optional.
     */
    public Optional<Vehicle> findByPlate(String plate) {
        return Optional.ofNullable(vehiclesByPlate.get(plate.toLowerCase()));
    }

    /**
     * Retrieves all vehicles stored in the repository.
     *
     * @return a new list containing all vehicles in the repository
     */
    public List<Vehicle> findAll() {
        return new ArrayList<>(vehiclesByPlate.values());
    }
}