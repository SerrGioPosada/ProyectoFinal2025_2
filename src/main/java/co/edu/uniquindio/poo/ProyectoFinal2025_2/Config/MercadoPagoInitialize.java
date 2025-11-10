package co.edu.uniquindio.poo.ProyectoFinal2025_2.Config;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;
import com.mercadopago.MercadoPagoConfig;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Configuration class for Mercado Pago integration.
 *
 * <p>This class manages the initialization and configuration of Mercado Pago SDK,
 * loading credentials from a secure properties file.</p>
 *
 * <h2>Setup Instructions:</h2>
 * <ol>
 *     <li>Create a file named {@code mercadopago.properties} in the project root</li>
 *     <li>Add your Mercado Pago credentials:
 *         <pre>
 *         mercadopago.access.token=YOUR_ACCESS_TOKEN
 *         mercadopago.public.key=YOUR_PUBLIC_KEY
 *         mercadopago.environment=sandbox
 *         </pre>
 *     </li>
 *     <li>IMPORTANT: Add {@code mercadopago.properties} to {@code .gitignore}</li>
 * </ol>
 *
 * <h2>How to get credentials:</h2>
 * <ol>
 *     <li>Go to <a href="https://www.mercadopago.com.co/developers">Mercado Pago Developers</a></li>
 *     <li>Login with your Mercado Pago account</li>
 *     <li>Go to "Tus aplicaciones" → "Crear aplicación"</li>
 *     <li>Copy your <b>Access Token</b> and <b>Public Key</b></li>
 *     <li>Use <b>TEST</b> credentials for development (sandbox)</li>
 *     <li>Use <b>PRODUCTION</b> credentials only when ready to go live</li>
 * </ol>
 */
public class MercadoPagoInitialize {

    private static final String CONFIG_FILE = "mercadopago.properties";
    private static String accessToken;
    private static String publicKey;
    private static String environment;
    private static boolean initialized = false;

    /**
     * Initializes Mercado Pago configuration.
     * This method should be called once at application startup.
     *
     * @throws RuntimeException if configuration fails
     */
    public static void initialize() {
        if (initialized) {
            Logger.info("Mercado Pago already initialized");
            return;
        }

        try {
            loadConfiguration();
            configureSdk();
            initialized = true;
            Logger.info("Mercado Pago initialized successfully in " + environment + " mode");
        } catch (Exception e) {
            Logger.error("Failed to initialize Mercado Pago: " + e.getMessage());
            throw new RuntimeException("Mercado Pago initialization failed", e);
        }
    }

    /**
     * Loads configuration from properties file.
     */
    private static void loadConfiguration() throws IOException {
        Properties props = new Properties();

        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            props.load(fis);

            accessToken = props.getProperty("mercadopago.access.token");
            publicKey = props.getProperty("mercadopago.public.key");
            environment = props.getProperty("mercadopago.environment", "sandbox");

            validateConfiguration();

        } catch (IOException e) {
            Logger.error("Could not load " + CONFIG_FILE);
            Logger.error("Please create mercadopago.properties file with your credentials");
            throw e;
        }
    }

    /**
     * Validates that all required configuration is present.
     */
    private static void validateConfiguration() {
        if (accessToken == null || accessToken.trim().isEmpty()) {
            throw new IllegalStateException("mercadopago.access.token is not configured");
        }

        if (publicKey == null || publicKey.trim().isEmpty()) {
            throw new IllegalStateException("mercadopago.public.key is not configured");
        }

        if (!environment.equals("sandbox") && !environment.equals("production")) {
            throw new IllegalStateException("mercadopago.environment must be 'sandbox' or 'production'");
        }

        Logger.info("Mercado Pago configuration validated");
    }

    /**
     * Configures the Mercado Pago SDK.
     */
    private static void configureSdk() {
        MercadoPagoConfig.setAccessToken(accessToken);

        com.mercadopago.MercadoPagoConfig.setConnectionTimeout(5000);            // tiempo máximo para conectar
        com.mercadopago.MercadoPagoConfig.setConnectionRequestTimeout(5000);     // tiempo máximo para obtener conexión del pool
        com.mercadopago.MercadoPagoConfig.setSocketTimeout(5000);                // tiempo máximo esperando respuesta

        Logger.info("Mercado Pago SDK configured");
    }

    /**
     * Gets the access token.
     *
     * @return The access token
     */
    public static String getAccessToken() {
        ensureInitialized();
        return accessToken;
    }

    /**
     * Gets the public key.
     *
     * @return The public key
     */
    public static String getPublicKey() {
        ensureInitialized();
        return publicKey;
    }

    /**
     * Gets the environment (sandbox or production).
     *
     * @return The environment
     */
    public static String getEnvironment() {
        ensureInitialized();
        return environment;
    }

    /**
     * Checks if running in sandbox mode.
     *
     * @return true if sandbox, false if production
     */
    public static boolean isSandbox() {
        ensureInitialized();
        return "sandbox".equals(environment);
    }

    /**
     * Checks if Mercado Pago is initialized.
     *
     * @return true if initialized, false otherwise
     */
    public static boolean isInitialized() {
        return initialized;
    }

    /**
     * Ensures that Mercado Pago is initialized before use.
     */
    private static void ensureInitialized() {
        if (!initialized) {
            throw new IllegalStateException("Mercado Pago not initialized. Call initialize() first.");
        }
    }

    /**
     * Gets a masked version of the access token for logging.
     *
     * @return Masked access token
     */
    public static String getMaskedAccessToken() {
        if (accessToken == null || accessToken.length() < 8) {
            return "****";
        }
        return accessToken.substring(0, 4) + "..." + accessToken.substring(accessToken.length() - 4);
    }
}
