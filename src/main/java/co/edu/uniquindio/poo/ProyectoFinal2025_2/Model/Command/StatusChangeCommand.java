package co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Command;

/**
 * Command interface for status change operations.
 * Implements the Command Pattern for shipment status history.
 */
public interface StatusChangeCommand {

    /**
     * Executes the status change command.
     */
    void execute();

    /**
     * Undoes the status change (if possible).
     */
    void undo();

    /**
     * Gets a description of this command.
     * @return Human-readable description
     */
    String getDescription();

    /**
     * Gets the timestamp when this command was created.
     * @return Timestamp in milliseconds
     */
    long getTimestamp();
}
