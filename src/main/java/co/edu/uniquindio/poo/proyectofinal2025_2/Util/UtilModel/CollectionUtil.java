package co.edu.uniquindio.poo.proyectofinal2025_2.Util.UtilModel;

import java.util.Collection;
import java.util.List;

/**
 * Utility class for safe collection operations and null-safe collection manipulations.
 *
 * <p>This utility provides null-safe collection operations that eliminate the need for
 * repetitive null checks when working with collections throughout the application.</p>
 *
 * <p><b>Core Functionality:</b></p>
 * <ul>
 *     <li><b>Size Operations:</b> {@link #safeSize(Collection)}</li>
 *     <li><b>Null/Empty Checks:</b> {@link #isNullOrEmpty(Collection)}, {@link #isNotEmpty(Collection)}</li>
 *     <li><b>Safe Access:</b> {@link #getFirstOrNull(List)}, {@link #getLastOrNull(List)}</li>
 * </ul>
 *
 * <p><b>Usage Examples:</b></p>
 * <pre>
 * // In DTOs or Services:
 * int orderCount = CollectionUtil.safeSize(user.getOrders());
 * int addressCount = CollectionUtil.safeSize(user.getFrequentAddresses());
 *
 * // Null-safe validation:
 * if (CollectionUtil.isNullOrEmpty(shipments)) {
 *     logger.warn("No shipments found for delivery person");
 * }
 *
 * // Safe element access:
 * Order firstOrder = CollectionUtil.getFirstOrNull(ordersList);
 * </pre>
 *
 * <p><b>Benefits:</b></p>
 * <ul>
 *     <li>Eliminates {@code collection != null ? collection.size() : 0} patterns</li>
 *     <li>Reduces NullPointerException risks when accessing collections</li>
 *     <li>Provides consistent collection handling behavior</li>
 *     <li>Makes code more readable and concise</li>
 * </ul>
 *
 * @author Sistema de Gestión de Envíos
 * @version 1.0
 * @since 2025
 */
public final class CollectionUtil {

    // =================================================================================================================
    // CONSTRUCTOR
    // =================================================================================================================

    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     *
     * @throws UnsupportedOperationException if instantiation is attempted
     */
    private CollectionUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // =================================================================================================================
    // SIZE OPERATIONS
    // =================================================================================================================

    /**
     * Returns the size of a collection, or 0 if the collection is null.
     *
     * <p>This method eliminates the need for null checks before calling {@code size()}.</p>
     *
     * <p><b>Examples:</b></p>
     * <pre>
     * List&lt;String&gt; list = Arrays.asList("a", "b", "c");
     * CollectionUtil.safeSize(list)           → 3
     * CollectionUtil.safeSize(null)           → 0
     * CollectionUtil.safeSize(new ArrayList&lt;&gt;()) → 0
     * </pre>
     *
     * <p><b>Common Use Cases:</b></p>
     * <ul>
     *     <li>DTO conversions: {@code user.getOrders() != null ? user.getOrders().size() : 0}
     *         becomes {@code CollectionUtil.safeSize(user.getOrders())}</li>
     *     <li>Displaying counts in UI without null checks</li>
     *     <li>Logging collection sizes safely</li>
     * </ul>
     *
     * @param collection The collection to get the size of (may be null)
     * @return The size of the collection, or 0 if null
     */
    public static int safeSize(Collection<?> collection) {
        return collection == null ? 0 : collection.size();
    }

    // =================================================================================================================
    // NULL/EMPTY VALIDATION METHODS
    // =================================================================================================================

    /**
     * Checks if a collection is null or empty.
     *
     * <p><b>Examples:</b></p>
     * <pre>
     * CollectionUtil.isNullOrEmpty(null)              → true
     * CollectionUtil.isNullOrEmpty(new ArrayList&lt;&gt;())  → true
     * CollectionUtil.isNullOrEmpty(Arrays.asList("a")) → false
     * </pre>
     *
     * @param collection The collection to check
     * @return {@code true} if the collection is null or empty, {@code false} otherwise
     */
    public static boolean isNullOrEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * Checks if a collection is not null and not empty.
     *
     * <p>This is the inverse of {@link #isNullOrEmpty(Collection)}.</p>
     *
     * <p><b>Examples:</b></p>
     * <pre>
     * CollectionUtil.isNotEmpty(null)              → false
     * CollectionUtil.isNotEmpty(new ArrayList&lt;&gt;())  → false
     * CollectionUtil.isNotEmpty(Arrays.asList("a")) → true
     * </pre>
     *
     * @param collection The collection to check
     * @return {@code true} if the collection is not null and not empty, {@code false} otherwise
     */
    public static boolean isNotEmpty(Collection<?> collection) {
        return !isNullOrEmpty(collection);
    }

    // =================================================================================================================
    // SAFE ELEMENT ACCESS METHODS
    // =================================================================================================================

    /**
     * Returns the first element of a list, or null if the list is null or empty.
     *
     * <p>This method provides safe access to the first element without index bounds checks.</p>
     *
     * <p><b>Examples:</b></p>
     * <pre>
     * List&lt;String&gt; list = Arrays.asList("first", "second", "third");
     * CollectionUtil.getFirstOrNull(list)       → "first"
     * CollectionUtil.getFirstOrNull(null)       → null
     * CollectionUtil.getFirstOrNull(new ArrayList&lt;&gt;()) → null
     * </pre>
     *
     * @param <T>  The type of elements in the list
     * @param list The list to get the first element from (may be null)
     * @return The first element, or null if the list is null or empty
     */
    public static <T> T getFirstOrNull(List<T> list) {
        return (list == null || list.isEmpty()) ? null : list.get(0);
    }

    /**
     * Returns the last element of a list, or null if the list is null or empty.
     *
     * <p>This method provides safe access to the last element without index bounds checks.</p>
     *
     * <p><b>Examples:</b></p>
     * <pre>
     * List&lt;String&gt; list = Arrays.asList("first", "second", "third");
     * CollectionUtil.getLastOrNull(list)        → "third"
     * CollectionUtil.getLastOrNull(null)        → null
     * CollectionUtil.getLastOrNull(new ArrayList&lt;&gt;()) → null
     * </pre>
     *
     * @param <T>  The type of elements in the list
     * @param list The list to get the last element from (may be null)
     * @return The last element, or null if the list is null or empty
     */
    public static <T> T getLastOrNull(List<T> list) {
        return (list == null || list.isEmpty()) ? null : list.get(list.size() - 1);
    }

    /**
     * Returns the element at the specified index, or null if the index is out of bounds or the list is null.
     *
     * <p>This method provides safe indexed access without {@link IndexOutOfBoundsException}.</p>
     *
     * <p><b>Examples:</b></p>
     * <pre>
     * List&lt;String&gt; list = Arrays.asList("a", "b", "c");
     * CollectionUtil.getOrNull(list, 0)     → "a"
     * CollectionUtil.getOrNull(list, 5)     → null (out of bounds)
     * CollectionUtil.getOrNull(list, -1)    → null (negative index)
     * CollectionUtil.getOrNull(null, 0)     → null
     * </pre>
     *
     * @param <T>   The type of elements in the list
     * @param list  The list to get the element from (may be null)
     * @param index The index of the element to retrieve
     * @return The element at the specified index, or null if out of bounds or list is null
     */
    public static <T> T getOrNull(List<T> list, int index) {
        if (list == null || index < 0 || index >= list.size()) {
            return null;
        }
        return list.get(index);
    }
}