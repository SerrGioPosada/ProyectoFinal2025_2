package co.edu.uniquindio.poo.proyectofinal2025_2.Model.dto;

import co.edu.uniquindio.poo.proyectofinal2025_2.Model.AdditionalService;
import co.edu.uniquindio.poo.proyectofinal2025_2.Model.Enums.ShipmentStatus;
import co.edu.uniquindio.poo.proyectofinal2025_2.Model.Incident;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for Shipment with enriched data from related entities.
 * Used to transfer shipment information between layers with user-friendly data.
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ShipmentDTO {

    // Basic shipment fields
    private String id;
    private String orderId;
    private String userId;
    private String deliveryPersonId;
    private String originId;
    private String destinationId;
    private double weightKg;
    private double heightCm;
    private double widthCm;
    private double lengthCm;
    private double volumeM3;
    private double baseCost;
    private double servicesCost;
    private double totalCost;
    private ShipmentStatus status;
    private int priority;
    private LocalDateTime creationDate;
    private LocalDateTime requestedPickupDate;
    private LocalDateTime assignmentDate;
    private LocalDateTime estimatedDeliveryDate;
    private LocalDateTime actualDeliveryDate;
    private List<AdditionalService> additionalServices;
    private Incident incident;
    private String userNotes;
    private String internalNotes;
    private boolean active;

    // Calculated/joined fields for UI
    private String userName;
    private String userPhone;
    private String userEmail;
    private String deliveryPersonName;
    private String deliveryPersonPhone;
    private String originAddressComplete;
    private String destinationAddressComplete;
    private String originZone;
    private String destinationZone;
    private double distanceKm;
    private boolean canBeCancelled;
    private boolean canBeModified;
    private long minutesUntilDelivery;
    private boolean isDelayed;
    private String statusDisplayName;
    private String statusColor;
}