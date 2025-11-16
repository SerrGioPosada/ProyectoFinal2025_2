package co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Represents a tracking event in the combined Order + Shipment timeline.
 * Used to show a unified tracking history to users.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TrackingEventDTO implements Comparable<TrackingEventDTO> {

    private String status;           // Status name (e.g., "AWAITING_PAYMENT", "PENDING_ASSIGNMENT")
    private String displayName;      // User-friendly display name
    private String description;      // Description of what happened
    private LocalDateTime timestamp; // When it happened
    private String color;            // Color for UI display
    private boolean isOrderEvent;    // true if from Order, false if from Shipment
    private boolean isCompleted;     // true if this event has occurred

    @Override
    public int compareTo(TrackingEventDTO other) {
        if (this.timestamp == null && other.timestamp == null) return 0;
        if (this.timestamp == null) return 1;
        if (other.timestamp == null) return -1;
        return this.timestamp.compareTo(other.timestamp);
    }
}
