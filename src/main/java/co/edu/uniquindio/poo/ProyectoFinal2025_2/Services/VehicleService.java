package co.edu.uniquindio.poo.ProyectoFinal2025_2.Services;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.DeliveryPerson;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.VehicleType;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Vehicle;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Repositories.DeliveryPersonRepository;
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
    private final DeliveryPersonRepository deliveryPersonRepository;

    /**
     * Package-private constructor for testing and dependency injection.
     *
     * @param vehicleRepository The VehicleRepository instance to use.
     */
    VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
        this.deliveryPersonRepository = DeliveryPersonRepository.getInstance();
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

    // ======================
    // Delivery Person Vehicle Management
    // ======================

    /**
     * Creates a vehicle for a specific delivery person.
     *
     * @param plate The license plate.
     * @param capacity The load capacity.
     * @param type The vehicle type.
     * @param available The availability status.
     * @param deliveryPersonId The owner's ID.
     * @return true if created successfully, false if plate already exists.
     */
    public boolean createVehicleForDeliveryPerson(String plate, double capacity, VehicleType type, boolean available, String deliveryPersonId) {
        ValidationUtil.requireNonNull(plate, "Vehicle plate cannot be null");
        ValidationUtil.requireNonNull(type, "Vehicle type cannot be null");
        ValidationUtil.requireNonNull(deliveryPersonId, "Delivery person ID cannot be null");

        // Check if vehicle with this plate already exists
        if (vehicleRepository.findByPlate(plate).isPresent()) {
            return false;
        }

        // Create the vehicle
        Vehicle vehicle = new Vehicle.Builder()
                .withPlate(plate)
                .withCapacity(capacity)
                .withType(type)
                .withAvailable(available)
                .withDeliveryPersonId(deliveryPersonId)
                .build();

        vehicleRepository.addVehicle(vehicle);

        // Add vehicle plate to delivery person's list
        DeliveryPerson dp = deliveryPersonRepository.getDeliveryPersonById(deliveryPersonId);
        if (dp != null) {
            dp.addVehiclePlate(plate);
            deliveryPersonRepository.updateDeliveryPerson(dp);
        }

        return true;
    }

    /**
     * Gets all vehicles owned by a specific delivery person.
     *
     * @param deliveryPersonId The delivery person's ID.
     * @return List of vehicles owned by this delivery person.
     */
    public List<Vehicle> getVehiclesByDeliveryPerson(String deliveryPersonId) {
        return vehicleRepository.findByDeliveryPersonId(deliveryPersonId);
    }

    /**
     * Gets all active (available) vehicles owned by a specific delivery person.
     *
     * @param deliveryPersonId The delivery person's ID.
     * @return List of active vehicles.
     */
    public List<Vehicle> getActiveVehiclesByDeliveryPerson(String deliveryPersonId) {
        return vehicleRepository.findByDeliveryPersonId(deliveryPersonId).stream()
                .filter(Vehicle::isAvailable)
                .collect(Collectors.toList());
    }

    /**
     * Sets a vehicle as the active vehicle for a delivery person.
     * The vehicle must be available and owned by the delivery person.
     *
     * @param deliveryPersonId The delivery person's ID.
     * @param plate The vehicle plate to set as active.
     * @return true if successful, false otherwise.
     */
    public boolean setActiveVehicle(String deliveryPersonId, String plate) {
        DeliveryPerson dp = deliveryPersonRepository.getDeliveryPersonById(deliveryPersonId);
        if (dp == null) {
            return false;
        }

        Optional<Vehicle> vehicleOpt = vehicleRepository.findByPlate(plate);
        if (vehicleOpt.isEmpty()) {
            return false;
        }

        Vehicle vehicle = vehicleOpt.get();

        // Validate ownership
        if (!deliveryPersonId.equals(vehicle.getDeliveryPersonId())) {
            return false;
        }

        // Validate availability
        if (!vehicle.isAvailable()) {
            return false;
        }

        // Set as active
        dp.setActiveVehiclePlate(plate);
        deliveryPersonRepository.updateDeliveryPerson(dp);
        return true;
    }

    /**
     * Deletes a vehicle and removes it from the delivery person's list.
     *
     * @param plate The vehicle plate to delete.
     * @return true if deleted successfully, false otherwise.
     */
    public boolean deleteVehicle(String plate) {
        Optional<Vehicle> vehicleOpt = vehicleRepository.findByPlate(plate);
        if (vehicleOpt.isEmpty()) {
            return false;
        }

        Vehicle vehicle = vehicleOpt.get();
        String deliveryPersonId = vehicle.getDeliveryPersonId();

        // Remove from repository
        boolean deleted = vehicleRepository.delete(plate);

        // Remove from delivery person's list if applicable
        if (deleted && deliveryPersonId != null) {
            DeliveryPerson dp = deliveryPersonRepository.getDeliveryPersonById(deliveryPersonId);
            if (dp != null) {
                dp.removeVehiclePlate(plate);
                deliveryPersonRepository.updateDeliveryPerson(dp);
            }
        }

        return deleted;
    }

    /**
     * Updates a vehicle's information.
     *
     * @param vehicle The vehicle to update.
     */
    public void updateVehicle(Vehicle vehicle) {
        vehicleRepository.update(vehicle);
    }
}
