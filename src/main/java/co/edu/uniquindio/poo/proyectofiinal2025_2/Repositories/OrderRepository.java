package co.edu.uniquindio.poo.proyectofiinal2025_2.Repositories;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Order;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Util.UtilModel.Logger;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Util.UtilRepository.GsonProvider;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Util.UtilRepository.JsonFileHandler;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Util.UtilRepository.RepositoryPaths;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Util.UtilRepository.RepositoryValidator;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Manages the persistence and retrieval of Order entities using a HashMap for fast lookups by ID.
 * <p>This class is implemented as a Singleton and saves data to a local JSON file.</p>
 */
public class OrderRepository {

    // =================================================================================================================
    // CONSTANTS AND FIELDS
    // =================================================================================================================

    private final Gson gson = GsonProvider.createGson();
    private static OrderRepository instance;

    private final Map<String, Order> ordersById;

    // =================================================================================================================
    // CONSTRUCTOR (Singleton)
    // =================================================================================================================

    private OrderRepository() {
        this.ordersById = new HashMap<>();
        loadFromFile();
        Logger.info("OrderRepository initialized. Orders loaded: " + ordersById.size());
    }

    public static synchronized OrderRepository getInstance() {
        if (instance == null) {
            instance = new OrderRepository();
        }
        return instance;
    }

    // =================================================================================================================
    // FILE I/O OPERATIONS
    // =================================================================================================================

    private void saveToFile() {
        List<Order> orderList = new ArrayList<>(ordersById.values());
        JsonFileHandler.saveToFile(RepositoryPaths.ORDERS_PATH, orderList, gson);
    }

    private void loadFromFile() {
        Type listType = new TypeToken<ArrayList<Order>>() {}.getType();
        Optional<List<Order>> loadedOrders = JsonFileHandler.loadFromFile(
                RepositoryPaths.ORDERS_PATH,
                listType,
                gson
        );

        loadedOrders.ifPresent(orders -> {
            Logger.info("Loading " + orders.size() + " orders from file...");
            for (Order order : orders) {
                if (RepositoryValidator.validateEntityWithId(order, order.getId(), "Order")) {
                    ordersById.put(order.getId(), order);
                } else {
                    Logger.warning("Warning: Skipping corrupt order entry in JSON file");
                }
            }
            Logger.info("Successfully loaded " + ordersById.size() + " orders");
        });
    }

    // =================================================================================================================
    // CRUD OPERATIONS
    // =================================================================================================================

    public void addOrder(Order order) {
        if (!RepositoryValidator.validateEntityWithId(order, order.getId(), "Order")) {
            return;
        }

        Logger.info("Adding order: " + order.getId());

        ordersById.put(order.getId(), order);

        Logger.info("Total orders in memory: " + ordersById.size());

        saveToFile();
    }

    public void update(Order newOrder) {
        if (!RepositoryValidator.validateEntityWithId(newOrder, newOrder.getId(), "Order")) {
            return;
        }

        if (ordersById.containsKey(newOrder.getId())) {
            Logger.info("Updating order: " + newOrder.getId());
            ordersById.put(newOrder.getId(), newOrder);
            saveToFile();
        } else {
            Logger.warning("Cannot update order: Order with ID " + newOrder.getId() + " not found");
        }
    }

    public void removeOrder(String orderId) {
        if (!RepositoryValidator.validateId(orderId, "Order")) {
            return;
        }

        Order orderToRemove = ordersById.get(orderId);
        if (orderToRemove != null) {
            Logger.info("Removing order: " + orderId);
            ordersById.remove(orderId);
            saveToFile();
        } else {
            Logger.warning("Cannot remove order: Order with ID " + orderId + " not found");
        }
    }

    // =================================================================================================================
    // QUERY OPERATIONS
    // =================================================================================================================

    public Optional<Order> findById(String id) {
        if (!RepositoryValidator.validateId(id, "Order")) {
            return Optional.empty();
        }
        return Optional.ofNullable(ordersById.get(id));
    }

    public List<Order> findAll() {
        return new ArrayList<>(ordersById.values());
    }

    public void printAllOrders() {
        Logger.info("=== Current Orders in Memory ===");
        ordersById.values().forEach(order ->
                Logger.info("- Order ID: " + order.getId() + " (Status: " + order.getStatus() + ")")
        );
        Logger.info("Total: " + ordersById.size() + " orders");
    }
}