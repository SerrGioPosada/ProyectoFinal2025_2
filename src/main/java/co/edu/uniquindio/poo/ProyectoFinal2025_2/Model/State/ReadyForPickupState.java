package co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.State;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Shipment;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.ShipmentStatus;

public class ReadyForPickupState implements ShipmentState {

    @Override
    public void next(Shipment shipment) {
        shipment.setStatus(ShipmentStatus.IN_TRANSIT);
        shipment.setShipmentState(new InTransitState());
    }

    @Override
    public void cancel(Shipment shipment) {
        shipment.setStatus(ShipmentStatus.CANCELLED);
        shipment.setShipmentState(new CancelledState());
    }

    @Override
    public ShipmentStatus getStatus() {
        return ShipmentStatus.READY_FOR_PICKUP;
    }

    @Override
    public boolean canTransitionToNext() {
        return true;
    }

    @Override
    public boolean canCancel() {
        return true;
    }

    @Override
    public String getStatusDescription() {
        return "Listo para Recolección";
    }
}
