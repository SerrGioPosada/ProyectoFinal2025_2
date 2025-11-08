package co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Decorator;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Tariff;

/**
 * The concrete base component that calculates the initial, undecorated cost of a shipment.
 * <p>This class uses the active Tariff to calculate base cost, distance cost, weight cost,
 * and volume cost. Additional services are added via decorators.</p>
 */
public class BaseShippingCost implements CostCalculator {

    private final Tariff tariff;
    private final double distanceKm;
    private final double weightKg;
    private final double volumeM3;

    /**
     * Constructs a base shipping cost calculator.
     * @param tariff The tariff to use for calculations
     * @param distanceKm Distance in kilometers
     * @param weightKg Weight in kilograms
     * @param volumeM3 Volume in cubic meters
     */
    public BaseShippingCost(Tariff tariff, double distanceKm, double weightKg, double volumeM3) {
        if (tariff == null) {
            throw new IllegalArgumentException("Tariff cannot be null");
        }
        if (distanceKm < 0 || weightKg < 0 || volumeM3 < 0) {
            throw new IllegalArgumentException("Distance, weight, and volume must be non-negative");
        }

        this.tariff = tariff;
        this.distanceKm = distanceKm;
        this.weightKg = weightKg;
        this.volumeM3 = volumeM3;
    }

    @Override
    public double calculateCost() {
        double baseCost = tariff.getBaseCost();
        double distanceCost = distanceKm * tariff.getCostPerKilometer();
        double weightCost = weightKg * tariff.getCostPerKilogram();

        // Volume cost: 50000 per cubic meter (realistic pricing)
        double volumeCost = volumeM3 * 50000.0;

        return baseCost + distanceCost + weightCost + volumeCost;
    }

    @Override
    public String getDescription() {
        return "Base Shipping Cost";
    }

    @Override
    public java.util.List<CostBreakdownItem> getBreakdown() {
        java.util.List<CostBreakdownItem> breakdown = new java.util.ArrayList<>();

        breakdown.add(new CostBreakdownItem("Costo Base", tariff.getBaseCost()));
        breakdown.add(new CostBreakdownItem(
            String.format("Distancia (%.2f km × $%.0f)", distanceKm, tariff.getCostPerKilometer()),
            distanceKm * tariff.getCostPerKilometer()
        ));
        breakdown.add(new CostBreakdownItem(
            String.format("Peso (%.2f kg × $%.0f)", weightKg, tariff.getCostPerKilogram()),
            weightKg * tariff.getCostPerKilogram()
        ));
        breakdown.add(new CostBreakdownItem(
            String.format("Volumen (%.4f m³ × $%.0f)", volumeM3, 50000.0),
            volumeM3 * 50000.0
        ));

        return breakdown;
    }

    public Tariff getTariff() {
        return tariff;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public double getWeightKg() {
        return weightKg;
    }

    public double getVolumeM3() {
        return volumeM3;
    }
}
