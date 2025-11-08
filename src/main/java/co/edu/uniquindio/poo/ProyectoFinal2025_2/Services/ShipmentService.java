package co.edu.uniquindio.poo.ProyectoFinal2025_2.Services;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.*;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.AvailabilityStatus;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.IncidentType;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.ServiceType;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.ShipmentStatus;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.dto.*;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Repositories.*;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.DistanceCalculator;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.ShipmentValidator;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilService.IdGenerationUtil;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.Adapter.ShipmentDTOConverter;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>Manages the business logic related to shipments.</p>
 * <p>This service is responsible for creating, updating, and tracking shipments.
 * It is typically called by other services, like OrderService, after business
 * rules (like payment confirmation) have been met.</p>
 */
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final UserRepository userRepository;
    private final DeliveryPersonRepository deliveryPersonRepository;
    private final AddressRepository addressRepository;
    private final TariffService tariffService;

    // Constants
    private static final int DEFAULT_DELIVERY_TIME_HOURS = 24; // 24 hours default

    /**
     * Constructor with dependency injection.
     * @param shipmentRepository The shipment repository
     * @param userRepository The user repository
     * @param deliveryPersonRepository The delivery person repository
     * @param addressRepository The address repository
     * @param tariffService The tariff service for cost calculations
     */
    public ShipmentService(ShipmentRepository shipmentRepository,
                          UserRepository userRepository,
                          DeliveryPersonRepository deliveryPersonRepository,
                          AddressRepository addressRepository,
                          TariffService tariffService) {
        this.shipmentRepository = shipmentRepository;
        this.userRepository = userRepository;
        this.deliveryPersonRepository = deliveryPersonRepository;
        this.addressRepository = addressRepository;
        this.tariffService = tariffService;
    }

    /**
     * Default constructor using singleton instances.
     */
    public ShipmentService() {
        this(ShipmentRepository.getInstance(),
             UserRepository.getInstance(),
             DeliveryPersonRepository.getInstance(),
             AddressRepository.getInstance(),
             new TariffService());
    }

    // ===========================
    // Shipment Management
    // ===========================

    /**
     * Creates a new shipment for an approved order.
     * <p>In the new workflow, shipments are created when orders are approved.
     * They start with PENDING_ASSIGNMENT status and delivery person is assigned later.</p>
     *
     * @param order The approved order for which to create a shipment.
     * @return The unique ID of the newly created shipment.
     */
    public String createShipmentForOrder(Order order) {
        Shipment newShipment = new Shipment.Builder()
                .withId(IdGenerationUtil.generateId())
                .withOrderId(order.getId())
                .withUserId(order.getUserId())
                .withOrigin(order.getOrigin())
                .withDestination(order.getDestination())
                .withCreatedAt(LocalDateTime.now())
                .withStatus(ShipmentStatus.PENDING_ASSIGNMENT)
                .build();

        shipmentRepository.addShipment(newShipment);

        return newShipment.getId();
    }

    // ===========================
    // CRUD Operations
    // ===========================

    /**
     * Creates a new shipment from DTO.
     * @param dto The shipment data
     * @return The created shipment DTO
     * @throws IllegalArgumentException if validation fails
     */
    public ShipmentDTO createShipment(ShipmentDTO dto) {
        // Validate input
        List<String> errors = ShipmentValidator.validateShipmentData(dto);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Errores de validación: " + String.join(", ", errors));
        }

        // Load addresses
        Optional<Address> originOpt = addressRepository.findById(dto.getOriginId());
        Optional<Address> destinationOpt = addressRepository.findById(dto.getDestinationId());

        if (!originOpt.isPresent()) {
            throw new IllegalArgumentException("Dirección de origen no encontrada");
        }

        if (!destinationOpt.isPresent()) {
            throw new IllegalArgumentException("Dirección de destino no encontrada");
        }

        Address origin = originOpt.get();
        Address destination = destinationOpt.get();

        // Calculate volume
        double volume = (dto.getHeightCm() * dto.getWidthCm() * dto.getLengthCm()) / 1000000.0;

        // Calculate costs
        QuoteDTO quoteDTO = new QuoteDTO();
        quoteDTO.setOriginId(dto.getOriginId());
        quoteDTO.setDestinationId(dto.getDestinationId());
        quoteDTO.setWeightKg(dto.getWeightKg());
        quoteDTO.setHeightCm(dto.getHeightCm());
        quoteDTO.setWidthCm(dto.getWidthCm());
        quoteDTO.setLengthCm(dto.getLengthCm());
        quoteDTO.setAdditionalServices(dto.getAdditionalServices() != null ?
                dto.getAdditionalServices().stream().map(AdditionalService::getType).collect(Collectors.toList()) :
                new ArrayList<>());
        quoteDTO.setPriority(dto.getPriority());
        quoteDTO.setRequestedPickupDate(dto.getRequestedPickupDate());

        QuoteResultDTO quote = quoteShipment(quoteDTO);

        // Create shipment entity
        Shipment shipment = new Shipment.Builder()
                .withId(IdGenerationUtil.generateId())
                .withUserId(dto.getUserId())
                .withOrigin(origin)
                .withDestination(destination)
                .withCreatedAt(LocalDateTime.now())
                .withRequestedPickupDate(dto.getRequestedPickupDate())
                .withEstimatedDate(quote.getEstimatedDelivery())
                .withStatus(ShipmentStatus.PENDING_ASSIGNMENT)
                .withWeightKg(dto.getWeightKg())
                .withHeightCm(dto.getHeightCm())
                .withWidthCm(dto.getWidthCm())
                .withLengthCm(dto.getLengthCm())
                .withVolumeM3(volume)
                .withBaseCost(quote.getBaseCost() + quote.getWeightCost() + quote.getVolumeCost() + quote.getDistanceCost())
                .withServicesCost(quote.getServicesCost() + quote.getPriorityCost())
                .withTotalCost(quote.getTotalCost())
                .withPriority(dto.getPriority())
                .withUserNotes(dto.getUserNotes())
                .withActive(true)
                .build();

        // Add status history
        shipment.addStatusChange(new StatusChange(null, ShipmentStatus.PENDING_ASSIGNMENT, LocalDateTime.now()));

        // Add additional services
        if (dto.getAdditionalServices() != null) {
            for (AdditionalService service : dto.getAdditionalServices()) {
                shipment.addAdditionalService(service);
            }
        }

        shipmentRepository.addShipment(shipment);

        Logger.info("Shipment created: " + shipment.getId() + " for user: " + dto.getUserId());

        // Convert to DTO and return
        return getShipment(shipment.getId()).orElse(null);
    }

    /**
     * Updates an existing shipment.
     * @param dto The shipment data to update
     * @return The updated shipment DTO
     * @throws IllegalArgumentException if shipment not found or validation fails
     */
    public ShipmentDTO updateShipment(ShipmentDTO dto) {
        if (dto.getId() == null) {
            throw new IllegalArgumentException("ID del envío requerido para actualización");
        }

        Optional<Shipment> shipmentOpt = shipmentRepository.findById(dto.getId());
        if (!shipmentOpt.isPresent()) {
            throw new IllegalArgumentException("Envío no encontrado");
        }

        Shipment shipment = shipmentOpt.get();

        // Only allow updates if status is PENDING_APPROVAL
        if (shipment.getStatus() != ShipmentStatus.PENDING_ASSIGNMENT) {
            throw new IllegalArgumentException("Solo se pueden modificar envíos pendientes de aprobación");
        }

        // Update editable fields
        if (dto.getUserNotes() != null) {
            shipment.setUserNotes(dto.getUserNotes());
        }

        if (dto.getRequestedPickupDate() != null) {
            shipment.setRequestedPickupDate(dto.getRequestedPickupDate());
        }

        shipmentRepository.update(shipment);

        return getShipment(shipment.getId()).orElse(null);
    }

    /**
     * Cancels a shipment (soft delete).
     * @param id The shipment ID
     * @return true if cancelled successfully
     * @throws IllegalArgumentException if shipment not found or cannot be cancelled
     */
    public boolean cancelShipment(String id) {
        Optional<Shipment> shipmentOpt = shipmentRepository.findById(id);
        if (!shipmentOpt.isPresent()) {
            throw new IllegalArgumentException("Envío no encontrado");
        }

        Shipment shipment = shipmentOpt.get();

        // Can only cancel if PENDING_APPROVAL
        if (shipment.getStatus() != ShipmentStatus.PENDING_ASSIGNMENT) {
            throw new IllegalArgumentException("Solo se pueden cancelar envíos pendientes de aprobación");
        }

        ShipmentStatus previousStatus = shipment.getStatus();
        shipment.setStatus(ShipmentStatus.CANCELLED);
        shipment.addStatusChange(new StatusChange(previousStatus, ShipmentStatus.CANCELLED, LocalDateTime.now()));

        shipmentRepository.update(shipment);

        Logger.info("Shipment cancelled: " + id);

        return true;
    }

    /**
     * Gets a shipment by ID as DTO.
     * @param id The shipment ID
     * @return Optional containing the shipment DTO
     */
    public Optional<ShipmentDTO> getShipment(String id) {
        Optional<Shipment> shipmentOpt = shipmentRepository.findById(id);
        if (!shipmentOpt.isPresent()) {
            return Optional.empty();
        }

        Shipment shipment = shipmentOpt.get();

        // Load related entities
        User user = null;
        if (shipment.getUserId() != null) {
            user = userRepository.findById(shipment.getUserId()).orElse(null);
        }

        DeliveryPerson deliveryPerson = null;
        if (shipment.getDeliveryPersonId() != null) {
            deliveryPerson = deliveryPersonRepository.findDeliveryPersonById(shipment.getDeliveryPersonId()).orElse(null);
        }

        return Optional.of(ShipmentDTOConverter.toDTO(shipment, user, deliveryPerson));
    }

    /**
     * Lists all active shipments as DTOs.
     * @return List of shipment DTOs
     */
    public List<ShipmentDTO> listAll() {
        return shipmentRepository.findAll().stream()
                .filter(Shipment::isActive)
                .map(shipment -> {
                    User user = shipment.getUserId() != null ?
                            userRepository.findById(shipment.getUserId()).orElse(null) : null;
                    DeliveryPerson deliveryPerson = shipment.getDeliveryPersonId() != null ?
                            deliveryPersonRepository.findDeliveryPersonById(shipment.getDeliveryPersonId()).orElse(null) : null;
                    return ShipmentDTOConverter.toDTO(shipment, user, deliveryPerson);
                })
                .collect(Collectors.toList());
    }

    // ===========================
    // Quote Management
    // ===========================

    /**
     * Calculates a quote for a shipment using the Decorator pattern via TariffService.
     * @param quote The quote request
     * @return The quote result with cost breakdown
     */
    public QuoteResultDTO quoteShipment(QuoteDTO quote) {
        // Get addresses - accept both Address objects and IDs for backwards compatibility
        Address origin;
        Address destination;

        if (quote.getOrigin() != null && quote.getDestination() != null) {
            // Use provided Address objects directly
            origin = quote.getOrigin();
            destination = quote.getDestination();
        } else if (quote.getOriginId() != null && quote.getDestinationId() != null) {
            // Load from repository using IDs
            Optional<Address> originOpt = addressRepository.findById(quote.getOriginId());
            Optional<Address> destinationOpt = addressRepository.findById(quote.getDestinationId());

            if (!originOpt.isPresent() || !destinationOpt.isPresent()) {
                throw new IllegalArgumentException("Direcciones inválidas");
            }

            origin = originOpt.get();
            destination = destinationOpt.get();
        } else {
            throw new IllegalArgumentException("Debe proporcionar direcciones (objetos o IDs)");
        }

        // Calculate distance
        double distance = DistanceCalculator.calculateDistance(origin, destination);

        // Calculate volume
        double volume = (quote.getHeightCm() * quote.getWidthCm() * quote.getLengthCm()) / 1000000.0;

        // Determine if shipment has priority or fragile services
        boolean isPriority = quote.getPriority() > 0;
        boolean isFragile = quote.getAdditionalServices() != null &&
                quote.getAdditionalServices().contains(co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.ServiceType.FRAGILE);

        // Auto-select vehicle type if not provided, or validate if provided
        co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.VehicleType vehicleType;
        if (quote.getVehicleType() == null) {
            // Auto-select vehicle type based on weight, volume, priority, and fragile status
            vehicleType = co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.VehicleSelector.selectVehicleType(
                quote.getWeightKg(),
                volume,
                isPriority,
                isFragile
            );
        } else {
            // Validate provided vehicle type
            vehicleType = quote.getVehicleType();
            if (!co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.VehicleSelector.canHandleLoad(
                    vehicleType, quote.getWeightKg(), volume)) {
                String error = co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.VehicleSelector.getValidationError(
                    vehicleType, quote.getWeightKg(), volume);
                throw new IllegalArgumentException(error);
            }
        }

        // Use TariffService with Decorator pattern to get cost breakdown
        var breakdown = tariffService.getCostBreakdown(
            distance,
            quote.getWeightKg(),
            volume,
            quote.getPriority(),
            quote.getAdditionalServices()
        );

        // Extract costs from breakdown
        double baseCost = 0.0;
        double weightCost = 0.0;
        double volumeCost = 0.0;
        double distanceCost = 0.0;
        double servicesCost = 0.0;
        double priorityCost = 0.0;

        for (var item : breakdown) {
            String desc = item.getDescription();
            double amount = item.getAmount();

            if (desc.equals("Costo Base")) {
                baseCost = amount;
            } else if (desc.contains("Distancia")) {
                distanceCost = amount;
            } else if (desc.contains("Peso")) {
                weightCost = amount;
            } else if (desc.contains("Volumen")) {
                volumeCost = amount;
            } else if (desc.contains("Prioridad")) {
                priorityCost = amount;
            } else {
                // All other decorators are services (Insurance, Fragile, Signature)
                servicesCost += amount;
            }
        }

        // Calculate total using TariffService
        double totalCost = tariffService.calculateTotalCost(
            distance,
            quote.getWeightKg(),
            volume,
            quote.getPriority(),
            quote.getAdditionalServices()
        );

        // Calculate estimated delivery
        LocalDateTime estimatedDelivery = calculateEstimatedDelivery(distance, quote.getPriority(),
                quote.getRequestedPickupDate());

        Logger.info("Quote calculated using Decorator pattern - Total: $" + totalCost + ", Vehicle: " + vehicleType);

        // Create result with vehicle type
        QuoteResultDTO result = new QuoteResultDTO(baseCost, weightCost, volumeCost, distanceCost,
                servicesCost, priorityCost, totalCost, distance, estimatedDelivery);
        result.setRecommendedVehicleType(vehicleType);

        return result;
    }

    /**
     * Calculates estimated delivery time.
     * @param distanceKm Distance in kilometers
     * @param priority Priority level (1-5)
     * @param requestedPickup Requested pickup date
     * @return Estimated delivery date-time
     */
    private LocalDateTime calculateEstimatedDelivery(double distanceKm, int priority, LocalDateTime requestedPickup) {
        double travelTime = DistanceCalculator.estimateTravelTime(distanceKm);

        // Base delivery time
        int deliveryHours = DEFAULT_DELIVERY_TIME_HOURS;

        // Adjust for priority (higher priority = faster)
        if (priority > 3) {
            deliveryHours -= (priority - 3) * 4; // Reduce 4 hours per priority level
        }

        // Add travel time
        deliveryHours += (int) Math.ceil(travelTime);

        // Calculate from pickup time or now
        LocalDateTime startTime = requestedPickup != null ? requestedPickup : LocalDateTime.now();

        return startTime.plusHours(deliveryHours);
    }

    // ===========================
    // Status Management
    // ===========================

    /**
     * Changes the status of a shipment.
     * @param id Shipment ID
     * @param newStatus New status
     * @param reason Reason for the change
     * @param userId User making the change
     * @return true if changed successfully
     */
    public boolean changeStatus(String id, ShipmentStatus newStatus, String reason, String userId) {
        Optional<Shipment> shipmentOpt = shipmentRepository.findById(id);
        if (!shipmentOpt.isPresent()) {
            throw new IllegalArgumentException("Envío no encontrado");
        }

        Shipment shipment = shipmentOpt.get();
        ShipmentStatus oldStatus = shipment.getStatus();

        if (!ShipmentValidator.isValidStatusTransition(oldStatus, newStatus)) {
            throw new IllegalArgumentException(
                ShipmentValidator.getStatusTransitionError(oldStatus, newStatus));
        }

        shipment.setStatus(newStatus);
        StatusChange statusChange = new StatusChange(oldStatus, newStatus, LocalDateTime.now(), userId, reason);
        shipment.addStatusChange(statusChange);

        if (newStatus == ShipmentStatus.DELIVERED) {
            shipment.setDeliveredDate(LocalDateTime.now());
        }

        shipmentRepository.update(shipment);
        Logger.info("Shipment " + id + " status changed from " + oldStatus + " to " + newStatus);

        return true;
    }

    /**
     * Assigns a delivery person to a shipment.
     * In the new workflow, this changes status from PENDING_ASSIGNMENT to READY_FOR_PICKUP.
     * @param shipmentId Shipment ID
     * @param deliveryPersonId Delivery person ID
     * @return true if assigned successfully
     */
    public boolean assignDeliveryPerson(String shipmentId, String deliveryPersonId) {
        Optional<Shipment> shipmentOpt = shipmentRepository.findById(shipmentId);
        if (!shipmentOpt.isPresent()) {
            throw new IllegalArgumentException("Envío no encontrado");
        }

        Optional<DeliveryPerson> deliveryPersonOpt = deliveryPersonRepository.findDeliveryPersonById(deliveryPersonId);
        if (!deliveryPersonOpt.isPresent()) {
            throw new IllegalArgumentException("Repartidor no encontrado");
        }

        Shipment shipment = shipmentOpt.get();
        DeliveryPerson deliveryPerson = deliveryPersonOpt.get();

        // Allow assignment for PENDING_ASSIGNMENT or READY_FOR_PICKUP (reassignment)
        if (shipment.getStatus() != ShipmentStatus.PENDING_ASSIGNMENT &&
            shipment.getStatus() != ShipmentStatus.READY_FOR_PICKUP) {
            throw new IllegalArgumentException("Solo se pueden asignar envíos con estado PENDING_ASSIGNMENT o READY_FOR_PICKUP");
        }

        if (deliveryPerson.getAvailability() != AvailabilityStatus.AVAILABLE) {
            throw new IllegalArgumentException("Repartidor no disponible");
        }

        shipment.setDeliveryPersonId(deliveryPersonId);
        shipment.setAssignmentDate(LocalDateTime.now());

        // Change status from PENDING_ASSIGNMENT to READY_FOR_PICKUP
        if (shipment.getStatus() == ShipmentStatus.PENDING_ASSIGNMENT) {
            changeStatus(shipmentId, ShipmentStatus.READY_FOR_PICKUP, "Repartidor asignado", "SYSTEM");
        }

        shipmentRepository.update(shipment);
        Logger.info("Shipment " + shipmentId + " assigned to " + deliveryPersonId);

        return true;
    }

    /**
     * Auto-assigns shipments to available delivery persons.
     * @param zone Optional zone filter
     * @return Number of shipments assigned
     */
    public int autoAssignShipments(String zone) {
        List<Shipment> unassigned = zone != null ?
            shipmentRepository.findByZone(zone).stream()
                .filter(s -> s.getDeliveryPersonId() == null)
                .filter(s -> s.getStatus() == ShipmentStatus.READY_FOR_PICKUP)
                .collect(Collectors.toList()) :
            shipmentRepository.findUnassigned();

        if (unassigned.isEmpty()) return 0;

        List<DeliveryPerson> available = deliveryPersonRepository.getAllDeliveryPersons().stream()
            .filter(dp -> dp.getAvailability() == AvailabilityStatus.AVAILABLE)
            .collect(Collectors.toList());

        if (available.isEmpty()) return 0;

        int assigned = 0;
        for (Shipment shipment : unassigned) {
            DeliveryPerson best = available.stream()
                .min(Comparator.comparingInt(dp ->
                    shipmentRepository.findByDeliveryPerson(dp.getId()).size()))
                .orElse(null);

            if (best != null) {
                try {
                    assignDeliveryPerson(shipment.getId(), best.getId());
                    assigned++;
                } catch (Exception e) {
                    Logger.error("Failed to auto-assign shipment " + shipment.getId() + ": " + e.getMessage());
                }
            }
        }

        return assigned;
    }

    /**
     * Registers an incident for a shipment.
     * @param shipmentId Shipment ID
     * @param type Incident type
     * @param description Description
     * @param userId User registering the incident
     * @return true if registered successfully
     */
    public boolean registerIncident(String shipmentId, IncidentType type, String description, String userId) {
        Optional<Shipment> shipmentOpt = shipmentRepository.findById(shipmentId);
        if (!shipmentOpt.isPresent()) {
            throw new IllegalArgumentException("Envío no encontrado");
        }

        Shipment shipment = shipmentOpt.get();

        Incident incident = new Incident(
            IdGenerationUtil.generateId(),
            type,
            description,
            userId
        );

        shipment.setIncident(incident);
        shipmentRepository.update(shipment);

        Logger.info("Incident registered for shipment " + shipmentId);

        return true;
    }

    // ===========================
    // Query Methods
    // ===========================

    /**
     * Filters shipments based on criteria.
     * @param filter Filter criteria
     * @return List of filtered shipment DTOs
     */
    public List<ShipmentDTO> filterShipments(ShipmentFilterDTO filter) {
        return shipmentRepository.search(filter).stream()
            .map(shipment -> {
                User user = shipment.getUserId() != null ?
                    userRepository.findById(shipment.getUserId()).orElse(null) : null;
                DeliveryPerson deliveryPerson = shipment.getDeliveryPersonId() != null ?
                    deliveryPersonRepository.findDeliveryPersonById(shipment.getDeliveryPersonId()).orElse(null) : null;
                return ShipmentDTOConverter.toDTO(shipment, user, deliveryPerson);
            })
            .collect(Collectors.toList());
    }

    /**
     * Gets shipments for a specific user.
     * @param userId User ID
     * @return List of user's shipments
     */
    public List<ShipmentDTO> getShipmentsByUser(String userId) {
        return shipmentRepository.findByUser(userId).stream()
            .map(shipment -> {
                User user = userRepository.findById(userId).orElse(null);
                DeliveryPerson deliveryPerson = shipment.getDeliveryPersonId() != null ?
                    deliveryPersonRepository.findDeliveryPersonById(shipment.getDeliveryPersonId()).orElse(null) : null;
                return ShipmentDTOConverter.toDTO(shipment, user, deliveryPerson);
            })
            .collect(Collectors.toList());
    }

    /**
     * Gets delayed shipments.
     * @return List of delayed shipments
     */
    public List<ShipmentDTO> getDelayedShipments() {
        return shipmentRepository.findDelayed().stream()
            .map(shipment -> {
                User user = shipment.getUserId() != null ?
                    userRepository.findById(shipment.getUserId()).orElse(null) : null;
                DeliveryPerson deliveryPerson = shipment.getDeliveryPersonId() != null ?
                    deliveryPersonRepository.findDeliveryPersonById(shipment.getDeliveryPersonId()).orElse(null) : null;
                return ShipmentDTOConverter.toDTO(shipment, user, deliveryPerson);
            })
            .collect(Collectors.toList());
    }

    /**
     * Gets shipment statistics for a date range.
     * @param dateFrom Start date
     * @param dateTo End date
     * @return Statistics DTO
     */
    public ShipmentStatsDTO getStatistics(LocalDate dateFrom, LocalDate dateTo) {
        List<Shipment> shipments = shipmentRepository.findByDateRange(dateFrom, dateTo);

        long total = shipments.size();
        long pending = shipments.stream().filter(s -> s.getStatus() == ShipmentStatus.READY_FOR_PICKUP).count();
        long inTransit = shipments.stream().filter(s -> s.getStatus() == ShipmentStatus.IN_TRANSIT).count();
        long outForDelivery = shipments.stream().filter(s -> s.getStatus() == ShipmentStatus.OUT_FOR_DELIVERY).count();
        long delivered = shipments.stream().filter(s -> s.getStatus() == ShipmentStatus.DELIVERED).count();
        long cancelled = shipments.stream().filter(s -> s.getStatus() == ShipmentStatus.CANCELLED).count();
        long returned = shipments.stream().filter(s -> s.getStatus() == ShipmentStatus.RETURNED).count();
        long incidents = shipments.stream().filter(s -> s.getIncident() != null).count();

        // Calculate average delivery time
        double avgDeliveryTime = shipments.stream()
            .filter(s -> s.getStatus() == ShipmentStatus.DELIVERED)
            .filter(s -> s.getCreatedAt() != null && s.getDeliveredDate() != null)
            .mapToDouble(s -> Duration.between(s.getCreatedAt(), s.getDeliveredDate()).toHours())
            .average()
            .orElse(0.0);

        // Calculate success rate
        long completed = delivered + returned;
        double successRate = total > 0 ? (double) delivered / total * 100 : 0.0;

        // Calculate total revenue
        double totalRevenue = shipments.stream()
            .filter(s -> s.getStatus() != ShipmentStatus.CANCELLED)
            .mapToDouble(Shipment::getTotalCost)
            .sum();

        return new ShipmentStatsDTO(total, pending, inTransit, outForDelivery, delivered,
            cancelled, returned, incidents, avgDeliveryTime, successRate, totalRevenue);
    }
}
