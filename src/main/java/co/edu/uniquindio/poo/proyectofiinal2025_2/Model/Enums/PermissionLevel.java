package co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Enums;

/**
 * Defines the permission levels for an administrator.
 * <ul>
 *     <li>{@code SUPER_ADMIN}: Can manage other admins and system settings.</li>
 *     <li>{@code STANDARD}: Can manage day-to-day operations like orders and shipments.</li>
 *     <li>{@code REPORTS_ONLY}: Can only view system reports.</li>
 * </ul>
 */
public enum PermissionLevel {
    SUPER_ADMIN,
    STANDARD,
    REPORTS_ONLY
}
