package co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.dto;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Address;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.AdditionalService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.ServiceType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for passing order details from quote to checkout.
 * Contains all information needed to create an order and proceed to payment.
 */
@Getter
@Setter
@ToString
public class OrderDetailDTO {

    // User Information
    private String userId;

    // Address Information
    private Address origin;
    private Address destination;

    // Package Details
    private double weightKg;
    private double heightCm;
    private double widthCm;
    private double lengthCm;
    private double volumeM3;

    // Shipment Details
    private int priority;
    private String userNotes;
    private LocalDateTime requestedPickupDate;
    private LocalDateTime estimatedDelivery;

    // Services
    private List<AdditionalService> additionalServices;

    // Cost Breakdown
    private double baseCost;
    private double distanceCost;
    private double weightCost;
    private double volumeCost;
    private double servicesCost;
    private double priorityCost;
    private double totalCost;

    // Distance
    private double distanceKm;

    /**
     * Default constructor.
     */
    public OrderDetailDTO() {
    }

    /**
     * Calculates the total cost from components.
     * @return The total cost.
     */
    public double calculateTotal() {
        return baseCost + distanceCost + weightCost + volumeCost + servicesCost + priorityCost;
    }
}