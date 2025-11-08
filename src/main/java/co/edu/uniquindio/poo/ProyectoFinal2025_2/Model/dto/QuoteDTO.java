package co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.dto;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Address;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.ServiceType;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for requesting a shipment quote.
 * Contains all necessary information to calculate shipping costs.
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class QuoteDTO {

    // Address references - can be IDs or objects
    private String originId;
    private String destinationId;
    private Address origin;
    private Address destination;

    // Package details
    private double weightKg;
    private double heightCm;
    private double widthCm;
    private double lengthCm;

    // Service details
    private List<ServiceType> additionalServices;
    private int priority;
    private LocalDateTime requestedPickupDate;
    private VehicleType vehicleType; // Requested or auto-selected vehicle type
}
