package co.edu.uniquindio.poo.ProyectoFinal2025_2.Repositories;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Shipment;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.ShipmentStatus;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.dto.ShipmentFilterDTO;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilRepository.GsonProvider;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilRepository.JsonFileHandler;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilRepository.RepositoryPaths;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Manages the persistence and retrieval of Shipment entities using a HashMap for fast lookups by ID.
 * <p>This class is implemented as a Singleton and saves data to a local JSON file.</p>
 */
public class ShipmentRepository {

    // --- Attributes for Persistence ---
    private final Gson gson = GsonProvider.createGson();
    private static ShipmentRepository instance;
    private final Map<String, Shipment> shipmentsById;

    /**
     * Private constructor that loads data from the file upon initialization.
     */
    private ShipmentRepository() {
        this.shipmentsById = new HashMap<>();
        loadFromFile(); // Load existing shipments
    }

    /**
     * Returns the singleton instance of the repository.
     *
     * @return the unique instance of {@code ShipmentRepository}
     */
    public static synchronized ShipmentRepository getInstance() {
        if (instance == null) {
            instance = new ShipmentRepository();
        }
        return instance;
    }

    // ======================
    // Private file handling methods
    // ======================

    /**
     * Saves the current list of shipments to the shipments.json file.
     */
    private void saveToFile() {
        List<Shipment> shipmentList = new ArrayList<>(shipmentsById.values());
        JsonFileHandler.saveToFile(RepositoryPaths.SHIPMENTS_PATH, shipmentList, gson);
    }

    /**
     * Loads the list of shipments from the shipments.json file when the application starts.
     */
    private void loadFromFile() {
        Type listType = new TypeToken<ArrayList<Shipment>>() {}.getType();
        Optional<List<Shipment>> loadedShipments = JsonFileHandler.loadFromFile(
                RepositoryPaths.SHIPMENTS_PATH,
                listType,
                gson
        );

        loadedShipments.ifPresent(shipments -> {
            Logger.info("Loading " + shipments.size() + " shipments from file...");
            for (Shipment shipment : shipments) {
                shipmentsById.put(shipment.getId(), shipment);
            }
            Logger.info("Successfully loaded " + shipmentsById.size() + " shipments");
        });
    }

    // ======================
    // Handling methods
    // ======================

    /**
     * Adds a new shipment to the repository and persists the change.
     *
     * @param shipment the shipment to add
     */
    public void addShipment(Shipment shipment) {
        shipmentsById.put(shipment.getId(), shipment);
        saveToFile();
    }

    /**
     * Updates an existing shipment and persists the change.
     *
     * @param shipment the shipment to update
     * @return the updated shipment
     */
    public Shipment update(Shipment shipment) {
        if (shipment == null || shipment.getId() == null) {
            return null;
        }
        shipmentsById.put(shipment.getId(), shipment);
        saveToFile();
        return shipment;
    }

    /**
     * Soft deletes a shipment by setting its active flag to false.
     *
     * @param id the ID of the shipment to delete
     * @return true if deleted successfully, false otherwise
     */
    public boolean softDelete(String id) {
        Optional<Shipment> shipmentOpt = findById(id);
        if (!shipmentOpt.isPresent()) {
            return false;
        }

        Shipment shipment = shipmentOpt.get();
        shipment.setActive(false);
        update(shipment);
        return true;
    }

    /**
     * Permanently deletes a shipment from the repository.
     *
     * @param id the ID of the shipment to delete
     * @return true if deleted successfully, false otherwise
     */
    public boolean deleteShipment(String id) {
        if (id == null || id.trim().isEmpty()) {
            Logger.warning("Cannot delete shipment: Invalid ID");
            return false;
        }

        Shipment shipmentToDelete = shipmentsById.get(id);
        if (shipmentToDelete != null) {
            Logger.info("Permanently deleting shipment: " + id);
            shipmentsById.remove(id);
            saveToFile();
            return true;
        } else {
            Logger.warning("Cannot delete shipment: Shipment with ID " + id + " not found");
            return false;
        }
    }

    // ======================
    // Query methods
    // ======================

    /**
     * Finds a shipment by its ID with O(1) complexity.
     *
     * @param id the ID to search for
     * @return an {@link Optional} containing the shipment if found, or empty otherwise
     */
    public Optional<Shipment> findById(String id) {
        return Optional.ofNullable(shipmentsById.get(id));
    }

    /**
     * Retrieves all shipments stored in the repository.
     *
     * @return a new list containing all shipments
     */
    public List<Shipment> findAll() {
        return new ArrayList<>(shipmentsById.values());
    }

    /**
     * Finds all shipments belonging to a specific user.
     *
     * @param userId the user ID
     * @return list of shipments for the user
     */
    public List<Shipment> findByUser(String userId) {
        if (userId == null) {
            return new ArrayList<>();
        }

        return shipmentsById.values().stream()
                .filter(s -> userId.equals(s.getUserId()))
                .filter(Shipment::isActive)
                .collect(Collectors.toList());
    }

    /**
     * Finds all shipments assigned to a specific delivery person.
     *
     * @param deliveryPersonId the delivery person ID
     * @return list of shipments for the delivery person
     */
    public List<Shipment> findByDeliveryPerson(String deliveryPersonId) {
        if (deliveryPersonId == null) {
            return new ArrayList<>();
        }

        return shipmentsById.values().stream()
                .filter(s -> deliveryPersonId.equals(s.getDeliveryPersonId()))
                .filter(Shipment::isActive)
                .collect(Collectors.toList());
    }

    /**
     * Finds all shipments with a specific status.
     *
     * @param status the shipment status
     * @return list of shipments with the given status
     */
    public List<Shipment> findByStatus(ShipmentStatus status) {
        if (status == null) {
            return new ArrayList<>();
        }

        return shipmentsById.values().stream()
                .filter(s -> status.equals(s.getStatus()))
                .filter(Shipment::isActive)
                .collect(Collectors.toList());
    }

    /**
     * Finds shipments created within a date range.
     *
     * @param start start date (inclusive)
     * @param end end date (inclusive)
     * @return list of shipments in the date range
     */
    public List<Shipment> findByDateRange(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            return new ArrayList<>();
        }

        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(23, 59, 59);

        return shipmentsById.values().stream()
                .filter(s -> s.getCreatedAt() != null)
                .filter(s -> !s.getCreatedAt().isBefore(startDateTime))
                .filter(s -> !s.getCreatedAt().isAfter(endDateTime))
                .filter(Shipment::isActive)
                .collect(Collectors.toList());
    }

    /**
     * Finds shipments by zone (origin or destination city).
     *
     * @param zone the city/zone name
     * @return list of shipments in the zone
     */
    public List<Shipment> findByZone(String zone) {
        if (zone == null || zone.trim().isEmpty()) {
            return new ArrayList<>();
        }

        return shipmentsById.values().stream()
                .filter(s -> s.getOrigin() != null || s.getDestination() != null)
                .filter(s -> (s.getOrigin() != null && zone.equalsIgnoreCase(s.getOrigin().getCity())) ||
                            (s.getDestination() != null && zone.equalsIgnoreCase(s.getDestination().getCity())))
                .filter(Shipment::isActive)
                .collect(Collectors.toList());
    }

    /**
     * Finds all unassigned shipments (no delivery person assigned).
     *
     * @return list of unassigned shipments
     */
    public List<Shipment> findUnassigned() {
        return shipmentsById.values().stream()
                .filter(s -> s.getDeliveryPersonId() == null)
                .filter(s -> s.getStatus() == ShipmentStatus.READY_FOR_PICKUP)
                .filter(Shipment::isActive)
                .collect(Collectors.toList());
    }

    /**
     * Finds all delayed shipments (estimated delivery date has passed).
     *
     * @return list of delayed shipments
     */
    public List<Shipment> findDelayed() {
        LocalDateTime now = LocalDateTime.now();

        return shipmentsById.values().stream()
                .filter(s -> s.getEstimatedDate() != null)
                .filter(s -> s.getEstimatedDate().isBefore(now))
                .filter(s -> s.getStatus() != ShipmentStatus.DELIVERED)
                .filter(s -> s.getStatus() != ShipmentStatus.CANCELLED)
                .filter(Shipment::isActive)
                .collect(Collectors.toList());
    }

    /**
     * Finds shipments with incidents.
     *
     * @return list of shipments with incidents
     */
    public List<Shipment> findWithIncidents() {
        return shipmentsById.values().stream()
                .filter(s -> s.getIncident() != null)
                .filter(Shipment::isActive)
                .collect(Collectors.toList());
    }

    /**
     * Searches shipments using advanced filtering.
     *
     * @param filter the filter criteria
     * @return list of shipments matching the filter
     */
    public List<Shipment> search(ShipmentFilterDTO filter) {
        if (filter == null) {
            return findAll();
        }

        return shipmentsById.values().stream()
                .filter(s -> matchesFilter(s, filter))
                .collect(Collectors.toList());
    }

    /**
     * Checks if a shipment matches the filter criteria.
     *
     * @param shipment the shipment to check
     * @param filter the filter criteria
     * @return true if shipment matches filter
     */
    private boolean matchesFilter(Shipment shipment, ShipmentFilterDTO filter) {
        // Filter by active status
        if (filter.getOnlyActive() != null && filter.getOnlyActive() && !shipment.isActive()) {
            return false;
        }

        // Filter by status
        if (filter.getStatus() != null && !filter.getStatus().equals(shipment.getStatus())) {
            return false;
        }

        // Filter by date range
        if (filter.getDateFrom() != null || filter.getDateTo() != null) {
            if (shipment.getCreatedAt() == null) return false;

            if (filter.getDateFrom() != null) {
                LocalDateTime startDateTime = filter.getDateFrom().atStartOfDay();
                if (shipment.getCreatedAt().isBefore(startDateTime)) return false;
            }

            if (filter.getDateTo() != null) {
                LocalDateTime endDateTime = filter.getDateTo().atTime(23, 59, 59);
                if (shipment.getCreatedAt().isAfter(endDateTime)) return false;
            }
        }

        // Filter by user
        if (filter.getUserId() != null && !filter.getUserId().equals(shipment.getUserId())) {
            return false;
        }

        // Filter by delivery person
        if (filter.getDeliveryPersonId() != null && !filter.getDeliveryPersonId().equals(shipment.getDeliveryPersonId())) {
            return false;
        }

        // Filter by zone - comparing CoverageArea enum with delivery person's coverage area
        if (filter.getZone() != null) {
            // Note: This filter compares the CoverageArea enum, not city strings
            // The zone filter is typically used in conjunction with delivery person filtering
            // For now, we skip this filter as it doesn't directly map to shipment properties
            // TODO: Consider adding a zone field to Shipment or using delivery person's coverage area
        }

        // Filter by search text (ID or address)
        if (filter.getSearchText() != null && !filter.getSearchText().trim().isEmpty()) {
            String searchLower = filter.getSearchText().toLowerCase();
            boolean matchesSearch = shipment.getId().toLowerCase().contains(searchLower) ||
                                   (shipment.getOrigin() != null && shipment.getOrigin().getStreet().toLowerCase().contains(searchLower)) ||
                                   (shipment.getDestination() != null && shipment.getDestination().getStreet().toLowerCase().contains(searchLower));
            if (!matchesSearch) return false;
        }

        // Filter by priority
        if (filter.getMinPriority() != null && shipment.getPriority() < filter.getMinPriority()) {
            return false;
        }

        // Filter by delayed
        if (filter.getOnlyDelayed() != null && filter.getOnlyDelayed()) {
            if (shipment.getEstimatedDate() == null || !shipment.getEstimatedDate().isBefore(LocalDateTime.now())) {
                return false;
            }
        }

        // Filter by incidents
        if (filter.getOnlyWithIncidents() != null && filter.getOnlyWithIncidents()) {
            if (shipment.getIncident() == null) return false;
        }

        return true;
    }
}
