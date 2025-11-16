package co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Controller.IndexController;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;

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
            Logger.error("Cannot load view '" + viewName + "': IndexController is null. (" + context.getSimpleName() + ")");
            return;
        }

        try {
            Logger.info("Navigating to view: " + viewName + " [" + context.getSimpleName() + "]");
            indexController.loadView(viewName);
        } catch (Exception e) {
            Logger.error("Error loading view '" + viewName + "': " + e.getMessage(), e);
        }
    }
}
