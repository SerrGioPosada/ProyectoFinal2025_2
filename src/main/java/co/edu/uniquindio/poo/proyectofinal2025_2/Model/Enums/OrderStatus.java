package co.edu.uniquindio.poo.proyectofinal2025_2.Model.Enums;

/**
 * Represents the lifecycle status of an order.
 * <ul>
 *     <li>{@code AWAITING_PAYMENT}: The order has been created but not yet paid for.</li>
 *     <li>{@code PAID}: The order has been successfully paid.</li>
 *     <li>{@code PREPARING_SHIPMENT}: The order is being prepared for dispatch.</li>
 *     <li>{@code SHIPPED}: The order has been dispatched and is now a shipment.</li>
 *     <li>{@code DELIVERED}: The order has been successfully delivered to the recipient.</li>
 *     <li>{@code CANCELLED}: The order has been cancelled.</li>
 * </ul>
 */
public enum OrderStatus {
    AWAITING_PAYMENT,
    PAID,
    PREPARING_SHIPMENT,
    SHIPPED,
    DELIVERED,
    CANCELLED
}
