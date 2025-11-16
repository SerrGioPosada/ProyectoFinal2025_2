package co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Map;

/**
 * Data Transfer Object for comprehensive dashboard statistics.
 * <p>
 * This DTO aggregates all metrics required for the admin dashboard,
 * including user metrics, shipment statistics, financial data, and incident reports.
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {

    // ===========================
    // User Metrics
    // ===========================
    private long totalUsers;
    private long activeUsers;
    private long inactiveUsers;
    private long newUsersToday;
    private long newUsersThisWeek;
    private long newUsersThisMonth;

    // ===========================
    // Shipment Metrics
    // ===========================
    private long totalShipments;
    private long pendingShipments;
    private long inTransitShipments;
    private long deliveredShipments;
    private long cancelledShipments;
    private long delayedShipments;
    private double deliverySuccessRate;
    private double averageDeliveryTimeHours;

    // ===========================
    // Delivery Person Metrics
    // ===========================
    private long totalDeliveryPersons;
    private long availableDeliveryPersons;
    private long busyDeliveryPersons;
    private long offlineDeliveryPersons;

    // ===========================
    // Financial Metrics
    // ===========================
    private double totalRevenue;
    private double revenueToday;
    private double revenueThisWeek;
    private double revenueThisMonth;
    private double revenueThisYear;
    private double averageOrderValue;

    // ===========================
    // Incident Metrics
    // ===========================
    private long totalIncidents;
    private long unresolvedIncidents;
    private long resolvedIncidents;
    private Map<String, Long> incidentsByType;

    // ===========================
    // Time-Based Data for Charts
    // ===========================
    private Map<LocalDate, Long> shipmentsPerDay;
    private Map<LocalDate, Double> revenuePerDay;
    private Map<String, Long> shipmentsByStatus;
    private Map<String, Long> userActivityByDay;

    // ===========================
    // Relationship Metrics
    // ===========================
    private Map<String, Long> shipmentsPerDeliveryPerson;
    private Map<String, Double> revenuePerDeliveryPerson;
    private double averageShipmentsPerDeliveryPerson;
}
