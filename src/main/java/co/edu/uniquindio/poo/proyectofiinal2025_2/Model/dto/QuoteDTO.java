package co.edu.uniquindio.poo.proyectofiinal2025_2.Model.dto;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Enums.ServiceType;
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

    private String originId;
    private String destinationId;
    private double weightKg;
    private double heightCm;
    private double widthCm;
    private double lengthCm;
    private List<ServiceType> additionalServices;
    private int priority;
    private LocalDateTime requestedPickupDate;
}