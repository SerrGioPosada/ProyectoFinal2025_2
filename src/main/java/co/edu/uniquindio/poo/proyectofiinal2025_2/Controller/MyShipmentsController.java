package co.edu.uniquindio.poo.proyectofiinal2025_2.Controller;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Enums.ShipmentStatus;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.User;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.dto.ShipmentDTO;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.dto.ShipmentFilterDTO;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Services.AuthenticationService;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Services.ShipmentService;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Util.UtilController.DialogUtil;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Util.UtilModel.Logger;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller for user's shipments view.
 * Displays only shipments belonging to the current logged-in user.
 */
public class MyShipmentsController implements Initializable {

    // Table and Columns
    @FXML private TableView<ShipmentDTO> shipmentsTable;
    @FXML private TableColumn<ShipmentDTO, String> colId;
    @FXML private TableColumn<ShipmentDTO, String> colRoute;
    @FXML private TableColumn<ShipmentDTO, String> colStatus;
    @FXML private TableColumn<ShipmentDTO, String> colDate;
    @FXML private TableColumn<ShipmentDTO, Double> colCost;

    // Filters
    @FXML private ComboBox<ShipmentStatus> filterStatus;
    @FXML private DatePicker filterDateFrom;
    @FXML private DatePicker filterDateTo;
    @FXML private TextField searchField;

    // Counter Labels
    @FXML private Label lblTotal;
    @FXML private Label lblPending;
    @FXML private Label lblInRoute;
    @FXML private Label lblDelivered;

    // Services
    private final ShipmentService shipmentService = new ShipmentService();
    private final AuthenticationService authService = AuthenticationService.getInstance();

    // Data
    private ObservableList<ShipmentDTO> shipmentsData;
    private String currentUserId;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        currentUserId = getCurrentUserId();

        if (currentUserId == null) {
            DialogUtil.showError("No user logged in");
            return;
        }

        setupTable();
        setupFilters();
        loadUserShipments();
        updateCounters();

        Logger.info("MyShipmentsController initialized for user: " + currentUserId);
    }

    /**
     * Sets up table columns with cell value factories.
     */
    private void setupTable() {
        colId.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getId()));

        colRoute.setCellValueFactory(data -> {
            String origin = data.getValue().getOriginAddressComplete();
            String destination = data.getValue().getDestinationAddressComplete();
            return new SimpleStringProperty(origin + " → " + destination);
        });

        colStatus.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getStatusDisplayName()));

        colDate.setCellValueFactory(data -> {
            if (data.getValue().getCreationDate() == null) return new SimpleStringProperty("--");
            return new SimpleStringProperty(
                data.getValue().getCreationDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            );
        });

        colCost.setCellValueFactory(data ->
            new SimpleDoubleProperty(data.getValue().getTotalCost()).asObject());

        // Format currency in cost column
        colCost.setCellFactory(column -> new TableCell<ShipmentDTO, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                setText(String.format("$%,.2f", item));
            }
        });

        // Apply status colors
        colStatus.setCellFactory(column -> new TableCell<ShipmentDTO, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(item);

                ShipmentDTO shipment = getTableView().getItems().get(getIndex());
                if (shipment == null) return;

                setStyle("-fx-text-fill: " + shipment.getStatusColor() + "; -fx-font-weight: bold;");
            }
        });
    }

    /**
     * Sets up filter combo boxes.
     */
    private void setupFilters() {
        // Status filter - add "All" option at the beginning
        ObservableList<ShipmentStatus> statusList = FXCollections.observableArrayList();
        statusList.add(null); // Null represents "All"
        statusList.addAll(ShipmentStatus.values());
        filterStatus.setItems(statusList);

        // Configure cell factory for dropdown items
        filterStatus.setCellFactory(lv -> new ListCell<ShipmentStatus>() {
            @Override
            protected void updateItem(ShipmentStatus item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else if (item == null) {
                    setText("Todos los Estados");
                } else {
                    setText(item.getDisplayName());
                }
            }
        });

        // Configure button cell for selected item display
        filterStatus.setButtonCell(new ListCell<ShipmentStatus>() {
            @Override
            protected void updateItem(ShipmentStatus item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null) {
                    setText("Todos los Estados");
                    setStyle("-fx-text-fill: #6c757d;");
                } else {
                    setText(item.getDisplayName());
                    setStyle("-fx-text-fill: #495057;");
                }
            }
        });

        // Set default value to null (All)
        filterStatus.setValue(null);
    }

    /**
     * Loads all shipments for the current user.
     */
    private void loadUserShipments() {
        if (currentUserId == null) return;

        List<ShipmentDTO> shipments = shipmentService.getShipmentsByUser(currentUserId);
        shipmentsData = FXCollections.observableArrayList(shipments);
        shipmentsTable.setItems(shipmentsData);

        Logger.info("Loaded " + shipments.size() + " shipments for user " + currentUserId);
    }

    /**
     * Updates counter labels with shipment counts by status.
     */
    private void updateCounters() {
        if (shipmentsData == null) return;

        long total = shipmentsData.size();
        long pending = shipmentsData.stream()
            .filter(s -> s.getStatus() == ShipmentStatus.PENDING_ASSIGNMENT)
            .count();
        long inRoute = shipmentsData.stream()
            .filter(s -> s.getStatus() == ShipmentStatus.IN_TRANSIT ||
                        s.getStatus() == ShipmentStatus.OUT_FOR_DELIVERY)
            .count();
        long delivered = shipmentsData.stream()
            .filter(s -> s.getStatus() == ShipmentStatus.DELIVERED)
            .count();

        lblTotal.setText(String.valueOf(total));
        lblPending.setText(String.valueOf(pending));
        lblInRoute.setText(String.valueOf(inRoute));
        lblDelivered.setText(String.valueOf(delivered));
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/co/edu/uniquindio/poo/proyectofiinal2025_2/View/CreateShipment.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Crear Nuevo Envío");

            // Create scene with stylesheet
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/co/edu/uniquindio/poo/proyectofiinal2025_2/Style.css").toExternalForm());
            stage.setScene(scene);

            // Set window size
            stage.setWidth(900);
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
        loadUserShipments();
        updateCounters();
        DialogUtil.showInfo("Refreshed", "Shipment list has been updated");
    }

    @FXML
    private void handleFilter() {
        if (currentUserId == null) return;

        ShipmentFilterDTO filter = new ShipmentFilterDTO();
        filter.setUserId(currentUserId);
        filter.setStatus(filterStatus.getValue());
        filter.setDateFrom(filterDateFrom.getValue());
        filter.setDateTo(filterDateTo.getValue());

        String searchText = searchField.getText();
        if (searchText != null && !searchText.trim().isEmpty()) {
            filter.setSearchText(searchText.trim());
        }

        List<ShipmentDTO> filtered = shipmentService.filterShipments(filter);
        shipmentsData = FXCollections.observableArrayList(filtered);
        shipmentsTable.setItems(shipmentsData);
        updateCounters();
    }

    @FXML
    private void handleClearFilter() {
        filterStatus.setValue(null);
        filterDateFrom.setValue(null);
        filterDateTo.setValue(null);
        searchField.clear();
        loadUserShipments();
        updateCounters();
    }

    @FXML
    private void handleViewDetails() {
        ShipmentDTO selected = shipmentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtil.showWarning("No Selection", "Please select a shipment to view details");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/co/edu/uniquindio/poo/proyectofiinal2025_2/View/ShipmentDetail.fxml"));
            Parent root = loader.load();

            ShipmentDetailController controller = loader.getController();
            controller.loadShipmentDetails(selected.getId());

            Stage stage = new Stage();
            stage.setTitle("Shipment Details - " + selected.getId());
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            Logger.error("Failed to load ShipmentDetail view: " + e.getMessage());
            DialogUtil.showError("Error", "Could not open shipment details");
        }
    }

    @FXML
    private void handleTrack() {
        ShipmentDTO selected = shipmentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtil.showWarning("No Selection", "Please select a shipment to track");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/co/edu/uniquindio/poo/proyectofiinal2025_2/View/TrackShipment.fxml"));
            Parent root = loader.load();

            TrackShipmentController controller = loader.getController();
            controller.trackShipment(selected.getId());

            Stage stage = new Stage();
            stage.setTitle("Track Shipment - " + selected.getId());
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            Logger.error("Failed to load TrackShipment view: " + e.getMessage());
            DialogUtil.showError("Error", "Could not open tracking view");
        }
    }

    @FXML
    private void handleCancel() {
        ShipmentDTO selected = shipmentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtil.showWarning("No Selection", "Please select a shipment to cancel");
            return;
        }

        if (!selected.isCanBeCancelled()) {
            DialogUtil.showError("Cannot Cancel", "This shipment cannot be cancelled in its current status");
            return;
        }

        boolean confirmed = DialogUtil.showConfirmation(
            "Cancel Shipment",
            "Are you sure you want to cancel shipment " + selected.getId() + "?"
        );

        if (!confirmed) return;

        try {
            boolean success = shipmentService.cancelShipment(selected.getId());
            if (success) {
                DialogUtil.showSuccess("Cancelled", "Shipment has been cancelled successfully");
                loadUserShipments();
                updateCounters();
            }
        } catch (Exception e) {
            Logger.error("Failed to cancel shipment: " + e.getMessage());
            DialogUtil.showError("Error", "Could not cancel shipment: " + e.getMessage());
        }
    }
}
