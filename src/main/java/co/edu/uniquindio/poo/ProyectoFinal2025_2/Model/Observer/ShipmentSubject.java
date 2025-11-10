package co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Observer;

/**
 * The Subject interface, which defines methods for attaching, detaching, and notifying observers.
 * This will be implemented by the Shipment class or a ShipmentEventPublisher.
 *
 * <p>Subjects maintain a list of observers and notify them when state changes occur.</p>
 */
public interface ShipmentSubject {

    /**
     * Attaches an observer to this subject.
     *
     * @param observer The observer to attach
     */
    void attach(ShipmentObserver observer);

    /**
     * Detaches an observer from this subject.
     *
     * @param observer The observer to detach
     */
    void detach(ShipmentObserver observer);

    /**
     * Notifies all attached observers of a state change.
     */
    void notifyObservers();
}
