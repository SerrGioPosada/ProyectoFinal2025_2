package co.edu.uniquindio.poo.proyectofinal2025_2.Services;

import co.edu.uniquindio.poo.proyectofinal2025_2.Model.*;
import co.edu.uniquindio.poo.proyectofinal2025_2.Model.Enums.AvailabilityStatus;
import co.edu.uniquindio.poo.proyectofinal2025_2.Model.Enums.ShipmentStatus;
import co.edu.uniquindio.poo.proyectofinal2025_2.Model.dto.ChartDataDTO;
import co.edu.uniquindio.poo.proyectofinal2025_2.Model.dto.DashboardStatsDTO;
import co.edu.uniquindio.poo.proyectofinal2025_2.Repositories.*;
import co.edu.uniquindio.poo.proyectofinal2025_2.Util.UtilModel.Logger;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for calculating and aggregating dashboard statistics.
 * <p>
 * This service provides comprehensive metrics for the admin dashboard,
 * including user activity, shipment statistics, financial data, and delivery person performance.
 * All calculations are performed in real-time based on current repository data.
 * </p>
 */
public class DashboardService {

    private final UserRepository userRepository;
    private final ShipmentRepository shipmentRepository;
    private final DeliveryPersonRepository deliveryPersonRepository;
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;

    /**
     * Constructor with dependency injection.
     * @param userRepository User repository instance
     * @param shipmentRepository Shipment repository instance
     * @param deliveryPersonRepository Delivery person repository instance
     * @param paymentRepository Payment repository instance
     * @param invoiceRepository Invoice repository instance
     */
    public DashboardService(UserRepository userRepository,
                           ShipmentRepository shipmentRepository,
                           DeliveryPersonRepository deliveryPersonRepository,
                           PaymentRepository paymentRepository,
                           InvoiceRepository invoiceRepository) {
        this.userRepository = userRepository;
        this.shipmentRepository = shipmentRepository;
        this.deliveryPersonRepository = deliveryPersonRepository;
        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
    }

    /**
     * Default constructor using singleton instances.
     */
    public DashboardService() {
        this(UserRepository.getInstance(),
             ShipmentRepository.getInstance(),
             DeliveryPersonRepository.getInstance(),
             PaymentRepository.getInstance(),
             InvoiceRepository.getInstance());
    }

    // ===========================
    // Main Dashboard Statistics
    // ===========================

    /**
     * Calculates comprehensive dashboard statistics.
     * <p>
     * This method aggregates all metrics needed for the admin dashboard,
     * including user counts, shipment statistics, financial data, and delivery person metrics.
     * </p>
     * @return DashboardStatsDTO containing all dashboard metrics
     */
    public DashboardStatsDTO calculateDashboardStats() {
        Logger.info("Calculating dashboard statistics...");

        DashboardStatsDTO stats = new DashboardStatsDTO();

        // Calculate user metrics
        calculateUserMetrics(stats);

        // Calculate shipment metrics
        calculateShipmentMetrics(stats);

        // Calculate delivery person metrics
        calculateDeliveryPersonMetrics(stats);

        // Calculate financial metrics
        calculateFinancialMetrics(stats);

        // Calculate incident metrics
        calculateIncidentMetrics(stats);

        // Calculate time-based data for charts
        calculateTimeBasedData(stats);

        // Calculate relationship metrics
        calculateRelationshipMetrics(stats);

        Logger.info("Dashboard statistics calculated successfully");
        return stats;
    }

    // ===========================
    // User Metrics
    // ===========================

    /**
     * Calculates user-related metrics.
     * @param stats DashboardStatsDTO to populate
     */
    private void calculateUserMetrics(DashboardStatsDTO stats) {
        List<User> allUsers = userRepository.getUsers();

        stats.setTotalUsers(allUsers.size());
        stats.setActiveUsers(allUsers.stream().filter(User::isActive).count());
        stats.setInactiveUsers(allUsers.stream().filter(u -> !u.isActive()).count());

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();
        LocalDateTime startOfWeek = now.minusWeeks(1);
        LocalDateTime startOfMonth = now.minusMonths(1);

        // Note: User entity doesn't have registrationDate field yet
        // These metrics will be 0 until the field is added to the User model
        stats.setNewUsersToday(0);
        stats.setNewUsersThisWeek(0);
        stats.setNewUsersThisMonth(0);
    }

    // ===========================
    // Shipment Metrics
    // ===========================

    /**
     * Calculates shipment-related metrics.
     * @param stats DashboardStatsDTO to populate
     */
    private void calculateShipmentMetrics(DashboardStatsDTO stats) {
        List<Shipment> allShipments = shipmentRepository.findAll();
        List<Shipment> activeShipments = allShipments.stream()
            .filter(Shipment::isActive)
            .collect(Collectors.toList());

        stats.setTotalShipments(activeShipments.size());

        stats.setPendingShipments(activeShipments.stream()
            .filter(s -> s.getStatus() == ShipmentStatus.PENDING_ASSIGNMENT)
            .count());

        stats.setInTransitShipments(activeShipments.stream()
            .filter(s -> s.getStatus() == ShipmentStatus.IN_TRANSIT ||
                        s.getStatus() == ShipmentStatus.OUT_FOR_DELIVERY)
            .count());

        stats.setDeliveredShipments(activeShipments.stream()
            .filter(s -> s.getStatus() == ShipmentStatus.DELIVERED)
            .count());

        stats.setCancelledShipments(allShipments.stream()
            .filter(s -> s.getStatus() == ShipmentStatus.CANCELLED)
            .count());

        // Calculate delayed shipments (estimated date passed but not delivered)
        LocalDateTime now = LocalDateTime.now();
        stats.setDelayedShipments(activeShipments.stream()
            .filter(s -> s.getEstimatedDate() != null &&
                        s.getEstimatedDate().isBefore(now) &&
                        s.getStatus() != ShipmentStatus.DELIVERED)
            .count());

        // Calculate delivery success rate
        long deliveredCount = stats.getDeliveredShipments();
        long completedShipments = deliveredCount + stats.getCancelledShipments();
        stats.setDeliverySuccessRate(
            completedShipments > 0 ? (double) deliveredCount / completedShipments * 100 : 0.0
        );

        // Calculate average delivery time
        double avgDeliveryTime = activeShipments.stream()
            .filter(s -> s.getStatus() == ShipmentStatus.DELIVERED)
            .filter(s -> s.getCreatedAt() != null && s.getDeliveredDate() != null)
            .mapToDouble(s -> Duration.between(s.getCreatedAt(), s.getDeliveredDate()).toHours())
            .average()
            .orElse(0.0);
        stats.setAverageDeliveryTimeHours(avgDeliveryTime);
    }

    // ===========================
    // Delivery Person Metrics
    // ===========================

    /**
     * Calculates delivery person-related metrics.
     * @param stats DashboardStatsDTO to populate
     */
    private void calculateDeliveryPersonMetrics(DashboardStatsDTO stats) {
        List<DeliveryPerson> allDeliveryPersons = deliveryPersonRepository.getAllDeliveryPersons();

        stats.setTotalDeliveryPersons(allDeliveryPersons.size());

        stats.setAvailableDeliveryPersons(allDeliveryPersons.stream()
            .filter(dp -> dp.getAvailability() == AvailabilityStatus.AVAILABLE)
            .count());

        stats.setBusyDeliveryPersons(allDeliveryPersons.stream()
            .filter(dp -> dp.getAvailability() == AvailabilityStatus.IN_TRANSIT)
            .count());

        stats.setOfflineDeliveryPersons(allDeliveryPersons.stream()
            .filter(dp -> dp.getAvailability() == AvailabilityStatus.INACTIVE)
            .count());
    }

    // ===========================
    // Financial Metrics
    // ===========================

    /**
     * Calculates financial metrics.
     * @param stats DashboardStatsDTO to populate
     */
    private void calculateFinancialMetrics(DashboardStatsDTO stats) {
        List<Shipment> allShipments = shipmentRepository.findAll();
        List<Shipment> completedShipments = allShipments.stream()
            .filter(s -> s.getStatus() != ShipmentStatus.CANCELLED)
            .collect(Collectors.toList());

        // Total revenue from all non-cancelled shipments
        double totalRevenue = completedShipments.stream()
            .mapToDouble(Shipment::getTotalCost)
            .sum();
        stats.setTotalRevenue(totalRevenue);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();
        LocalDateTime startOfWeek = now.minusWeeks(1);
        LocalDateTime startOfMonth = now.minusMonths(1);
        LocalDateTime startOfYear = now.minusYears(1);

        // Revenue today
        stats.setRevenueToday(completedShipments.stream()
            .filter(s -> s.getCreatedAt() != null && s.getCreatedAt().isAfter(startOfToday))
            .mapToDouble(Shipment::getTotalCost)
            .sum());

        // Revenue this week
        stats.setRevenueThisWeek(completedShipments.stream()
            .filter(s -> s.getCreatedAt() != null && s.getCreatedAt().isAfter(startOfWeek))
            .mapToDouble(Shipment::getTotalCost)
            .sum());

        // Revenue this month
        stats.setRevenueThisMonth(completedShipments.stream()
            .filter(s -> s.getCreatedAt() != null && s.getCreatedAt().isAfter(startOfMonth))
            .mapToDouble(Shipment::getTotalCost)
            .sum());

        // Revenue this year
        stats.setRevenueThisYear(completedShipments.stream()
            .filter(s -> s.getCreatedAt() != null && s.getCreatedAt().isAfter(startOfYear))
            .mapToDouble(Shipment::getTotalCost)
            .sum());

        // Average order value
        stats.setAverageOrderValue(
            completedShipments.isEmpty() ? 0.0 : totalRevenue / completedShipments.size()
        );
    }

    // ===========================
    // Incident Metrics
    // ===========================

    /**
     * Calculates incident-related metrics.
     * @param stats DashboardStatsDTO to populate
     */
    private void calculateIncidentMetrics(DashboardStatsDTO stats) {
        List<Shipment> allShipments = shipmentRepository.findAll();
        List<Incident> allIncidents = allShipments.stream()
            .map(Shipment::getIncident)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        stats.setTotalIncidents(allIncidents.size());

        stats.setUnresolvedIncidents(allIncidents.stream()
            .filter(i -> !i.isResolved())
            .count());

        stats.setResolvedIncidents(allIncidents.stream()
            .filter(Incident::isResolved)
            .count());

        // Group incidents by type
        Map<String, Long> incidentsByType = allIncidents.stream()
            .collect(Collectors.groupingBy(
                i -> i.getType() != null ? i.getType().toString() : "UNKNOWN",
                Collectors.counting()
            ));
        stats.setIncidentsByType(incidentsByType);
    }

    // ===========================
    // Time-Based Data for Charts
    // ===========================

    /**
     * Calculates time-based data for charts.
     * @param stats DashboardStatsDTO to populate
     */
    private void calculateTimeBasedData(DashboardStatsDTO stats) {
        List<Shipment> allShipments = shipmentRepository.findAll();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);

        // Shipments per day (last 30 days)
        Map<LocalDate, Long> shipmentsPerDay = new LinkedHashMap<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            LocalDate currentDate = date;
            long count = allShipments.stream()
                .filter(s -> s.getCreatedAt() != null &&
                            s.getCreatedAt().toLocalDate().equals(currentDate))
                .count();
            shipmentsPerDay.put(date, count);
        }
        stats.setShipmentsPerDay(shipmentsPerDay);

        // Revenue per day (last 30 days)
        Map<LocalDate, Double> revenuePerDay = new LinkedHashMap<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            LocalDate currentDate = date;
            double revenue = allShipments.stream()
                .filter(s -> s.getCreatedAt() != null &&
                            s.getCreatedAt().toLocalDate().equals(currentDate) &&
                            s.getStatus() != ShipmentStatus.CANCELLED)
                .mapToDouble(Shipment::getTotalCost)
                .sum();
            revenuePerDay.put(date, revenue);
        }
        stats.setRevenuePerDay(revenuePerDay);

        // Shipments by status
        Map<String, Long> shipmentsByStatus = allShipments.stream()
            .filter(Shipment::isActive)
            .collect(Collectors.groupingBy(
                s -> s.getStatus() != null ? s.getStatus().toString() : "UNKNOWN",
                Collectors.counting()
            ));
        stats.setShipmentsByStatus(shipmentsByStatus);

        // User activity by day (registrations per day in last 30 days)
        // Note: User entity doesn't have registrationDate field yet
        // This metric will be empty until the field is added
        Map<String, Long> userActivityByDay = new LinkedHashMap<>();
        stats.setUserActivityByDay(userActivityByDay);
    }

    // ===========================
    // Relationship Metrics
    // ===========================

    /**
     * Calculates metrics showing relationships between entities.
     * @param stats DashboardStatsDTO to populate
     */
    private void calculateRelationshipMetrics(DashboardStatsDTO stats) {
        List<Shipment> allShipments = shipmentRepository.findAll();
        List<DeliveryPerson> allDeliveryPersons = deliveryPersonRepository.getAllDeliveryPersons();

        // Shipments per delivery person
        Map<String, Long> shipmentsPerDP = allShipments.stream()
            .filter(s -> s.getDeliveryPersonId() != null)
            .collect(Collectors.groupingBy(
                Shipment::getDeliveryPersonId,
                Collectors.counting()
            ));

        // Replace IDs with names
        Map<String, Long> shipmentsPerDPWithNames = new LinkedHashMap<>();
        shipmentsPerDP.forEach((dpId, count) -> {
            deliveryPersonRepository.findDeliveryPersonById(dpId).ifPresent(dp -> {
                String name = dp.getName() + " " + (dp.getLastName() != null ? dp.getLastName() : "");
                shipmentsPerDPWithNames.put(name.trim(), count);
            });
        });
        stats.setShipmentsPerDeliveryPerson(shipmentsPerDPWithNames);

        // Revenue per delivery person
        Map<String, Double> revenuePerDP = allShipments.stream()
            .filter(s -> s.getDeliveryPersonId() != null && s.getStatus() != ShipmentStatus.CANCELLED)
            .collect(Collectors.groupingBy(
                Shipment::getDeliveryPersonId,
                Collectors.summingDouble(Shipment::getTotalCost)
            ));

        // Replace IDs with names
        Map<String, Double> revenuePerDPWithNames = new LinkedHashMap<>();
        revenuePerDP.forEach((dpId, revenue) -> {
            deliveryPersonRepository.findDeliveryPersonById(dpId).ifPresent(dp -> {
                String name = dp.getName() + " " + (dp.getLastName() != null ? dp.getLastName() : "");
                revenuePerDPWithNames.put(name.trim(), revenue);
            });
        });
        stats.setRevenuePerDeliveryPerson(revenuePerDPWithNames);

        // Average shipments per delivery person
        stats.setAverageShipmentsPerDeliveryPerson(
            allDeliveryPersons.isEmpty() ? 0.0 : (double) allShipments.size() / allDeliveryPersons.size()
        );
    }

    // ===========================
    // Chart Data Helpers
    // ===========================

    /**
     * Converts a map to a list of ChartDataDTO for easy chart rendering.
     * @param dataMap Map with labels as keys and values as data
     * @return List of ChartDataDTO
     */
    public List<ChartDataDTO> convertMapToChartData(Map<String, ? extends Number> dataMap) {
        return dataMap.entrySet().stream()
            .map(entry -> new ChartDataDTO(entry.getKey(), entry.getValue().doubleValue()))
            .collect(Collectors.toList());
    }

    /**
     * Gets chart data for shipments by status.
     * @return List of ChartDataDTO
     */
    public List<ChartDataDTO> getShipmentsByStatusChartData() {
        DashboardStatsDTO stats = calculateDashboardStats();
        return convertMapToChartData(stats.getShipmentsByStatus());
    }

    /**
     * Gets chart data for incidents by type.
     * @return List of ChartDataDTO
     */
    public List<ChartDataDTO> getIncidentsByTypeChartData() {
        DashboardStatsDTO stats = calculateDashboardStats();
        return convertMapToChartData(stats.getIncidentsByType());
    }

    /**
     * Gets chart data for revenue trend (last 7 days).
     * @return List of ChartDataDTO
     */
    public List<ChartDataDTO> getRevenueTrendChartData() {
        DashboardStatsDTO stats = calculateDashboardStats();
        Map<LocalDate, Double> revenuePerDay = stats.getRevenuePerDay();

        // Get last 7 days
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6);

        return revenuePerDay.entrySet().stream()
            .filter(entry -> !entry.getKey().isBefore(startDate) && !entry.getKey().isAfter(endDate))
            .map(entry -> new ChartDataDTO(entry.getKey().toString(), entry.getValue()))
            .collect(Collectors.toList());
    }

    /**
     * Gets chart data for shipments trend (last 7 days).
     * @return List of ChartDataDTO
     */
    public List<ChartDataDTO> getShipmentsTrendChartData() {
        DashboardStatsDTO stats = calculateDashboardStats();
        Map<LocalDate, Long> shipmentsPerDay = stats.getShipmentsPerDay();

        // Get last 7 days
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6);

        return shipmentsPerDay.entrySet().stream()
            .filter(entry -> !entry.getKey().isBefore(startDate) && !entry.getKey().isAfter(endDate))
            .map(entry -> new ChartDataDTO(entry.getKey().toString(), entry.getValue().doubleValue()))
            .collect(Collectors.toList());
    }
}
