package co.edu.uniquindio.poo.proyectofinal2025_2.Util.UtilModel;

import co.edu.uniquindio.poo.proyectofinal2025_2.Model.Enums.ShipmentStatus;
import co.edu.uniquindio.poo.proyectofinal2025_2.Model.dto.ShipmentDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for validating shipment data.
 * Provides validation methods for shipment creation and updates.
 */
public class ShipmentValidator {

    private static final double MIN_WEIGHT = 0.1; // kg
    private static final double MAX_WEIGHT = 50.0; // kg
    private static final double MIN_DIMENSION = 1.0; // cm
    private static final double MAX_DIMENSION = 200.0; // cm

    /**
     * Validates shipment data before creation or update.
     * @param dto The shipment DTO to validate
     * @return List of validation error messages (empty if valid)
     */
    public static List<String> validateShipmentData(ShipmentDTO dto) {
        List<String> errors = new ArrayList<>();

        if (dto == null) {
            errors.add("Los datos del envío no pueden ser nulos");
            return errors;
        }

        // Validate addresses
        if (dto.getOriginId() == null || dto.getOriginId().trim().isEmpty()) {
            errors.add("Debe especificar una dirección de origen");
        }

        if (dto.getDestinationId() == null || dto.getDestinationId().trim().isEmpty()) {
            errors.add("Debe especificar una dirección de destino");
        }

        if (dto.getOriginId() != null && dto.getDestinationId() != null &&
            dto.getOriginId().equals(dto.getDestinationId())) {
            errors.add("La dirección de origen y destino deben ser diferentes");
        }

        // Validate weight
        if (dto.getWeightKg() < MIN_WEIGHT) {
            errors.add(String.format("El peso debe ser al menos %.1f kg", MIN_WEIGHT));
        }

        if (dto.getWeightKg() > MAX_WEIGHT) {
            errors.add(String.format("El peso no puede exceder %.1f kg", MAX_WEIGHT));
        }

        // Validate dimensions
        if (dto.getHeightCm() < MIN_DIMENSION || dto.getHeightCm() > MAX_DIMENSION) {
            errors.add(String.format("La altura debe estar entre %.0f y %.0f cm", MIN_DIMENSION, MAX_DIMENSION));
        }

        if (dto.getWidthCm() < MIN_DIMENSION || dto.getWidthCm() > MAX_DIMENSION) {
            errors.add(String.format("El ancho debe estar entre %.0f y %.0f cm", MIN_DIMENSION, MAX_DIMENSION));
        }

        if (dto.getLengthCm() < MIN_DIMENSION || dto.getLengthCm() > MAX_DIMENSION) {
            errors.add(String.format("El largo debe estar entre %.0f y %.0f cm", MIN_DIMENSION, MAX_DIMENSION));
        }

        // Validate priority
        if (dto.getPriority() < 1 || dto.getPriority() > 5) {
            errors.add("La prioridad debe estar entre 1 y 5");
        }

        return errors;
    }

    /**
     * Validates if a status transition is allowed.
     * @param from Current status
     * @param to New status
     * @return True if transition is valid, false otherwise
     */
    public static boolean isValidStatusTransition(ShipmentStatus from, ShipmentStatus to) {
        if (from == null || to == null) {
            return false;
        }

        if (from == to) {
            return false; // No self-transitions
        }

        return switch (from) {
            case PENDING_ASSIGNMENT -> to == ShipmentStatus.IN_TRANSIT ||
                                      to == ShipmentStatus.CANCELLED;
            case IN_TRANSIT -> to == ShipmentStatus.OUT_FOR_DELIVERY ||
                              to == ShipmentStatus.RETURNED ||
                              to == ShipmentStatus.CANCELLED;
            case OUT_FOR_DELIVERY -> to == ShipmentStatus.DELIVERED ||
                                     to == ShipmentStatus.RETURNED;
            case DELIVERED, RETURNED, CANCELLED -> false; // Terminal states
        };
    }

    /**
     * Gets a validation error message for an invalid status transition.
     * @param from Current status
     * @param to Attempted new status
     * @return Error message
     */
    public static String getStatusTransitionError(ShipmentStatus from, ShipmentStatus to) {
        if (from == null || to == null) {
            return "Estado inválido";
        }

        if (from == to) {
            return "El envío ya se encuentra en ese estado";
        }

        return String.format("No se puede cambiar de '%s' a '%s'",
                from.getDisplayName(), to.getDisplayName());
    }

    /**
     * Validates if package dimensions are reasonable.
     * @param height Height in cm
     * @param width Width in cm
     * @param length Length in cm
     * @return True if dimensions are reasonable
     */
    public static boolean areDimensionsReasonable(double height, double width, double length) {
        if (height <= 0 || width <= 0 || length <= 0) {
            return false;
        }

        // Check if dimensions are within range
        if (height < MIN_DIMENSION || height > MAX_DIMENSION) return false;
        if (width < MIN_DIMENSION || width > MAX_DIMENSION) return false;
        if (length < MIN_DIMENSION || length > MAX_DIMENSION) return false;

        // Calculate volume and check if reasonable
        double volumeM3 = (height * width * length) / 1000000.0;
        return volumeM3 <= 1.0; // Max 1 cubic meter
    }
}