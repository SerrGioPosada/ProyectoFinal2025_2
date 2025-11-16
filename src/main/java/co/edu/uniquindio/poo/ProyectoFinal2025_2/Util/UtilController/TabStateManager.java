package co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController;

import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 * Manages the persistent state of collapsible tab sections across application sessions.
 * Stores state PER USER to avoid sharing preferences between different application users.
 * Stores:
 * - Whether each tab section is expanded or collapsed
 * - Which tab is currently active in each section
 */
public class TabStateManager {

    private static final Preferences prefs = Preferences.userNodeForPackage(TabStateManager.class);
    private static final String EXPANDED_SUFFIX = "_expanded";
    private static final String ACTIVE_TAB_SUFFIX = "_activeTab";

    // In-memory cache for current session
    private static final Map<String, Boolean> expandedStates = new HashMap<>();
    private static final Map<String, String> activeTabStates = new HashMap<>();

    // Current user ID for scoping preferences
    private static String currentUserId = null;

    /**
     * Sets the current user ID to scope all preference operations.
     * Should be called when a user logs in.
     *
     * @param userId The ID of the currently logged-in user
     */
    public static void setCurrentUserId(String userId) {
        if (currentUserId != null && !currentUserId.equals(userId)) {
            // User changed, clear in-memory cache
            expandedStates.clear();
            activeTabStates.clear();
        }
        currentUserId = userId;
    }

    /**
     * Gets the current user ID.
     *
     * @return The current user ID, or null if not set
     */
    public static String getCurrentUserId() {
        return currentUserId;
    }

    /**
     * Builds a scoped key including the user ID.
     *
     * @param viewName The view name
     * @param suffix The suffix (e.g., "_expanded" or "_activeTab")
     * @return A scoped key like "user123_ManageVehicles_expanded"
     */
    private static String buildKey(String viewName, String suffix) {
        if (currentUserId == null) {
            // Fallback to non-scoped key if no user is set
            return viewName + suffix;
        }
        return currentUserId + "_" + viewName + suffix;
    }

    /**
     * Checks if a tab section is expanded.
     * Defaults to true (expanded) if no state is saved.
     *
     * @param viewName The name of the view (e.g., "ManageVehicles")
     * @return true if expanded, false if collapsed
     */
    public static boolean isExpanded(String viewName) {
        String key = buildKey(viewName, EXPANDED_SUFFIX);

        // Check in-memory cache first
        if (expandedStates.containsKey(key)) {
            return expandedStates.get(key);
        }

        // Load from preferences - DEFAULT TO TRUE (OPEN)
        boolean expanded = prefs.getBoolean(key, true);
        expandedStates.put(key, expanded);
        return expanded;
    }

    /**
     * Saves the expanded state of a tab section.
     *
     * @param viewName The name of the view
     * @param expanded true if expanded, false if collapsed
     */
    public static void setExpanded(String viewName, boolean expanded) {
        String key = buildKey(viewName, EXPANDED_SUFFIX);
        expandedStates.put(key, expanded);
        prefs.putBoolean(key, expanded);
    }

    /**
     * Gets the currently active tab for a view.
     * Defaults to the first tab if no state is saved.
     *
     * @param viewName The name of the view
     * @return The ID of the active tab (e.g., "stats", "filters")
     */
    public static String getActiveTab(String viewName) {
        String key = buildKey(viewName, ACTIVE_TAB_SUFFIX);

        // Check in-memory cache first
        if (activeTabStates.containsKey(key)) {
            return activeTabStates.get(key);
        }

        // Load from preferences (default to "stats")
        String activeTab = prefs.get(key, "stats");
        activeTabStates.put(key, activeTab);
        return activeTab;
    }

    /**
     * Saves the active tab for a view.
     *
     * @param viewName The name of the view
     * @param tabId The ID of the active tab
     */
    public static void setActiveTab(String viewName, String tabId) {
        String key = buildKey(viewName, ACTIVE_TAB_SUFFIX);
        activeTabStates.put(key, tabId);
        prefs.put(key, tabId);
    }

    /**
     * Clears all saved states (useful for testing or reset functionality).
     */
    public static void clearAllStates() {
        try {
            prefs.clear();
            expandedStates.clear();
            activeTabStates.clear();
        } catch (Exception e) {
            // Silently fail
        }
    }

    /**
     * Clears all states for the current user only.
     * Should be called when a user logs out to clean up their session data.
     */
    public static void clearCurrentUserStates() {
        if (currentUserId == null) {
            return;
        }

        try {
            // Remove all keys that start with the current user ID
            String[] keys = prefs.keys();
            String userPrefix = currentUserId + "_";
            for (String key : keys) {
                if (key.startsWith(userPrefix)) {
                    prefs.remove(key);
                }
            }

            // Clear in-memory cache
            expandedStates.clear();
            activeTabStates.clear();
        } catch (Exception e) {
            // Silently fail
        }
    }

    /**
     * Clears the current user ID when logging out.
     * Should be called when a user logs out.
     */
    public static void clearCurrentUserId() {
        currentUserId = null;
        expandedStates.clear();
        activeTabStates.clear();
    }

    // =================================================================================================================
    // Sidebar State Management
    // =================================================================================================================

    private static final String SIDEBAR_EXPANDED_KEY = "sidebar_expanded";
    private static Boolean sidebarExpanded = null;

    /**
     * Checks if the sidebar is expanded (open).
     * Defaults to true (expanded) if no state is saved.
     *
     * @return true if sidebar is expanded, false if collapsed
     */
    public static boolean isSidebarExpanded() {
        // Check in-memory cache first
        if (sidebarExpanded != null) {
            return sidebarExpanded;
        }

        // Build scoped key for current user
        String key = (currentUserId != null) ? currentUserId + "_" + SIDEBAR_EXPANDED_KEY : SIDEBAR_EXPANDED_KEY;

        // Load from preferences - DEFAULT TO TRUE (OPEN)
        sidebarExpanded = prefs.getBoolean(key, true);
        return sidebarExpanded;
    }

    /**
     * Saves the expanded state of the sidebar.
     *
     * @param expanded true if sidebar is expanded, false if collapsed
     */
    public static void setSidebarExpanded(boolean expanded) {
        sidebarExpanded = expanded;

        // Build scoped key for current user
        String key = (currentUserId != null) ? currentUserId + "_" + SIDEBAR_EXPANDED_KEY : SIDEBAR_EXPANDED_KEY;

        prefs.putBoolean(key, expanded);
    }
}
