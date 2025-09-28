package co.edu.uniquindio.poo.proyectofiinal2025_2.Model;

/**
 * Represents a single line item on an invoice, such as a base cost or a surcharge.
 * <p>This is an immutable value object.</p>
 */
public class LineItem {

    private final String description;
    private final double amount;

    /**
     * Constructs a new LineItem.
     *
     * @param description A description of the line item (e.g., "Base Cost").
     * @param amount      The cost associated with this line item.
     */
    public LineItem(String description, double amount) {
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
