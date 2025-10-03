package co.edu.uniquindio.poo.proyectofiinal2025_2.Repositories;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Payment;
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
 * Manages the persistence and retrieval of Payment entities using a HashMap for fast lookups by ID.
 * <p>
 * Implements the Singleton pattern and saves data to a local JSON file.
 * </p>
 */
public class PaymentRepository {

    // File path for persistence
    private static final String FILE_PATH = "data/payments.json";
    private final Gson gson = new Gson();

    private static PaymentRepository instance;
    private final Map<String, Payment> paymentsById;

    /**
     * Private constructor that initializes the map and loads data from the file.
     */
    private PaymentRepository() {
        this.paymentsById = new HashMap<>();
        loadFromFile();
    }

    /**
     * Returns the singleton instance of the repository.
     *
     * @return the unique instance of PaymentRepository
     */
    public static synchronized PaymentRepository getInstance() {
        if (instance == null) {
            instance = new PaymentRepository();
        }
        return instance;
    }

    // ======================
    // Private file handling
    // ======================

    private void saveToFile() {
        try (Writer writer = new FileWriter(FILE_PATH)) {

            gson.toJson(paymentsById.values(), writer);
        } catch (IOException e) {
            System.err.println("Error saving payments to file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadFromFile() {
        File file = new File(FILE_PATH);
        if (file.exists() && file.length() > 0) {
            try (Reader reader = new FileReader(file)) {
                Type listType = new TypeToken<ArrayList<Payment>>() {}.getType();
                List<Payment> loadedPayments = gson.fromJson(reader, listType);
                if (loadedPayments != null) {

                    for (Payment payment : loadedPayments) {
                        paymentsById.put(payment.getId(), payment);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error loading payments from file: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // ======================
    // Handling methods
    // ======================

    public void addPayment(Payment payment) {
        paymentsById.put(payment.getId(), payment);
        saveToFile();
    }

    // ======================
    // Query methods
    // ======================

    /**
     * Finds a payment by its unique ID with O(1) complexity.
     *
     * @param id the ID of the payment to find.
     * @return an Optional containing the payment if found, or empty otherwise.
     */
    public Optional<Payment> findById(String id) {
        return Optional.ofNullable(paymentsById.get(id));
    }

    /**
     * Retrieves all payments stored in the repository.
     *
     * @return a new list containing all payments
     */
    public List<Payment> findAll() {
        return new ArrayList<>(paymentsById.values());
    }
}