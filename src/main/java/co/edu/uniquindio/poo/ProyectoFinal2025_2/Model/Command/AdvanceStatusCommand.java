package co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Command;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Shipment;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.StatusChange;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.ShipmentStatus;

import java.time.LocalDateTime;

/**
 * Command to advance shipment to next state.
 */
public class AdvanceStatusCommand implements StatusChangeCommand {

    private final Shipment shipment;
    private final ShipmentStatus previousStatus;
    private final long timestamp;
    private final String changedBy;
    private final String reason;

    public AdvanceStatusCommand(Shipment shipment, String changedBy, String reason) {
        this.shipment = shipment;
        this.previousStatus = shipment.getStatus();
        this.timestamp = System.currentTimeMillis();
        this.changedBy = changedBy;
        this.reason = reason;
    }

    @Override
    public void execute() {
        // Advance to next state using State Pattern
        shipment.advanceToNextState();

        // Record the change in history
        StatusChange statusChange = new StatusChange();
        statusChange.setPreviousStatus(previousStatus);
        statusChange.setNewStatus(shipment.getStatus());
        statusChange.setTimestamp(LocalDateTime.now());
        statusChange.setChangedBy(changedBy);
        statusChange.setReason(reason != null ? reason : "Estado avanzado automáticamente");

        shipment.addStatusChange(statusChange);
    }

    @Override
    public void undo() {
        // Reverting status changes is complex and may not always be safe
        // This is a simplified implementation
        shipment.setStatus(previousStatus);

        // Record the undo in history
        StatusChange statusChange = new StatusChange();
        statusChange.setPreviousStatus(shipment.getStatus());
        statusChange.setNewStatus(previousStatus);
        statusChange.setTimestamp(LocalDateTime.now());
        statusChange.setChangedBy(changedBy);
        statusChange.setReason("Cambio de estado revertido");

        shipment.addStatusChange(statusChange);
    }

    @Override
    public String getDescription() {
        return String.format("Avanzar envío %s de %s a siguiente estado (por %s)",
                           shipment.getId(),
                           previousStatus,
                           changedBy);
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }
}
