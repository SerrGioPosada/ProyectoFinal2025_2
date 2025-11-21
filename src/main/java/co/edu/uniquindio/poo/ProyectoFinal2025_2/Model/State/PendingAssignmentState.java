package co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.State;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Shipment;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.ShipmentStatus;

/**
 * Estado inicial del envío: Pendiente de asignación de repartidor.
 */
public class PendingAssignmentState implements ShipmentState {

    @Override
    public void next(Shipment shipment) {
        // Transición: PENDING_ASSIGNMENT → READY_FOR_PICKUP
        shipment.setStatus(ShipmentStatus.READY_FOR_PICKUP);
        shipment.setShipmentState(new ReadyForPickupState());
    }

    @Override
    public void cancel(Shipment shipment) {
        // Se puede cancelar desde este estado
        shipment.setStatus(ShipmentStatus.CANCELLED);
        shipment.setShipmentState(new CancelledState());
    }

    @Override
    public ShipmentStatus getStatus() {
        return ShipmentStatus.PENDING_ASSIGNMENT;
    }

    @Override
    public boolean canTransitionToNext() {
        return true; // Puede avanzar cuando se asigne repartidor
    }

    @Override
    public boolean canCancel() {
        return true;
    }

    @Override
    public String getStatusDescription() {
        return "Pendiente de Asignación de Repartidor";
    }
}
