package co.edu.uniquindio.poo.proyectofiinal2025_2.Repositories;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Vehicle;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * <p>Manages the persistence and retrieval of Vehicle entities.</p>
 * <p>This class is implemented as a Singleton to ensure that there is only one
 * instance managing all vehicle data in the application.</p>
 */
public class VehicleRepository {

    private static VehicleRepository instance;
    private final List<Vehicle> vehicles;

    private VehicleRepository() {
        this.vehicles = new ArrayList<>();
    }

    public static synchronized VehicleRepository getInstance() {
        if (instance == null) {
            instance = new VehicleRepository();
        }
        return instance;
    }

    public void addVehicle(Vehicle vehicle) {
        vehicles.add(vehicle);
    }

    public Optional<Vehicle> findByPlate(String plate) {
        return vehicles.stream()
                .filter(vehicle -> vehicle.getPlate().equalsIgnoreCase(plate))
                .findFirst();
    }

    public List<Vehicle> findAll() {
        return new ArrayList<>(vehicles);
    }

    public void update(Vehicle updatedVehicle) {
        findByPlate(updatedVehicle.getPlate()).ifPresent(existingVehicle -> {
            int index = vehicles.indexOf(existingVehicle);
            vehicles.set(index, updatedVehicle);
        });
    }

    public void delete(String plate) {
        findByPlate(plate).ifPresent(vehicles::remove);
    }
}
