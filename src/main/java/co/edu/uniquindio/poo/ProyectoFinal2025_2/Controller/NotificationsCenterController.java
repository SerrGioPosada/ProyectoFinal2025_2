package co.edu.uniquindio.poo.ProyectoFinal2025_2.Controller;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.DeliveryPerson;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.AuthenticablePerson;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.dto.NotificationDTO;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.AuthenticationService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.NotificationService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.DialogUtil;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;
import javafx.collections.ListChangeListener;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for the Notifications Center view (NotificationsCenter.fxml).
 * <p>
 * This controller manages the notification center for all user types (Admin, User, DeliveryPerson).
 * It displays system notifications, shipment updates, alerts, and messages in card format.
 * Integrates with NotificationService using Observer pattern for real-time updates.
 * </p>
 */
public class NotificationsCenterController implements Initializable {

    private static final String VIEW_NAME = "NotificationsCenter";
    private static final int NOTIFICATIONS_PER_PAGE = 10;
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // =================================================================================================================
    // FXML Fields - Statistics
    // =================================================================================================================

    @FXML private Label lblSubtitle;
    @FXML private Label lblTotalNotifications;
    @FXML private Label lblUnreadNotifications;
    @FXML private Label lblTodayNotifications;
    @FXML private Label lblWeekNotifications;

    // =================================================================================================================
    // FXML Fields - Filter Tabs
    // =================================================================================================================

    @FXML private Button btnTabAll;
    @FXML private Button btnTabUnread;
    @FXML private Button btnTabNewShipments;
    @FXML private Button btnTabUpdates;
    @FXML private Button btnTabAlerts;

    // =================================================================================================================
    // FXML Fields - Notifications List
    // =================================================================================================================

    @FXML private TextField searchField;
    @FXML private VBox notificationsContainer;
    @FXML private VBox emptyState;

    // =================================================================================================================
    // FXML Fields - Pagination
    // =================================================================================================================

    @FXML private Button btnPrevPage;
    @FXML private Button btnNextPage;
    @FXML private Label lblPageInfo;

    // =================================================================================================================
    // FXML Fields - Settings
    // =================================================================================================================

    @FXML private Button btnToggleSettings;
    @FXML private VBox settingsSection;
    @FXML private CheckBox chkNewShipments;
    @FXML private CheckBox chkStatusUpdates;
    @FXML private CheckBox chkMessages;
    @FXML private CheckBox chkSystemAlerts;
    @FXML private ComboBox<String> cmbRetentionPeriod;

    // =================================================================================================================
    // Services and Data
    // =================================================================================================================

    private final AuthenticationService authService;
    private final NotificationService notificationService;
    private AuthenticablePerson currentPerson;
    private IndexController indexController;

    private List<NotificationDTO> allNotifications;
    private List<NotificationDTO> filteredNotifications;

    private int currentPage = 0;
    private int totalPages = 1;

    private String currentFilter = "ALL";

    // =================================================================================================================
    // Constructor
    // =================================================================================================================

    /**
     * Default constructor. Initializes services.
     */
    public NotificationsCenterController() {
        this.authService = AuthenticationService.getInstance();
        this.notificationService = NotificationService.getInstance();
        this.allNotifications = new ArrayList<>();
        this.filteredNotifications = new ArrayList<>();
    }

    // =================================================================================================================
    // Initialization
    // =================================================================================================================

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Logger.info("Initializing NotificationsCenterController");

        // Get current logged-in person (Admin, User, or DeliveryPerson)
        if (!(authService.getCurrentPerson() instanceof AuthenticablePerson person)) {
            DialogUtil.showError("Error", "No se pudo obtener la información del usuario.");
            return;
        }
        this.currentPerson = person;

        // Initialize retention period options
        initializeRetentionPeriod();

        // Load notifications from NotificationService
        loadNotifications();

        // Set up search functionality
        setupSearchFilter();

        // Set up real-time notification listener (Observer pattern)
        setupNotificationListener();

        Logger.info("NotificationsCenterController initialized for user: " + currentPerson.getId());
    }

    /**
     * Initializes retention period ComboBox options.
     */
    private void initializeRetentionPeriod() {
        cmbRetentionPeriod.getItems().addAll(
                "Nunca",
                "Después de 7 días",
                "Después de 30 días",
                "Después de 90 días"
        );
        cmbRetentionPeriod.getSelectionModel().selectFirst();
    }

    /**
     * Sets up the search filter functionality.
     */
    private void setupSearchFilter() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterNotifications();
        });
    }

    /**
     * Sets up real-time notification listener using Observer pattern.
     * Listens to changes in the NotificationService's observable list.
     */
    private void setupNotificationListener() {
        // Get the observable list from NotificationService
        javafx.collections.ObservableList<NotificationDTO> userNotifications =
                notificationService.getUserNotifications(currentPerson.getId());

        // Add listener for new notifications
        userNotifications.addListener((ListChangeListener<NotificationDTO>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    Logger.info("New notifications detected, refreshing view");
                    loadNotifications();
                }
            }
        });
    }

    // =================================================================================================================
    // Data Loading
    // =================================================================================================================

    /**
     * Loads notifications from NotificationService.
     * Uses Observer pattern - notifications are automatically updated in real-time.
     */
    private void loadNotifications() {
        try {
            // Get notifications from NotificationService
            javafx.collections.ObservableList<NotificationDTO> userNotifications =
                    notificationService.getUserNotifications(currentPerson.getId());

            // Convert to ArrayList for filtering
            allNotifications = new ArrayList<>(userNotifications);

            // Apply current filter
            filterNotifications();

            // Update statistics
            updateStatistics();

            Logger.info("Notifications loaded successfully: " + allNotifications.size() + " total");

        } catch (Exception e) {
            Logger.error("Error loading notifications: " + e.getMessage());
            DialogUtil.showError("Error", "No se pudieron cargar las notificaciones.");
        }
    }


    /**
     * Updates the statistics labels.
     */
    private void updateStatistics() {
        int total = allNotifications.size();
        int unread = (int) allNotifications.stream().filter(n -> !n.isRead()).count();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.withHour(0).withMinute(0).withSecond(0);
        LocalDateTime startOfWeek = now.minusDays(7);

        int today = (int) allNotifications.stream()
                .filter(n -> n.getTimestamp().isAfter(startOfDay))
                .count();

        int week = (int) allNotifications.stream()
                .filter(n -> n.getTimestamp().isAfter(startOfWeek))
                .count();

        lblTotalNotifications.setText(String.valueOf(total));
        lblUnreadNotifications.setText(String.valueOf(unread));
        lblTodayNotifications.setText(String.valueOf(today));
        lblWeekNotifications.setText(String.valueOf(week));

        // Update subtitle
        lblSubtitle.setText(unread > 0 ?
                "Tienes " + unread + " notificación(es) sin leer" :
                "No tienes notificaciones sin leer");
    }

    /**
     * Filters notifications based on current filter and search text.
     */
    private void filterNotifications() {
        String searchText = searchField.getText();

        filteredNotifications = allNotifications.stream()
                .filter(n -> matchesFilter(n, currentFilter))
                .filter(n -> matchesSearch(n, searchText))
                .sorted(Comparator.comparing(NotificationDTO::getTimestamp).reversed())
                .collect(Collectors.toList());

        // Reset to first page
        currentPage = 0;

        // Update UI
        displayNotifications();
        updatePaginationControls();
    }

    /**
     * Checks if a notification matches the current filter.
     */
    private boolean matchesFilter(NotificationDTO notification, String filter) {
        return switch (filter) {
            case "UNREAD" -> !notification.isRead();
            case "NEW_SHIPMENT" -> "NEW_SHIPMENT".equals(notification.getType());
            case "STATUS_UPDATE" -> "STATUS_UPDATE".equals(notification.getType());
            case "ALERT" -> "SYSTEM_ALERT".equals(notification.getType());
            default -> true; // ALL
        };
    }

    /**
     * Checks if a notification matches the search text.
     */
    private boolean matchesSearch(NotificationDTO notification, String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            return true;
        }

        String lowerSearch = searchText.toLowerCase().trim();
        return notification.getTitle().toLowerCase().contains(lowerSearch) ||
                notification.getMessage().toLowerCase().contains(lowerSearch);
    }

    /**
     * Displays the notifications for the current page.
     */
    private void displayNotifications() {
        notificationsContainer.getChildren().clear();

        if (filteredNotifications.isEmpty()) {
            emptyState.setManaged(true);
            emptyState.setVisible(true);
            notificationsContainer.setManaged(false);
            notificationsContainer.setVisible(false);
            return;
        }

        emptyState.setManaged(false);
        emptyState.setVisible(false);
        notificationsContainer.setManaged(true);
        notificationsContainer.setVisible(true);

        // Calculate page bounds
        int startIndex = currentPage * NOTIFICATIONS_PER_PAGE;
        int endIndex = Math.min(startIndex + NOTIFICATIONS_PER_PAGE, filteredNotifications.size());

        // Display notifications for current page
        for (int i = startIndex; i < endIndex; i++) {
            NotificationDTO notification = filteredNotifications.get(i);
            VBox notificationCard = createNotificationCard(notification);
            notificationsContainer.getChildren().add(notificationCard);

            // Add fade-in animation
            FadeTransition fade = new FadeTransition(Duration.millis(300), notificationCard);
            fade.setFromValue(0.0);
            fade.setToValue(1.0);
            fade.play();
        }
    }

    /**
     * Creates a visual card for a notification.
     */
    private VBox createNotificationCard(NotificationDTO notification) {
        VBox card = new VBox(10);
        card.getStyleClass().add(notification.isRead() ? "notification-card-read" : "notification-card-unread");
        card.setPadding(new Insets(15));

        // Header row
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        // Icon based on type
        Label icon = new Label(getIconForType(notification.getType()));
        icon.setStyle("-fx-font-size: 24px;");

        // Title and timestamp
        VBox titleBox = new VBox(3);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        Label title = new Label(notification.getTitle());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #032d4d;");

        Label timestamp = new Label(getRelativeTime(notification.getTimestamp()));
        timestamp.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");

        titleBox.getChildren().addAll(title, timestamp);

        // Mark as read/unread button
        Button btnToggleRead = new Button(notification.isRead() ? "Marcar como no leída" : "Marcar como leída");
        btnToggleRead.getStyleClass().add("btn-text");
        btnToggleRead.setOnAction(e -> handleToggleRead(notification));

        // Delete button
        Button btnDelete = new Button("🗑️");
        btnDelete.getStyleClass().add("btn-icon-danger");
        btnDelete.setOnAction(e -> handleDeleteNotification(notification));

        header.getChildren().addAll(icon, titleBox, btnToggleRead, btnDelete);

        // Message
        Label message = new Label(notification.getMessage());
        message.setWrapText(true);
        message.setStyle("-fx-font-size: 13px; -fx-text-fill: #495057;");

        card.getChildren().addAll(header, message);

        // Priority indicator
        if ("high".equals(notification.getPriority())) {
            card.setStyle(card.getStyle() + "-fx-border-color: #dc3545; -fx-border-width: 0 0 0 4;");
        } else if ("medium".equals(notification.getPriority())) {
            card.setStyle(card.getStyle() + "-fx-border-color: #ffc107; -fx-border-width: 0 0 0 4;");
        }

        return card;
    }

    /**
     * Gets the emoji icon for a notification type.
     */
    private String getIconForType(String type) {
        return switch (type) {
            case "NEW_SHIPMENT" -> "📦";
            case "STATUS_UPDATE" -> "🔄";
            case "MESSAGE" -> "💬";
            case "SYSTEM_ALERT" -> "⚠️";
            default -> "🔔";
        };
    }

    /**
     * Gets relative time string (e.g., "Hace 2 horas").
     */
    private String getRelativeTime(LocalDateTime timestamp) {
        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(timestamp, now);

        if (minutes < 1) return "Ahora mismo";
        if (minutes < 60) return "Hace " + minutes + " minuto" + (minutes > 1 ? "s" : "");

        long hours = ChronoUnit.HOURS.between(timestamp, now);
        if (hours < 24) return "Hace " + hours + " hora" + (hours > 1 ? "s" : "");

        long days = ChronoUnit.DAYS.between(timestamp, now);
        if (days < 7) return "Hace " + days + " día" + (days > 1 ? "s" : "");

        return timestamp.format(DATETIME_FORMATTER);
    }

    /**
     * Updates pagination controls.
     */
    private void updatePaginationControls() {
        totalPages = (int) Math.ceil((double) filteredNotifications.size() / NOTIFICATIONS_PER_PAGE);
        totalPages = Math.max(1, totalPages);

        lblPageInfo.setText("Página " + (currentPage + 1) + " de " + totalPages);

        btnPrevPage.setDisable(currentPage == 0);
        btnNextPage.setDisable(currentPage >= totalPages - 1);
    }

    // =================================================================================================================
    // Event Handlers
    // =================================================================================================================

    /**
     * Handles the refresh button action.
     */
    @FXML
    private void handleRefresh() {
        Logger.info("Refreshing notifications");
        loadNotifications();
        DialogUtil.showSuccess("Notificaciones actualizadas");
    }

    /**
     * Handles marking all notifications as read.
     */
    @FXML
    private void handleMarkAllAsRead() {
        notificationService.markAllAsRead(currentPerson.getId());
        loadNotifications();
        DialogUtil.showSuccess("Todas las notificaciones marcadas como leídas");
        Logger.info("All notifications marked as read for user: " + currentPerson.getId());
    }

    /**
     * Toggles read/unread status of a notification.
     */
    private void handleToggleRead(NotificationDTO notification) {
        notification.setRead(!notification.isRead());
        notificationService.markAsRead(notification.getId());
        updateStatistics();
        displayNotifications();
        Logger.info("Notification " + notification.getId() + " marked as " + (notification.isRead() ? "read" : "unread"));
    }

    /**
     * Deletes a notification.
     */
    private void handleDeleteNotification(NotificationDTO notification) {
        boolean confirmed = DialogUtil.showConfirmation(
                "Eliminar Notificación",
                "¿Estás seguro de que deseas eliminar esta notificación?"
        );

        if (confirmed) {
            // Remove from NotificationService
            javafx.collections.ObservableList<NotificationDTO> userNotifications =
                    notificationService.getUserNotifications(currentPerson.getId());
            userNotifications.remove(notification);

            // Reload notifications
            loadNotifications();
            DialogUtil.showSuccess("Notificación eliminada");
            Logger.info("Notification deleted: " + notification.getId());
        }
    }

    // Filter tab handlers
    @FXML
    private void handleShowAll() {
        currentFilter = "ALL";
        filterNotifications();
    }

    @FXML
    private void handleShowUnread() {
        currentFilter = "UNREAD";
        filterNotifications();
    }

    @FXML
    private void handleShowNewShipments() {
        currentFilter = "NEW_SHIPMENT";
        filterNotifications();
    }

    @FXML
    private void handleShowUpdates() {
        currentFilter = "STATUS_UPDATE";
        filterNotifications();
    }

    @FXML
    private void handleShowAlerts() {
        currentFilter = "ALERT";
        filterNotifications();
    }

    // Pagination handlers
    @FXML
    private void handlePreviousPage() {
        if (currentPage > 0) {
            currentPage--;
            displayNotifications();
            updatePaginationControls();
        }
    }

    @FXML
    private void handleNextPage() {
        if (currentPage < totalPages - 1) {
            currentPage++;
            displayNotifications();
            updatePaginationControls();
        }
    }

    // Settings handlers
    @FXML
    private void toggleSettings() {
        boolean isVisible = settingsSection.isVisible();
        settingsSection.setManaged(!isVisible);
        settingsSection.setVisible(!isVisible);
        btnToggleSettings.setText(isVisible ? "▼" : "▲");
    }

    @FXML
    private void handleSaveSettings() {
        DialogUtil.showSuccess("Configuración guardada exitosamente");
        Logger.info("Notification settings saved");
    }

    // =================================================================================================================
    // IndexController Integration
    // =================================================================================================================

    /**
     * Sets the IndexController reference.
     * This allows the notifications center to return to the previous view.
     *
     * @param indexController The IndexController instance
     */
    public void setIndexController(IndexController indexController) {
        this.indexController = indexController;
    }

    /**
     * Handles the back button click.
     * Returns to the previous view via IndexController.
     */
    @FXML
    private void handleBackButton() {
        if (indexController != null) {
            Logger.info("Back button clicked, returning to previous view");
            indexController.returnToPreviousView();
        } else {
            Logger.warn("IndexController is null, cannot return to previous view");
        }
    }
}
