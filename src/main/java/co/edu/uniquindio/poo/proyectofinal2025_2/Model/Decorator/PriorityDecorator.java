package co.edu.uniquindio.poo.proyectofinal2025_2.Model.Decorator;

import java.util.ArrayList;
import java.util.List;

/**
 * A concrete decorator that adds priority delivery cost to a shipment.
 * <p>Priority delivery ensures faster processing and delivery times.</p>
 */
public class PriorityDecorator implements CostCalculator {

    private final CostCalculator wrappedCalculator;
    private final int priorityLevel;
    private final double priorityMultiplier;

    /**
     * Constructs a priority delivery decorator.
     * @param wrappedCalculator The cost calculator to wrap
     * @param priorityLevel The priority level (1-5, where 5 is highest)
     * @param priorityMultiplier The multiplier per priority level above 3
     */
    public PriorityDecorator(CostCalculator wrappedCalculator, int priorityLevel, double priorityMultiplier) {
        if (wrappedCalculator == null) {
            throw new IllegalArgumentException("Wrapped calculator cannot be null");
        }
        if (priorityLevel < 1 || priorityLevel > 5) {
            throw new IllegalArgumentException("Priority level must be between 1 and 5");
        }
        if (priorityMultiplier < 0) {
            throw new IllegalArgumentException("Priority multiplier must be non-negative");
        }

        this.wrappedCalculator = wrappedCalculator;
        this.priorityLevel = priorityLevel;
        this.priorityMultiplier = priorityMultiplier;
    }

    /**
     * Constructs a priority delivery decorator with default multiplier of 0.1 (10% per level).
     * @param wrappedCalculator The cost calculator to wrap
     * @param priorityLevel The priority level (1-5)
     */
    public PriorityDecorator(CostCalculator wrappedCalculator, int priorityLevel) {
        this(wrappedCalculator, priorityLevel, 0.1);
    }

    @Override
    public double calculateCost() {
        double baseCost = wrappedCalculator.calculateCost();

        // Priority 3 is standard, no surcharge
        // Priority 4 adds 10%, Priority 5 adds 20%
        // Priority 1-2 could have discounts, but we keep them at base
        int levelsAboveStandard = Math.max(0, priorityLevel - 3);
        double prioritySurcharge = baseCost * priorityMultiplier * levelsAboveStandard;

        return baseCost + prioritySurcharge;
    }

    @Override
    public String getDescription() {
        String priorityName = getPriorityName(priorityLevel);
        return wrappedCalculator.getDescription() + " + Prioridad " + priorityName;
    }

    @Override
    public List<CostBreakdownItem> getBreakdown() {
        List<CostBreakdownItem> breakdown = new ArrayList<>(wrappedCalculator.getBreakdown());

        int levelsAboveStandard = Math.max(0, priorityLevel - 3);
        if (levelsAboveStandard > 0) {
            double baseCost = wrappedCalculator.calculateCost();
            double prioritySurcharge = baseCost * priorityMultiplier * levelsAboveStandard;
            String priorityName = getPriorityName(priorityLevel);
            breakdown.add(new CostBreakdownItem(
                String.format("Prioridad %s (Nivel %d, +%.0f%%)",
                    priorityName, priorityLevel, priorityMultiplier * levelsAboveStandard * 100),
                prioritySurcharge
            ));
        }

        return breakdown;
    }

    private String getPriorityName(int level) {
        return switch (level) {
            case 5 -> "Urgente";
            case 4 -> "Alta";
            case 3 -> "Normal";
            case 2 -> "Baja";
            case 1 -> "Económica";
            default -> "Normal";
        };
    }
}
