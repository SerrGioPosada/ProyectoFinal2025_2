package co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilRepository;

/**
 * Centralized constants for all repository data file paths.
 *
 * <p>This utility eliminates scattered file path string literals across repository classes,
 * providing a single source of truth for all data file locations. It makes it easy to
 * reorganize data directories or change file naming conventions globally.</p>
 *
 * <p><b>Path Organization:</b></p>
 * <ul>
 *     <li><b>Person Entities:</b> {@link #ADMINS_PATH}, {@link #USERS_PATH}, {@link #DELIVERY_PERSONS_PATH}</li>
 *     <li><b>Business Entities:</b> {@link #ORDERS_PATH}, {@link #SHIPMENTS_PATH}, {@link #PAYMENTS_PATH}</li>
 *     <li><b>Supporting Entities:</b> {@link #INVOICES_PATH}, {@link #TARIFFS_PATH}, {@link #VEHICLES_PATH}, {@link #ADDRESSES_PATH}</li>
 * </ul>
 *
 * <p><b>Usage Examples:</b></p>
 * <pre>
 * // In Repositories:
 * public class AdminRepository {
 *     private static final String FILE_PATH = RepositoryPaths.ADMINS_PATH;
 *     // ...
 * }
 *
 * // Or directly in methods:
 * File file = new File(RepositoryPaths.USERS_PATH);
 * </pre>
 *
 * <p><b>Benefits:</b></p>
 * <ul>
 *     <li>Single point of modification for all data file paths</li>
 *     <li>Eliminates typos in file path strings</li>
 *     <li>IDE autocomplete for path names</li>
 *     <li>Easy to refactor directory structure</li>
 *     <li>Clear documentation of all data files in one place</li>
 * </ul>
 *
 * <p><b>Directory Structure:</b></p>
 * <pre>
 * data/
 * ├── admins.json
 * ├── users.json
 * ├── delivery_persons.json
 * ├── orders.json
 * ├── shipments.json
 * ├── payments.json
 * ├── invoices.json
 * ├── tariffs.json
 * ├── vehicles.json
 * └── addresses.json
 * </pre>
 *
 * @author Sistema de Gestión de Envíos
 * @version 1.0
 * @since 2025
 */
public final class RepositoryPaths {

    // =================================================================================================================
    // CONSTRUCTOR
    // =================================================================================================================

    /**
     * Private constructor to prevent instantiation.
     * This is a constants class with only static final fields.
     *
     * @throws UnsupportedOperationException if instantiation is attempted
     */
    private RepositoryPaths() {
        throw new UnsupportedOperationException("Constants class cannot be instantiated");
    }

    // =================================================================================================================
    // BASE DIRECTORY
    // =================================================================================================================

    /**
     * Base directory for all data files.
     * All repository JSON files are stored under this directory.
     */
    public static final String DATA_DIR = "data";

    // =================================================================================================================
    // PERSON ENTITY PATHS
    // =================================================================================================================

    /**
     * File path for administrators data.
     *
     * <p><b>Used by:</b> {@code AdminRepository}</p>
     * <p><b>Contains:</b> Admin entities with employee IDs and permission levels</p>
     */
    public static final String ADMINS_PATH = DATA_DIR + "/admins.json";

    /**
     * File path for regular users data.
     *
     * <p><b>Used by:</b> {@code UserRepository}</p>
     * <p><b>Contains:</b> User entities with orders and addresses</p>
     */
    public static final String USERS_PATH = DATA_DIR + "/users.json";

    /**
     * File path for delivery persons data.
     *
     * <p><b>Used by:</b> {@code DeliveryPersonRepository}</p>
     * <p><b>Contains:</b> Delivery person entities with vehicle and availability info</p>
     */
    public static final String DELIVERY_PERSONS_PATH = DATA_DIR + "/delivery_persons.json";

    // =================================================================================================================
    // BUSINESS ENTITY PATHS
    // =================================================================================================================

    /**
     * File path for orders data.
     *
     * <p><b>Used by:</b> {@code OrderRepository}</p>
     * <p><b>Contains:</b> Order entities with items, prices, and status</p>
     */
    public static final String ORDERS_PATH = DATA_DIR + "/orders.json";

    /**
     * File path for shipments data.
     *
     * <p><b>Used by:</b> {@code ShipmentRepository}</p>
     * <p><b>Contains:</b> Shipment entities with tracking numbers and delivery info</p>
     */
    public static final String SHIPMENTS_PATH = DATA_DIR + "/shipments.json";

    /**
     * File path for payments data.
     *
     * <p><b>Used by:</b> {@code PaymentRepository}</p>
     * <p><b>Contains:</b> Payment entities with transaction details</p>
     */
    public static final String PAYMENTS_PATH = DATA_DIR + "/payments.json";

    /**
     * File path for payment methods data.
     *
     * <p><b>Used by:</b> {@code PaymentMethodRepository}</p>
     * <p><b>Contains:</b> Payment method entities registered by users</p>
     */
    public static final String PAYMENT_METHODS_PATH = DATA_DIR + "/payment_methods.json";

    /**
     * File path for invoices data.
     *
     * <p><b>Used by:</b> {@code InvoiceRepository}</p>
     * <p><b>Contains:</b> Invoice entities with billing information</p>
     */
    public static final String INVOICES_PATH = DATA_DIR + "/invoices.json";

    // =================================================================================================================
    // SUPPORTING ENTITY PATHS
    // =================================================================================================================

    /**
     * File path for tariffs data.
     *
     * <p><b>Used by:</b> {@code TariffRepository}</p>
     * <p><b>Contains:</b> Tariff entities with pricing rules</p>
     */
    public static final String TARIFFS_PATH = DATA_DIR + "/tariffs.json";

    /**
     * File path for vehicles data.
     *
     * <p><b>Used by:</b> {@code VehicleRepository}</p>
     * <p><b>Contains:</b> Vehicle entities assigned to delivery persons</p>
     */
    public static final String VEHICLES_PATH = DATA_DIR + "/vehicles.json";

    /**
     * File path for addresses data.
     *
     * <p><b>Used by:</b> {@code AddressRepository}</p>
     * <p><b>Contains:</b> Address entities for deliveries and users</p>
     */
    public static final String ADDRESSES_PATH = DATA_DIR + "/addresses.json";
}
