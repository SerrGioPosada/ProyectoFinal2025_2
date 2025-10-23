package co.edu.uniquindio.poo.proyectofinal2025_2.Model;

import co.edu.uniquindio.poo.proyectofinal2025_2.Model.Enums.ServiceType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * Represents an additional service added to a shipment.
 * Each service has a type, cost, and optional description.
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class AdditionalService {

    private ServiceType type;
    private double cost;
    private String description;

    /**
     * Constructor for creating a service with auto-calculated cost.
     * @param type The type of service
     * @param baseCost The base cost of the shipment to calculate percentage-based costs
     */
    public AdditionalService(ServiceType type, double baseCost) {
        this.type = type;
        this.cost = type.calculateCost(baseCost);
        this.description = type.getName();
    }
}