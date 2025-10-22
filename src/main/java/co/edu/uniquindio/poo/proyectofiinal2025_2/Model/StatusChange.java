package co.edu.uniquindio.poo.proyectofiinal2025_2.Model;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Enums.ShipmentStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents a status change in a shipment's lifecycle.
 * This is used for audit trail and tracking shipment history.
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class StatusChange {

    private ShipmentStatus previousStatus;
    private ShipmentStatus newStatus;
    private LocalDateTime timestamp;
    private String changedBy;
    private String reason;

    /**
     * Constructor for status changes without specified user or reason.
     * @param previousStatus The previous status
     * @param newStatus The new status
     * @param timestamp The timestamp of the change
     */
    public StatusChange(ShipmentStatus previousStatus, ShipmentStatus newStatus, LocalDateTime timestamp) {
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.timestamp = timestamp;
    }
}