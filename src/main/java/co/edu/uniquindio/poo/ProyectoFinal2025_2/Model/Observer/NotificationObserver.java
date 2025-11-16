package co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Observer;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Shipment;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.ShipmentStatus;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;

/**
 * Concrete Observer that sends notifications when shipment events occur.
 * In a real implementation, this would integrate with email/SMS services.
 */
public class NotificationObserver implements ShipmentObserver {

    private final String userId;

    public NotificationObserver(String userId) {
        this.userId = userId;
    }

    @Override
    public void onStatusChanged(Shipment shipment, ShipmentStatus oldStatus, ShipmentStatus newStatus) {
        String message = String.format(
            "Tu envío #%s cambió de estado: %s → %s",
            shipment.getId(),
            translateStatus(oldStatus),
            translateStatus(newStatus)
        );
        sendNotification(userId, message);
    }

    @Override
    public void onShipmentAssigned(Shipment shipment, String deliveryPersonId) {
        // Notify the user (customer)
        String userMessage = String.format(
            "Tu envío #%s ha sido asignado a un repartidor. ¡Pronto estará en camino!",
            shipment.getId()
        );
        sendNotification(userId, userMessage);

        // Notify the delivery person
        String deliveryMessage = String.format(
            "Se te ha asignado un nuevo envío #%s. Revisa los detalles en tu panel.",
            shipment.getId()
        );
        sendNotification(deliveryPersonId, deliveryMessage);
    }

    @Override
    public void onIncidentReported(Shipment shipment, String incidentDescription) {
        String message = String.format(
            "Se reportó un incidente en tu envío #%s: %s",
            shipment.getId(),
            incidentDescription
        );
        sendNotification(userId, message);
    }

    private void sendNotification(String userId, String message) {
        // In a real implementation, this would send email/SMS/push notification
        Logger.info("NOTIFICATION to user " + userId + ": " + message);
        // TODO: Integrate with notification service
    }

    private String translateStatus(ShipmentStatus status) {
        return switch (status) {
            case PENDING_ASSIGNMENT -> "Pendiente de Asignación";
            case READY_FOR_PICKUP -> "Listo para Recoger";
            case PICKED_UP -> "Recogido por el Usuario";
            case IN_TRANSIT -> "En tránsito";
            case OUT_FOR_DELIVERY -> "En Reparto";
            case DELIVERED -> "Entregado";
            case CANCELLED -> "Cancelado";
            case RETURNED -> "Devuelto";
        };
    }
}
