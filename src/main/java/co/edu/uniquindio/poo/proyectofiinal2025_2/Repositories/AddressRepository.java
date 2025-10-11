package co.edu.uniquindio.poo.proyectofiinal2025_2.Repositories;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Address;
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
 * Manages the persistence and retrieval of Address entities using a HashMap for fast lookups by ID.
 * <p>
 * Implements the Singleton pattern and saves data to a local JSON file.
 * </p>
 */
public class AddressRepository {

    private static final String FILE_PATH = "data/addresses.json";
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

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
        try (Writer writer = new FileWriter(FILE_PATH)) {
            gson.toJson(addressesById.values(), writer);
        } catch (IOException e) {
            System.err.println("Error saving addresses to file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadFromFile() {
        File file = new File(FILE_PATH);
        if (file.exists() && file.length() > 0) {
            try (Reader reader = new FileReader(file)) {
                Type listType = new TypeToken<ArrayList<Address>>() {}.getType();
                List<Address> loadedAddresses = gson.fromJson(reader, listType);
                if (loadedAddresses != null) {
                    for (Address address : loadedAddresses) {
                        addressesById.put(address.getId(), address);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error loading addresses from file: " + e.getMessage());
                e.printStackTrace();
            }
        }
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