package co.edu.uniquindio.poo.proyectofiinal2025_2.Model;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Enums.VehicleType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Represents a vehicle used for shipments.
 * Each vehicle has a license plate, capacity, type and availability status.
 */
@Getter
@Setter
@ToString
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
}
