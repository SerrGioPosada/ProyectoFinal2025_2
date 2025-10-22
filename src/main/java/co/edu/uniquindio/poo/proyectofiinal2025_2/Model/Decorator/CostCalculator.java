package co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Decorator;

import java.util.ArrayList;
import java.util.List;

/**
 * The common interface for all components in the Decorator pattern,
 * defining the cost calculation method.
 * <p>This interface allows both base components and decorators to be
 * used interchangeably, enabling flexible cost calculation with
 * optional add-ons.</p>
 */
public interface CostCalculator {

    /**
     * Calculates the total cost.
     * @return The calculated cost
     */
    double calculateCost();

    /**
     * Gets a description of this cost component.
     * @return Description string
     */
    String getDescription();

    /**
     * Gets a detailed breakdown of all cost components.
     * @return List of cost breakdown items
     */
    default List<CostBreakdownItem> getBreakdown() {
        List<CostBreakdownItem> breakdown = new ArrayList<>();
        breakdown.add(new CostBreakdownItem(getDescription(), calculateCost()));
        return breakdown;
    }

    /**
     * Represents a single item in the cost breakdown.
     */
    class CostBreakdownItem {
        private final String description;
        private final double amount;

        public CostBreakdownItem(String description, double amount) {
            this.description = description;
            this.amount = amount;
        }

        public String getDescription() {
            return description;
        }

        public double getAmount() {
            return amount;
        }
    }
}
