package co.edu.uniquindio.poo.proyectofiinal2025_2.Util.Adapter;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.*;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Enums.ShipmentStatus;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.dto.ShipmentDTO;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Util.UtilModel.DistanceCalculator;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Utility class for converting between Shipment entities and ShipmentDTOs.
 * Handles data enrichment and transformation.
 */
public class ShipmentDTOConverter {

    /**
     * Converts a Shipment entity to a DTO with enriched data.
     * @param shipment The shipment entity
     * @param user The user who owns the shipment (can be null)
     * @param deliveryPerson The assigned delivery person (can be null)
     * @return The enriched ShipmentDTO
     */
    public static ShipmentDTO toDTO(Shipment shipment, User user, DeliveryPerson deliveryPerson) {
        if (shipment == null) {
            return null;
        }

        ShipmentDTO dto = new ShipmentDTO();

        // Basic fields
        dto.setId(shipment.getId());
        dto.setOrderId(shipment.getOrderId());
        dto.setUserId(shipment.getUserId());
        dto.setDeliveryPersonId(shipment.getDeliveryPersonId());
        dto.setOriginId(shipment.getOrigin() != null ? shipment.getOrigin().getId() : null);
        dto.setDestinationId(shipment.getDestination() != null ? shipment.getDestination().getId() : null);
        dto.setWeightKg(shipment.getWeightKg());
        dto.setHeightCm(shipment.getHeightCm());
        dto.setWidthCm(shipment.getWidthCm());
        dto.setLengthCm(shipment.getLengthCm());
        dto.setVolumeM3(shipment.getVolumeM3());
        dto.setBaseCost(shipment.getBaseCost());
        dto.setServicesCost(shipment.getServicesCost());
        dto.setTotalCost(shipment.getTotalCost());
        dto.setStatus(shipment.getStatus());
        dto.setPriority(shipment.getPriority());
        dto.setCreationDate(shipment.getCreatedAt());
        dto.setRequestedPickupDate(shipment.getRequestedPickupDate());
        dto.setAssignmentDate(shipment.getAssignmentDate());
        dto.setEstimatedDeliveryDate(shipment.getEstimatedDate());
        dto.setActualDeliveryDate(shipment.getDeliveredDate());
        dto.setAdditionalServices(shipment.getAdditionalServices());
        dto.setIncident(shipment.getIncident());
        dto.setUserNotes(shipment.getUserNotes());
        dto.setInternalNotes(shipment.getInternalNotes());
        dto.setActive(shipment.isActive());

        // Enrich with user data
        if (user != null) {
            dto.setUserName(user.getName() + " " + user.getLastName());
            dto.setUserPhone(user.getPhone());
            dto.setUserEmail(user.getEmail());
        }

        // Enrich with delivery person data
        if (deliveryPerson != null) {
            dto.setDeliveryPersonName(deliveryPerson.getName() + " " + deliveryPerson.getLastName());
            dto.setDeliveryPersonPhone(deliveryPerson.getPhone());
        }

        // Enrich with address data
        if (shipment.getOrigin() != null) {
            dto.setOriginAddressComplete(formatAddress(shipment.getOrigin()));
            dto.setOriginZone(shipment.getOrigin().getCity());
        }

        if (shipment.getDestination() != null) {
            dto.setDestinationAddressComplete(formatAddress(shipment.getDestination()));
            dto.setDestinationZone(shipment.getDestination().getCity());
        }

        // Calculate distance
        if (shipment.getOrigin() != null && shipment.getDestination() != null) {
            dto.setDistanceKm(DistanceCalculator.calculateDistance(shipment.getOrigin(), shipment.getDestination()));
        }

        // Calculate derived fields
        dto.setCanBeCancelled(canBeCancelled(shipment));
        dto.setCanBeModified(canBeModified(shipment));
        dto.setMinutesUntilDelivery(calculateMinutesUntilDelivery(shipment));
        dto.setDelayed(isDelayed(shipment));
        dto.setStatusDisplayName(shipment.getStatus() != null ? shipment.getStatus().getDisplayName() : "");
        dto.setStatusColor(shipment.getStatus() != null ? shipment.getStatus().getColor() : "");

        return dto;
    }

    /**
     * Converts a ShipmentDTO to a Shipment entity.
     * @param dto The shipment DTO
     * @param origin The origin address
     * @param destination The destination address
     * @return The Shipment entity
     */
    public static Shipment toEntity(ShipmentDTO dto, Address origin, Address destination) {
        if (dto == null) {
            return null;
        }

        return new Shipment.Builder()
                .withId(dto.getId())
                .withOrderId(dto.getOrderId())
                .withUserId(dto.getUserId())
                .withDeliveryPersonId(dto.getDeliveryPersonId())
                .withOrigin(origin)
                .withDestination(destination)
                .withCreatedAt(dto.getCreationDate())
                .withEstimatedDate(dto.getEstimatedDeliveryDate())
                .withDeliveredDate(dto.getActualDeliveryDate())
                .withAssignmentDate(dto.getAssignmentDate())
                .withRequestedPickupDate(dto.getRequestedPickupDate())
                .withStatus(dto.getStatus())
                .withWeightKg(dto.getWeightKg())
                .withHeightCm(dto.getHeightCm())
                .withWidthCm(dto.getWidthCm())
                .withLengthCm(dto.getLengthCm())
                .withVolumeM3(dto.getVolumeM3())
                .withBaseCost(dto.getBaseCost())
                .withServicesCost(dto.getServicesCost())
                .withTotalCost(dto.getTotalCost())
                .withPriority(dto.getPriority())
                .withAdditionalServices(dto.getAdditionalServices())
                .withIncident(dto.getIncident())
                .withUserNotes(dto.getUserNotes())
                .withInternalNotes(dto.getInternalNotes())
                .withActive(dto.isActive())
                .build();
    }

    /**
     * Formats an address for display.
     * @param address The address to format
     * @return Formatted address string
     */
    private static String formatAddress(Address address) {
        if (address == null) {
            return "";
        }

        return String.format("%s, %s, %s %s",
                address.getStreet(),
                address.getCity(),
                address.getState(),
                address.getZipCode());
    }

    /**
     * Determines if a shipment can be cancelled.
     * @param shipment The shipment to check
     * @return True if can be cancelled
     */
    private static boolean canBeCancelled(Shipment shipment) {
        if (shipment == null || shipment.getStatus() == null) {
            return false;
        }

        return shipment.getStatus() == ShipmentStatus.PENDING_ASSIGNMENT;
    }

    /**
     * Determines if a shipment can be modified.
     * @param shipment The shipment to check
     * @return True if can be modified
     */
    private static boolean canBeModified(Shipment shipment) {
        if (shipment == null || shipment.getStatus() == null) {
            return false;
        }

        return shipment.getStatus() == ShipmentStatus.PENDING_ASSIGNMENT;
    }

    /**
     * Calculates minutes until estimated delivery.
     * @param shipment The shipment
     * @return Minutes until delivery, or -1 if not applicable
     */
    private static long calculateMinutesUntilDelivery(Shipment shipment) {
        if (shipment == null || shipment.getEstimatedDate() == null) {
            return -1;
        }

        if (shipment.getStatus() == ShipmentStatus.DELIVERED ||
            shipment.getStatus() == ShipmentStatus.CANCELLED) {
            return -1;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime estimated = shipment.getEstimatedDate();

        if (estimated.isBefore(now)) {
            return 0; // Overdue
        }

        return Duration.between(now, estimated).toMinutes();
    }

    /**
     * Determines if a shipment is delayed.
     * @param shipment The shipment to check
     * @return True if delayed
     */
    private static boolean isDelayed(Shipment shipment) {
        if (shipment == null || shipment.getEstimatedDate() == null) {
            return false;
        }

        if (shipment.getStatus() == ShipmentStatus.DELIVERED ||
            shipment.getStatus() == ShipmentStatus.CANCELLED) {
            return false;
        }

        return LocalDateTime.now().isAfter(shipment.getEstimatedDate());
    }
}