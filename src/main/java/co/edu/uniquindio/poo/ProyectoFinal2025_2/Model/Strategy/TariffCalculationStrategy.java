package co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Strategy;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.VehicleType;

/**
 * The common interface for all tariff calculation algorithms in the Strategy pattern.
 *
 * <p>This pattern allows different calculation strategies for different vehicle types,
 * making it easy to add new vehicle types without modifying existing code.</p>
 */
public interface TariffCalculationStrategy {

    /**
     * Calculates the base cost for this vehicle type.
     *
     * @return The base cost
     */
    double calculateBaseCost();

    /**
     * Calculates the cost per kilometer for this vehicle type.
     *
     * @param distanceKm The distance in kilometers
     * @return The cost for the given distance
     */
    double calculateDistanceCost(double distanceKm);

    /**
     * Calculates the cost per kilogram for this vehicle type.
     *
     * @param weightKg The weight in kilograms
     * @return The cost for the given weight
     */
    double calculateWeightCost(double weightKg);

    /**
     * Gets the vehicle type this strategy is for.
     *
     * @return The vehicle type
     */
    VehicleType getVehicleType();

    /**
     * Gets the maximum weight capacity for this vehicle type.
     *
     * @return Maximum weight in kg
     */
    double getMaxWeightCapacity();

    /**
     * Gets the maximum volume capacity for this vehicle type.
     *
     * @return Maximum volume in cubic meters
     */
    double getMaxVolumeCapacity();
}
