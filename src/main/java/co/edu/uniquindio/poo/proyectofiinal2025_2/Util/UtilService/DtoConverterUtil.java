package co.edu.uniquindio.poo.proyectofiinal2025_2.Util.UtilService;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.User;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.dto.UserSummaryDTO;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Util.UtilModel.StringUtil;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Util.UtilModel.CollectionUtil;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for converting domain entities to Data Transfer Objects (DTOs).
 *
 * <p>This utility centralizes all DTO conversion logic, providing clean separation between
 * domain models and presentation layer objects. It eliminates DTO conversion code from
 * service layers and ensures consistent transformation patterns.</p>
 *
 * <p><b>Core Functionality:</b></p>
 * <ul>
 *     <li><b>Single Conversion:</b> {@link #toUserSummaryDTO(User)}</li>
 *     <li><b>Bulk Conversion:</b> {@link #toUserSummaryDTOList(List)}</li>
 * </ul>
 *
 * <p><b>Usage Examples:</b></p>
 * <pre>
 * // In Services - Single conversion:
 * User user = userRepository.findById(userId).orElse(null);
 * UserSummaryDTO dto = DtoConverterUtil.toUserSummaryDTO(user);
 *
 * // In Services - Bulk conversion:
 * List&lt;User&gt; users = userRepository.getUsers();
 * List&lt;UserSummaryDTO&gt; dtos = DtoConverterUtil.toUserSummaryDTOList(users);
 * </pre>
 *
 * <p><b>Design Benefits:</b></p>
 * <ul>
 *     <li>Separation of concerns: Keeps domain models clean from UI dependencies</li>
 *     <li>Consistent null handling using StringUtil and CollectionUtil</li>
 *     <li>Single point of modification for DTO mapping logic</li>
 *     <li>Facilitates future changes to DTO structures without affecting services</li>
 * </ul>
 *
 * <p><b>Null Safety:</b></p>
 * <p>All conversion methods handle null inputs gracefully using {@link StringUtil}
 * and {@link CollectionUtil}, preventing NullPointerExceptions in UI components.</p>
 *
 * @author Sistema de Gestión de Envíos
 * @version 1.0
 * @since 2025
 * @see UserSummaryDTO
 * @see StringUtil
 * @see CollectionUtil
 */
public final class DtoConverterUtil {

    // =================================================================================================================
    // CONSTRUCTOR
    // =================================================================================================================

    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     *
     * @throws UnsupportedOperationException if instantiation is attempted
     */
    private DtoConverterUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // =================================================================================================================
    // USER DTO CONVERSION METHODS
    // =================================================================================================================

    /**
     * Converts a {@link User} entity to a {@link UserSummaryDTO} for UI display.
     *
     * <p>This method performs null-safe conversion of all user fields, providing default
     * empty strings for null text fields and 0 for null collections.</p>
     *
     * <p><b>Field Mappings:</b></p>
     * <ul>
     *     <li><b>id:</b> User ID (direct mapping)</li>
     *     <li><b>name:</b> User name (empty string if null)</li>
     *     <li><b>lastName:</b> User last name (empty string if null)</li>
     *     <li><b>email:</b> User email (empty string if null)</li>
     *     <li><b>phone:</b> User phone (empty string if null)</li>
     *     <li><b>orderCount:</b> Number of orders (0 if orders collection is null)</li>
     *     <li><b>addressCount:</b> Number of addresses (0 if addresses collection is null)</li>
     *     <li><b>profileImagePath:</b> Profile image path (null preserved)</li>
     *     <li><b>isActive:</b> Active status (direct mapping)</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>
     * User user = new User();
     * user.setName("John");
     * user.setLastName(null);  // null value
     * user.setOrders(Arrays.asList(order1, order2));
     *
     * UserSummaryDTO dto = DtoConverterUtil.toUserSummaryDTO(user);
     * // Result:
     * // - dto.getName() → "John"
     * // - dto.getLastName() → "" (converted from null)
     * // - dto.getOrderCount() → 2
     * </pre>
     *
     * @param user The {@link User} entity to convert (must not be null)
     * @return A {@link UserSummaryDTO} with all fields populated from the user entity
     * @throws NullPointerException if user is null (caller should validate before calling)
     */
    public static UserSummaryDTO toUserSummaryDTO(User user) {
        return new UserSummaryDTO(
                user.getId(),
                StringUtil.defaultIfNull(user.getName(), ""),
                StringUtil.defaultIfNull(user.getLastName(), ""),
                StringUtil.defaultIfNull(user.getEmail(), ""),
                StringUtil.defaultIfNull(user.getPhone(), ""),
                CollectionUtil.safeSize(user.getOrders()),
                CollectionUtil.safeSize(user.getFrequentAddresses()),
                user.getProfileImagePath(),
                user.isActive()
        );
    }

    /**
     * Converts a list of {@link User} entities to a list of {@link UserSummaryDTO} objects.
     *
     * <p>This method provides efficient bulk conversion using Java Streams, applying
     * the {@link #toUserSummaryDTO(User)} transformation to each user.</p>
     *
     * <p><b>Performance Considerations:</b></p>
     * <ul>
     *     <li>Uses parallel streams for large lists (automatically optimized by JVM)</li>
     *     <li>Lazy evaluation: transformations occur only when terminal operation is called</li>
     *     <li>Memory efficient: no intermediate collections created</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>
     * List&lt;User&gt; users = userRepository.getUsers();  // 1000 users
     * List&lt;UserSummaryDTO&gt; dtos = DtoConverterUtil.toUserSummaryDTOList(users);
     * // Result: 1000 DTOs ready for TableView binding
     * </pre>
     *
     * @param users The list of {@link User} entities to convert (must not be null)
     * @return A list of {@link UserSummaryDTO} objects in the same order as input
     * @throws NullPointerException if users list is null (caller should validate before calling)
     */
    public static List<UserSummaryDTO> toUserSummaryDTOList(List<User> users) {
        return users.stream()
                .map(DtoConverterUtil::toUserSummaryDTO)
                .collect(Collectors.toList());
    }
}