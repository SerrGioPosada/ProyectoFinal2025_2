package co.edu.uniquindio.poo.ProyectoFinal2025_2.Services;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.dto.NotificationDTO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for managing in-app notifications.
 * Handles real-time notifications for delivery persons and users.
 */
public class NotificationService {

    private static NotificationService instance;

    // Store notifications per user ID
    private final Map<String, ObservableList<NotificationDTO>> userNotifications;

    private NotificationService() {
        this.userNotifications = new HashMap<>();
    }

    /**
     * Gets the singleton instance of NotificationService.
     */
    public static synchronized NotificationService getInstance() {
        if (instance == null) {
            instance = new NotificationService();
        }
        return instance;
    }

    /**
     * Adds a notification for a specific user.
     *
     * @param userId       The user ID to notify
     * @param title        Notification title
     * @param message      Notification message
     * @param notificationType Type of notification (INFO, WARNING, SUCCESS, ERROR)
     */
    public void addNotification(String userId, String title, String message, NotificationType notificationType) {
        Platform.runLater(() -> {
            NotificationDTO notification = new NotificationDTO(
                    java.util.UUID.randomUUID().toString(),
                    title,
                    message,
                    notificationType.name(),
                    LocalDateTime.now(),
                    false,
                    getPriority(notificationType)
            );

            userNotifications
                    .computeIfAbsent(userId, k -> FXCollections.observableArrayList())
                    .add(notification);
        });
    }

    /**
     * Gets all notifications for a specific user.
     *
     * @param userId The user ID
     * @return Observable list of notifications
     */
    public ObservableList<NotificationDTO> getUserNotifications(String userId) {
        return userNotifications.computeIfAbsent(userId, k -> FXCollections.observableArrayList());
    }

    /**
     * Gets unread notifications for a specific user.
     *
     * @param userId The user ID
     * @return List of unread notifications
     */
    public List<NotificationDTO> getUnreadNotifications(String userId) {
        return getUserNotifications(userId).stream()
                .filter(n -> !n.isRead())
                .collect(Collectors.toList());
    }

    /**
     * Gets the count of unread notifications for a specific user.
     *
     * @param userId The user ID
     * @return Count of unread notifications
     */
    public int getUnreadCount(String userId) {
        return (int) getUserNotifications(userId).stream()
                .filter(n -> !n.isRead())
                .count();
    }

    /**
     * Marks a notification as read.
     *
     * @param notificationId The notification ID
     */
    public void markAsRead(String notificationId) {
        Platform.runLater(() -> {
            for (ObservableList<NotificationDTO> notifications : userNotifications.values()) {
                notifications.stream()
                        .filter(n -> n.getId().equals(notificationId))
                        .findFirst()
                        .ifPresent(n -> n.setRead(true));
            }
        });
    }

    /**
     * Marks all notifications as read for a specific user.
     *
     * @param userId The user ID
     */
    public void markAllAsRead(String userId) {
        Platform.runLater(() -> {
            getUserNotifications(userId).forEach(n -> n.setRead(true));
        });
    }

    /**
     * Clears all notifications for a specific user.
     *
     * @param userId The user ID
     */
    public void clearNotifications(String userId) {
        Platform.runLater(() -> {
            getUserNotifications(userId).clear();
        });
    }

    /**
     * Notifies a delivery person about a new shipment assignment.
     *
     * @param deliveryPersonId The delivery person's ID
     * @param shipmentId       The shipment ID
     * @param origin           Origin address
     * @param destination      Destination address
     */
    public void notifyShipmentAssignment(String deliveryPersonId, String shipmentId, String origin, String destination) {
        String title = "Nuevo Envío Asignado";
        String message = String.format("Se te ha asignado el envío %s de %s a %s", shipmentId, origin, destination);
        addNotification(deliveryPersonId, title, message, NotificationType.INFO);
    }

    /**
     * Notifies a user about their shipment status change.
     *
     * @param userId      The user's ID
     * @param shipmentId  The shipment ID
     * @param newStatus   The new status
     */
    public void notifyShipmentStatusChange(String userId, String shipmentId, String newStatus) {
        String title = "Actualización de Envío";
        String message = String.format("Tu envío %s ha cambiado a estado: %s", shipmentId, newStatus);
        addNotification(userId, title, message, NotificationType.SUCCESS);
    }

    /**
     * Maps NotificationType to priority level.
     *
     * @param type The notification type
     * @return Priority level as string (low, medium, high)
     */
    private String getPriority(NotificationType type) {
        return switch (type) {
            case ERROR -> "high";
            case WARNING -> "medium";
            case INFO, SUCCESS -> "low";
        };
    }

    /**
     * Notification types
     */
    public enum NotificationType {
        INFO,
        SUCCESS,
        WARNING,
        ERROR
    }
}
