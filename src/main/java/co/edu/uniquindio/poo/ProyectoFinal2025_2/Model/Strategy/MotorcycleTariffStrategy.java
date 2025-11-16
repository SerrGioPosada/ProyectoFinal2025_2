package co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Strategy;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.VehicleType;

/**
 * A concrete strategy for calculating shipping costs specifically for motorcycles.
 * Motorcycles have lower costs but limited capacity.
 */
public class MotorcycleTariffStrategy implements TariffCalculationStrategy {

    private static final double BASE_COST = 5000.0;
    private static final double COST_PER_KM = 500.0;
    private static final double COST_PER_KG = 300.0;
    private static final double MAX_WEIGHT_KG = 30.0;
    private static final double MAX_VOLUME_M3 = 0.15;

    @Override
    public double calculateBaseCost() {
        return BASE_COST;
    }

    @Override
    public double calculateDistanceCost(double distanceKm) {
        return distanceKm * COST_PER_KM;
    }

    @Override
    public double calculateWeightCost(double weightKg) {
        if (weightKg > MAX_WEIGHT_KG) {
            throw new IllegalArgumentException(
                String.format("Weight %.2f kg exceeds motorcycle capacity of %.2f kg", weightKg, MAX_WEIGHT_KG)
            );
        }
        return weightKg * COST_PER_KG;
    }

    @Override
    public VehicleType getVehicleType() {
        return VehicleType.MOTORCYCLE;
    }

    @Override
    public double getMaxWeightCapacity() {
        return MAX_WEIGHT_KG;
    }

    @Override
    public double getMaxVolumeCapacity() {
        return MAX_VOLUME_M3;
    }
}
