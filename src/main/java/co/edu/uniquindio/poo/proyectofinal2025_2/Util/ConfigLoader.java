package co.edu.uniquindio.poo.proyectofinal2025_2.Util;

import co.edu.uniquindio.poo.proyectofinal2025_2.Util.UtilModel.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utility class to load configuration files from the config directory.
 * Provides access to sensitive credentials without hardcoding them in source code.
 */
public class ConfigLoader {

    private static final String CONFIG_DIR = "config/";
    private static final String OAUTH_CONFIG_FILE = CONFIG_DIR + "oauth.properties";

    private static Properties oauthProperties = null;

    /**
     * Loads OAuth configuration from oauth.properties file.
     * This method is called lazily and caches the properties after first load.
     *
     * @return Properties object containing OAuth configuration
     * @throws RuntimeException if the configuration file cannot be loaded
     */
    public static Properties getOAuthConfig() {
        if (oauthProperties == null) {
            oauthProperties = loadProperties(OAUTH_CONFIG_FILE);
        }
        return oauthProperties;
    }

    /**
     * Gets the Google OAuth Client ID from configuration.
     *
     * @return The Client ID
     */
    public static String getGoogleClientId() {
        return getOAuthConfig().getProperty("google.client.id");
    }

    /**
     * Gets the Google OAuth Client Secret from configuration.
     *
     * @return The Client Secret
     */
    public static String getGoogleClientSecret() {
        return getOAuthConfig().getProperty("google.client.secret");
    }

    /**
     * Gets the Google OAuth Redirect Port from configuration.
     *
     * @return The redirect port as integer, defaults to 8888 if not configured
     */
    public static int getGoogleRedirectPort() {
        String port = getOAuthConfig().getProperty("google.redirect.port", "8888");
        return Integer.parseInt(port);
    }

    /**
     * Loads properties from a file.
     *
     * @param filePath Path to the properties file
     * @return Properties object
     * @throws RuntimeException if file cannot be loaded
     */
    private static Properties loadProperties(String filePath) {
        Properties properties = new Properties();

        try (InputStream input = new FileInputStream(filePath)) {
            properties.load(input);
            Logger.info("Successfully loaded configuration from: " + filePath);
        } catch (IOException e) {
            Logger.error("Failed to load configuration file: " + filePath, e);
            Logger.error("Make sure the file exists and is properly configured.");
            Logger.error("See config/oauth.properties.example for template.");
            throw new RuntimeException("Configuration file not found or invalid: " + filePath +
                    ". Please create it from the template at config/oauth.properties.example", e);
        }

        return properties;
    }

    /**
     * Validates that all required OAuth properties are present and non-empty.
     *
     * @throws IllegalStateException if any required property is missing
     */
    public static void validateOAuthConfig() {
        Properties config = getOAuthConfig();

        validateProperty(config, "google.client.id");
        validateProperty(config, "google.client.secret");
        validateProperty(config, "google.redirect.port");

        Logger.info("OAuth configuration validated successfully");
    }

    /**
     * Validates that a specific property exists and is not empty.
     *
     * @param properties The properties object to check
     * @param propertyName The name of the property to validate
     * @throws IllegalStateException if property is missing or empty
     */
    private static void validateProperty(Properties properties, String propertyName) {
        String value = properties.getProperty(propertyName);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException(
                    "Missing or empty required property: " + propertyName +
                            " in OAuth configuration file"
            );
        }
    }
}
