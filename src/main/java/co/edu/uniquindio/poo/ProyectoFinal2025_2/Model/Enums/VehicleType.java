package co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums;

/**
 * Types of vehicles available in the system.
 */
public enum VehicleType {
    MOTORCYCLE("Motocicleta"),
    CAR("Automóvil"),
    TRUCK("Camión"),
    VAN("Furgoneta");

    private final String displayName;

    /**
     * Constructor for VehicleType enum.
     * @param displayName The display name in Spanish
     */
    VehicleType(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Gets the display name in Spanish.
     * @return The vehicle type display name
     */
    public String getDisplayName() {
        return displayName;
    }
}
