package co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Observer;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Shipment;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.ShipmentStatus;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Concrete Observer that logs shipment events for auditing and debugging purposes.
 */
public class LoggingObserver implements ShipmentObserver {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void onStatusChanged(Shipment shipment, ShipmentStatus oldStatus, ShipmentStatus newStatus) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        Logger.info(String.format(
            "[%s] Shipment %s status changed: %s -> %s (User: %s, Order: %s)",
            timestamp,
            shipment.getId(),
            oldStatus,
            newStatus,
            shipment.getUserId(),
            shipment.getOrderId()
        ));
    }

    @Override
    public void onShipmentAssigned(Shipment shipment, String deliveryPersonId) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        Logger.info(String.format(
            "[%s] Shipment %s assigned to delivery person %s",
            timestamp,
            shipment.getId(),
            deliveryPersonId
        ));
    }

    @Override
    public void onIncidentReported(Shipment shipment, String incidentDescription) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        Logger.warning(String.format(
            "[%s] INCIDENT reported for shipment %s: %s",
            timestamp,
            shipment.getId(),
            incidentDescription
        ));
    }
}
