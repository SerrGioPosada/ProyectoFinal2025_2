package co.edu.uniquindio.poo.proyectofinal2025_2.Model.Enums;

/**
 * Represents the type of incident that can occur during a shipment.
 */
public enum IncidentType {
    INCORRECT_ADDRESS("Dirección Incorrecta"),
    RECIPIENT_ABSENT("Destinatario Ausente"),
    DAMAGED_PACKAGE("Paquete Dañado"),
    DELAY("Retraso en Entrega"),
    LOST_PACKAGE("Paquete Perdido"),
    REFUSED_DELIVERY("Entrega Rechazada"),
    OTHER("Otro");

    private final String description;

    /**
     * Constructor for IncidentType enum.
     * @param description The description in Spanish
     */
    IncidentType(String description) {
        this.description = description;
    }

    /**
     * Gets the description in Spanish.
     * @return The incident description
     */
    public String getDescription() {
        return description;
    }
}
