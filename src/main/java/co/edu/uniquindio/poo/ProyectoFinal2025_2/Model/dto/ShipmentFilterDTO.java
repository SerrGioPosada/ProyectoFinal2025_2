package co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.dto;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.CoverageArea;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.ShipmentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

/**
 * Data Transfer Object for filtering shipments.
 * Used in search and filter operations.
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ShipmentFilterDTO {

    private ShipmentStatus status;
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private String userId;
    private String deliveryPersonId;
    private CoverageArea zone;
    private String searchText;
    private Integer minPriority;
    private Boolean onlyDelayed;
    private Boolean onlyWithIncidents;
    private Boolean onlyActive;
}
