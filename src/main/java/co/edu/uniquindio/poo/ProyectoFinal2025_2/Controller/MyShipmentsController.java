package co.edu.uniquindio.poo.ProyectoFinal2025_2.Controller;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Invoice;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Order;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Shipment;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.User;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.dto.OrderDetailDTO;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.dto.OrderShipmentViewDTO;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.dto.ShipmentDTO;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Repositories.InvoiceRepository;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Repositories.OrderRepository;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Repositories.ShipmentRepository;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.AuthenticationService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.OrderService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.ShipmentService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.Adapter.OrderShipmentConverterUtil;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.DialogUtil;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.TabStateManager;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller for user's orders and shipments view.
 * Displays both Orders and Shipments in a unified table.
 */
public class MyShipmentsController implements Initializable {

    private static final String VIEW_NAME = "MyShipments";

    // Table and Columns
    @FXML private TableView<OrderShipmentViewDTO> shipmentsTable;
    @FXML private TableColumn<OrderShipmentViewDTO, String> colType;
    @FXML private TableColumn<OrderShipmentViewDTO, String> colId;
    @FXML private TableColumn<OrderShipmentViewDTO, String> colRoute;
    @FXML private TableColumn<OrderShipmentViewDTO, String> colStatus;
    @FXML private TableColumn<OrderShipmentViewDTO, String> colDate;
    @FXML private TableColumn<OrderShipmentViewDTO, Double> colCost;

    // Filters
    @FXML private ComboBox<OrderShipmentViewDTO.ItemType> filterType;
    @FXML private TextField searchField;

    // Tab System
    @FXML private VBox collapsibleTabSection;
    @FXML private Button btnCollapseToggle;
    @FXML private Button btnTabStats;
    @FXML private Button btnTabFilters;
    @FXML private HBox statsTabContent;
    @FXML private VBox filtersTabContent;

    // Counter Labels
    @FXML private Label lblTotal;
    @FXML private Label lblOrders;
    @FXML private Label lblShipments;
    @FXML private Label lblPending;

    // Services
    private final ShipmentService shipmentService = new ShipmentService();
    private final OrderService orderService = new OrderService();
    private final OrderRepository orderRepository = OrderRepository.getInstance();
    private final InvoiceRepository invoiceRepository = InvoiceRepository.getInstance();
    private final AuthenticationService authService = AuthenticationService.getInstance();

    // Data
    private ObservableList<OrderShipmentViewDTO> allData;
    private String currentUserId;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        currentUserId = getCurrentUserId();

        if (currentUserId == null) {
            DialogUtil.showError("No user logged in");
            return;
        }

        setupTable();
        setupContextMenu();
        setupFilters();
        loadAllData();
        updateCounters();
        restoreViewState();

        Logger.info("MyShipmentsController initialized for user: " + currentUserId);
    }

    /**
     * Sets up table columns with cell value factories.
     */
    private void setupTable() {
        colType.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTypeDisplay()));
        colId.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getId()));
        colRoute.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRoute()));
        colStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatusDisplay()));
        colDate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDateFormatted()));
        colCost.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getCost()).asObject());

        // Format currency in cost column
        colCost.setCellFactory(column -> new TableCell<OrderShipmentViewDTO, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item == 0.0) {
                    setText("--");
                    return;
                }
                setText(String.format("$%,.2f", item));
            }
        });

        // Apply status colors with badge style
        colStatus.setCellFactory(column -> new TableCell<OrderShipmentViewDTO, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                    return;
                }

                OrderShipmentViewDTO dto = getTableView().getItems().get(getIndex());
                if (dto == null) return;

                // Create styled label for status badge
                Label badge = new Label(item);
                badge.setStyle(
                    "-fx-background-color: " + dto.getStatusColor() + ";" +
                    "-fx-text-fill: white;" +
                    "-fx-padding: 5 10 5 10;" +
                    "-fx-background-radius: 12;" +
                    "-fx-font-weight: bold;" +
                    "-fx-font-size: 11px;"
                );

                setText(null);
                setGraphic(badge);
            }
        });

        // Color type column
        colType.setCellFactory(column -> new TableCell<OrderShipmentViewDTO, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(item);

                OrderShipmentViewDTO dto = getTableView().getItems().get(getIndex());
                if (dto == null) return;

                String color = dto.getItemType() == OrderShipmentViewDTO.ItemType.ORDER
                    ? "#6610f2" // Purple for orders
                    : "#17a2b8"; // Cyan for shipments
                setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
            }
        });
    }

    /**
     * Sets up the context menu for right-click actions on table rows.
     */
    private void setupContextMenu() {
        shipmentsTable.setRowFactory(tv -> {
            TableRow<OrderShipmentViewDTO> row = new TableRow<>();
            ContextMenu contextMenu = new ContextMenu();

            // Ver Detalles
            MenuItem viewDetailsItem = new MenuItem("Ver Detalles");
            viewDetailsItem.setOnAction(event -> {
                OrderShipmentViewDTO selected = row.getItem();
                if (selected != null) {
                    handleViewDetailsForItem(selected);
                }
            });

            // Rastrear Envío (solo para envíos)
            MenuItem trackItem = new MenuItem("Rastrear Envío");
            trackItem.setOnAction(event -> {
                OrderShipmentViewDTO selected = row.getItem();
                if (selected != null && selected.getItemType() == OrderShipmentViewDTO.ItemType.SHIPMENT) {
                    handleTrackForItem(selected);
                }
            });

            // Ver Orden Asociada (solo para envíos)
            MenuItem viewOrderItem = new MenuItem("Ver Orden Asociada");
            viewOrderItem.setOnAction(event -> {
                OrderShipmentViewDTO selected = row.getItem();
                if (selected != null && selected.getItemType() == OrderShipmentViewDTO.ItemType.SHIPMENT) {
                    handleViewAssociatedOrder(selected);
                }
            });

            // Cancelar
            MenuItem cancelItem = new MenuItem("Cancelar");
            cancelItem.setOnAction(event -> {
                OrderShipmentViewDTO selected = row.getItem();
                if (selected != null) {
                    handleCancelForItem(selected);
                }
            });
            cancelItem.setStyle("-fx-text-fill: #dc3545;");

            // Pagar (solo para órdenes pendientes de pago)
            MenuItem payItem = new MenuItem("Pagar");
            payItem.setOnAction(event -> {
                OrderShipmentViewDTO selected = row.getItem();
                if (selected != null) {
                    handlePayForOrder(selected);
                }
            });
            payItem.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");

            // Eliminar (solo para cancelados)
            MenuItem deleteItem = new MenuItem("Eliminar");
            deleteItem.setOnAction(event -> {
                OrderShipmentViewDTO selected = row.getItem();
                if (selected != null) {
                    handleDeleteForItem(selected);
                }
            });
            deleteItem.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");

            // Bind menu visibility
            row.itemProperty().addListener((obs, oldItem, newItem) -> {
                if (newItem != null) {
                    boolean isCancelled = newItem.getStatusDisplay().toLowerCase().contains("cancelad");
                    boolean isPendingPayment = newItem.getStatusDisplay().toLowerCase().contains("pendiente de pago");

                    if (newItem.getItemType() == OrderShipmentViewDTO.ItemType.ORDER) {
                        if (isCancelled) {
                            contextMenu.getItems().setAll(viewDetailsItem, new SeparatorMenuItem(), deleteItem);
                        } else if (isPendingPayment) {
                            contextMenu.getItems().setAll(viewDetailsItem, payItem, new SeparatorMenuItem(), cancelItem);
                        } else {
                            contextMenu.getItems().setAll(viewDetailsItem, cancelItem);
                        }
                    } else {
                        if (isCancelled) {
                            contextMenu.getItems().setAll(viewDetailsItem, trackItem, viewOrderItem, new SeparatorMenuItem(), deleteItem);
                        } else {
                            contextMenu.getItems().setAll(viewDetailsItem, trackItem, viewOrderItem, new SeparatorMenuItem(), cancelItem);
                        }
                    }
                }
            });

            row.contextMenuProperty().bind(
                    javafx.beans.binding.Bindings.when(row.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(contextMenu)
            );

            return row;
        });
    }

    /**
     * Sets up filter combo boxes.
     */
    private void setupFilters() {
        // Type filter
        ObservableList<OrderShipmentViewDTO.ItemType> typeList = FXCollections.observableArrayList();
        typeList.add(null); // "All"
        typeList.addAll(OrderShipmentViewDTO.ItemType.values());
        filterType.setItems(typeList);

        filterType.setCellFactory(lv -> new ListCell<OrderShipmentViewDTO.ItemType>() {
            @Override
            protected void updateItem(OrderShipmentViewDTO.ItemType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else if (item == null) {
                    setText("Todos");
                } else {
                    setText(item.getDisplayName());
                }
            }
        });

        filterType.setButtonCell(new ListCell<OrderShipmentViewDTO.ItemType>() {
            @Override
            protected void updateItem(OrderShipmentViewDTO.ItemType item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null) {
                    setText("Todos");
                    setStyle("-fx-text-fill: #6c757d;");
                } else {
                    setText(item.getDisplayName());
                    setStyle("-fx-text-fill: #495057;");
                }
            }
        });

        filterType.setValue(null);
    }

    /**
     * Loads all orders and shipments for the current user.
     */
    private void loadAllData() {
        if (currentUserId == null) return;

        List<OrderShipmentViewDTO> combined = new ArrayList<>();

        // Load orders
        List<Order> orders = orderService.getOrdersByUser(currentUserId);
        combined.addAll(orders.stream()
                .map(OrderShipmentConverterUtil::fromOrder)
                .collect(Collectors.toList()));

        // Load shipments
        List<ShipmentDTO> shipments = shipmentService.getShipmentsByUser(currentUserId);
        combined.addAll(shipments.stream()
                .map(OrderShipmentConverterUtil::fromShipment)
                .collect(Collectors.toList()));

        // Sort by creation date (newest first)
        combined.sort((a, b) -> {
            if (a.getCreatedDate() == null) return 1;
            if (b.getCreatedDate() == null) return -1;
            return b.getCreatedDate().compareTo(a.getCreatedDate());
        });

        allData = FXCollections.observableArrayList(combined);
        shipmentsTable.setItems(allData);

        Logger.info("Loaded " + orders.size() + " orders and " + shipments.size() + " shipments for user " + currentUserId);
    }

    /**
     * Updates counter labels.
     */
    private void updateCounters() {
        if (allData == null) return;

        long total = allData.size();
        long orders = allData.stream()
            .filter(dto -> dto.getItemType() == OrderShipmentViewDTO.ItemType.ORDER)
            .count();
        long shipments = allData.stream()
            .filter(dto -> dto.getItemType() == OrderShipmentViewDTO.ItemType.SHIPMENT)
            .count();
        long pending = allData.stream()
            .filter(dto -> dto.getStatusDisplay().contains("Esperando") || dto.getStatusDisplay().contains("Pendiente"))
            .count();

        lblTotal.setText(String.valueOf(total));
        lblOrders.setText(String.valueOf(orders));
        lblShipments.setText(String.valueOf(shipments));
        lblPending.setText(String.valueOf(pending));
    }

    /**
     * Gets the current logged-in user's ID.
     */
    private String getCurrentUserId() {
        User currentUser = (User) authService.getCurrentPerson();
        if (currentUser == null) return null;
        return currentUser.getId();
    }

    // ===========================
    // Button Handlers
    // ===========================

    @FXML
    private void handleCreate() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/co/edu/uniquindio/poo/ProyectoFinal2025_2/View/CreateShipment.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Crear Nuevo Envío");

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/co/edu/uniquindio/poo/ProyectoFinal2025_2/Style.css").toExternalForm());
            stage.setScene(scene);

            stage.setWidth(1300);
            stage.setHeight(700);
            stage.setResizable(true);

            stage.show();

            stage.setOnHidden(e -> handleRefresh());

        } catch (IOException e) {
            Logger.error("Failed to load CreateShipment view: " + e.getMessage());
            DialogUtil.showError("Error", "No se pudo abrir el formulario de creación de envíos");
        }
    }

    @FXML
    private void handleRefresh() {
        loadAllData();
        updateCounters();
        DialogUtil.showInfo("Actualizado", "La lista ha sido actualizada");
    }

    @FXML
    private void handleFilter() {
        if (currentUserId == null || allData == null) return;

        List<OrderShipmentViewDTO> filtered = new ArrayList<>(allData);

        // Filter by type
        if (filterType.getValue() != null) {
            filtered = filtered.stream()
                    .filter(dto -> dto.getItemType() == filterType.getValue())
                    .collect(Collectors.toList());
        }

        // Filter by search text
        String searchText = searchField.getText();
        if (searchText != null && !searchText.trim().isEmpty()) {
            String search = searchText.trim().toLowerCase();
            filtered = filtered.stream()
                    .filter(dto -> dto.getId().toLowerCase().contains(search) ||
                                 dto.getRoute().toLowerCase().contains(search))
                    .collect(Collectors.toList());
        }

        shipmentsTable.setItems(FXCollections.observableArrayList(filtered));
    }

    @FXML
    private void handleClearFilter() {
        filterType.setValue(null);
        searchField.clear();
        shipmentsTable.setItems(allData);
    }

    @FXML
    private void handleViewDetails() {
        OrderShipmentViewDTO selected = shipmentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtil.showWarning("No Selection", "Por favor seleccione un elemento para ver detalles");
            return;
        }

        if (selected.getItemType() == OrderShipmentViewDTO.ItemType.ORDER) {
            viewOrderDetails(selected);
        } else {
            viewShipmentDetails(selected.getId());
        }
    }

    @FXML
    private void handleTrack() {
        OrderShipmentViewDTO selected = shipmentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtil.showWarning("No Selection", "Por favor seleccione un elemento para rastrear");
            return;
        }

        if (!selected.isCanTrack()) {
            DialogUtil.showWarning("No disponible", "Este elemento no se puede rastrear todavía");
            return;
        }

        String trackingId = selected.getItemType() == OrderShipmentViewDTO.ItemType.ORDER
            ? selected.getShipmentId()
            : selected.getId();

        if (trackingId == null) {
            DialogUtil.showWarning("No disponible", "No hay envío asociado para rastrear");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/co/edu/uniquindio/poo/ProyectoFinal2025_2/View/TrackShipment.fxml"));
            Parent root = loader.load();

            TrackShipmentController controller = loader.getController();
            controller.trackShipment(trackingId);

            Stage stage = new Stage();
            stage.setTitle("Rastrear Envío - " + trackingId);
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            Logger.error("Failed to load TrackShipment view: " + e.getMessage());
            DialogUtil.showError("Error", "No se pudo abrir la vista de rastreo");
        }
    }

    @FXML
    private void handleCancel() {
        OrderShipmentViewDTO selected = shipmentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtil.showWarning("No Selection", "Por favor seleccione un elemento para cancelar");
            return;
        }

        if (!selected.isCanCancel()) {
            DialogUtil.showError("No se puede cancelar", "Este elemento no se puede cancelar en su estado actual");
            return;
        }

        boolean confirmed = DialogUtil.showConfirmation(
            "Cancelar " + selected.getTypeDisplay(),
            "¿Está seguro de que desea cancelar " + selected.getTypeDisplay() + " " + selected.getId() + "?"
        );

        if (!confirmed) return;

        try {
            boolean success = false;
            if (selected.getItemType() == OrderShipmentViewDTO.ItemType.SHIPMENT) {
                success = shipmentService.cancelShipment(selected.getId());
            }
            // TODO: Add order cancellation logic when implemented

            if (success) {
                DialogUtil.showSuccess("Cancelado", "El elemento ha sido cancelado correctamente");
                loadAllData();
                updateCounters();
            }
        } catch (Exception e) {
            Logger.error("Failed to cancel: " + e.getMessage());
            DialogUtil.showError("Error", "No se pudo cancelar: " + e.getMessage());
        }
    }

    // ===========================
    // Context Menu Handlers
    // ===========================

    /**
     * Handles view details for a specific item from context menu.
     */
    private void handleViewDetailsForItem(OrderShipmentViewDTO item) {
        if (item.getItemType() == OrderShipmentViewDTO.ItemType.ORDER) {
            viewOrderDetails(item);
        } else {
            viewShipmentDetails(item.getId());
        }
    }

    /**
     * Handles track for a specific shipment from context menu.
     */
    private void handleTrackForItem(OrderShipmentViewDTO item) {
        if (!item.isCanTrack()) {
            DialogUtil.showWarning("No disponible", "Este elemento no se puede rastrear todavía");
            return;
        }

        String trackingId = item.getId();
        if (trackingId == null) {
            DialogUtil.showWarning("No disponible", "No hay envío asociado para rastrear");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/co/edu/uniquindio/poo/ProyectoFinal2025_2/View/TrackShipment.fxml"));
            Parent root = loader.load();

            TrackShipmentController controller = loader.getController();
            controller.trackShipment(trackingId);

            Stage stage = new Stage();
            stage.setTitle("Rastrear Envío");
            Scene scene = new Scene(root);
            co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.ThemeManager.getInstance().applyThemeToScene(scene);
            stage.setScene(scene);
            stage.setResizable(true);
            stage.show();
        } catch (IOException e) {
            Logger.error("Failed to load TrackShipment view: " + e.getMessage());
            DialogUtil.showError("Error", "No se pudo abrir la vista de rastreo");
        }
    }

    /**
     * Handles view associated order for a shipment from context menu.
     */
    private void handleViewAssociatedOrder(OrderShipmentViewDTO shipmentDto) {
        if (shipmentDto == null || shipmentDto.getItemType() != OrderShipmentViewDTO.ItemType.SHIPMENT) {
            return;
        }

        try {
            // Get the shipment to access orderId
            Optional<ShipmentDTO> shipmentOpt = shipmentService.getShipment(shipmentDto.getId());
            if (!shipmentOpt.isPresent() || shipmentOpt.get().getOrderId() == null) {
                DialogUtil.showWarning("Sin Orden Asociada", "Este envío no tiene una orden asociada.");
                return;
            }

            // Get the associated order
            ShipmentDTO shipment = shipmentOpt.get();
            Optional<Order> orderOpt = orderRepository.findById(shipment.getOrderId());
            if (!orderOpt.isPresent()) {
                DialogUtil.showError("Error", "No se pudo encontrar la orden asociada.");
                return;
            }

            // Display order details
            Order order = orderOpt.get();
            StringBuilder details = new StringBuilder();
            details.append("===== ORDEN ASOCIADA =====\n\n");
            details.append("ID Orden: ").append(order.getId()).append("\n");
            details.append("Estado: ").append(order.getStatus().getDisplayName()).append("\n");
            details.append("Fecha Creación: ").append(order.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n");

            // Get cost from invoice
            String costStr = "N/A";
            if (order.getInvoiceId() != null) {
                Optional<Invoice> invoiceOpt = invoiceRepository.findById(order.getInvoiceId());
                if (invoiceOpt.isPresent()) {
                    costStr = String.format("%.2f", invoiceOpt.get().getTotalAmount());
                }
            }
            details.append("Costo Total: $").append(costStr).append("\n");
            details.append("\n--- Origen ---\n");
            if (order.getOrigin() != null) {
                details.append(order.getOrigin().getStreet()).append(", ")
                       .append(order.getOrigin().getCity()).append("\n");
            }
            details.append("\n--- Destino ---\n");
            if (order.getDestination() != null) {
                details.append(order.getDestination().getStreet()).append(", ")
                       .append(order.getDestination().getCity()).append("\n");
            }
            if (order.getPaymentId() != null) {
                details.append("\nID Pago: ").append(order.getPaymentId()).append("\n");
            }
            if (order.getInvoiceId() != null) {
                details.append("ID Factura: ").append(order.getInvoiceId()).append("\n");
            }

            DialogUtil.showInfo("Orden Asociada al Envío", details.toString());
            Logger.info("Viewed associated order " + order.getId() + " for shipment " + shipmentDto.getId());

        } catch (Exception e) {
            Logger.error("Failed to view associated order: " + e.getMessage());
            DialogUtil.showError("Error", "No se pudo mostrar la orden asociada: " + e.getMessage());
        }
    }

    /**
     * Handles cancel for a specific item from context menu.
     */
    private void handleCancelForItem(OrderShipmentViewDTO item) {
        if (!item.isCanCancel()) {
            DialogUtil.showError("No se puede cancelar", "Este elemento no se puede cancelar en su estado actual");
            return;
        }

        boolean confirmed = DialogUtil.showWarningConfirmation(
            "Cancelar " + item.getTypeDisplay(),
            "¿Está seguro de que desea cancelar " + item.getTypeDisplay() + " " + item.getId() + "?",
            "Esta acción NO se puede deshacer."
        );

        if (!confirmed) return;

        try {
            boolean success = false;
            if (item.getItemType() == OrderShipmentViewDTO.ItemType.SHIPMENT) {
                success = shipmentService.cancelShipment(item.getId());
            } else if (item.getItemType() == OrderShipmentViewDTO.ItemType.ORDER) {
                // Cancel the order
                try {
                    orderService.cancelOrder(item.getId());
                    success = true;
                } catch (IllegalStateException | IllegalArgumentException e) {
                    Logger.error("Cannot cancel order: " + e.getMessage());
                    DialogUtil.showError("No se puede cancelar", e.getMessage());
                    return;
                }
            }

            if (success) {
                DialogUtil.showSuccess("El elemento ha sido cancelado correctamente");
                loadAllData();
                updateCounters();
            } else {
                DialogUtil.showError("Error", "No se pudo cancelar el elemento");
            }
        } catch (Exception e) {
            Logger.error("Failed to cancel: " + e.getMessage());
            DialogUtil.showError("Error", "No se pudo cancelar: " + e.getMessage());
        }
    }

    /**
     * Handles deletion of cancelled items (orders or shipments).
     * Only cancelled items can be deleted.
     */
    private void handleDeleteForItem(OrderShipmentViewDTO item) {
        // Verify item is cancelled
        if (!item.getStatusDisplay().toLowerCase().contains("cancelad")) {
            DialogUtil.showError("No se puede eliminar", "Solo se pueden eliminar elementos cancelados");
            return;
        }

        boolean confirmed = DialogUtil.showWarningConfirmation(
            "Eliminar " + item.getTypeDisplay(),
            "¿Está seguro de que desea eliminar " + item.getTypeDisplay() + " " + item.getId() + "?",
            "Esta acción eliminará permanentemente el registro. NO se puede deshacer."
        );

        if (!confirmed) return;

        try {
            boolean success = false;
            if (item.getItemType() == OrderShipmentViewDTO.ItemType.SHIPMENT) {
                success = shipmentService.deleteShipment(item.getId());
            } else if (item.getItemType() == OrderShipmentViewDTO.ItemType.ORDER) {
                success = orderService.deleteOrder(item.getId());
            }

            if (success) {
                DialogUtil.showSuccess("El elemento ha sido eliminado correctamente");
                loadAllData();
                updateCounters();
            } else {
                DialogUtil.showError("Error", "No se pudo eliminar el elemento");
            }
        } catch (Exception e) {
            Logger.error("Failed to delete: " + e.getMessage());
            DialogUtil.showError("Error", "No se pudo eliminar: " + e.getMessage());
        }
    }

    /**
     * Handles payment for an order with pending payment status.
     * Opens a payment processor selection dialog.
     */
    private void handlePayForOrder(OrderShipmentViewDTO item) {
        if (item.getItemType() != OrderShipmentViewDTO.ItemType.ORDER) {
            DialogUtil.showError("Error", "Solo se puede pagar una orden");
            return;
        }

        if (!item.getStatusDisplay().toLowerCase().contains("pendiente de pago")) {
            DialogUtil.showError("Error", "Esta orden no está pendiente de pago");
            return;
        }

        try {
            // Get full order object
            Optional<Order> orderOpt = orderRepository.findById(item.getId());
            if (!orderOpt.isPresent()) {
                DialogUtil.showError("Error", "No se pudo encontrar la orden");
                return;
            }

            Order order = orderOpt.get();

            // Load payment processor selection dialog
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "/co/edu/uniquindio/poo/ProyectoFinal2025_2/View/PaymentProcessorSelection.fxml"));
            Parent root = loader.load();

            PaymentProcessorSelectionController selectionController = loader.getController();

            // Create an OrderDetailDTO from the order and shipment
            OrderDetailDTO orderDetail = new OrderDetailDTO();
            orderDetail.setUserId(order.getUserId());
            orderDetail.setOrigin(order.getOrigin());
            orderDetail.setDestination(order.getDestination());

            // Get shipment details if shipment exists
            if (order.getShipmentId() != null && !order.getShipmentId().isEmpty()) {
                Optional<Shipment> shipmentOpt = ShipmentRepository.getInstance().findById(order.getShipmentId());
                if (shipmentOpt.isPresent()) {
                    Shipment shipment = shipmentOpt.get();
                    orderDetail.setWeightKg(shipment.getWeightKg());
                    orderDetail.setHeightCm(shipment.getHeightCm());
                    orderDetail.setWidthCm(shipment.getWidthCm());
                    orderDetail.setLengthCm(shipment.getLengthCm());
                    orderDetail.setVolumeM3(shipment.getVolumeM3());
                    orderDetail.setPriority(shipment.getPriority());
                    orderDetail.setAdditionalServices(shipment.getAdditionalServices());
                    orderDetail.setBaseCost(shipment.getBaseCost());
                    orderDetail.setServicesCost(shipment.getServicesCost());
                    orderDetail.setTotalCost(shipment.getTotalCost());
                    orderDetail.setUserNotes(shipment.getUserNotes());
                    orderDetail.setRequestedPickupDate(shipment.getRequestedPickupDate());
                    orderDetail.setEstimatedDelivery(shipment.getEstimatedDate());
                }
            }

            selectionController.setOrder(order, orderDetail);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Seleccionar Método de Pago");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(false);

            dialogStage.showAndWait();

            // Refresh after payment
            loadAllData();
            updateCounters();

        } catch (Exception e) {
            Logger.error("Error opening payment selection: " + e.getMessage());
            e.printStackTrace();
            DialogUtil.showError("Error", "No se pudo abrir la selección de pago: " + e.getMessage());
        }
    }

    // ===========================
    // Helper Methods
    // ===========================

    private void viewOrderDetails(OrderShipmentViewDTO orderDto) {
        StringBuilder details = new StringBuilder();
        details.append("ID: ").append(orderDto.getId()).append("\n");
        details.append("Tipo: Orden\n");
        details.append("Ruta: ").append(orderDto.getRoute()).append("\n");
        details.append("Estado: ").append(orderDto.getStatusDisplay()).append("\n");
        details.append("Fecha: ").append(orderDto.getDateFormatted()).append("\n");
        if (orderDto.getShipmentId() != null) {
            details.append("ID Envío: ").append(orderDto.getShipmentId()).append("\n");
        }
        if (orderDto.getPaymentId() != null) {
            details.append("ID Pago: ").append(orderDto.getPaymentId()).append("\n");
        }
        if (orderDto.getInvoiceId() != null) {
            details.append("ID Factura: ").append(orderDto.getInvoiceId());
        }

        DialogUtil.showInfo("Detalles de la Orden", details.toString());
    }

    private void viewShipmentDetails(String shipmentId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/co/edu/uniquindio/poo/ProyectoFinal2025_2/View/ShipmentDetail.fxml"));
            Parent root = loader.load();

            ShipmentDetailController controller = loader.getController();
            controller.loadShipmentDetails(shipmentId);

            Stage stage = new Stage();
            stage.setTitle("Detalles del Envío - " + shipmentId);
            Scene scene = new Scene(root, 650, 800);
            co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.ThemeManager.getInstance().applyThemeToScene(scene);
            stage.setScene(scene);
            stage.setResizable(true);
            stage.setMaximized(false);
            stage.show();

        } catch (IOException e) {
            Logger.error("Failed to load ShipmentDetail view: " + e.getMessage());
            DialogUtil.showError("Error", "No se pudo abrir los detalles del envío");
        }
    }

    // ===========================
    // Tab System Methods
    // ===========================

    /**
     * Switches to the Stats tab.
     * Expands the section if collapsed (user interaction).
     */
    @FXML
    private void switchToStatsTab() {
        setActiveTab(btnTabStats);
        showTabContent(statsTabContent, "stats", true);
    }

    /**
     * Switches to the Filters tab.
     * Expands the section if collapsed (user interaction).
     */
    @FXML
    private void switchToFiltersTab() {
        setActiveTab(btnTabFilters);
        showTabContent(filtersTabContent, "filters", true);
    }

    /**
     * Sets the active tab button and removes active class from others.
     */
    private void setActiveTab(Button activeButton) {
        // Remove active class from all tab buttons
        btnTabStats.getStyleClass().remove("tab-button-active");
        btnTabFilters.getStyleClass().remove("tab-button-active");

        // Add active class to the selected tab
        activeButton.getStyleClass().add("tab-button-active");
    }

    /**
     * Shows the specified tab content and hides all others.
     *
     * @param contentToShow The tab content pane to display
     * @param tabId The ID of the tab for state persistence
     * @param expandIfCollapsed If true, expands the section if it's currently collapsed (for user clicks).
     *                          If false, respects the current collapsed state (for restoring view state).
     */
    private void showTabContent(javafx.scene.layout.Pane contentToShow, String tabId, boolean expandIfCollapsed) {
        // If user clicked a tab and section is collapsed, expand it
        if (expandIfCollapsed) {
            boolean isExpanded = TabStateManager.isExpanded(VIEW_NAME);
            if (!isExpanded) {
                TabStateManager.setExpanded(VIEW_NAME, true);
                applyCollapseState(true);
            }
        }

        // Hide all tab contents
        statsTabContent.setVisible(false);
        statsTabContent.setManaged(false);
        filtersTabContent.setVisible(false);
        filtersTabContent.setManaged(false);

        // Show the selected content
        contentToShow.setVisible(true);
        contentToShow.setManaged(true);

        // Save state
        TabStateManager.setActiveTab(VIEW_NAME, tabId);
    }

    // =================================================================================================================
    // Collapse/Expand Methods
    // =================================================================================================================

    /**
     * Toggles the collapse/expand state of the tab section.
     */
    @FXML
    private void toggleCollapse() {
        boolean currentlyExpanded = TabStateManager.isExpanded(VIEW_NAME);
        boolean newExpanded = !currentlyExpanded;
        TabStateManager.setExpanded(VIEW_NAME, newExpanded);
        applyCollapseState(newExpanded);
    }

    /**
     * Applies the collapse/expand visual state.
     */
    private void applyCollapseState(boolean expanded) {
        if (expanded) {
            // Remove collapsed class and add expanded class
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
            // Remove expanded class and add collapsed class
            collapsibleTabSection.getStyleClass().removeAll("tab-section-expanded");
            if (!collapsibleTabSection.getStyleClass().contains("tab-section-collapsed")) {
                collapsibleTabSection.getStyleClass().add("tab-section-collapsed");
            }

            // Hide ONLY content container (tabs remain visible)
            javafx.scene.Node contentContainer = collapsibleTabSection.lookup(".tab-content-container");
            if (contentContainer != null) {
                contentContainer.setVisible(false);
                contentContainer.setManaged(false);
            }

            btnCollapseToggle.setText("▼");
        }
    }

    /**
     * Restores the saved collapse/expand state and active tab from previous session.
     */
    private void restoreViewState() {
        // Restore collapse/expand state
        boolean expanded = TabStateManager.isExpanded(VIEW_NAME);
        applyCollapseState(expanded);

        // Restore active tab (without auto-expanding)
        String activeTab = TabStateManager.getActiveTab(VIEW_NAME);
        javafx.scene.layout.Pane contentToRestore;
        String tabIdToRestore;

        if ("filters".equals(activeTab)) {
            contentToRestore = filtersTabContent;
            tabIdToRestore = "filters";
            setActiveTab(btnTabFilters);
        } else {
            contentToRestore = statsTabContent;
            tabIdToRestore = "stats";
            setActiveTab(btnTabStats);
        }

        showTabContent(contentToRestore, tabIdToRestore, false); // false = don't auto-expand
    }
}
