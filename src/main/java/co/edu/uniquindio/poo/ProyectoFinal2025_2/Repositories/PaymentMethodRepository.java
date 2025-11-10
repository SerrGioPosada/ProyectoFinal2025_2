package co.edu.uniquindio.poo.ProyectoFinal2025_2.Repositories;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.PaymentMethod;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilRepository.GsonProvider;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilRepository.JsonFileHandler;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilRepository.RepositoryPaths;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilRepository.RepositoryValidator;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Manages the persistence and retrieval of PaymentMethod entities using HashMaps for fast lookups.
 * <p>Implements the Singleton pattern and saves data to a local JSON file.</p>
 */
public class PaymentMethodRepository {

    // =================================================================================================================
    // CONSTANTS AND FIELDS
    // =================================================================================================================

    private final Gson gson = GsonProvider.createGsonWithPrettyPrinting();
    private static PaymentMethodRepository instance;
    private final Map<String, PaymentMethod> paymentMethodsById;
    private final Map<String, List<PaymentMethod>> paymentMethodsByUserId;

    // =================================================================================================================
    // CONSTRUCTOR (Singleton)
    // =================================================================================================================

    private PaymentMethodRepository() {
        this.paymentMethodsById = new HashMap<>();
        this.paymentMethodsByUserId = new HashMap<>();
        loadFromFile();
        Logger.info("PaymentMethodRepository initialized. Payment methods loaded: " + paymentMethodsById.size());
    }

    public static synchronized PaymentMethodRepository getInstance() {
        if (instance == null) {
            instance = new PaymentMethodRepository();
        }
        return instance;
    }

    // =================================================================================================================
    // FILE I/O OPERATIONS
    // =================================================================================================================

    private void saveToFile() {
        List<PaymentMethod> paymentMethodList = new ArrayList<>(paymentMethodsById.values());
        JsonFileHandler.saveToFile(RepositoryPaths.PAYMENT_METHODS_PATH, paymentMethodList, gson);
    }

    private void loadFromFile() {
        Type listType = new TypeToken<ArrayList<PaymentMethod>>() {}.getType();
        Optional<List<PaymentMethod>> loadedPaymentMethods = JsonFileHandler.loadFromFile(
                RepositoryPaths.PAYMENT_METHODS_PATH,
                listType,
                gson
        );

        loadedPaymentMethods.ifPresent(paymentMethods -> {
            Logger.info("Loading " + paymentMethods.size() + " payment methods from file...");
            for (PaymentMethod paymentMethod : paymentMethods) {
                if (RepositoryValidator.validateEntityWithId(paymentMethod, paymentMethod.getId(), "PaymentMethod")) {
                    paymentMethodsById.put(paymentMethod.getId(), paymentMethod);

                    // Index by userId
                    paymentMethodsByUserId
                            .computeIfAbsent(paymentMethod.getUserId(), k -> new ArrayList<>())
                            .add(paymentMethod);
                } else {
                    Logger.warning("Warning: Skipping corrupt payment method entry in JSON file");
                }
            }
            Logger.info("Successfully loaded " + paymentMethodsById.size() + " payment methods");
        });
    }

    // =================================================================================================================
    // CRUD OPERATIONS
    // =================================================================================================================

    public void addPaymentMethod(PaymentMethod paymentMethod) {
        if (!RepositoryValidator.validateEntityWithId(paymentMethod, paymentMethod.getId(), "PaymentMethod")) {
            return;
        }

        Logger.info("Adding payment method with ID: " + paymentMethod.getId());
        paymentMethodsById.put(paymentMethod.getId(), paymentMethod);

        // Index by userId
        paymentMethodsByUserId
                .computeIfAbsent(paymentMethod.getUserId(), k -> new ArrayList<>())
                .add(paymentMethod);

        Logger.info("Total payment methods in memory: " + paymentMethodsById.size());
        saveToFile();
    }

    public void updatePaymentMethod(PaymentMethod paymentMethod) {
        if (!RepositoryValidator.validateEntityWithId(paymentMethod, paymentMethod.getId(), "PaymentMethod")) {
            return;
        }

        Logger.info("Updating payment method with ID: " + paymentMethod.getId());

        // Remove old entry from userId index
        PaymentMethod oldMethod = paymentMethodsById.get(paymentMethod.getId());
        if (oldMethod != null) {
            List<PaymentMethod> userMethods = paymentMethodsByUserId.get(oldMethod.getUserId());
            if (userMethods != null) {
                userMethods.removeIf(pm -> pm.getId().equals(paymentMethod.getId()));
            }
        }

        // Update with new data
        paymentMethodsById.put(paymentMethod.getId(), paymentMethod);
        paymentMethodsByUserId
                .computeIfAbsent(paymentMethod.getUserId(), k -> new ArrayList<>())
                .add(paymentMethod);

        saveToFile();
    }

    public void deletePaymentMethod(String id) {
        if (!RepositoryValidator.validateId(id, "PaymentMethod")) {
            return;
        }

        Logger.info("Deleting payment method with ID: " + id);
        PaymentMethod removedMethod = paymentMethodsById.remove(id);

        if (removedMethod != null) {
            // Remove from userId index
            List<PaymentMethod> userMethods = paymentMethodsByUserId.get(removedMethod.getUserId());
            if (userMethods != null) {
                userMethods.removeIf(pm -> pm.getId().equals(id));
            }
            saveToFile();
        }
    }

    // =================================================================================================================
    // QUERY METHODS
    // =================================================================================================================

    public Optional<PaymentMethod> findById(String id) {
        if (!RepositoryValidator.validateId(id, "PaymentMethod")) {
            return Optional.empty();
        }
        return Optional.ofNullable(paymentMethodsById.get(id));
    }

    public List<PaymentMethod> findAll() {
        return new ArrayList<>(paymentMethodsById.values());
    }

    public List<PaymentMethod> findByUserId(String userId) {
        if (!RepositoryValidator.validateId(userId, "User")) {
            return new ArrayList<>();
        }
        return paymentMethodsByUserId.getOrDefault(userId, new ArrayList<>());
    }
}
