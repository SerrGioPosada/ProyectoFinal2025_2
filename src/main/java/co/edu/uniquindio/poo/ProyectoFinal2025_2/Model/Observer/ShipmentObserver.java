package co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Observer;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Shipment;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.ShipmentStatus;

/**
 * The Observer interface, which defines the update method that all concrete observers must implement.
 * Observers are notified when the state of the subject (Shipment) changes.
 *
 * <p>This pattern allows multiple observers to react to shipment status changes without
 * tight coupling. Examples include notification services, logging systems, and analytics.</p>
 */
public interface ShipmentObserver {

    /**
     * Called when the observed shipment's status changes.
     *
     * @param shipment The shipment that changed
     * @param oldStatus The previous status
     * @param newStatus The new status
     */
    void onStatusChanged(Shipment shipment, ShipmentStatus oldStatus, ShipmentStatus newStatus);

    /**
     * Called when the shipment is assigned to a delivery person.
     *
     * @param shipment The shipment that was assigned
     * @param deliveryPersonId The ID of the assigned delivery person
     */
    void onShipmentAssigned(Shipment shipment, String deliveryPersonId);

    /**
     * Called when an incident is reported for the shipment.
     *
     * @param shipment The shipment with the incident
     * @param incidentDescription Description of the incident
     */
    void onIncidentReported(Shipment shipment, String incidentDescription);
}
