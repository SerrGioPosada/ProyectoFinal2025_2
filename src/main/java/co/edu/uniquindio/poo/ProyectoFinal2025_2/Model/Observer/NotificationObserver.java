package co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Observer;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Shipment;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.ShipmentStatus;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.NotificationService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;

/**
 * Concrete Observer that sends notifications when shipment events occur.
 * Integrates with NotificationService to display notifications in the UI.
 */
public class NotificationObserver implements ShipmentObserver {

    private final String userId;
    private final NotificationService notificationService;

    public NotificationObserver(String userId) {
        this.userId = userId;
        this.notificationService = NotificationService.getInstance();
    }

    @Override
    public void onStatusChanged(Shipment shipment, ShipmentStatus oldStatus, ShipmentStatus newStatus) {
        String message = String.format(
            "Tu envío #%s cambió de estado: %s → %s",
            shipment.getId(),
            translateStatus(oldStatus),
            translateStatus(newStatus)
        );
        sendNotification(userId, "Actualización de Envío", message, NotificationService.NotificationType.SUCCESS);
    }

    @Override
    public void onShipmentAssigned(Shipment shipment, String deliveryPersonId) {
        // Notify the user (customer)
        String userMessage = String.format(
            "Tu envío #%s ha sido asignado a un repartidor. ¡Pronto estará en camino!",
            shipment.getId()
        );
        sendNotification(userId, "Envío Asignado", userMessage, NotificationService.NotificationType.INFO);

        // Notify the delivery person
        String origin = shipment.getOrigin() != null ? shipment.getOrigin().getCity() : "N/A";
        String destination = shipment.getDestination() != null ? shipment.getDestination().getCity() : "N/A";
        notificationService.notifyShipmentAssignment(deliveryPersonId, shipment.getId(), origin, destination);
    }

    @Override
    public void onIncidentReported(Shipment shipment, String incidentDescription) {
        String message = String.format(
            "Se reportó un incidente en tu envío #%s: %s",
            shipment.getId(),
            incidentDescription
        );
        sendNotification(userId, "Incidente Reportado", message, NotificationService.NotificationType.WARNING);
    }

    private void sendNotification(String userId, String title, String message, NotificationService.NotificationType type) {
        // Send notification through NotificationService
        notificationService.addNotification(userId, title, message, type);
        Logger.info("NOTIFICATION sent to user " + userId + ": " + message);
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
