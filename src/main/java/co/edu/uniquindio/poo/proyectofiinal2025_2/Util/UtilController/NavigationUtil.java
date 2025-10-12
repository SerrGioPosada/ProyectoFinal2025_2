package co.edu.uniquindio.poo.proyectofiinal2025_2.Util.UtilController;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Controller.IndexController;

public class NavigationUtil {

    /**
     * Handles safe navigation to a specific FXML view through the IndexController.
     *
     * @param indexController The main IndexController managing the central content area.
     * @param viewName The FXML view file to load (e.g., "AdminDashboard.fxml").
     * @param context The class calling this method (used for logging context).
     */
    public static void navigate(IndexController indexController, String viewName, Class<?> context) {
        if (indexController == null) {
            System.err.println("Cannot load view '" + viewName + "': IndexController is null. (" + context.getSimpleName() + ")");
            return;
        }

        try {
            System.out.println("Navigating to view: " + viewName + " [" + context.getSimpleName() + "]");
            indexController.loadView(viewName);
        } catch (Exception e) {
            System.err.println("Error loading view '" + viewName + "': " + e.getMessage());
        }
    }
}
