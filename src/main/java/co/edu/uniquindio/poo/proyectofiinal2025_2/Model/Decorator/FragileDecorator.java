package co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Decorator;

import java.util.ArrayList;
import java.util.List;

/**
 * A concrete decorator that adds fragile handling cost to a shipment.
 * <p>Fragile handling requires special care and packaging.</p>
 */
public class FragileDecorator implements CostCalculator {

    private final CostCalculator wrappedCalculator;
    private final double fragileSurcharge;

    /**
     * Constructs a fragile handling decorator.
     * @param wrappedCalculator The cost calculator to wrap
     * @param fragileSurcharge The fixed surcharge for fragile handling
     */
    public FragileDecorator(CostCalculator wrappedCalculator, double fragileSurcharge) {
        if (wrappedCalculator == null) {
            throw new IllegalArgumentException("Wrapped calculator cannot be null");
        }
        if (fragileSurcharge < 0) {
            throw new IllegalArgumentException("Fragile surcharge must be non-negative");
        }

        this.wrappedCalculator = wrappedCalculator;
        this.fragileSurcharge = fragileSurcharge;
    }

    /**
     * Constructs a fragile handling decorator with default surcharge of $15,000.
     * @param wrappedCalculator The cost calculator to wrap
     */
    public FragileDecorator(CostCalculator wrappedCalculator) {
        this(wrappedCalculator, 15000.0);
    }

    @Override
    public double calculateCost() {
        return wrappedCalculator.calculateCost() + fragileSurcharge;
    }

    @Override
    public String getDescription() {
        return wrappedCalculator.getDescription() + " + Manejo Frágil";
    }

    @Override
    public List<CostBreakdownItem> getBreakdown() {
        List<CostBreakdownItem> breakdown = new ArrayList<>(wrappedCalculator.getBreakdown());
        breakdown.add(new CostBreakdownItem("Manejo Frágil", fragileSurcharge));
        return breakdown;
    }
}
