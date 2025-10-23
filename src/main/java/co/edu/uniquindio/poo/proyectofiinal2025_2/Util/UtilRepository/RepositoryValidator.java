package co.edu.uniquindio.poo.proyectofiinal2025_2.Util.UtilRepository;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Util.UtilModel.Logger;

/**
 * Centralized validation utility for repository operations.
 *
 * <p>This utility provides common validation logic used across all repository classes,
 * eliminating duplicated null checks and validation code. It ensures consistent validation
 * behavior and error messaging throughout the persistence layer.</p>
 *
 * <p><b>Core Functionality:</b></p>
 * <ul>
 *     <li><b>Entity Validation:</b> {@link #validateEntity(Object, String)}</li>
 *     <li><b>ID Validation:</b> {@link #validateId(String, String)}</li>
 *     <li><b>Email Validation:</b> {@link #validateEmail(String, String)}</li>
 *     <li><b>Combined Validation:</b> {@link #validateEntityWithIdAndEmail(Object, String, String, String)}</li>
 * </ul>
 *
 * <p><b>Usage Examples:</b></p>
 * <pre>
 * // In Repositories - Validate before adding:
 * public void addUser(User user) {
 *     if (!RepositoryValidator.validateEntityWithIdAndEmail(
 *             user, user.getId(), user.getEmail(), "User")) {
 *         return; // Validation failed, error already logged
 *     }
 *     // Proceed with adding user
 * }
 *
 * // Validate ID before lookup:
 * public Optional&lt;Admin&gt; findById(String id) {
 *     if (!RepositoryValidator.validateId(id, "Admin")) {
 *         return Optional.empty();
 *     }
 *     return Optional.ofNullable(adminsById.get(id));
 * }
 * </pre>
 *
 * <p><b>Design Benefits:</b></p>
 * <ul>
 *     <li>Eliminates duplicated validation code across 9+ repositories</li>
 *     <li>Consistent error messages for validation failures</li>
 *     <li>Centralized logging via {@link Logger}</li>
 *     <li>Easy to add new validation rules globally</li>
 * </ul>
 *
 * <p><b>Validation Philosophy:</b></p>
 * <ul>
 *     <li>Returns {@code false} on validation failure (never throws exceptions)</li>
 *     <li>Logs detailed error messages via {@link Logger} for debugging</li>
 *     <li>Allows repositories to fail gracefully without crashing</li>
 * </ul>
 *
 * @author Sistema de Gestión de Envíos
 * @version 1.0
 * @since 2025
 * @see Logger
 */
public final class RepositoryValidator {

    // =================================================================================================================
    // CONSTRUCTOR
    // =================================================================================================================

    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     *
     * @throws UnsupportedOperationException if instantiation is attempted
     */
    private RepositoryValidator() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // =================================================================================================================
    // ENTITY VALIDATION
    // =================================================================================================================

    /**
     * Validates that an entity is not null.
     *
     * <p>This is the most basic validation that should be performed before any repository operation.</p>
     *
     * <p><b>Example:</b></p>
     * <pre>
     * if (!RepositoryValidator.validateEntity(user, "User")) {
     *     return; // Entity is null, error logged
     * }
     * </pre>
     *
     * @param entity     The entity to validate (can be null)
     * @param entityType The type name of the entity (for error messages)
     * @return {@code true} if entity is not null, {@code false} otherwise
     */
    public static boolean validateEntity(Object entity, String entityType) {
        if (entity == null) {
            Logger.error("RepositoryValidator: Cannot process null " + entityType);
            return false;
        }
        return true;
    }

    // =================================================================================================================
    // ID VALIDATION
    // =================================================================================================================

    /**
     * Validates that an ID is not null and not empty.
     *
     * <p>This validation should be used before any ID-based lookup or when adding entities.</p>
     *
     * <p><b>Examples:</b></p>
     * <pre>
     * // Before lookup:
     * if (!RepositoryValidator.validateId(userId, "User")) {
     *     return Optional.empty();
     * }
     *
     * // When adding entity:
     * if (!RepositoryValidator.validateId(admin.getId(), "Admin")) {
     *     return;
     * }
     * </pre>
     *
     * @param id         The ID to validate (can be null)
     * @param entityType The type name of the entity (for error messages)
     * @return {@code true} if ID is not null and not empty, {@code false} otherwise
     */
    public static boolean validateId(String id, String entityType) {
        if (id == null) {
            Logger.error("RepositoryValidator: " + entityType + " ID is null");
            return false;
        }
        if (id.trim().isEmpty()) {
            Logger.error("RepositoryValidator: " + entityType + " ID is empty");
            return false;
        }
        return true;
    }

    // =================================================================================================================
    // EMAIL VALIDATION
    // =================================================================================================================

    /**
     * Validates that an email is not null and not empty.
     *
     * <p>This validation ensures emails are present before using them as map keys.
     * For full email format validation, use {@link co.edu.uniquindio.poo.proyectofiinal2025_2.Util.UtilModel.ValidationUtil#isValidEmail(String)}.</p>
     *
     * <p><b>Example:</b></p>
     * <pre>
     * if (!RepositoryValidator.validateEmail(user.getEmail(), "User")) {
     *     return;
     * }
     * </pre>
     *
     * @param email      The email to validate (can be null)
     * @param entityType The type name of the entity (for error messages)
     * @return {@code true} if email is not null and not empty, {@code false} otherwise
     */
    public static boolean validateEmail(String email, String entityType) {
        if (email == null) {
            Logger.error("RepositoryValidator: " + entityType + " email is null");
            return false;
        }
        if (email.trim().isEmpty()) {
            Logger.error("RepositoryValidator: " + entityType + " email is empty");
            return false;
        }
        return true;
    }

    // =================================================================================================================
    // COMBINED VALIDATION
    // =================================================================================================================

    /**
     * Performs comprehensive validation of an entity with ID and email.
     *
     * <p>This is a convenience method that combines {@link #validateEntity(Object, String)},
     * {@link #validateId(String, String)}, and {@link #validateEmail(String, String)} into a
     * single call. Use this for entities that require both ID and email (User, Admin, DeliveryPerson).</p>
     *
     * <p><b>Example:</b></p>
     * <pre>
     * public void addAdmin(Admin admin) {
     *     if (!RepositoryValidator.validateEntityWithIdAndEmail(
     *             admin, admin.getId(), admin.getEmail(), "Admin")) {
     *         return; // Validation failed
     *     }
     *
     *     // Proceed with adding admin to maps
     *     adminsById.put(admin.getId(), admin);
     *     adminsByEmail.put(admin.getEmail().toLowerCase(), admin);
     *     saveToFile();
     * }
     * </pre>
     *
     * @param entity     The entity to validate (can be null)
     * @param id         The entity's ID (can be null)
     * @param email      The entity's email (can be null)
     * @param entityType The type name of the entity (for error messages)
     * @return {@code true} if all validations pass, {@code false} if any validation fails
     */
    public static boolean validateEntityWithIdAndEmail(Object entity, String id, String email, String entityType) {
        if (!validateEntity(entity, entityType)) {
            return false;
        }
        if (!validateId(id, entityType)) {
            return false;
        }
        if (!validateEmail(email, entityType)) {
            return false;
        }
        return true;
    }

    /**
     * Performs validation of an entity with ID only (no email required).
     *
     * <p>Use this for entities that have IDs but not emails (Orders, Shipments, Payments, etc.).</p>
     *
     * <p><b>Example:</b></p>
     * <pre>
     * public void addOrder(Order order) {
     *     if (!RepositoryValidator.validateEntityWithId(order, order.getId(), "Order")) {
     *         return; // Validation failed
     *     }
     *     ordersById.put(order.getId(), order);
     *     saveToFile();
     * }
     * </pre>
     *
     * @param entity     The entity to validate (can be null)
     * @param id         The entity's ID (can be null)
     * @param entityType The type name of the entity (for error messages)
     * @return {@code true} if all validations pass, {@code false} if any validation fails
     */
    public static boolean validateEntityWithId(Object entity, String id, String entityType) {
        if (!validateEntity(entity, entityType)) {
            return false;
        }
        if (!validateId(id, entityType)) {
            return false;
        }
        return true;
    }
}
