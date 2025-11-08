package co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for in-app notifications.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private String id;
    private String title;
    private String message;
    private String type; // NEW_SHIPMENT, STATUS_UPDATE, MESSAGE, SYSTEM_ALERT
    private LocalDateTime timestamp;
    private boolean read;
    private String priority; // low, medium, high
}
