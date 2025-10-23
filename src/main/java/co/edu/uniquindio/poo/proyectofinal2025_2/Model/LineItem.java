package co.edu.uniquindio.poo.proyectofinal2025_2.Model;

import lombok.Getter;
import lombok.ToString;

/**
 * Represents a single line item on an invoice, such as a base cost or a surcharge.
 * <p>This is an immutable value object.</p>
 */

@Getter
@ToString

public class LineItem {

    private final String description; // Description of the line item (e.g., product or service)
    private final double amount;      // Monetary amount for this line item

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
}
