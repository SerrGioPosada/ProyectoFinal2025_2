package co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.State;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Shipment;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.ShipmentStatus;

/**
 * Interface que define el contrato para los diferentes estados de un envío.
 * Implementa el patrón State para encapsular el comportamiento específico de cada estado.
 */
public interface ShipmentState {

    /**
     * Intenta avanzar al siguiente estado del envío.
     * @param shipment El envío que cambia de estado
     */
    void next(Shipment shipment);

    /**
     * Intenta cancelar el envío desde el estado actual.
     * @param shipment El envío a cancelar
     * @throws IllegalStateException si no se puede cancelar desde este estado
     */
    void cancel(Shipment shipment);

    /**
     * Obtiene el estado actual representado por esta clase.
     * @return El estado del envío
     */
    ShipmentStatus getStatus();

    /**
     * Verifica si se puede avanzar al siguiente estado.
     * @return true si es posible avanzar
     */
    boolean canTransitionToNext();

    /**
     * Verifica si se puede cancelar desde este estado.
     * @return true si es posible cancelar
     */
    boolean canCancel();

    /**
     * Obtiene el nombre descriptivo del estado.
     * @return Descripción del estado en español
     */
    String getStatusDescription();
}
