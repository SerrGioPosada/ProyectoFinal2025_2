package co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.VehicleType;

/**
 * Utility class for automatic vehicle type selection and validation based on shipment characteristics.
 * Provides methods to determine appropriate vehicle types and validate weight constraints.
 */
public class VehicleSelector {

    // Vehicle capacity constants (in kg)
    private static final double MOTORCYCLE_MAX_WEIGHT = 30.0;
    private static final double CAR_MAX_WEIGHT = 200.0;
    private static final double VAN_MAX_WEIGHT = 800.0;
    private static final double TRUCK_MAX_WEIGHT = 5000.0;

    // Volume constants (in m³)
    private static final double MOTORCYCLE_MAX_VOLUME = 0.2;
    private static final double CAR_MAX_VOLUME = 1.5;
    private static final double VAN_MAX_VOLUME = 5.0;
    private static final double TRUCK_MAX_VOLUME = 20.0;

    /**
     * Automatically selects the most appropriate vehicle type based on weight, volume, and services.
     *
     * @param weightKg the package weight in kilograms
     * @param volumeM3 the package volume in cubic meters
     * @param isPriority whether the shipment has priority service
     * @param isFragile whether the shipment is fragile
     * @return the recommended VehicleType
     */
    public static VehicleType selectVehicleType(double weightKg, double volumeM3, boolean isPriority, boolean isFragile) {
        // RESTRICTION: Priority shipments MUST use motorcycle (fastest delivery)
        if (isPriority) {
            if (weightKg <= MOTORCYCLE_MAX_WEIGHT && volumeM3 <= MOTORCYCLE_MAX_VOLUME) {
                return VehicleType.MOTORCYCLE;
            } else {
                // If package is too heavy/large for motorcycle but has priority, use CAR
                // (next fastest option)
                if (weightKg <= CAR_MAX_WEIGHT && volumeM3 <= CAR_MAX_VOLUME) {
                    return VehicleType.CAR;
                } else {
                    return VehicleType.VAN; // Last resort for priority
                }
            }
        }

        // RESTRICTION: Fragile items CANNOT use motorcycle (too risky)
        if (isFragile) {
            if (weightKg <= CAR_MAX_WEIGHT && volumeM3 <= CAR_MAX_VOLUME) {
                return VehicleType.CAR;
            } else if (weightKg <= VAN_MAX_WEIGHT && volumeM3 <= VAN_MAX_VOLUME) {
                return VehicleType.VAN;
            } else {
                return VehicleType.TRUCK;
            }
        }

        // Standard selection based on weight and volume (most economical)
        if (weightKg <= MOTORCYCLE_MAX_WEIGHT && volumeM3 <= MOTORCYCLE_MAX_VOLUME) {
            return VehicleType.MOTORCYCLE;
        } else if (weightKg <= CAR_MAX_WEIGHT && volumeM3 <= CAR_MAX_VOLUME) {
            return VehicleType.CAR;
        } else if (weightKg <= VAN_MAX_WEIGHT && volumeM3 <= VAN_MAX_VOLUME) {
            return VehicleType.VAN;
        } else {
            return VehicleType.TRUCK;
        }
    }

    /**
     * Validates if a vehicle type can handle the given weight and volume.
     *
     * @param vehicleType the vehicle type to validate
     * @param weightKg the package weight in kilograms
     * @param volumeM3 the package volume in cubic meters
     * @return true if the vehicle can handle the load, false otherwise
     */
    public static boolean canHandleLoad(VehicleType vehicleType, double weightKg, double volumeM3) {
        switch (vehicleType) {
            case MOTORCYCLE:
                return weightKg <= MOTORCYCLE_MAX_WEIGHT && volumeM3 <= MOTORCYCLE_MAX_VOLUME;
            case CAR:
                return weightKg <= CAR_MAX_WEIGHT && volumeM3 <= CAR_MAX_VOLUME;
            case VAN:
                return weightKg <= VAN_MAX_WEIGHT && volumeM3 <= VAN_MAX_VOLUME;
            case TRUCK:
                return weightKg <= TRUCK_MAX_WEIGHT && volumeM3 <= TRUCK_MAX_VOLUME;
            default:
                return false;
        }
    }

    /**
     * Gets the maximum weight capacity for a vehicle type.
     *
     * @param vehicleType the vehicle type
     * @return the maximum weight in kg
     */
    public static double getMaxWeight(VehicleType vehicleType) {
        switch (vehicleType) {
            case MOTORCYCLE:
                return MOTORCYCLE_MAX_WEIGHT;
            case CAR:
                return CAR_MAX_WEIGHT;
            case VAN:
                return VAN_MAX_WEIGHT;
            case TRUCK:
                return TRUCK_MAX_WEIGHT;
            default:
                return 0.0;
        }
    }

    /**
     * Gets the maximum volume capacity for a vehicle type.
     *
     * @param vehicleType the vehicle type
     * @return the maximum volume in m³
     */
    public static double getMaxVolume(VehicleType vehicleType) {
        switch (vehicleType) {
            case MOTORCYCLE:
                return MOTORCYCLE_MAX_VOLUME;
            case CAR:
                return CAR_MAX_VOLUME;
            case VAN:
                return VAN_MAX_VOLUME;
            case TRUCK:
                return TRUCK_MAX_VOLUME;
            default:
                return 0.0;
        }
    }

    /**
     * Gets a user-friendly error message explaining why a vehicle type cannot handle the load.
     *
     * @param vehicleType the vehicle type
     * @param weightKg the package weight in kilograms
     * @param volumeM3 the package volume in cubic meters
     * @return error message or null if the vehicle can handle the load
     */
    public static String getValidationError(VehicleType vehicleType, double weightKg, double volumeM3) {
        if (canHandleLoad(vehicleType, weightKg, volumeM3)) {
            return null;
        }

        StringBuilder error = new StringBuilder("El vehículo tipo ");
        error.append(getVehicleTypeName(vehicleType));
        error.append(" no puede transportar este paquete:\n");

        double maxWeight = getMaxWeight(vehicleType);
        double maxVolume = getMaxVolume(vehicleType);

        if (weightKg > maxWeight) {
            error.append(String.format("• Peso excedido: %.1f kg (máximo: %.1f kg)\n", weightKg, maxWeight));
        }

        if (volumeM3 > maxVolume) {
            error.append(String.format("• Volumen excedido: %.3f m³ (máximo: %.3f m³)\n", volumeM3, maxVolume));
        }

        error.append("\nTipo de vehículo recomendado: ");
        error.append(getVehicleTypeName(selectVehicleType(weightKg, volumeM3, false, false)));

        return error.toString();
    }

    /**
     * Gets a user-friendly name for a vehicle type.
     *
     * @param vehicleType the vehicle type
     * @return the display name
     */
    public static String getVehicleTypeName(VehicleType vehicleType) {
        switch (vehicleType) {
            case MOTORCYCLE:
                return "Motocicleta";
            case CAR:
                return "Automóvil";
            case VAN:
                return "Camioneta";
            case TRUCK:
                return "Camión";
            default:
                return vehicleType.name();
        }
    }

    /**
     * Gets vehicle capacity information as a formatted string.
     *
     * @param vehicleType the vehicle type
     * @return formatted capacity information
     */
    public static String getCapacityInfo(VehicleType vehicleType) {
        return String.format("%s (Máx: %.1f kg, %.2f m³)",
            getVehicleTypeName(vehicleType),
            getMaxWeight(vehicleType),
            getMaxVolume(vehicleType)
        );
    }

    /**
     * Validates if a vehicle type is compatible with shipment services.
     *
     * @param vehicleType the vehicle type
     * @param isPriority whether the shipment has priority service
     * @param isFragile whether the shipment is fragile
     * @return error message if incompatible, null if compatible
     */
    public static String validateServiceCompatibility(VehicleType vehicleType, boolean isPriority, boolean isFragile) {
        // RESTRICTION: Fragile items cannot use motorcycle
        if (isFragile && vehicleType == VehicleType.MOTORCYCLE) {
            return "⚠️ RESTRICCIÓN: Los envíos frágiles NO pueden transportarse en motocicleta.\n" +
                   "Por seguridad, seleccione Automóvil, Camioneta o Camión.";
        }

        // RESTRICTION: Priority items should use motorcycle when possible
        if (isPriority && vehicleType != VehicleType.MOTORCYCLE && vehicleType != VehicleType.CAR) {
            return "⚠️ ADVERTENCIA: Los envíos prioritarios se entregan más rápido en Motocicleta o Automóvil.\n" +
                   "El vehículo seleccionado puede retrasar la entrega prioritaria.";
        }

        return null; // Compatible
    }

    /**
     * Gets a comprehensive validation message considering weight, volume, and services.
     *
     * @param vehicleType the selected vehicle type
     * @param weightKg the package weight in kilograms
     * @param volumeM3 the package volume in cubic meters
     * @param isPriority whether the shipment has priority service
     * @param isFragile whether the shipment is fragile
     * @return error/warning message or null if everything is valid
     */
    public static String getComprehensiveValidation(VehicleType vehicleType, double weightKg,
                                                    double volumeM3, boolean isPriority, boolean isFragile) {
        // First check service compatibility
        String serviceError = validateServiceCompatibility(vehicleType, isPriority, isFragile);
        if (serviceError != null) {
            return serviceError;
        }

        // Then check weight/volume capacity
        String capacityError = getValidationError(vehicleType, weightKg, volumeM3);
        if (capacityError != null) {
            return capacityError;
        }

        return null; // All validations passed
    }

    /**
     * Gets a recommendation message explaining why the suggested vehicle was chosen.
     *
     * @param vehicleType the recommended vehicle type
     * @param isPriority whether the shipment has priority service
     * @param isFragile whether the shipment is fragile
     * @return recommendation explanation
     */
    public static String getRecommendationReason(VehicleType vehicleType, boolean isPriority, boolean isFragile) {
        StringBuilder reason = new StringBuilder("ℹ️ VEHÍCULO RECOMENDADO: ");
        reason.append(getVehicleTypeName(vehicleType)).append("\n\n");
        reason.append("Razones:\n");

        if (isPriority) {
            if (vehicleType == VehicleType.MOTORCYCLE) {
                reason.append("• Envío prioritario: La motocicleta ofrece la entrega más rápida\n");
            } else {
                reason.append("• Envío prioritario: Vehículo más rápido compatible con el tamaño del paquete\n");
            }
        }

        if (isFragile) {
            reason.append("• Paquete frágil: Este vehículo garantiza un transporte más seguro\n");
        }

        if (!isPriority && !isFragile) {
            reason.append("• Opción más económica para el peso y volumen de su paquete\n");
        }

        return reason.toString();
    }
}
