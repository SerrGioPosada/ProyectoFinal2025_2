package co.edu.uniquindio.poo.ProyectoFinal2025_2.Repositories;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Tariff;
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
 * Manages the persistence and retrieval of Tariff entities using a HashMap for fast lookups.
 * <p>This class is implemented as a Singleton and saves data to a local JSON file.</p>
 */
public class TariffRepository {

    // --- Attributes for Persistence ---
    private final Gson gson = GsonProvider.createGson();
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
        List<Tariff> tariffList = new ArrayList<>(tariffsById.values());
        JsonFileHandler.saveToFile(RepositoryPaths.TARIFFS_PATH, tariffList, gson);
    }

    private void loadFromFile() {
        Type listType = new TypeToken<ArrayList<Tariff>>() {}.getType();
        Optional<List<Tariff>> loadedTariffs = JsonFileHandler.loadFromFile(
                RepositoryPaths.TARIFFS_PATH,
                listType,
                gson
        );

        loadedTariffs.ifPresent(tariffs -> {
            Logger.info("Loading " + tariffs.size() + " tariffs from file...");
            for (Tariff tariff : tariffs) {
                tariffsById.put(tariff.getId(), tariff);
            }
            Logger.info("Successfully loaded " + tariffsById.size() + " tariffs");
        });
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
