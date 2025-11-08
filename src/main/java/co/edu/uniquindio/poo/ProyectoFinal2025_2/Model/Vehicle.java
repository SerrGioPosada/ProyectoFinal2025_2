package co.edu.uniquindio.poo.ProyectoFinal2025_2.Model;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.VehicleType;
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

    private String plate;
    private double capacity;
    private VehicleType type;
    private boolean available;

    /**
     * Default constructor.
     */
    public Vehicle() {
    }

    /**
     * Constructs a new Vehicle.
     *
     * @param plate     vehicle license plate
     * @param capacity  load capacity in kg
     * @param type      type of vehicle
     * @param available availability status
     */
    public Vehicle(String plate, double capacity, VehicleType type, boolean available) {
        this.plate = plate;
        this.capacity = capacity;
        this.type = type;
        this.available = available;
    }

    /**
     * Private constructor for the builder pattern.
     * @param builder The builder instance to construct from.
     */
    private Vehicle(Builder builder) {
        this.plate = builder.plate;
        this.capacity = builder.capacity;
        this.type = builder.type;
        this.available = builder.available;
    }

    // ======================================
    //               BUILDER
    // ======================================

    /**
     * Static builder class for creating Vehicle instances.
     */
    public static class Builder {
        private String plate;
        private double capacity;
        private VehicleType type;
        private boolean available;

        public Builder withPlate(String plate) {
            this.plate = plate;
            return this;
        }

        public Builder withCapacity(double capacity) {
            this.capacity = capacity;
            return this;
        }

        public Builder withType(VehicleType type) {
            this.type = type;
            return this;
        }

        public Builder withAvailable(boolean available) {
            this.available = available;
            return this;
        }

        /**
         * Creates a new Vehicle instance from the builder's properties.
         * @return A new Vehicle instance.
         */
        public Vehicle build() {
            return new Vehicle(this);
        }
    }
}
