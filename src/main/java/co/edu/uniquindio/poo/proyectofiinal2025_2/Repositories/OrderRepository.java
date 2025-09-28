package co.edu.uniquindio.poo.proyectofiinal2025_2.Repositories;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Order;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * <p>Manages the persistence and retrieval of Order entities.</p>
 * <p>This class is implemented as a Singleton to ensure that there is only one
 * instance managing all order data in the application.</p>
 */
public class OrderRepository {

    private static OrderRepository instance;
    private final List<Order> orders;

    private OrderRepository() {
        this.orders = new ArrayList<>();
    }

    public static synchronized OrderRepository getInstance() {
        if (instance == null) {
            instance = new OrderRepository();
        }
        return instance;
    }

    public void addOrder(Order order) {
        orders.add(order);
    }

    public Optional<Order> findById(String id) {
        return orders.stream()
                .filter(order -> order.getId().equals(id))
                .findFirst();
    }

    public List<Order> findAll() {
        return new ArrayList<>(orders);
    }

    public void update(Order updatedOrder) {
        findById(updatedOrder.getId()).ifPresent(existingOrder -> {
            int index = orders.indexOf(existingOrder);
            orders.set(index, updatedOrder);
        });
    }

    public void delete(String id) {
        findById(id).ifPresent(orders::remove);
    }
}
