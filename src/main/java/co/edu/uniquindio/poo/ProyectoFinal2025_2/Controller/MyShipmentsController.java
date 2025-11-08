package co.edu.uniquindio.poo.ProyectoFinal2025_2.Controller;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Order;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.User;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.dto.OrderShipmentViewDTO;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.dto.ShipmentDTO;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.AuthenticationService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.OrderService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.ShipmentService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.Adapter.OrderShipmentConverterUtil;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.DialogUtil;
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
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller for user's orders and shipments view.
 * Displays both Orders and Shipments in a unified table.
 */
public class MyShipmentsController implements Initializable {

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

    // Counter Labels
    @FXML private Label lblTotal;
    @FXML private Label lblOrders;
    @FXML private Label lblShipments;
    @FXML private Label lblPending;

    // Services
    private final ShipmentService shipmentService = new ShipmentService();
    private final OrderService orderService = new OrderService();
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

            // Cancelar
            MenuItem cancelItem = new MenuItem("Cancelar");
            cancelItem.setOnAction(event -> {
                OrderShipmentViewDTO selected = row.getItem();
                if (selected != null) {
                    handleCancelForItem(selected);
                }
            });
            cancelItem.setStyle("-fx-text-fill: #dc3545;");

            // Bind menu visibility
            row.itemProperty().addListener((obs, oldItem, newItem) -> {
                if (newItem != null && newItem.getItemType() == OrderShipmentViewDTO.ItemType.ORDER) {
                    contextMenu.getItems().setAll(viewDetailsItem, cancelItem);
                } else if (newItem != null) {
                    contextMenu.getItems().setAll(viewDetailsItem, trackItem, new SeparatorMenuItem(), cancelItem);
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
            }
            // TODO: Add order cancellation logic when implemented

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
            Scene scene = new Scene(root, 900, 700);
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
}
