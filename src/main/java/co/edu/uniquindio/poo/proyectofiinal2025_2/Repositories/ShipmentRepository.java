package co.edu.uniquindio.poo.proyectofiinal2025_2.Repositories;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Shipment;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Util.UtilModel.Logger;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Util.UtilRepository.GsonProvider;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Util.UtilRepository.JsonFileHandler;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Util.UtilRepository.RepositoryPaths;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Manages the persistence and retrieval of Shipment entities using a HashMap for fast lookups by ID.
 * <p>This class is implemented as a Singleton and saves data to a local JSON file.</p>
 */
public class ShipmentRepository {

    // --- Attributes for Persistence ---
    private final Gson gson = GsonProvider.createGson();
    private static ShipmentRepository instance;
    private final Map<String, Shipment> shipmentsById;

    /**
     * Private constructor that loads data from the file upon initialization.
     */
    private ShipmentRepository() {
        this.shipmentsById = new HashMap<>();
        loadFromFile(); // Load existing shipments
    }

    /**
     * Returns the singleton instance of the repository.
     *
     * @return the unique instance of {@code ShipmentRepository}
     */
    public static synchronized ShipmentRepository getInstance() {
        if (instance == null) {
            instance = new ShipmentRepository();
        }
        return instance;
    }

    // ======================
    // Private file handling methods
    // ======================

    /**
     * Saves the current list of shipments to the shipments.json file.
     */
    private void saveToFile() {
        List<Shipment> shipmentList = new ArrayList<>(shipmentsById.values());
        JsonFileHandler.saveToFile(RepositoryPaths.SHIPMENTS_PATH, shipmentList, gson);
    }

    /**
     * Loads the list of shipments from the shipments.json file when the application starts.
     */
    private void loadFromFile() {
        Type listType = new TypeToken<ArrayList<Shipment>>() {}.getType();
        Optional<List<Shipment>> loadedShipments = JsonFileHandler.loadFromFile(
                RepositoryPaths.SHIPMENTS_PATH,
                listType,
                gson
        );

        loadedShipments.ifPresent(shipments -> {
            Logger.info("Loading " + shipments.size() + " shipments from file...");
            for (Shipment shipment : shipments) {
                shipmentsById.put(shipment.getId(), shipment);
            }
            Logger.info("Successfully loaded " + shipmentsById.size() + " shipments");
        });
    }

    // ======================
    // Handling methods
    // ======================

    /**
     * Adds a new shipment to the repository and persists the change.
     *
     * @param shipment the shipment to add
     */
    public void addShipment(Shipment shipment) {
        shipmentsById.put(shipment.getId(), shipment);
        saveToFile();
    }

    // ======================
    // Query methods
    // ======================

    /**
     * Finds a shipment by its ID with O(1) complexity.
     *
     * @param id the ID to search for
     * @return an {@link Optional} containing the shipment if found, or empty otherwise
     */
    public Optional<Shipment> findById(String id) {
        return Optional.ofNullable(shipmentsById.get(id));
    }

    /**
     * Retrieves all shipments stored in the repository.
     *
     * @return a new list containing all shipments
     */
    public List<Shipment> findAll() {
        return new ArrayList<>(shipmentsById.values());
    }
}
