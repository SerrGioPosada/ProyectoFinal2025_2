package co.edu.uniquindio.poo.proyectofinal2025_2.Model.Enums;

/**
 * Defines the geographical coverage areas for a delivery person.
 * <ul>
 *     <li>{@code NORTH}: North zone of the city.</li>
 *     <li>{@code SOUTH}: South zone of the city.</li>
 *     <li>{@code CENTRAL}: Central zone of the city.</li>
 *     <li>{@code CITY_WIDE}: Covers the entire city.</li>
 * </ul>
 */
public enum CoverageArea {
    NORTH("Zona Norte"),
    SOUTH("Zona Sur"),
    CENTRAL("Zona Central"),
    CITY_WIDE("Ciudad Completa");

    private final String displayName;

    /**
     * Constructor for CoverageArea enum.
     * @param displayName The display name in Spanish for the UI
     */
    CoverageArea(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Gets the display name in Spanish.
     * @return The display name
     */
    public String getDisplayName() {
        return displayName;
    }
}
