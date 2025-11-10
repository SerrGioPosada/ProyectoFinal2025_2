package co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Command;

/**
 * Command pattern interface for payment operations.
 *
 * <p>This pattern encapsulates payment operations as objects, allowing for:
 * - Queuing of payment operations
 * - Logging and auditing of payment attempts
 * - Undo/rollback of payments (refunds)
 * - Retry mechanisms for failed payments</p>
 */
public interface PaymentCommand {

    /**
     * Executes the payment operation.
     *
     * @return true if successful, false otherwise
     */
    boolean execute();

    /**
     * Undoes the payment operation (refund).
     *
     * @return true if successful, false otherwise
     */
    boolean undo();

    /**
     * Gets the description of this payment command.
     *
     * @return The description
     */
    String getDescription();

    /**
     * Gets the amount being processed.
     *
     * @return The amount
     */
    double getAmount();

    /**
     * Checks if this command has been executed.
     *
     * @return true if executed, false otherwise
     */
    boolean isExecuted();

    /**
     * Checks if this command can be undone.
     *
     * @return true if can undo, false otherwise
     */
    boolean canUndo();
}
