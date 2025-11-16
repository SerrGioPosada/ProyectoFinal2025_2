package co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums;

/**
 * Represents additional services that can be added to a shipment.
 * Each service has a name, value (either percentage or fixed amount), and type.
 */
public enum ServiceType {
    INSURANCE("Seguro", 0.05, true),
    FRAGILE("Manejo Frágil", 2000, false),
    SIGNATURE_REQUIRED("Firma Requerida", 1500, false),
    PRIORITY("Entrega Prioritaria", 0.15, true);

    private final String name;
    private final double value;
    private final boolean isPercentage;

    /**
     * Constructor for ServiceType enum.
     * @param name The display name in Spanish
     * @param value The cost value (percentage or fixed amount)
     * @param isPercentage True if value is a percentage, false if it's a fixed amount
     */
    ServiceType(String name, double value, boolean isPercentage) {
        this.name = name;
        this.value = value;
        this.isPercentage = isPercentage;
    }

    /**
     * Gets the display name in Spanish.
     * @return The service name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the value of the service.
     * @return The cost value
     */
    public double getValue() {
        return value;
    }

    /**
     * Checks if the value is a percentage.
     * @return True if percentage, false if fixed amount
     */
    public boolean isPercentage() {
        return isPercentage;
    }

    /**
     * Calculates the cost for this service based on the base cost.
     * @param baseCost The base cost of the shipment
     * @return The calculated cost for this service
     */
    public double calculateCost(double baseCost) {
        if (isPercentage) {
            return baseCost * value;
        }
        return value;
    }
}
