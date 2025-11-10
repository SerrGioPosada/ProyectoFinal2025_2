package co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Template;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Order;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;

/**
 * Template Method pattern for order processing workflow.
 *
 * <p>This abstract class defines the skeleton of the order processing algorithm.
 * Subclasses can override specific steps while keeping the overall structure intact.</p>
 */
public abstract class OrderProcessor {

    /**
     * Template method that defines the order processing workflow.
     * This method is final to prevent subclasses from changing the overall algorithm.
     *
     * @param order The order to process
     * @return true if processing succeeds, false otherwise
     */
    public final boolean processOrder(Order order) {
        Logger.info("Starting order processing for order: " + order.getId());

        if (!validateOrder(order)) {
            Logger.error("Order validation failed");
            return false;
        }

        if (!calculateCosts(order)) {
            Logger.error("Cost calculation failed");
            return false;
        }

        if (!generateInvoice(order)) {
            Logger.error("Invoice generation failed");
            return false;
        }

        if (!processPayment(order)) {
            Logger.error("Payment processing failed");
            return false;
        }

        if (!createShipment(order)) {
            Logger.error("Shipment creation failed");
            return false;
        }

        if (!notifyCustomer(order)) {
            Logger.warning("Customer notification failed (non-critical)");
        }

        finalizeOrder(order);
        Logger.info("Order processing completed successfully");
        return true;
    }

    /**
     * Validates the order data.
     *
     * @param order The order to validate
     * @return true if valid, false otherwise
     */
    protected abstract boolean validateOrder(Order order);

    /**
     * Calculates all costs for the order.
     *
     * @param order The order to calculate costs for
     * @return true if successful, false otherwise
     */
    protected abstract boolean calculateCosts(Order order);

    /**
     * Generates an invoice for the order.
     *
     * @param order The order to generate invoice for
     * @return true if successful, false otherwise
     */
    protected abstract boolean generateInvoice(Order order);

    /**
     * Processes the payment for the order.
     *
     * @param order The order to process payment for
     * @return true if successful, false otherwise
     */
    protected abstract boolean processPayment(Order order);

    /**
     * Creates a shipment from the order.
     *
     * @param order The order to create shipment from
     * @return true if successful, false otherwise
     */
    protected abstract boolean createShipment(Order order);

    /**
     * Notifies the customer about order status.
     * This is an optional hook - default implementation does nothing.
     *
     * @param order The order to notify about
     * @return true if successful, false otherwise
     */
    protected boolean notifyCustomer(Order order) {
        // Default implementation - can be overridden
        return true;
    }

    /**
     * Performs final cleanup and finalization.
     * This is an optional hook - default implementation does nothing.
     *
     * @param order The order to finalize
     */
    protected void finalizeOrder(Order order) {
        // Default implementation - can be overridden
    }
}
