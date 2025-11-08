package co.edu.uniquindio.poo.ProyectoFinal2025_2.Services;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.VehicleType;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Vehicle;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Repositories.VehicleRepository;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.ValidationUtil;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Provides business logic services for vehicle management.
 * <p>
 * This service encapsulates operations related to vehicles, including
 * CRUD operations, availability management, and filtering by type.
 * </p>
 */
public class VehicleService {

    private static VehicleService instance;
    private final VehicleRepository vehicleRepository;

    /**
     * Package-private constructor for testing and dependency injection.
     *
     * @param vehicleRepository The VehicleRepository instance to use.
     */
    VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    /**
     * Private constructor to enforce Singleton pattern.
     */
    private VehicleService() {
        this(VehicleRepository.getInstance());
    }

    /**
     * Returns the single instance of VehicleService.
     *
     * @return The singleton instance.
     */
    public static synchronized VehicleService getInstance() {
        if (instance == null) {
            instance = new VehicleService();
        }
        return instance;
    }

    /**
     * Allows setting a custom instance for testing purposes.
     *
     * @param customInstance The custom VehicleService instance.
     */
    static void setInstance(VehicleService customInstance) {
        instance = customInstance;
    }

    /**
     * Creates and registers a new vehicle in the system.
     *
     * @param plate     The license plate of the vehicle (must be unique).
     * @param capacity  The load capacity of the vehicle in kg.
     * @param type      The type of the vehicle.
     * @param available The initial availability status.
     * @return true if the vehicle was created successfully, false if a vehicle with the same plate already exists.
     */
    public boolean createVehicle(String plate, double capacity, VehicleType type, boolean available) {
        ValidationUtil.requireNonNull(plate, "Vehicle plate cannot be null");
        ValidationUtil.requireNonNull(type, "Vehicle type cannot be null");

        // Check if vehicle with this plate already exists
        if (vehicleRepository.findByPlate(plate).isPresent()) {
            return false;
        }

        Vehicle vehicle = new Vehicle.Builder()
                .withPlate(plate)
                .withCapacity(capacity)
                .withType(type)
                .withAvailable(available)
                .build();

        vehicleRepository.addVehicle(vehicle);
        return true;
    }

    /**
     * Finds a vehicle by its license plate.
     *
     * @param plate The license plate to search for.
     * @return An Optional containing the vehicle if found, empty otherwise.
     */
    public Optional<Vehicle> findVehicleByPlate(String plate) {
        return vehicleRepository.findByPlate(plate);
    }

    /**
     * Retrieves all vehicles in the system.
     *
     * @return A list of all vehicles.
     */
    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    /**
     * Retrieves all available vehicles.
     *
     * @return A list of available vehicles.
     */
    public List<Vehicle> getAvailableVehicles() {
        return vehicleRepository.findAll().stream()
                .filter(Vehicle::isAvailable)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all vehicles of a specific type.
     *
     * @param type The type of vehicle to filter by.
     * @return A list of vehicles of the specified type.
     */
    public List<Vehicle> getVehiclesByType(VehicleType type) {
        return vehicleRepository.findAll().stream()
                .filter(vehicle -> vehicle.getType() == type)
                .collect(Collectors.toList());
    }

    /**
     * Updates the availability status of a vehicle.
     *
     * @param plate     The license plate of the vehicle to update.
     * @param available The new availability status.
     * @return true if the update was successful, false if the vehicle was not found.
     */
    public boolean updateVehicleAvailability(String plate, boolean available) {
        Optional<Vehicle> vehicleOpt = vehicleRepository.findByPlate(plate);

        if (vehicleOpt.isEmpty()) {
            return false;
        }

        Vehicle vehicle = vehicleOpt.get();
        vehicle.setAvailable(available);
        vehicleRepository.addVehicle(vehicle); // addVehicle acts as update due to HashMap
        return true;
    }

    /**
     * Updates all properties of an existing vehicle.
     *
     * @param plate    The license plate of the vehicle to update.
     * @param capacity The new capacity.
     * @param type     The new type.
     * @param available The new availability status.
     * @return true if the update was successful, false if the vehicle was not found.
     */
    public boolean updateVehicle(String plate, double capacity, VehicleType type, boolean available) {
        Optional<Vehicle> vehicleOpt = vehicleRepository.findByPlate(plate);

        if (vehicleOpt.isEmpty()) {
            return false;
        }

        Vehicle vehicle = vehicleOpt.get();
        vehicle.setCapacity(capacity);
        vehicle.setType(type);
        vehicle.setAvailable(available);
        vehicleRepository.addVehicle(vehicle);
        return true;
    }

    /**
     * Counts the total number of vehicles in the system.
     *
     * @return The total count of vehicles.
     */
    public int getTotalVehicleCount() {
        return vehicleRepository.findAll().size();
    }

    /**
     * Counts the number of available vehicles.
     *
     * @return The count of available vehicles.
     */
    public int getAvailableVehicleCount() {
        return (int) vehicleRepository.findAll().stream()
                .filter(Vehicle::isAvailable)
                .count();
    }
}
