package co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.State;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.ShipmentStatus;

/**
 * Factory para crear instancias de ShipmentState basadas en el ShipmentStatus.
 */
public class ShipmentStateFactory {

    public static ShipmentState createState(ShipmentStatus status) {
        return switch (status) {
            case PENDING_ASSIGNMENT -> new PendingAssignmentState();
            case READY_FOR_PICKUP -> new ReadyForPickupState();
            case IN_TRANSIT -> new InTransitState();
            case OUT_FOR_DELIVERY -> new OutForDeliveryState();
            case DELIVERED -> new DeliveredState();
            case CANCELLED -> new CancelledState();
            case RETURNED -> new ReturnedState();
            default -> new PendingAssignmentState(); // Default
        };
    }
}
