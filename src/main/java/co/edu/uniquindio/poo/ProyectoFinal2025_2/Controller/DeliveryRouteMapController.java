package co.edu.uniquindio.poo.ProyectoFinal2025_2.Controller;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Address;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.DeliveryPerson;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.ShipmentStatus;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.dto.ShipmentDTO;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.AuthenticationService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.ShipmentService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.DialogUtil;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.DistanceCalculator;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for the Delivery Route Map view (DeliveryRouteMap.fxml).
 * <p>
 * This controller manages the route map visualization for delivery persons,
 * showing pickup and delivery points, calculating distances, and optimizing routes.
 * </p>
 */
public class DeliveryRouteMapController implements Initializable {

    // =================================================================================================================
    // FXML Fields
    // =================================================================================================================

    @FXML private Label lblTotalPoints;
    @FXML private Label lblPickups;
    @FXML private Label lblDeliveries;
    @FXML private Label lblTotalDistance;
    @FXML private Label lblMapStatus;

    @FXML private ComboBox<String> cmbMapView;
    @FXML private StackPane mapContainer;

    @FXML private ListView<String> listPickupPoints;
    @FXML private ListView<String> listDeliveryPoints;

    // =================================================================================================================
    // Services
    // =================================================================================================================

    private final AuthenticationService authService;
    private final ShipmentService shipmentService;

    private DeliveryPerson currentDeliveryPerson;
    private List<ShipmentDTO> assignedShipments;

    // =================================================================================================================
    // Constructor
    // =================================================================================================================

    /**
     * Default constructor. Initializes services.
     */
    public DeliveryRouteMapController() {
        this.authService = AuthenticationService.getInstance();
        this.shipmentService = new ShipmentService();
        this.assignedShipments = new ArrayList<>();
    }

    // =================================================================================================================
    // Initialization
    // =================================================================================================================

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Logger.info("Initializing DeliveryRouteMapController");

        // Get current delivery person
        this.currentDeliveryPerson = (DeliveryPerson) authService.getCurrentPerson();

        if (currentDeliveryPerson == null) {
            DialogUtil.showError("Error", "No se pudo obtener la información del repartidor.");
            return;
        }

        // Initialize map view options
        initializeMapViewOptions();

        // Load route data
        loadRouteData();

        Logger.info("DeliveryRouteMapController initialized successfully");
    }

    /**
     * Initializes the map view options in the ComboBox.
     */
    private void initializeMapViewOptions() {
        cmbMapView.getItems().addAll(
                "Vista General",
                "Solo Recolecciones",
                "Solo Entregas",
                "Vista Satélite"
        );
        cmbMapView.getSelectionModel().selectFirst();
        cmbMapView.setOnAction(e -> handleMapViewChange());
    }

    // =================================================================================================================
    // Data Loading
    // =================================================================================================================

    /**
     * Loads route data including pickup and delivery points.
     */
    private void loadRouteData() {
        try {
            // Get assigned shipments
            assignedShipments = shipmentService.listAll().stream()
                    .filter(shipment -> currentDeliveryPerson.getId().equals(shipment.getDeliveryPersonId()))
                    .filter(shipment -> shipment.getStatus() == ShipmentStatus.READY_FOR_PICKUP ||
                            shipment.getStatus() == ShipmentStatus.IN_TRANSIT ||
                            shipment.getStatus() == ShipmentStatus.OUT_FOR_DELIVERY)
                    .collect(Collectors.toList());

            // Update statistics
            updateStatistics();

            // Update route points lists
            updateRoutePointsLists();

            // Update map status
            lblMapStatus.setText(assignedShipments.isEmpty() ?
                    "No hay envíos asignados" :
                    "Mostrando " + assignedShipments.size() + " envío(s) en ruta");

            Logger.info("Route data loaded successfully: " + assignedShipments.size() + " shipments");

        } catch (Exception e) {
            Logger.error("Error loading route data: " + e.getMessage());
            DialogUtil.showError("Error", "No se pudo cargar la información de la ruta.");
        }
    }

    /**
     * Updates the statistics cards with current route information.
     */
    private void updateStatistics() {
        int pickupCount = 0;
        int deliveryCount = 0;
        double totalDistance = 0.0;

        for (ShipmentDTO shipment : assignedShipments) {
            if (shipment.getStatus() == ShipmentStatus.READY_FOR_PICKUP) {
                pickupCount++;
            }
            if (shipment.getStatus() == ShipmentStatus.IN_TRANSIT || shipment.getStatus() == ShipmentStatus.OUT_FOR_DELIVERY) {
                deliveryCount++;
            }

            // Use precalculated distance from DTO
            totalDistance += shipment.getDistanceKm();
        }

        int totalPoints = pickupCount + deliveryCount;

        lblTotalPoints.setText(String.valueOf(totalPoints));
        lblPickups.setText(String.valueOf(pickupCount));
        lblDeliveries.setText(String.valueOf(deliveryCount));
        lblTotalDistance.setText(String.format("%.2f km", totalDistance));
    }

    /**
     * Updates the ListView components with pickup and delivery points.
     */
    private void updateRoutePointsLists() {
        List<String> pickupPoints = new ArrayList<>();
        List<String> deliveryPoints = new ArrayList<>();

        for (ShipmentDTO shipment : assignedShipments) {
            String shipmentId = "Envío #" + shipment.getId();

            // Pickup points (ready for pickup shipments)
            if (shipment.getStatus() == ShipmentStatus.READY_FOR_PICKUP && shipment.getOriginAddressComplete() != null) {
                String pickupInfo = String.format("🔴 %s - %s",
                        shipmentId,
                        shipment.getOriginAddressComplete());
                pickupPoints.add(pickupInfo);
            }

            // Delivery points (in transit or out for delivery)
            if ((shipment.getStatus() == ShipmentStatus.IN_TRANSIT || shipment.getStatus() == ShipmentStatus.OUT_FOR_DELIVERY)
                    && shipment.getDestinationAddressComplete() != null) {
                String deliveryInfo = String.format("🔵 %s - %s",
                        shipmentId,
                        shipment.getDestinationAddressComplete());
                deliveryPoints.add(deliveryInfo);
            }
        }

        listPickupPoints.getItems().setAll(pickupPoints);
        listDeliveryPoints.getItems().setAll(deliveryPoints);

        // Set placeholders
        listPickupPoints.setPlaceholder(new Label("No hay puntos de recolección pendientes"));
        listDeliveryPoints.setPlaceholder(new Label("No hay puntos de entrega pendientes"));
    }

    // =================================================================================================================
    // Event Handlers
    // =================================================================================================================

    /**
     * Handles the refresh route button action.
     */
    @FXML
    private void handleRefreshRoute() {
        Logger.info("Refreshing route data");
        loadRouteData();
        DialogUtil.showInfo("Éxito", "Ruta actualizada exitosamente");
    }

    /**
     * Handles the optimize route button action.
     * This is a simplified version - in a real app, this would use a routing algorithm.
     */
    @FXML
    private void handleOptimizeRoute() {
        if (assignedShipments.isEmpty()) {
            DialogUtil.showWarning("Sin Envíos", "No hay envíos para optimizar la ruta.");
            return;
        }

        Logger.info("Optimizing route for " + assignedShipments.size() + " shipments");

        // Simple optimization: sort by distance from a reference point
        // In a real app, this would use a proper routing algorithm (TSP, etc.)
        try {
            assignedShipments.sort((s1, s2) -> {
                if (s1.getOriginZone() == null || s2.getOriginZone() == null) {
                    return 0;
                }
                // Sort by origin zone
                return s1.getOriginZone().compareTo(s2.getOriginZone());
            });

            updateRoutePointsLists();
            DialogUtil.showInfo("Éxito", "Ruta optimizada exitosamente");
            Logger.info("Route optimized successfully");

        } catch (Exception e) {
            Logger.error("Error optimizing route: " + e.getMessage());
            DialogUtil.showError("Error", "No se pudo optimizar la ruta.");
        }
    }

    /**
     * Handles map view change from ComboBox.
     */
    private void handleMapViewChange() {
        String selectedView = cmbMapView.getSelectionModel().getSelectedItem();
        Logger.info("Map view changed to: " + selectedView);

        // In a real app, this would filter the map markers
        // For now, we just update the status label
        lblMapStatus.setText("Vista: " + selectedView);
    }
}
