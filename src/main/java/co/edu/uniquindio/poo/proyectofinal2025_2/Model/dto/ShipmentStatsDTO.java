package co.edu.uniquindio.poo.proyectofinal2025_2.Model.dto;

import co.edu.uniquindio.poo.proyectofinal2025_2.Model.Enums.ServiceType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

/**
 * Data Transfer Object for shipment statistics.
 * Contains aggregated data for dashboards and reports.
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ShipmentStatsDTO {

    private long totalShipments;
    private long pendingShipments;
    private long inTransitShipments;
    private long outForDeliveryShipments;
    private long deliveredShipments;
    private long cancelledShipments;
    private long returnedShipments;
    private long incidentShipments;
    private double averageDeliveryTimeHours;
    private double successRate;
    private double totalRevenue;
    private Map<String, Long> shipmentsByZone;
    private Map<ServiceType, Long> mostUsedServices;

    /**
     * Constructor initializing the maps.
     */
    public ShipmentStatsDTO(long totalShipments, long pendingShipments, long inTransitShipments,
                           long outForDeliveryShipments, long deliveredShipments, long cancelledShipments,
                           long returnedShipments, long incidentShipments, double averageDeliveryTimeHours,
                           double successRate, double totalRevenue) {
        this.totalShipments = totalShipments;
        this.pendingShipments = pendingShipments;
        this.inTransitShipments = inTransitShipments;
        this.outForDeliveryShipments = outForDeliveryShipments;
        this.deliveredShipments = deliveredShipments;
        this.cancelledShipments = cancelledShipments;
        this.returnedShipments = returnedShipments;
        this.incidentShipments = incidentShipments;
        this.averageDeliveryTimeHours = averageDeliveryTimeHours;
        this.successRate = successRate;
        this.totalRevenue = totalRevenue;
        this.shipmentsByZone = new HashMap<>();
        this.mostUsedServices = new HashMap<>();
    }
}