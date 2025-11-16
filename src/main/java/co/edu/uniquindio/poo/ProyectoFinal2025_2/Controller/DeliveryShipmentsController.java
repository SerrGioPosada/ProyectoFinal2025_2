package co.edu.uniquindio.poo.ProyectoFinal2025_2.Controller;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Address;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.DeliveryPerson;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Invoice;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Order;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Shipment;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.ShipmentStatus;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.dto.ShipmentDTO;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Repositories.InvoiceRepository;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Repositories.OrderRepository;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Repositories.ShipmentRepository;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.AuthenticationService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.ShipmentService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.DialogUtil;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.TabStateManager;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller for the Delivery Shipments view (DeliveryShipments.fxml).
 * <p>
 * This controller manages the shipments assigned to a delivery person,
 * providing filtering, viewing details, and status updates with a collapsible tab system.
 * </p>
 */
public class DeliveryShipmentsController implements Initializable {

    // =================================================================================================================
    // FXML Fields - Tab System
    // =================================================================================================================

    @FXML private VBox collapsibleTabSection;
    @FXML private Button btnCollapseToggle;
    @FXML private Button btnTabStats;
    @FXML private Button btnTabFilters;

    @FXML private HBox statsTabContent;
    @FXML private VBox filtersTabContent;

    // =================================================================================================================
    // FXML Fields - Statistics
    // =================================================================================================================

    @FXML private Label lblTotal;
    @FXML private Label lblPending;
    @FXML private Label lblInTransit;
    @FXML private Label lblDelivered;

    // =================================================================================================================
    // FXML Fields - Filters
    // =================================================================================================================

    @FXML private ComboBox<String> filterStatus;
    @FXML private TextField searchField;

    // =================================================================================================================
    // FXML Fields - Table
    // =================================================================================================================

    @FXML private TableView<ShipmentDTO> shipmentsTable;
    @FXML private TableColumn<ShipmentDTO, String> colShipmentId;
    @FXML private TableColumn<ShipmentDTO, String> colOrigin;
    @FXML private TableColumn<ShipmentDTO, String> colDestination;
    @FXML private TableColumn<ShipmentDTO, String> colStatus;
    @FXML private TableColumn<ShipmentDTO, String> colDate;
    @FXML private TableColumn<ShipmentDTO, String> colCost;

    // =================================================================================================================
    // Services and Data
    // =================================================================================================================

    private final AuthenticationService authService = AuthenticationService.getInstance();
    private final ShipmentService shipmentService = new ShipmentService();
    private final ShipmentRepository shipmentRepository = ShipmentRepository.getInstance();
    private final OrderRepository orderRepository = OrderRepository.getInstance();
    private final InvoiceRepository invoiceRepository = InvoiceRepository.getInstance();

    private DeliveryPerson currentDeliveryPerson;
    private ObservableList<ShipmentDTO> shipmentsData;
    private FilteredList<ShipmentDTO> filteredShipments;

    private static final String VIEW_NAME = "DeliveryShipments";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // =================================================================================================================
    // Initialization
    // =================================================================================================================

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Logger.info("Initializing DeliveryShipmentsController");

        currentDeliveryPerson = (DeliveryPerson) authService.getCurrentPerson();

        if (currentDeliveryPerson == null) {
            DialogUtil.showError("Error", "No se pudo obtener la información del repartidor.");
            return;
        }

        setupTable();
        setupFilters();
        loadShipments();
        updateStatistics();
        restoreViewState();

        Logger.info("DeliveryShipmentsController initialized successfully");
    }

    // =================================================================================================================
    // Setup Methods
    // =================================================================================================================

    private void setupTable() {
        colShipmentId.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getId()));
        colOrigin.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getOriginAddressComplete() != null ? data.getValue().getOriginAddressComplete() : "N/A"
        ));
        colDestination.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getDestinationAddressComplete() != null ? data.getValue().getDestinationAddressComplete() : "N/A"
        ));
        colStatus.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getStatus() != null ? getStatusSpanish(data.getValue().getStatus()) : "N/A"
        ));
        colDate.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getCreationDate() != null ? data.getValue().getCreationDate().format(DATE_FORMATTER) : "N/A"
        ));
        colCost.setCellValueFactory(data -> new SimpleStringProperty(
            "$" + String.format("%.2f", data.getValue().getTotalCost())
        ));

        // Apply styled cell factory for status column (badges)
        colStatus.setCellFactory(column -> new TableCell<ShipmentDTO, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    Label badge = new Label(item);
                    String backgroundColor = getStatusColor(item);
                    badge.setStyle(
                        "-fx-background-color: " + backgroundColor + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 5 10 5 10;" +
                        "-fx-background-radius: 12;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 11px;"
                    );
                    setText(null);
                    setGraphic(badge);
                }
            }
        });

        // Setup context menu
        setupContextMenu();
    }

    private void setupContextMenu() {
        shipmentsTable.setRowFactory(tv -> {
            TableRow<ShipmentDTO> row = new TableRow<>();
            ContextMenu contextMenu = new ContextMenu();

            // Ver Detalles
            MenuItem viewDetails = new MenuItem("Ver Detalles");
            viewDetails.setOnAction(event -> {
                ShipmentDTO selected = row.getItem();
                if (selected != null) {
                    showShipmentDetails(selected);
                }
            });

            // Ver Orden Asociada
            MenuItem viewOrder = new MenuItem("Ver Orden Asociada");
            viewOrder.setOnAction(event -> {
                ShipmentDTO selected = row.getItem();
                if (selected != null) {
                    showAssociatedOrder(selected);
                }
            });

            // Marcar En Tránsito
            MenuItem markInTransit = new MenuItem("Marcar En Tránsito");
            markInTransit.setOnAction(event -> {
                ShipmentDTO selected = row.getItem();
                if (selected != null) {
                    quickUpdateStatus(selected, ShipmentStatus.IN_TRANSIT);
                }
            });

            // Marcar En Camino (Out for Delivery)
            MenuItem markOutForDelivery = new MenuItem("Marcar En Camino");
            markOutForDelivery.setOnAction(event -> {
                ShipmentDTO selected = row.getItem();
                if (selected != null) {
                    quickUpdateStatus(selected, ShipmentStatus.OUT_FOR_DELIVERY);
                }
            });

            // Marcar Entregado
            MenuItem markDelivered = new MenuItem("Marcar Entregado");
            markDelivered.setOnAction(event -> {
                ShipmentDTO selected = row.getItem();
                if (selected != null) {
                    quickUpdateStatus(selected, ShipmentStatus.DELIVERED);
                }
            });

            // Marcar Devuelto
            MenuItem markReturned = new MenuItem("Marcar Devuelto");
            markReturned.setOnAction(event -> {
                ShipmentDTO selected = row.getItem();
                if (selected != null) {
                    quickUpdateStatus(selected, ShipmentStatus.RETURNED);
                }
            });

            // Reportar Incidencia
            MenuItem reportIncident = new MenuItem("⚠️ Reportar Incidencia");
            reportIncident.setOnAction(event -> {
                ShipmentDTO selected = row.getItem();
                if (selected != null) {
                    handleReportIncident(selected);
                }
            });

            contextMenu.getItems().addAll(
                viewDetails,
                viewOrder,
                new SeparatorMenuItem(),
                markInTransit,
                markOutForDelivery,
                markDelivered,
                markReturned,
                new SeparatorMenuItem(),
                reportIncident
            );

            row.contextMenuProperty().bind(
                javafx.beans.binding.Bindings.when(row.emptyProperty())
                    .then((ContextMenu) null)
                    .otherwise(contextMenu)
            );

            return row;
        });
    }

    private void setupFilters() {
        // Status filter with all shipment statuses
        filterStatus.getItems().clear();
        filterStatus.getItems().add("Todos los estados");
        filterStatus.getItems().addAll(
            "Pendiente",
            "Listo para Recoger",
            "En Tránsito",
            "En Entrega",
            "Entregado",
            "Cancelado",
            "Devuelto"
        );

        // Configure ButtonCell to show selected item properly
        filterStatus.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Todos los estados");
                    setStyle("-fx-text-fill: #6c757d;");
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: #495057;");
                }
            }
        });

        filterStatus.setValue("Todos los estados");

        // Dynamic filtering
        filterStatus.setOnAction(event -> applyFilters());

        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        }
    }

    // =================================================================================================================
    // Data Loading
    // =================================================================================================================

    private void loadShipments() {
        try {
            List<ShipmentDTO> allShipments = shipmentService.listAll().stream()
                    .filter(shipment -> currentDeliveryPerson.getId().equals(shipment.getDeliveryPersonId()))
                    .collect(Collectors.toList());

            shipmentsData = FXCollections.observableArrayList(allShipments);
            filteredShipments = new FilteredList<>(shipmentsData, p -> true);
            shipmentsTable.setItems(filteredShipments);

            Logger.info("Loaded " + allShipments.size() + " shipments for delivery person");

        } catch (Exception e) {
            Logger.error("Error loading shipments: " + e.getMessage());
            DialogUtil.showError("Error", "No se pudieron cargar los envíos.");
        }
    }

    private void updateStatistics() {
        int total = shipmentsData != null ? shipmentsData.size() : 0;
        long pending = shipmentsData.stream()
                .filter(s -> s.getStatus() == ShipmentStatus.PENDING_ASSIGNMENT || s.getStatus() == ShipmentStatus.READY_FOR_PICKUP)
                .count();
        long inTransit = shipmentsData.stream()
                .filter(s -> s.getStatus() == ShipmentStatus.IN_TRANSIT || s.getStatus() == ShipmentStatus.OUT_FOR_DELIVERY)
                .count();
        long delivered = shipmentsData.stream()
                .filter(s -> s.getStatus() == ShipmentStatus.DELIVERED)
                .count();

        lblTotal.setText(String.valueOf(total));
        lblPending.setText(String.valueOf(pending));
        lblInTransit.setText(String.valueOf(inTransit));
        lblDelivered.setText(String.valueOf(delivered));
    }

    private void applyFilters() {
        if (filteredShipments == null) return;

        filteredShipments.setPredicate(shipment -> {
            // Status filter
            String selectedStatus = filterStatus.getValue();
            if (selectedStatus != null && !selectedStatus.equals("Todos los estados")) {
                String shipmentStatus = getStatusSpanish(shipment.getStatus());
                if (!shipmentStatus.equals(selectedStatus)) {
                    return false;
                }
            }

            // Search filter (ID or address)
            if (searchField != null && searchField.getText() != null && !searchField.getText().trim().isEmpty()) {
                String search = searchField.getText().toLowerCase();
                boolean matchesId = shipment.getId().toLowerCase().contains(search);
                boolean matchesOrigin = shipment.getOriginAddressComplete() != null &&
                        shipment.getOriginAddressComplete().toLowerCase().contains(search);
                boolean matchesDestination = shipment.getDestinationAddressComplete() != null &&
                        shipment.getDestinationAddressComplete().toLowerCase().contains(search);

                if (!matchesId && !matchesOrigin && !matchesDestination) {
                    return false;
                }
            }

            return true;
        });
    }

    // =================================================================================================================
    // Tab System Event Handlers
    // =================================================================================================================

    @FXML
    private void switchToStatsTab() {
        setActiveTab(btnTabStats);
        showTabContent(statsTabContent, true);
    }

    @FXML
    private void switchToFiltersTab() {
        setActiveTab(btnTabFilters);
        showTabContent(filtersTabContent, true);
    }

    private void setActiveTab(Button activeButton) {
        btnTabStats.getStyleClass().remove("tab-button-active");
        btnTabFilters.getStyleClass().remove("tab-button-active");

        activeButton.getStyleClass().add("tab-button-active");
    }

    private void showTabContent(javafx.scene.Node contentToShow, boolean shouldExpand) {
        statsTabContent.setVisible(false);
        statsTabContent.setManaged(false);
        filtersTabContent.setVisible(false);
        filtersTabContent.setManaged(false);

        contentToShow.setVisible(true);
        contentToShow.setManaged(true);

        if (shouldExpand && !TabStateManager.isExpanded(VIEW_NAME)) {
            TabStateManager.setExpanded(VIEW_NAME, true);
            applyCollapseState(true);
        }

        if (contentToShow == statsTabContent) {
            TabStateManager.setActiveTab(VIEW_NAME, "stats");
        } else if (contentToShow == filtersTabContent) {
            TabStateManager.setActiveTab(VIEW_NAME, "filters");
        }
    }

    @FXML
    private void toggleCollapse() {
        boolean currentlyExpanded = TabStateManager.isExpanded(VIEW_NAME);
        boolean newExpanded = !currentlyExpanded;

        TabStateManager.setExpanded(VIEW_NAME, newExpanded);
        applyCollapseState(newExpanded);
    }

    private void applyCollapseState(boolean expanded) {
        if (expanded) {
            collapsibleTabSection.getStyleClass().removeAll("tab-section-collapsed");
            if (!collapsibleTabSection.getStyleClass().contains("tab-section-expanded")) {
                collapsibleTabSection.getStyleClass().add("tab-section-expanded");
            }

            // Make ONLY content container visible (tabs always visible)
            javafx.scene.Node contentContainer = collapsibleTabSection.lookup(".tab-content-container");
            if (contentContainer != null) {
                contentContainer.setVisible(true);
                contentContainer.setManaged(true);
            }

            btnCollapseToggle.setText("▲");
        } else {
            collapsibleTabSection.getStyleClass().removeAll("tab-section-expanded");
            if (!collapsibleTabSection.getStyleClass().contains("tab-section-collapsed")) {
                collapsibleTabSection.getStyleClass().add("tab-section-collapsed");
            }

            // Hide content container
            javafx.scene.Node contentContainer = collapsibleTabSection.lookup(".tab-content-container");
            if (contentContainer != null) {
                contentContainer.setVisible(false);
                contentContainer.setManaged(false);
            }

            btnCollapseToggle.setText("▼");
        }
    }

    private void restoreViewState() {
        boolean expanded = TabStateManager.isExpanded(VIEW_NAME);
        applyCollapseState(expanded);

        String activeTab = TabStateManager.getActiveTab(VIEW_NAME);
        if (activeTab != null) {
            switch (activeTab) {
                case "stats":
                    setActiveTab(btnTabStats);
                    showTabContent(statsTabContent, false);
                    break;
                case "filters":
                    setActiveTab(btnTabFilters);
                    showTabContent(filtersTabContent, false);
                    break;
                default:
                    setActiveTab(btnTabStats);
                    showTabContent(statsTabContent, false);
                    break;
            }
        } else {
            setActiveTab(btnTabStats);
            showTabContent(statsTabContent, false);
        }
    }

    // =================================================================================================================
    // Action Handlers
    // =================================================================================================================

    @FXML
    private void handleRefresh() {
        Logger.info("Refreshing shipments...");
        loadShipments();
        updateStatistics();
        DialogUtil.showSuccess("Actualizado", "Envíos actualizados correctamente.");
    }

    @FXML
    private void handleClearFilters() {
        filterStatus.setValue("Todos los estados");
        if (searchField != null) {
            searchField.clear();
        }
    }

    private void showShipmentDetails(ShipmentDTO shipment) {
        StringBuilder details = new StringBuilder();
        details.append("===== DETALLES DEL ENVÍO =====\n\n");
        details.append("ID: ").append(shipment.getId()).append("\n");
        details.append("Estado: ").append(getStatusSpanish(shipment.getStatus())).append("\n");
        details.append("Fecha Creación: ").append(shipment.getCreationDate() != null ?
            shipment.getCreationDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "N/A").append("\n\n");

        details.append("--- Origen ---\n");
        details.append(shipment.getOriginAddressComplete() != null ?
            shipment.getOriginAddressComplete() : "N/A").append("\n");
        if (shipment.getOriginZone() != null) {
            details.append("Zona: ").append(shipment.getOriginZone()).append("\n");
        }

        details.append("\n--- Destino ---\n");
        details.append(shipment.getDestinationAddressComplete() != null ?
            shipment.getDestinationAddressComplete() : "N/A").append("\n");
        if (shipment.getDestinationZone() != null) {
            details.append("Zona: ").append(shipment.getDestinationZone()).append("\n");
        }

        details.append("\n--- Información Adicional ---\n");
        details.append("Distancia: ").append(String.format("%.2f km", shipment.getDistanceKm())).append("\n");
        details.append("Costo Total: $").append(String.format("%.2f", shipment.getTotalCost())).append("\n");

        if (shipment.getEstimatedDeliveryDate() != null) {
            details.append("Entrega Estimada: ").append(
                shipment.getEstimatedDeliveryDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            ).append("\n");
        }

        if (shipment.getActualDeliveryDate() != null) {
            details.append("Fecha de Entrega Real: ").append(
                shipment.getActualDeliveryDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            ).append("\n");
        }

        DialogUtil.showInfo("Detalles del Envío", details.toString());
    }

    private void updateShipmentStatus(ShipmentDTO shipmentDTO) {
        // Create dialog with status options
        ChoiceDialog<String> dialog = new ChoiceDialog<>("",
            "En Tránsito",
            "En Entrega",
            "Entregado"
        );

        dialog.setTitle("Actualizar Estado");
        dialog.setHeaderText("Actualizar estado del envío #" + shipmentDTO.getId());
        dialog.setContentText("Nuevo estado:");

        dialog.showAndWait().ifPresent(statusSpanish -> {
            try {
                // Convert Spanish status to enum
                ShipmentStatus newStatus = switch (statusSpanish) {
                    case "En Tránsito" -> ShipmentStatus.IN_TRANSIT;
                    case "En Entrega" -> ShipmentStatus.OUT_FOR_DELIVERY;
                    case "Entregado" -> ShipmentStatus.DELIVERED;
                    default -> null;
                };

                if (newStatus != null) {
                    // Get full Shipment object and update
                    Optional<Shipment> shipmentOpt = shipmentRepository.findById(shipmentDTO.getId());
                    if (shipmentOpt.isPresent()) {
                        Shipment shipment = shipmentOpt.get();
                        shipment.setStatus(newStatus);
                        if (newStatus == ShipmentStatus.DELIVERED) {
                            shipment.setDeliveredDate(LocalDateTime.now());
                        }
                        shipmentRepository.update(shipment);

                        loadShipments();
                        updateStatistics();
                        DialogUtil.showSuccess("Éxito", "Estado del envío actualizado correctamente.");
                    }
                }
            } catch (Exception e) {
                Logger.error("Error updating shipment status: " + e.getMessage());
                DialogUtil.showError("Error", "No se pudo actualizar el estado del envío.");
            }
        });
    }

    private void quickUpdateStatus(ShipmentDTO shipmentDTO, ShipmentStatus newStatus) {
        try {
            // Confirm with user
            boolean confirmed = DialogUtil.showConfirmation(
                "Confirmar Cambio de Estado",
                "¿Está seguro de cambiar el estado del envío #" + shipmentDTO.getId() +
                " a \"" + getStatusSpanish(newStatus) + "\"?"
            );

            if (confirmed) {
                // Get full Shipment object and update
                Optional<Shipment> shipmentOpt = shipmentRepository.findById(shipmentDTO.getId());
                if (shipmentOpt.isPresent()) {
                    Shipment shipment = shipmentOpt.get();
                    shipment.setStatus(newStatus);
                    if (newStatus == ShipmentStatus.DELIVERED) {
                        shipment.setDeliveredDate(LocalDateTime.now());
                    }
                    shipmentRepository.update(shipment);

                    loadShipments();
                    updateStatistics();
                    DialogUtil.showSuccess("Éxito", "Estado actualizado a: " + getStatusSpanish(newStatus));
                    Logger.info("Updated shipment " + shipmentDTO.getId() + " to status: " + newStatus);
                }
            }
        } catch (Exception e) {
            Logger.error("Error updating shipment status: " + e.getMessage());
            DialogUtil.showError("Error", "No se pudo actualizar el estado del envío.");
        }
    }

    private void showAssociatedOrder(ShipmentDTO shipmentDTO) {
        try {
            // Get the full Shipment object to access orderId
            Optional<Shipment> shipmentOpt = shipmentRepository.findById(shipmentDTO.getId());
            if (!shipmentOpt.isPresent()) {
                DialogUtil.showError("Error", "No se pudo encontrar el envío.");
                return;
            }

            Shipment shipment = shipmentOpt.get();

            if (shipment.getOrderId() == null || shipment.getOrderId().isEmpty()) {
                DialogUtil.showWarning("Sin Orden Asociada", "Este envío no tiene una orden asociada.");
                return;
            }

            Optional<Order> orderOpt = orderRepository.findById(shipment.getOrderId());
            if (!orderOpt.isPresent()) {
                DialogUtil.showError("Error", "No se pudo encontrar la orden asociada.");
                return;
            }

            Order order = orderOpt.get();
            StringBuilder details = new StringBuilder();
            details.append("===== ORDEN ASOCIADA =====\n\n");
            details.append("ID Orden: ").append(order.getId()).append("\n");
            details.append("Estado: ").append(order.getStatus() != null ? order.getStatus().getDisplayName() : "N/A").append("\n");
            details.append("Fecha Creación: ").append(order.getCreatedAt() != null ?
                order.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "N/A").append("\n");

            // Get cost from invoice
            String costStr = "N/A";
            if (order.getInvoiceId() != null && !order.getInvoiceId().isEmpty()) {
                Optional<Invoice> invoiceOpt = invoiceRepository.findById(order.getInvoiceId());
                if (invoiceOpt.isPresent()) {
                    costStr = String.format("%.2f", invoiceOpt.get().getTotalAmount());
                }
            }
            details.append("Costo Total: $").append(costStr).append("\n");

            details.append("\n--- Origen ---\n");
            if (order.getOrigin() != null) {
                Address origin = order.getOrigin();
                details.append(origin.getStreet()).append(", ")
                       .append(origin.getCity()).append(", ")
                       .append(origin.getState()).append("\n");
            } else {
                details.append("N/A\n");
            }

            details.append("\n--- Destino ---\n");
            if (order.getDestination() != null) {
                Address dest = order.getDestination();
                details.append(dest.getStreet()).append(", ")
                       .append(dest.getCity()).append(", ")
                       .append(dest.getState()).append("\n");
            } else {
                details.append("N/A\n");
            }

            DialogUtil.showInfo("Orden Asociada", details.toString());

        } catch (Exception e) {
            Logger.error("Error showing associated order: " + e.getMessage());
            DialogUtil.showError("Error", "No se pudo cargar la información de la orden.");
        }
    }

    private void handleReportIncident(ShipmentDTO shipmentDTO) {
        try {
            // Check if ReportIncidentDialog exists
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/co/edu/uniquindio/poo/ProyectoFinal2025_2/View/ReportIncidentDialog.fxml")
            );
            Parent root = loader.load();

            // Get the full Shipment object
            Optional<Shipment> shipmentOpt = shipmentRepository.findById(shipmentDTO.getId());
            if (!shipmentOpt.isPresent()) {
                DialogUtil.showError("Error", "No se pudo encontrar el envío.");
                return;
            }

            ReportIncidentDialogController controller = loader.getController();
            controller.setShipment(shipmentOpt.get());

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Reportar Incidencia");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(false);

            dialogStage.showAndWait();

            // Refresh after incident report
            loadShipments();
            updateStatistics();

        } catch (Exception e) {
            Logger.error("Error opening incident report dialog: " + e.getMessage());
            // Fallback: show simple dialog
            showSimpleIncidentReport(shipmentDTO);
        }
    }

    private void showSimpleIncidentReport(ShipmentDTO shipment) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Reportar Incidencia");
        dialog.setHeaderText("Reportar problema con envío #" + shipment.getId());
        dialog.setContentText("Describa la incidencia:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(incidentDescription -> {
            if (!incidentDescription.trim().isEmpty()) {
                Logger.info("Incident reported for shipment " + shipment.getId() + ": " + incidentDescription);
                DialogUtil.showInfo("Incidencia Reportada",
                    "La incidencia ha sido registrada y será revisada por el equipo de soporte.");
            }
        });
    }

    // =================================================================================================================
    // Helper Methods
    // =================================================================================================================

    private String getStatusSpanish(ShipmentStatus status) {
        if (status == null) return "Desconocido";
        return switch (status) {
            case PENDING_ASSIGNMENT -> "Pendiente";
            case READY_FOR_PICKUP -> "Listo para Recoger";
            case IN_TRANSIT -> "En Tránsito";
            case OUT_FOR_DELIVERY -> "En Entrega";
            case DELIVERED -> "Entregado";
            case CANCELLED -> "Cancelado";
            case RETURNED -> "Devuelto";
            default -> status.toString();
        };
    }

    private String getStatusColor(String status) {
        return switch (status.toLowerCase()) {
            case "entregado" -> "#28a745";
            case "en tránsito", "en entrega" -> "#17a2b8";
            case "listo para recoger" -> "#6610f2";
            case "pendiente" -> "#ffc107";
            case "cancelado", "devuelto" -> "#dc3545";
            default -> "#6c757d";
        };
    }
}
