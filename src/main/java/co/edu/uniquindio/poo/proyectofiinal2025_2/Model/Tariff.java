package co.edu.uniquindio.poo.proyectofiinal2025_2.Model;

import lombok.Builder;
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
@Builder

public class Tariff {

    private String id;                // Unique identifier for the tariff
    private String description;       // Description of the tariff (e.g., Standard, Express)
    private double baseCost;          // Base cost for the service
    private double costPerKilometer;  // Additional cost per kilometer
    private double costPerKilogram;   // Additional cost per kilogram of weight
    private double prioritySurcharge; // Extra cost for priority/express shipments

}
