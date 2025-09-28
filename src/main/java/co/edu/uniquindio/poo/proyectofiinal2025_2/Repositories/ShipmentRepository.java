package co.edu.uniquindio.poo.proyectofiinal2025_2.Repositories;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Shipment;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * <p>Manages the persistence and retrieval of Shipment entities.</p>
 * <p>This class is implemented as a Singleton to ensure that there is only one
 * instance managing all shipment data in the application.</p>
 */
public class ShipmentRepository {

    private static ShipmentRepository instance;
    private final List<Shipment> shipments;

    private ShipmentRepository() {
        this.shipments = new ArrayList<>();
    }

    public static synchronized ShipmentRepository getInstance() {
        if (instance == null) {
            instance = new ShipmentRepository();
        }
        return instance;
    }

    public void addShipment(Shipment shipment) {
        shipments.add(shipment);
    }

    public Optional<Shipment> findById(String id) {
        return shipments.stream()
                .filter(shipment -> shipment.getId().equals(id))
                .findFirst();
    }

    public List<Shipment> findAll() {
        return new ArrayList<>(shipments);
    }

    public void update(Shipment updatedShipment) {
        findById(updatedShipment.getId()).ifPresent(existingShipment -> {
            int index = shipments.indexOf(existingShipment);
            shipments.set(index, updatedShipment);
        });
    }

    public void delete(String id) {
        findById(id).ifPresent(shipments::remove);
    }
}
