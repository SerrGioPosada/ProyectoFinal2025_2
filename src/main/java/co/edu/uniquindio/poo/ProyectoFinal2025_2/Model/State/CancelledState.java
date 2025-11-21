package co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.State;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Shipment;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.ShipmentStatus;

public class CancelledState implements ShipmentState {

    @Override
    public void next(Shipment shipment) {
        throw new IllegalStateException("El envío está cancelado. No puede avanzar a otro estado.");
    }

    @Override
    public void cancel(Shipment shipment) {
        // Ya está cancelado
        throw new IllegalStateException("El envío ya está cancelado.");
    }

    @Override
    public ShipmentStatus getStatus() {
        return ShipmentStatus.CANCELLED;
    }

    @Override
    public boolean canTransitionToNext() {
        return false; // Estado final
    }

    @Override
    public boolean canCancel() {
        return false;
    }

    @Override
    public String getStatusDescription() {
        return "Cancelado";
    }
}
