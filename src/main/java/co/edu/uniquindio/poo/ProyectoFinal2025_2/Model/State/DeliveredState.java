package co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.State;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Shipment;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.ShipmentStatus;

public class DeliveredState implements ShipmentState {

    @Override
    public void next(Shipment shipment) {
        throw new IllegalStateException("El envío ya fue entregado. No hay siguiente estado.");
    }

    @Override
    public void cancel(Shipment shipment) {
        throw new IllegalStateException("No se puede cancelar un envío ya entregado.");
    }

    @Override
    public ShipmentStatus getStatus() {
        return ShipmentStatus.DELIVERED;
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
        return "Entregado";
    }
}
