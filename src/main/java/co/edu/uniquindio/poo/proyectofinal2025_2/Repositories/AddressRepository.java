package co.edu.uniquindio.poo.proyectofinal2025_2.Repositories;

import co.edu.uniquindio.poo.proyectofinal2025_2.Model.Address;
import co.edu.uniquindio.poo.proyectofinal2025_2.Util.UtilModel.Logger;
import co.edu.uniquindio.poo.proyectofinal2025_2.Util.UtilRepository.GsonProvider;
import co.edu.uniquindio.poo.proyectofinal2025_2.Util.UtilRepository.JsonFileHandler;
import co.edu.uniquindio.poo.proyectofinal2025_2.Util.UtilRepository.RepositoryPaths;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Manages the persistence and retrieval of Address entities using a HashMap for fast lookups by ID.
 * <p>
 * Implements the Singleton pattern and saves data to a local JSON file.
 * </p>
 */
public class AddressRepository {

    private final Gson gson = GsonProvider.createGson();
    private static AddressRepository instance;
    private final Map<String, Address> addressesById;

    /**
     * Private constructor that initializes the map and loads data from the file.
     */
    private AddressRepository() {
        this.addressesById = new HashMap<>();
        loadFromFile();
    }

    /**
     * Returns the singleton instance of the repository.
     *
     * @return the unique instance of AddressRepository
     */
    public static synchronized AddressRepository getInstance() {
        if (instance == null) {
            instance = new AddressRepository();
        }
        return instance;
    }

    // ======================
    // Private file handling
    // ======================

    private void saveToFile() {
        List<Address> addressList = new ArrayList<>(addressesById.values());
        JsonFileHandler.saveToFile(RepositoryPaths.ADDRESSES_PATH, addressList, gson);
    }

    private void loadFromFile() {
        Type listType = new TypeToken<ArrayList<Address>>() {}.getType();
        Optional<List<Address>> loadedAddresses = JsonFileHandler.loadFromFile(
                RepositoryPaths.ADDRESSES_PATH,
                listType,
                gson
        );

        loadedAddresses.ifPresent(addresses -> {
            Logger.info("Loading " + addresses.size() + " addresses from file...");
            for (Address address : addresses) {
                addressesById.put(address.getId(), address);
            }
            Logger.info("Successfully loaded " + addressesById.size() + " addresses");
        });
    }

    // ======================
    // Handling methods
    // ======================

    public void addAddress(Address address) {
        addressesById.put(address.getId(), address);
        saveToFile();
    }

    // ======================
    // Query methods
    // ======================

    /**
     * Finds an address by its unique ID with O(1) complexity.
     *
     * @param id the ID of the address to find.
     * @return an Optional containing the address if found, or empty otherwise.
     */
    public Optional<Address> findById(String id) {
        return Optional.ofNullable(addressesById.get(id));
    }

    public List<Address> findAll() {
        return new ArrayList<>(addressesById.values());
    }
}