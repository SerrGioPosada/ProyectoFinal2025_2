package co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Command;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Shipment;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.StatusChange;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.ShipmentStatus;

import java.time.LocalDateTime;

/**
 * Command to assign a delivery person to a shipment.
 */
public class AssignDeliveryPersonCommand implements StatusChangeCommand {

    private final Shipment shipment;
    private final String deliveryPersonId;
    private final String vehiclePlate;
    private final String previousDeliveryPersonId;
    private final ShipmentStatus previousStatus;
    private final long timestamp;
    private final String assignedBy;

    public AssignDeliveryPersonCommand(Shipment shipment, String deliveryPersonId,
                                      String vehiclePlate, String assignedBy) {
        this.shipment = shipment;
        this.deliveryPersonId = deliveryPersonId;
        this.vehiclePlate = vehiclePlate;
        this.previousDeliveryPersonId = shipment.getDeliveryPersonId();
        this.previousStatus = shipment.getStatus();
        this.timestamp = System.currentTimeMillis();
        this.assignedBy = assignedBy;
    }

    @Override
    public void execute() {
        // Assign delivery person
        shipment.setDeliveryPersonId(deliveryPersonId);
        shipment.setAssignedVehiclePlate(vehiclePlate);
        shipment.setAssignmentDate(LocalDateTime.now());

        // Advance to READY_FOR_PICKUP if currently PENDING_ASSIGNMENT
        if (shipment.getStatus() == ShipmentStatus.PENDING_ASSIGNMENT) {
            shipment.advanceToNextState();
        }

        // Record the assignment in history
        StatusChange statusChange = new StatusChange();
        statusChange.setPreviousStatus(previousStatus);
        statusChange.setNewStatus(shipment.getStatus());
        statusChange.setTimestamp(LocalDateTime.now());
        statusChange.setChangedBy(assignedBy);
        statusChange.setReason(String.format("Repartidor asignado: %s (vehículo: %s)",
                                            deliveryPersonId, vehiclePlate));

        shipment.addStatusChange(statusChange);
    }

    @Override
    public void undo() {
        // Revert assignment
        shipment.setDeliveryPersonId(previousDeliveryPersonId);
        shipment.setAssignedVehiclePlate(null);
        shipment.setAssignmentDate(null);
        shipment.setStatus(previousStatus);

        // Record the undo in history
        StatusChange statusChange = new StatusChange();
        statusChange.setPreviousStatus(shipment.getStatus());
        statusChange.setNewStatus(previousStatus);
        statusChange.setTimestamp(LocalDateTime.now());
        statusChange.setChangedBy(assignedBy);
        statusChange.setReason("Asignación de repartidor revertida");

        shipment.addStatusChange(statusChange);
    }

    @Override
    public String getDescription() {
        return String.format("Asignar repartidor %s al envío %s con vehículo %s",
                           deliveryPersonId,
                           shipment.getId(),
                           vehiclePlate);
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }
}
