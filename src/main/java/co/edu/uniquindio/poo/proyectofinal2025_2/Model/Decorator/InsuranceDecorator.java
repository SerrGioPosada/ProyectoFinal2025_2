package co.edu.uniquindio.poo.proyectofinal2025_2.Model.Decorator;

import java.util.ArrayList;
import java.util.List;

/**
 * A concrete decorator that adds the cost of insurance to a shipment.
 * <p>Insurance cost is calculated as a percentage of the base shipment value.</p>
 */
public class InsuranceDecorator implements CostCalculator {

    private final CostCalculator wrappedCalculator;
    private final double insuranceRate;

    /**
     * Constructs an insurance decorator.
     * @param wrappedCalculator The cost calculator to wrap
     * @param insuranceRate The insurance rate (e.g., 0.05 for 5%)
     */
    public InsuranceDecorator(CostCalculator wrappedCalculator, double insuranceRate) {
        if (wrappedCalculator == null) {
            throw new IllegalArgumentException("Wrapped calculator cannot be null");
        }
        if (insuranceRate < 0 || insuranceRate > 1) {
            throw new IllegalArgumentException("Insurance rate must be between 0 and 1");
        }

        this.wrappedCalculator = wrappedCalculator;
        this.insuranceRate = insuranceRate;
    }

    /**
     * Constructs an insurance decorator with default rate of 5%.
     * @param wrappedCalculator The cost calculator to wrap
     */
    public InsuranceDecorator(CostCalculator wrappedCalculator) {
        this(wrappedCalculator, 0.05); // Default 5%
    }

    @Override
    public double calculateCost() {
        double baseCost = wrappedCalculator.calculateCost();
        double insuranceCost = baseCost * insuranceRate;
        return baseCost + insuranceCost;
    }

    @Override
    public String getDescription() {
        return wrappedCalculator.getDescription() + " + Seguro";
    }

    @Override
    public List<CostBreakdownItem> getBreakdown() {
        List<CostBreakdownItem> breakdown = new ArrayList<>(wrappedCalculator.getBreakdown());
        double baseCost = wrappedCalculator.calculateCost();
        double insuranceCost = baseCost * insuranceRate;
        breakdown.add(new CostBreakdownItem(
            String.format("Seguro (%.0f%%)", insuranceRate * 100),
            insuranceCost
        ));
        return breakdown;
    }
}
