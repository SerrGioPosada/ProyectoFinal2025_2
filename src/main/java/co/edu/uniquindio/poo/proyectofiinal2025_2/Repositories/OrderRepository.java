package co.edu.uniquindio.poo.proyectofiinal2025_2.Repositories;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Order;
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
 * Manages the persistence and retrieval of Order entities using a HashMap for fast lookups by ID.
 * <p>
 * Implements the Singleton pattern and saves data to a local JSON file.
 * </p>
 */
public class OrderRepository {

    // --- Attributes for Persistence ---
    private static final String FILE_PATH = "data/orders.json";
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    private static OrderRepository instance;
    private final Map<String, Order> ordersById;

    /**
     * Private constructor that initializes the map and loads data from the file.
     */
    private OrderRepository() {
        this.ordersById = new HashMap<>();
        loadFromFile();
    }

    /**
     * Returns the singleton instance of the repository.
     *
     * @return the unique instance of OrderRepository
     */
    public static synchronized OrderRepository getInstance() {
        if (instance == null) {
            instance = new OrderRepository();
        }
        return instance;
    }

    // ======================
    // Private file handling
    // ======================

    private void saveToFile() {
        try (Writer writer = new FileWriter(FILE_PATH)) {
            gson.toJson(ordersById.values(), writer);
        } catch (IOException e) {
            System.err.println("Error saving orders to file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadFromFile() {
        File file = new File(FILE_PATH);
        if (file.exists() && file.length() > 0) {
            try (Reader reader = new FileReader(file)) {
                Type listType = new TypeToken<ArrayList<Order>>() {}.getType();
                List<Order> loadedOrders = gson.fromJson(reader, listType);
                if (loadedOrders != null) {
                    for (Order order : loadedOrders) {
                        ordersById.put(order.getId(), order);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error loading orders from file: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // ======================
    // Handling methods
    // ======================

    public void addOrder(Order order) {
        ordersById.put(order.getId(), order);
        saveToFile();
    }

    public void update(Order newOrder){
        if (ordersById.containsKey(newOrder.getId())) {
            ordersById.put(newOrder.getId(), newOrder);
            saveToFile();
        }
    }

    // ======================
    // Query methods
    // ======================

    public Optional<Order> findById(String id) {
        return Optional.ofNullable(ordersById.get(id));
    }

    public List<Order> findAll() {
        return new ArrayList<>(ordersById.values());
    }
}