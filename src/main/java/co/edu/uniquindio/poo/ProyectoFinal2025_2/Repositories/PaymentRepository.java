package co.edu.uniquindio.poo.ProyectoFinal2025_2.Repositories;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Payment;
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

/**
 * Manages the persistence and retrieval of Payment entities using a HashMap for fast lookups by ID.
 * <p>Implements the Singleton pattern and saves data to a local JSON file.</p>
 */
public class PaymentRepository {

    // =================================================================================================================
    // CONSTANTS AND FIELDS
    // =================================================================================================================

    private final Gson gson = GsonProvider.createGson();
    private static PaymentRepository instance;
    private final Map<String, Payment> paymentsById;

    // =================================================================================================================
    // CONSTRUCTOR (Singleton)
    // =================================================================================================================

    private PaymentRepository() {
        this.paymentsById = new HashMap<>();
        loadFromFile();
        Logger.info("PaymentRepository initialized. Payments loaded: " + paymentsById.size());
    }

    public static synchronized PaymentRepository getInstance() {
        if (instance == null) {
            instance = new PaymentRepository();
        }
        return instance;
    }

    // =================================================================================================================
    // FILE I/O OPERATIONS
    // =================================================================================================================

    private void saveToFile() {
        List<Payment> paymentList = new ArrayList<>(paymentsById.values());
        JsonFileHandler.saveToFile(RepositoryPaths.PAYMENTS_PATH, paymentList, gson);
    }

    private void loadFromFile() {
        Type listType = new TypeToken<ArrayList<Payment>>() {}.getType();
        Optional<List<Payment>> loadedPayments = JsonFileHandler.loadFromFile(
                RepositoryPaths.PAYMENTS_PATH,
                listType,
                gson
        );

        loadedPayments.ifPresent(payments -> {
            Logger.info("Loading " + payments.size() + " payments from file...");
            for (Payment payment : payments) {
                if (RepositoryValidator.validateEntityWithId(payment, payment.getId(), "Payment")) {
                    paymentsById.put(payment.getId(), payment);
                } else {
                    Logger.warning("Warning: Skipping corrupt payment entry in JSON file");
                }
            }
            Logger.info("Successfully loaded " + paymentsById.size() + " payments");
        });
    }

    // =================================================================================================================
    // CRUD OPERATIONS
    // =================================================================================================================

    public void addPayment(Payment payment) {
        if (!RepositoryValidator.validateEntityWithId(payment, payment.getId(), "Payment")) {
            return;
        }

        Logger.info("Adding payment with ID: " + payment.getId());
        paymentsById.put(payment.getId(), payment);
        Logger.info("Total payments in memory: " + paymentsById.size());
        saveToFile();
    }

    // =================================================================================================================
    // QUERY METHODS
    // =================================================================================================================

    public Optional<Payment> findById(String id) {
        if (!RepositoryValidator.validateId(id, "Payment")) {
            return Optional.empty();
        }
        return Optional.ofNullable(paymentsById.get(id));
    }

    public List<Payment> findAll() {
        return new ArrayList<>(paymentsById.values());
    }

    public Optional<Payment> findByInvoiceId(String invoiceId) {
        if (!RepositoryValidator.validateId(invoiceId, "Invoice")) {
            return Optional.empty();
        }
        return paymentsById.values().stream()
                .filter(payment -> payment.getInvoiceId().equals(invoiceId))
                .findFirst();
    }
}
