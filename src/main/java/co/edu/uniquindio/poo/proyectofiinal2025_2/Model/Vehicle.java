package co.edu.uniquindio.poo.proyectofiinal2025_2.Model;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Enums.VehicleType;

/**
 * Represents a vehicle used for shipments.
 * Each vehicle has a license plate, capacity, type and availability status.
 */
public class Vehicle {

    private String plate;          // Vehicle license plate
    private double capacity;       // Capacity in kg
    private VehicleType type;      // Type of vehicle (MOTORCYCLE, CAR, TRUCK, etc.)
    private boolean available;     // Whether the vehicle is currently available

    /**
     * Constructs a new Vehicle.
     *
     * @param plate vehicle license plate
     * @param capacity load capacity in kg
     * @param type type of vehicle
     * @param available availability status
     */
    public Vehicle(String plate, double capacity, VehicleType type, boolean available) {
        this.plate = plate;
        this.capacity = capacity;
        this.type = type;
        this.available = available;
    }

    // ======================
    // Getters
    // ======================

    public String getPlate() {
        return plate;
    }

    public double getCapacity() {
        return capacity;
    }

    public VehicleType getType() {
        return type;
    }

    public boolean isAvailable() {
        return available;
    }

    // ======================
    // Setters
    // ======================

    public void setPlate(String plate) {
        this.plate = plate;
    }

    public void setCapacity(double capacity) {
        this.capacity = capacity;
    }

    public void setType(VehicleType type) {
        this.type = type;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    // ======================
    // ToString
    // ======================

    @Override
    public String toString() {
        return "Vehicle{" +
                "plate='" + plate + '\'' +
                ", capacity=" + capacity +
                ", type=" + type +
                ", available=" + available +
                '}';
    }
}
