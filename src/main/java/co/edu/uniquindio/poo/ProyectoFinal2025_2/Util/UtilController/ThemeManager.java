package co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;
import javafx.scene.Scene;

import java.util.prefs.Preferences;

/**
 * Manages application theming (light/dark mode).
 * Persists user theme preference using Java Preferences API.
 */
public class ThemeManager {

    private static ThemeManager instance;
    private static final String THEME_PREFERENCE_KEY = "app_theme";
    private static final String DARK_THEME = "dark";
    private static final String LIGHT_THEME = "light";

    private final Preferences preferences;
    private String currentTheme;
    private Scene currentScene;

    private ThemeManager() {
        this.preferences = Preferences.userNodeForPackage(ThemeManager.class);
        this.currentTheme = preferences.get(THEME_PREFERENCE_KEY, LIGHT_THEME);
    }

    public static ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }

    /**
     * Sets the scene to apply themes to.
     */
    public void setScene(Scene scene) {
        this.currentScene = scene;
        applyTheme();
    }

    /**
     * Returns true if dark theme is active.
     */
    public boolean isDarkTheme() {
        return DARK_THEME.equals(currentTheme);
    }

    /**
     * Toggles between light and dark theme.
     */
    public void toggleTheme() {
        if (isDarkTheme()) {
            setTheme(LIGHT_THEME);
        } else {
            setTheme(DARK_THEME);
        }
    }

    /**
     * Sets the theme to light or dark.
     */
    public void setTheme(String theme) {
        if (!LIGHT_THEME.equals(theme) && !DARK_THEME.equals(theme)) {
            Logger.error("Invalid theme: " + theme);
            return;
        }

        this.currentTheme = theme;
        preferences.put(THEME_PREFERENCE_KEY, theme);
        applyTheme();

        Logger.info("Theme changed to: " + theme);
    }

    /**
     * Applies the current theme to the scene.
     */
    private void applyTheme() {
        if (currentScene == null) {
            return;
        }

        currentScene.getStylesheets().clear();

        // Always load base styles first
        String baseStylesheet = getClass().getResource("/co/edu/uniquindio/poo/ProyectoFinal2025_2/Style.css").toExternalForm();
        currentScene.getStylesheets().add(baseStylesheet);

        // Apply dark theme if active
        if (isDarkTheme()) {
            String darkStylesheet = getClass().getResource("/co/edu/uniquindio/poo/ProyectoFinal2025_2/DarkTheme.css").toExternalForm();
            currentScene.getStylesheets().add(darkStylesheet);
        }
    }

    /**
     * Gets the current theme name.
     */
    public String getCurrentTheme() {
        return currentTheme;
    }

    /**
     * Applies the current theme to any Scene (useful for dialogs and modal windows).
     * @param scene The scene to apply the theme to
     */
    public void applyThemeToScene(Scene scene) {
        if (scene == null) {
            return;
        }

        scene.getStylesheets().clear();

        // Always load base styles first
        String baseStylesheet = getClass().getResource("/co/edu/uniquindio/poo/ProyectoFinal2025_2/Style.css").toExternalForm();
        scene.getStylesheets().add(baseStylesheet);

        // Apply dark theme if active
        if (isDarkTheme()) {
            String darkStylesheet = getClass().getResource("/co/edu/uniquindio/poo/ProyectoFinal2025_2/DarkTheme.css").toExternalForm();
            scene.getStylesheets().add(darkStylesheet);
        }
    }
}
