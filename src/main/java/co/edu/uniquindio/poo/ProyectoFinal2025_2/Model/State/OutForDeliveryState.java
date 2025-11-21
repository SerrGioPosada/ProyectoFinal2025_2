package co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.State;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Shipment;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.ShipmentStatus;

public class OutForDeliveryState implements ShipmentState {

    @Override
    public void next(Shipment shipment) {
        shipment.setStatus(ShipmentStatus.DELIVERED);
        shipment.setShipmentState(new DeliveredState());
    }

    @Override
    public void cancel(Shipment shipment) {
        throw new IllegalStateException("No se puede cancelar un envío que está fuera para entrega.");
    }

    @Override
    public ShipmentStatus getStatus() {
        return ShipmentStatus.OUT_FOR_DELIVERY;
    }

    @Override
    public boolean canTransitionToNext() {
        return true;
    }

    @Override
    public boolean canCancel() {
        return false;
    }

    @Override
    public String getStatusDescription() {
        return "Fuera para Entrega";
    }
}
