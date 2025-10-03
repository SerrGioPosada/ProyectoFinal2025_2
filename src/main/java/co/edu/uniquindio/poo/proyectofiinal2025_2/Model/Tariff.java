package co.edu.uniquindio.poo.proyectofiinal2025_2.Model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * <p>Defines the pricing rules and tariffs for calculating shipping costs.</p>
 * <p>This class holds all the variables required to calculate the final cost of a shipment,
 * including base costs, per-kilometer rates, per-kilogram rates, and surcharges for
 * different priority levels.</p>
 */
@Getter
@Setter
@ToString
public class Tariff {

    private String id;
    private String description;
    private double baseCost;
    private double costPerKilometer;
    private double costPerKilogram;
    private double prioritySurcharge;

    /**
     * Constructs a new Tariff with the specified pricing rules.
     *
     * @param id                The unique identifier for the tariff.
     * @param description       A brief description of the tariff (e.g., "Standard Rate").
     * @param baseCost          The flat base cost for any shipment.
     * @param costPerKilometer  The additional cost for each kilometer of distance.
     * @param costPerKilogram   The additional cost for each kilogram of weight.
     * @param prioritySurcharge A flat surcharge to be added for high-priority shipments.
     */
    public Tariff(String id, String description, double baseCost, double costPerKilometer, double costPerKilogram, double prioritySurcharge) {
        this.id = id;
        this.description = description;
        this.baseCost = baseCost;
        this.costPerKilometer = costPerKilometer;
        this.costPerKilogram = costPerKilogram;
        this.prioritySurcharge = prioritySurcharge;
    }
}
