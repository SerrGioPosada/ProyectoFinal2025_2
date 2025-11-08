package co.edu.uniquindio.poo.ProyectoFinal2025_2.Repositories;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Vehicle;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilRepository.GsonProvider;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilRepository.JsonFileHandler;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilRepository.RepositoryPaths;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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
    private final Gson gson = GsonProvider.createGson();
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
        List<Vehicle> vehicleList = new ArrayList<>(vehiclesByPlate.values());
        JsonFileHandler.saveToFile(RepositoryPaths.VEHICLES_PATH, vehicleList, gson);
    }

    private void loadFromFile() {
        Type listType = new TypeToken<ArrayList<Vehicle>>() {}.getType();
        Optional<List<Vehicle>> loadedVehicles = JsonFileHandler.loadFromFile(
                RepositoryPaths.VEHICLES_PATH,
                listType,
                gson
        );

        loadedVehicles.ifPresent(vehicles -> {
            Logger.info("Loading " + vehicles.size() + " vehicles from file...");
            for (Vehicle vehicle : vehicles) {
                vehiclesByPlate.put(vehicle.getPlate().toLowerCase(), vehicle);
            }
            Logger.info("Successfully loaded " + vehiclesByPlate.size() + " vehicles");
        });
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
