package co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Command;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Shipment;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.StatusChange;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.ShipmentStatus;

import java.time.LocalDateTime;

/**
 * Command to cancel a shipment.
 */
public class CancelShipmentCommand implements StatusChangeCommand {

    private final Shipment shipment;
    private final ShipmentStatus previousStatus;
    private final long timestamp;
    private final String cancelledBy;
    private final String reason;

    public CancelShipmentCommand(Shipment shipment, String cancelledBy, String reason) {
        this.shipment = shipment;
        this.previousStatus = shipment.getStatus();
        this.timestamp = System.currentTimeMillis();
        this.cancelledBy = cancelledBy;
        this.reason = reason;
    }

    @Override
    public void execute() {
        // Cancel using State Pattern
        shipment.cancelShipment();

        // Record the cancellation in history
        StatusChange statusChange = new StatusChange();
        statusChange.setPreviousStatus(previousStatus);
        statusChange.setNewStatus(ShipmentStatus.CANCELLED);
        statusChange.setTimestamp(LocalDateTime.now());
        statusChange.setChangedBy(cancelledBy);
        statusChange.setReason(reason != null ? reason : "Cancelado por usuario");

        shipment.addStatusChange(statusChange);
    }

    @Override
    public void undo() {
        throw new UnsupportedOperationException(
            "No se puede revertir una cancelación. El envío debe ser recreado."
        );
    }

    @Override
    public String getDescription() {
        return String.format("Cancelar envío %s (por %s): %s",
                           shipment.getId(),
                           cancelledBy,
                           reason);
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }
}
