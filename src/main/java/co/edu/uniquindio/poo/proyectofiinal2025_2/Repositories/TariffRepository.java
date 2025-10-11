package co.edu.uniquindio.poo.proyectofiinal2025_2.Repositories;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Tariff;
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
 * Manages the persistence and retrieval of Tariff entities using a HashMap for fast lookups.
 * <p>This class is implemented as a Singleton and saves data to a local JSON file.</p>
 */
public class TariffRepository {

    // --- Attributes for Persistence ---
    private static final String FILE_PATH = "data/tariffs.json";
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    private static TariffRepository instance;

    private final Map<String, Tariff> tariffsById;

    /**
     * Private constructor that loads data from the file upon initialization.
     */
    private TariffRepository() {
        this.tariffsById = new HashMap<>();
        loadFromFile();
    }

    /**
     * Returns the singleton instance of the repository.
     *
     * @return the unique instance of {@code TariffRepository}
     */
    public static synchronized TariffRepository getInstance() {
        if (instance == null) {
            instance = new TariffRepository();
        }
        return instance;
    }

    // ======================
    // Private file handling methods
    // ======================

    private void saveToFile() {
        try (Writer writer = new FileWriter(FILE_PATH)) {
            gson.toJson(tariffsById.values(), writer);
        } catch (IOException e) {
            System.err.println("Error saving tariffs to file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadFromFile() {
        File file = new File(FILE_PATH);
        if (file.exists() && file.length() > 0) {
            try (Reader reader = new FileReader(file)) {
                Type listType = new TypeToken<ArrayList<Tariff>>() {}.getType();
                List<Tariff> loadedTariffs = gson.fromJson(reader, listType);
                if (loadedTariffs != null) {
                    for (Tariff tariff : loadedTariffs) {
                        tariffsById.put(tariff.getId(), tariff);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error loading tariffs from file: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // ======================
    // Handling methods
    // ======================

    public void addTariff(Tariff tariff) {
        tariffsById.put(tariff.getId(), tariff);
        saveToFile();
    }

    // ======================
    // Query methods
    // ======================

    public Optional<Tariff> findById(String id) {
        return Optional.ofNullable(tariffsById.get(id));
    }

    public List<Tariff> findAll() {
        return new ArrayList<>(tariffsById.values());
    }
}