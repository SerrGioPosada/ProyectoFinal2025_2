package co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums;

/**
 * Represents the availability status of a delivery person.
 * <ul>
 *     <li>{@code AVAILABLE}: The delivery person is ready to be assigned a new shipment.</li>
 *     <li>{@code IN_TRANSIT}: The delivery person is currently handling a shipment.</li>
 *     <li>{@code INACTIVE}: The delivery person is not currently working or available.</li>
 * </ul>
 */
public enum AvailabilityStatus {
    AVAILABLE,
    IN_TRANSIT,
    INACTIVE
}
