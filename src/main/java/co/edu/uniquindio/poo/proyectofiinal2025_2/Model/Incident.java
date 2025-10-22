package co.edu.uniquindio.poo.proyectofiinal2025_2.Model;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Enums.IncidentType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents an incident that occurred during a shipment.
 * Used to track and document issues with deliveries.
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Incident {

    private String id;
    private IncidentType type;
    private String description;
    private LocalDateTime registrationDate;
    private String registeredBy;
    private boolean resolved;
    private String solution;
    private LocalDateTime resolvedDate;

    /**
     * Constructor for creating a new incident.
     * @param id The incident ID
     * @param type The type of incident
     * @param description The description of what happened
     * @param registeredBy The ID of the user who registered the incident
     */
    public Incident(String id, IncidentType type, String description, String registeredBy) {
        this.id = id;
        this.type = type;
        this.description = description;
        this.registrationDate = LocalDateTime.now();
        this.registeredBy = registeredBy;
        this.resolved = false;
    }

    /**
     * Marks the incident as resolved with a solution.
     * @param solution The solution description
     */
    public void resolve(String solution) {
        this.resolved = true;
        this.solution = solution;
        this.resolvedDate = LocalDateTime.now();
    }
}