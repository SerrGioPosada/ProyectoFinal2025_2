package co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

/**
 * Invoker for status change commands.
 * Manages command execution and maintains a history for potential undo operations.
 */
public class StatusHistoryInvoker {

    private final Stack<StatusChangeCommand> executedCommands;
    private final List<String> commandLog;
    private final int maxHistorySize;

    /**
     * Creates a new invoker with default max history size of 50.
     */
    public StatusHistoryInvoker() {
        this(50);
    }

    /**
     * Creates a new invoker with specified max history size.
     * @param maxHistorySize Maximum number of commands to keep in history
     */
    public StatusHistoryInvoker(int maxHistorySize) {
        this.executedCommands = new Stack<>();
        this.commandLog = new ArrayList<>();
        this.maxHistorySize = maxHistorySize;
    }

    /**
     * Executes a command and adds it to history.
     * @param command The command to execute
     * @throws RuntimeException if command execution fails
     */
    public void executeCommand(StatusChangeCommand command) {
        try {
            command.execute();
            executedCommands.push(command);
            logCommand(command);

            // Limit history size
            if (executedCommands.size() > maxHistorySize) {
                executedCommands.remove(0);
            }

        } catch (Exception e) {
            String errorMsg = String.format("Error ejecutando comando: %s - %s",
                                          command.getDescription(),
                                          e.getMessage());
            commandLog.add(errorMsg);
            throw new RuntimeException(errorMsg, e);
        }
    }

    /**
     * Undoes the last executed command.
     * @throws IllegalStateException if no commands to undo
     */
    public void undoLastCommand() {
        if (executedCommands.isEmpty()) {
            throw new IllegalStateException("No hay comandos para revertir");
        }

        StatusChangeCommand command = executedCommands.pop();
        try {
            command.undo();
            commandLog.add(String.format("UNDO: %s", command.getDescription()));
        } catch (Exception e) {
            // If undo fails, push command back onto stack
            executedCommands.push(command);
            String errorMsg = String.format("Error revirtiendo comando: %s - %s",
                                          command.getDescription(),
                                          e.getMessage());
            commandLog.add(errorMsg);
            throw new RuntimeException(errorMsg, e);
        }
    }

    /**
     * Gets the complete command log.
     * @return Unmodifiable list of log entries
     */
    public List<String> getCommandLog() {
        return Collections.unmodifiableList(commandLog);
    }

    /**
     * Gets the number of executed commands in history.
     * @return Number of commands
     */
    public int getHistorySize() {
        return executedCommands.size();
    }

    /**
     * Checks if there are commands to undo.
     * @return true if undo is possible
     */
    public boolean canUndo() {
        return !executedCommands.isEmpty();
    }

    /**
     * Clears all command history.
     */
    public void clearHistory() {
        executedCommands.clear();
        commandLog.add("Historial de comandos limpiado");
    }

    /**
     * Gets a description of the last executed command.
     * @return Description or null if no commands
     */
    public String getLastCommandDescription() {
        if (executedCommands.isEmpty()) {
            return null;
        }
        return executedCommands.peek().getDescription();
    }

    private void logCommand(StatusChangeCommand command) {
        String logEntry = String.format("[%d] %s",
                                       command.getTimestamp(),
                                       command.getDescription());
        commandLog.add(logEntry);
    }
}
