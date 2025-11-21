package co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.State;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Shipment;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.ShipmentStatus;

public class InTransitState implements ShipmentState {

    @Override
    public void next(Shipment shipment) {
        shipment.setStatus(ShipmentStatus.OUT_FOR_DELIVERY);
        shipment.setShipmentState(new OutForDeliveryState());
    }

    @Override
    public void cancel(Shipment shipment) {
        // Ya no se puede cancelar una vez en tránsito, solo devolver
        throw new IllegalStateException("No se puede cancelar un envío en tránsito. Debe ser devuelto.");
    }

    @Override
    public ShipmentStatus getStatus() {
        return ShipmentStatus.IN_TRANSIT;
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
        return "En Tránsito";
    }
}
