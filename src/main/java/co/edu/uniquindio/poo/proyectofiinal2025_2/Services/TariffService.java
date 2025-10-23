package co.edu.uniquindio.poo.proyectofiinal2025_2.Services;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Decorator.*;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Enums.ServiceType;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Tariff;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Repositories.TariffRepository;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Util.UtilModel.Logger;

import java.util.List;

/**
 * Provides business logic services related to tariffs and pricing.
 * <p>This service uses the Decorator pattern to calculate shipping costs
 * by applying various services to a base cost calculation.</p>
 */
public class TariffService {

    private final TariffRepository tariffRepository;

    /**
     * Constructor with dependency injection.
     * @param tariffRepository The tariff repository
     */
    public TariffService(TariffRepository tariffRepository) {
        this.tariffRepository = tariffRepository;
    }

    /**
     * Default constructor using singleton repository.
     */
    public TariffService() {
        this(TariffRepository.getInstance());
    }

    // ===========================
    // Tariff Retrieval
    // ===========================

    /**
     * Gets the currently active tariff.
     * @return The active Tariff
     */
    public Tariff getActiveTariff() {
        return tariffRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("No active tariff found in the system."));
    }

    /**
     * Gets all tariffs.
     * @return List of all tariffs
     */
    public List<Tariff> getAllTariffs() {
        return tariffRepository.findAll();
    }

    /**
     * Gets a tariff by ID.
     * @param id The tariff ID
     * @return The tariff
     */
    public Tariff getTariffById(String id) {
        return tariffRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Tariff not found: " + id));
    }

    // ===========================
    // Cost Calculation using Decorator Pattern
    // ===========================

    /**
     * Builds a cost calculator using the Decorator pattern.
     * @param distanceKm Distance in kilometers
     * @param weightKg Weight in kilograms
     * @param volumeM3 Volume in cubic meters
     * @param priorityLevel Priority level (1-5)
     * @param services List of additional services to apply
     * @return A fully decorated cost calculator
     */
    public CostCalculator buildCostCalculator(
            double distanceKm,
            double weightKg,
            double volumeM3,
            int priorityLevel,
            List<ServiceType> services) {

        // Get active tariff
        Tariff tariff = getActiveTariff();

        // Start with base cost
        CostCalculator calculator = new BaseShippingCost(tariff, distanceKm, weightKg, volumeM3);

        // Apply priority decorator if not standard (3)
        if (priorityLevel > 3) {
            calculator = new PriorityDecorator(calculator, priorityLevel);
        }

        // Apply service decorators
        if (services != null) {
            for (ServiceType service : services) {
                calculator = applyServiceDecorator(calculator, service);
            }
        }

        Logger.info("Cost calculator built with " + (services != null ? services.size() : 0) + " services");
        return calculator;
    }

    /**
     * Applies a service decorator based on service type.
     * @param calculator The current calculator
     * @param serviceType The service type to apply
     * @return The decorated calculator
     */
    private CostCalculator applyServiceDecorator(CostCalculator calculator, ServiceType serviceType) {
        return switch (serviceType) {
            case INSURANCE -> new InsuranceDecorator(calculator);
            case FRAGILE -> new FragileDecorator(calculator);
            case SIGNATURE_REQUIRED -> new SignatureDecorator(calculator);
            case PRIORITY -> calculator; // Priority is handled separately
        };
    }

    /**
     * Calculates the total cost using the Decorator pattern.
     * @param distanceKm Distance in kilometers
     * @param weightKg Weight in kilograms
     * @param volumeM3 Volume in cubic meters
     * @param priorityLevel Priority level (1-5)
     * @param services List of additional services
     * @return The total calculated cost
     */
    public double calculateTotalCost(
            double distanceKm,
            double weightKg,
            double volumeM3,
            int priorityLevel,
            List<ServiceType> services) {

        CostCalculator calculator = buildCostCalculator(distanceKm, weightKg, volumeM3, priorityLevel, services);
        return calculator.calculateCost();
    }

    /**
     * Gets a detailed cost breakdown using the Decorator pattern.
     * @param distanceKm Distance in kilometers
     * @param weightKg Weight in kilograms
     * @param volumeM3 Volume in cubic meters
     * @param priorityLevel Priority level (1-5)
     * @param services List of additional services
     * @return List of cost breakdown items
     */
    public List<CostCalculator.CostBreakdownItem> getCostBreakdown(
            double distanceKm,
            double weightKg,
            double volumeM3,
            int priorityLevel,
            List<ServiceType> services) {

        CostCalculator calculator = buildCostCalculator(distanceKm, weightKg, volumeM3, priorityLevel, services);
        return calculator.getBreakdown();
    }

    // ===========================
    // Management Methods
    // ===========================

    /**
     * Creates a new tariff.
     * @param tariff The tariff to create
     */
    public void createTariff(Tariff tariff) {
        tariffRepository.addTariff(tariff);
        Logger.info("Tariff created: " + tariff.getId());
    }
}
