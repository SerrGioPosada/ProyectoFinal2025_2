package co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.dto;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Data Transfer Object for shipment quote results.
 * Contains detailed cost breakdown and delivery estimates.
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class QuoteResultDTO {

    private double baseCost;
    private double weightCost;
    private double volumeCost;
    private double distanceCost;
    private double servicesCost;
    private double priorityCost;
    private double totalCost;
    private double distanceKm;
    private LocalDateTime estimatedDelivery;
    private VehicleType recommendedVehicleType; // Auto-selected vehicle type
    private Map<String, Double> breakdown;

    /**
     * Constructor initializing the breakdown map.
     */
    public QuoteResultDTO(double baseCost, double weightCost, double volumeCost,
                         double distanceCost, double servicesCost, double priorityCost,
                         double totalCost, double distanceKm, LocalDateTime estimatedDelivery) {
        this.baseCost = baseCost;
        this.weightCost = weightCost;
        this.volumeCost = volumeCost;
        this.distanceCost = distanceCost;
        this.servicesCost = servicesCost;
        this.priorityCost = priorityCost;
        this.totalCost = totalCost;
        this.distanceKm = distanceKm;
        this.estimatedDelivery = estimatedDelivery;
        this.breakdown = new HashMap<>();
        initializeBreakdown();
    }

    /**
     * Initializes the cost breakdown map with all components.
     */
    private void initializeBreakdown() {
        breakdown.put("Costo Base", baseCost);
        breakdown.put("Costo por Peso", weightCost);
        breakdown.put("Costo por Volumen", volumeCost);
        breakdown.put("Costo por Distancia", distanceCost);
        breakdown.put("Servicios Adicionales", servicesCost);
        breakdown.put("Prioridad", priorityCost);
        breakdown.put("Total", totalCost);
    }
}
