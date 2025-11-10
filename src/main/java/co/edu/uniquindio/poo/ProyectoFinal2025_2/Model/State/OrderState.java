package co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.State;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Order;

/**
 * State pattern interface for Order lifecycle management.
 *
 * <p>This pattern encapsulates state-specific behavior and transitions,
 * making it easier to add new states and modify state behavior without
 * changing the Order class itself.</p>
 */
public interface OrderState {

    /**
     * Handles the transition to the next state.
     *
     * @param order The order context
     */
    void next(Order order);

    /**
     * Handles the transition to the previous state.
     *
     * @param order The order context
     */
    void previous(Order order);

    /**
     * Processes the payment for this order state.
     *
     * @param order The order context
     */
    void processPayment(Order order);

    /**
     * Cancels the order in this state.
     *
     * @param order The order context
     */
    void cancel(Order order);

    /**
     * Gets the human-readable name of this state.
     *
     * @return The state name
     */
    String getStateName();

    /**
     * Checks if the order can be modified in this state.
     *
     * @return true if modifiable, false otherwise
     */
    boolean isModifiable();
}
