package co.edu.uniquindio.poo.proyectofiinal2025_2.Controller;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.DeliveryPerson;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Enums.CoverageArea;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Enums.IncidentType;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Enums.ShipmentStatus;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.User;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.dto.ShipmentDTO;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.dto.ShipmentFilterDTO;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Repositories.DeliveryPersonRepository;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Repositories.UserRepository;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Services.AuthenticationService;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Services.ShipmentService;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Util.UtilController.DialogUtil;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Util.UtilModel.Logger;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller for shipment management (Admin view).
 * Allows admins to manage ALL shipments in the system.
 */
public class ShipmentManagementController implements Initializable {

    // Root pane
    @FXML private VBox rootPane;

    // Table and Columns
    @FXML private TableView<ShipmentDTO> shipmentsTable;
    @FXML private TableColumn<ShipmentDTO, String> colId;
    @FXML private TableColumn<ShipmentDTO, String> colUser;
    @FXML private TableColumn<ShipmentDTO, String> colRoute;
    @FXML private TableColumn<ShipmentDTO, Double> colWeight;
    @FXML private TableColumn<ShipmentDTO, String> colStatus;
    @FXML private TableColumn<ShipmentDTO, String> colDeliveryPerson;
    @FXML private TableColumn<ShipmentDTO, String> colCreationDate;
    @FXML private TableColumn<ShipmentDTO, String> colEstimatedDate;
    @FXML private TableColumn<ShipmentDTO, Double> colCost;
    @FXML private TableColumn<ShipmentDTO, Integer> colPriority;

    // Filters
    @FXML private ComboBox<ShipmentStatus> filterStatus;
    @FXML private ComboBox<CoverageArea> filterZone;
    @FXML private ComboBox<String> filterUser;
    @FXML private ComboBox<String> filterDeliveryPerson;
    @FXML private DatePicker filterDateFrom;
    @FXML private DatePicker filterDateTo;
    @FXML private TextField searchField;
    @FXML private CheckBox chkDelayed;
    @FXML private CheckBox chkIncidents;

    // Counter Labels
    @FXML private Label lblTotalShipments;
    @FXML private Label lblPending;
    @FXML private Label lblInRoute;
    @FXML private Label lblDelivered;
    @FXML private Label lblIncidents;

    // Services and Repositories
    private final ShipmentService shipmentService = new ShipmentService();
    private final UserRepository userRepository = UserRepository.getInstance();
    private final DeliveryPersonRepository deliveryPersonRepository = DeliveryPersonRepository.getInstance();
    private final AuthenticationService authService = AuthenticationService.getInstance();

    // Data
    private ObservableList<ShipmentDTO> shipmentsData;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        setupFilters();
        loadAllShipments();
        updateCounters();

        Logger.info("ShipmentManagementController initialized");
    }

    /**
     * Sets up table columns with cell value factories.
     */
    private void setupTable() {
        colId.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getId()));
        colUser.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUserName()));

        colRoute.setCellValueFactory(data -> {
            String origin = data.getValue().getOriginAddressComplete();
            String destination = data.getValue().getDestinationAddressComplete();
            if (origin == null) origin = "N/A";
            if (destination == null) destination = "N/A";
            return new SimpleStringProperty(origin + " → " + destination);
        });

        colWeight.setCellValueFactory(data ->
            new SimpleDoubleProperty(data.getValue().getWeightKg()).asObject());

        colStatus.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getStatusDisplayName()));

        colDeliveryPerson.setCellValueFactory(data -> {
            String name = data.getValue().getDeliveryPersonName();
            return new SimpleStringProperty(name != null ? name : "Unassigned");
        });

        colCreationDate.setCellValueFactory(data -> {
            if (data.getValue().getCreationDate() == null) return new SimpleStringProperty("--");
            return new SimpleStringProperty(
                data.getValue().getCreationDate().format(DATE_FORMATTER)
            );
        });

        colEstimatedDate.setCellValueFactory(data -> {
            if (data.getValue().getEstimatedDeliveryDate() == null) return new SimpleStringProperty("--");
            return new SimpleStringProperty(
                data.getValue().getEstimatedDeliveryDate().format(DATE_FORMATTER)
            );
        });

        colCost.setCellValueFactory(data ->
            new SimpleDoubleProperty(data.getValue().getTotalCost()).asObject());

        colPriority.setCellValueFactory(data ->
            new SimpleIntegerProperty(data.getValue().getPriority()).asObject());

        // Format currency
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

        // Color status column
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

        // Enable multiple selection
        shipmentsTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    /**
     * Sets up filter combo boxes with data.
     * Each ComboBox includes a special "All" option and uses a custom converter
     * to display the appropriate text.
     */
    private void setupFilters() {
        // Status filter - add "All" option at the beginning
        ObservableList<ShipmentStatus> statusList = FXCollections.observableArrayList();
        statusList.add(null); // Null represents "All"
        statusList.addAll(ShipmentStatus.values());
        filterStatus.setItems(statusList);
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
        filterStatus.setButtonCell(new ListCell<ShipmentStatus>() {
            @Override
            protected void updateItem(ShipmentStatus item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null) {
                    setText("Todos los Estados");
                    setStyle("-fx-text-fill: #6c757d;"); // Gray color for placeholder
                } else {
                    setText(item.getDisplayName());
                    setStyle("-fx-text-fill: #495057;"); // Normal text color
                }
            }
        });
        filterStatus.setValue(null);

        // Zone filter - add "All" option at the beginning
        ObservableList<CoverageArea> zoneList = FXCollections.observableArrayList();
        zoneList.add(null); // Null represents "All"
        zoneList.addAll(CoverageArea.values());
        filterZone.setItems(zoneList);
        filterZone.setCellFactory(lv -> new ListCell<CoverageArea>() {
            @Override
            protected void updateItem(CoverageArea item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else if (item == null) {
                    setText("Todas las Zonas");
                } else {
                    setText(item.getDisplayName());
                }
            }
        });
        filterZone.setButtonCell(new ListCell<CoverageArea>() {
            @Override
            protected void updateItem(CoverageArea item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null) {
                    setText("Todas las Zonas");
                    setStyle("-fx-text-fill: #6c757d;");
                } else {
                    setText(item.getDisplayName());
                    setStyle("-fx-text-fill: #495057;");
                }
            }
        });
        filterZone.setValue(null);

        // User filter - add "All" option at the beginning
        ObservableList<String> userList = FXCollections.observableArrayList();
        userList.add(null); // Null represents "All"
        userList.addAll(userRepository.getUsers().stream()
            .map(User::getEmail)
            .collect(Collectors.toList()));
        filterUser.setItems(userList);
        filterUser.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else if (item == null) {
                    setText("Todos los Usuarios");
                } else {
                    setText(item);
                }
            }
        });
        filterUser.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null) {
                    setText("Todos los Usuarios");
                    setStyle("-fx-text-fill: #6c757d;");
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: #495057;");
                }
            }
        });
        filterUser.setValue(null);

        // Delivery person filter - add "All" option at the beginning
        ObservableList<String> dpList = FXCollections.observableArrayList();
        dpList.add(null); // Null represents "All"
        dpList.addAll(deliveryPersonRepository.getAllDeliveryPersons().stream()
            .map(DeliveryPerson::getEmail)
            .collect(Collectors.toList()));
        filterDeliveryPerson.setItems(dpList);
        filterDeliveryPerson.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else if (item == null) {
                    setText("Todos los Repartidores");
                } else {
                    setText(item);
                }
            }
        });
        filterDeliveryPerson.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null) {
                    setText("Todos los Repartidores");
                    setStyle("-fx-text-fill: #6c757d;");
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: #495057;");
                }
            }
        });
        filterDeliveryPerson.setValue(null);
    }

    /**
     * Loads all shipments in the system.
     */
    private void loadAllShipments() {
        List<ShipmentDTO> shipments = shipmentService.listAll();
        shipmentsData = FXCollections.observableArrayList(shipments);
        shipmentsTable.setItems(shipmentsData);

        Logger.info("Loaded " + shipments.size() + " total shipments");
    }

    /**
     * Updates counter labels.
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
        long incidents = shipmentsData.stream()
            .filter(s -> s.getIncident() != null)
            .count();

        lblTotalShipments.setText(String.valueOf(total));
        lblPending.setText(String.valueOf(pending));
        lblInRoute.setText(String.valueOf(inRoute));
        lblDelivered.setText(String.valueOf(delivered));
        lblIncidents.setText(String.valueOf(incidents));
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
            stage.setTitle("Create New Shipment");

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
            Logger.error("Failed to load CreateShipment view: " + e.getMessage(), e);
            e.printStackTrace();
            DialogUtil.showError("Error", "Could not open create shipment form: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        loadAllShipments();
        updateCounters();
        DialogUtil.showInfo("Refreshed", "Shipment list has been updated");
    }

    @FXML
    private void handleFilter() {
        ShipmentFilterDTO filter = new ShipmentFilterDTO();
        filter.setStatus(filterStatus.getValue());
        filter.setDateFrom(filterDateFrom.getValue());
        filter.setDateTo(filterDateTo.getValue());
        filter.setZone(filterZone.getValue());
        filter.setOnlyDelayed(chkDelayed.isSelected());
        filter.setOnlyWithIncidents(chkIncidents.isSelected());

        String searchText = searchField.getText();
        if (searchText != null && !searchText.trim().isEmpty()) {
            filter.setSearchText(searchText.trim());
        }

        // Extract user ID from selection (now it's just the email)
        if (filterUser.getValue() != null) {
            String email = filterUser.getValue();
            Optional<User> userOpt = userRepository.findByEmail(email);
            userOpt.ifPresent(user -> filter.setUserId(user.getId()));
        }

        // Extract delivery person ID from selection (now it's just the email)
        if (filterDeliveryPerson.getValue() != null) {
            String email = filterDeliveryPerson.getValue();
            Optional<DeliveryPerson> dpOpt = deliveryPersonRepository.findDeliveryPersonByEmail(email);
            dpOpt.ifPresent(dp -> filter.setDeliveryPersonId(dp.getId()));
        }

        List<ShipmentDTO> filtered = shipmentService.filterShipments(filter);
        shipmentsData = FXCollections.observableArrayList(filtered);
        shipmentsTable.setItems(shipmentsData);
        updateCounters();
    }

    @FXML
    private void handleClearFilter() {
        filterStatus.setValue(null);
        filterZone.setValue(null);
        filterUser.setValue(null);
        filterDeliveryPerson.setValue(null);
        filterDateFrom.setValue(null);
        filterDateTo.setValue(null);
        searchField.clear();
        chkDelayed.setSelected(false);
        chkIncidents.setSelected(false);
        loadAllShipments();
        updateCounters();
    }

    @FXML
    private void handleViewDetails() {
        ShipmentDTO selected = shipmentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtil.showWarning("No Selection", "Please select a shipment to view details");
            return;
        }

        openShipmentDetails(selected.getId());
    }

    @FXML
    private void handleAssign() {
        ShipmentDTO selected = shipmentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtil.showWarning("No Selection", "Please select a shipment to assign");
            return;
        }

        if (selected.getStatus() != ShipmentStatus.PENDING_ASSIGNMENT) {
            DialogUtil.showError("Invalid Status", "Can only assign shipments with PENDING_ASSIGNMENT status");
            return;
        }

        showAssignDialog(selected);
    }

    @FXML
    private void handleBulkAssign() {
        List<ShipmentDTO> selected = shipmentsTable.getSelectionModel().getSelectedItems();
        if (selected.isEmpty()) {
            DialogUtil.showWarning("No Selection", "Please select at least one shipment to assign");
            return;
        }

        List<ShipmentDTO> assignable = selected.stream()
            .filter(s -> s.getStatus() == ShipmentStatus.PENDING_ASSIGNMENT)
            .collect(Collectors.toList());

        if (assignable.isEmpty()) {
            DialogUtil.showError("Invalid Selection", "None of the selected shipments can be assigned");
            return;
        }

        showBulkAssignDialog(assignable);
    }

    @FXML
    private void handleAutoAssign() {
        CoverageArea zone = filterZone.getValue();
        String zoneStr = (zone != null) ? zone.name() : null;
        String zoneDisplayName = (zone != null) ? zone.getDisplayName() : "";

        boolean confirmed = DialogUtil.showConfirmation(
            "Asignación Automática de Envíos",
            "¿Asignar automáticamente envíos no asignados" + (zone != null ? " en la zona " + zoneDisplayName : "") + "?"
        );

        if (!confirmed) return;

        try {
            int assigned = shipmentService.autoAssignShipments(zoneStr);
            DialogUtil.showSuccess("Asignación Automática Completada", "Se asignaron exitosamente " + assigned + " envíos");
            handleRefresh();
        } catch (Exception e) {
            Logger.error("Auto-assignment failed: " + e.getMessage());
            DialogUtil.showError("Error", "La asignación automática falló: " + e.getMessage());
        }
    }

    @FXML
    private void handleChangeStatus() {
        ShipmentDTO selected = shipmentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtil.showWarning("No Selection", "Please select a shipment");
            return;
        }

        showChangeStatusDialog(selected);
    }

    @FXML
    private void handleBulkStatusUpdate() {
        List<ShipmentDTO> selected = shipmentsTable.getSelectionModel().getSelectedItems();
        if (selected.isEmpty()) {
            DialogUtil.showWarning("No Selection", "Please select at least one shipment");
            return;
        }

        showBulkStatusUpdateDialog(selected);
    }

    @FXML
    private void handleRegisterIncident() {
        ShipmentDTO selected = shipmentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtil.showWarning("No Selection", "Please select a shipment");
            return;
        }

        showRegisterIncidentDialog(selected);
    }

    @FXML
    private void handleDelete() {
        ShipmentDTO selected = shipmentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtil.showWarning("No Selection", "Please select a shipment to delete");
            return;
        }

        boolean confirmed = DialogUtil.showConfirmation(
            "Delete Shipment",
            "Are you sure you want to delete shipment " + selected.getId() + "?\nThis action cannot be undone."
        );

        if (!confirmed) return;

        try {
            boolean success = shipmentService.cancelShipment(selected.getId());
            if (success) {
                DialogUtil.showSuccess("Deleted", "Shipment has been deleted successfully");
                handleRefresh();
            }
        } catch (Exception e) {
            Logger.error("Failed to delete shipment: " + e.getMessage());
            DialogUtil.showError("Error", "Could not delete shipment: " + e.getMessage());
        }
    }

    @FXML
    private void handleExport() {
        DialogUtil.showInfo("Export", "Export functionality will be implemented soon");
        // TODO: Implement CSV/PDF export
    }

    // ===========================
    // Helper Methods
    // ===========================

    /**
     * Opens shipment details view.
     */
    private void openShipmentDetails(String shipmentId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/co/edu/uniquindio/poo/proyectofiinal2025_2/View/ShipmentDetail.fxml"));
            Parent root = loader.load();

            ShipmentDetailController controller = loader.getController();
            controller.loadShipmentDetails(shipmentId);

            Stage stage = new Stage();
            stage.setTitle("Shipment Details - " + shipmentId);
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            Logger.error("Failed to load ShipmentDetail view: " + e.getMessage());
            DialogUtil.showError("Error", "Could not open shipment details");
        }
    }

    /**
     * Shows dialog to assign a delivery person to a shipment.
     */
    private void showAssignDialog(ShipmentDTO shipment) {
        List<DeliveryPerson> available = deliveryPersonRepository.getAllDeliveryPersons().stream()
            .filter(dp -> dp.getAvailability() == co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Enums.AvailabilityStatus.AVAILABLE)
            .collect(Collectors.toList());

        if (available.isEmpty()) {
            DialogUtil.showError("No Delivery Persons", "No delivery persons are currently available");
            return;
        }

        ChoiceDialog<DeliveryPerson> dialog = new ChoiceDialog<>(available.get(0), available);
        dialog.setTitle("Assign Delivery Person");
        dialog.setHeaderText("Assign delivery person to shipment " + shipment.getId());
        dialog.setContentText("Select delivery person:");

        dialog.getItems().forEach(dp -> {
            // Custom display for delivery persons
        });

        Optional<DeliveryPerson> result = dialog.showAndWait();
        result.ifPresent(dp -> {
            try {
                boolean success = shipmentService.assignDeliveryPerson(shipment.getId(), dp.getId());
                if (success) {
                    DialogUtil.showSuccess("Assigned", "Delivery person assigned successfully");
                    handleRefresh();
                }
            } catch (Exception e) {
                Logger.error("Assignment failed: " + e.getMessage());
                DialogUtil.showError("Error", "Assignment failed: " + e.getMessage());
            }
        });
    }

    /**
     * Shows dialog for bulk assignment.
     */
    private void showBulkAssignDialog(List<ShipmentDTO> shipments) {
        List<DeliveryPerson> available = deliveryPersonRepository.getAllDeliveryPersons().stream()
            .filter(dp -> dp.getAvailability() == co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Enums.AvailabilityStatus.AVAILABLE)
            .collect(Collectors.toList());

        if (available.isEmpty()) {
            DialogUtil.showError("No Delivery Persons", "No delivery persons are currently available");
            return;
        }

        ChoiceDialog<DeliveryPerson> dialog = new ChoiceDialog<>(available.get(0), available);
        dialog.setTitle("Bulk Assign Delivery Person");
        dialog.setHeaderText("Assign delivery person to " + shipments.size() + " shipments");
        dialog.setContentText("Select delivery person:");

        Optional<DeliveryPerson> result = dialog.showAndWait();
        result.ifPresent(dp -> {
            int successCount = 0;
            for (ShipmentDTO shipment : shipments) {
                try {
                    boolean success = shipmentService.assignDeliveryPerson(shipment.getId(), dp.getId());
                    if (success) successCount++;
                } catch (Exception e) {
                    Logger.error("Failed to assign shipment " + shipment.getId() + ": " + e.getMessage());
                }
            }

            DialogUtil.showSuccess("Bulk Assignment Complete",
                "Successfully assigned " + successCount + " out of " + shipments.size() + " shipments");
            handleRefresh();
        });
    }

    /**
     * Shows dialog to change shipment status.
     */
    private void showChangeStatusDialog(ShipmentDTO shipment) {
        ChoiceDialog<ShipmentStatus> dialog = new ChoiceDialog<>(shipment.getStatus(), ShipmentStatus.values());
        dialog.setTitle("Change Status");
        dialog.setHeaderText("Change status for shipment " + shipment.getId());
        dialog.setContentText("Select new status:");

        Optional<ShipmentStatus> result = dialog.showAndWait();
        result.ifPresent(newStatus -> {
            if (newStatus == shipment.getStatus()) {
                DialogUtil.showInfo("No Change", "Status is already " + newStatus.getDisplayName());
                return;
            }

            TextInputDialog reasonDialog = new TextInputDialog();
            reasonDialog.setTitle("Status Change Reason");
            reasonDialog.setHeaderText("Provide reason for status change");
            reasonDialog.setContentText("Reason:");

            Optional<String> reason = reasonDialog.showAndWait();
            reason.ifPresent(r -> {
                try {
                    String adminId = authService.getCurrentPerson().getId();
                    boolean success = shipmentService.changeStatus(shipment.getId(), newStatus, r, adminId);
                    if (success) {
                        DialogUtil.showSuccess("Status Changed", "Status updated successfully");
                        handleRefresh();
                    }
                } catch (Exception e) {
                    Logger.error("Status change failed: " + e.getMessage());
                    DialogUtil.showError("Error", "Status change failed: " + e.getMessage());
                }
            });
        });
    }

    /**
     * Shows dialog for bulk status update.
     */
    private void showBulkStatusUpdateDialog(List<ShipmentDTO> shipments) {
        ChoiceDialog<ShipmentStatus> dialog = new ChoiceDialog<>(ShipmentStatus.IN_TRANSIT, ShipmentStatus.values());
        dialog.setTitle("Bulk Status Update");
        dialog.setHeaderText("Update status for " + shipments.size() + " shipments");
        dialog.setContentText("Select new status:");

        Optional<ShipmentStatus> result = dialog.showAndWait();
        result.ifPresent(newStatus -> {
            TextInputDialog reasonDialog = new TextInputDialog();
            reasonDialog.setTitle("Status Change Reason");
            reasonDialog.setHeaderText("Provide reason for bulk status change");
            reasonDialog.setContentText("Reason:");

            Optional<String> reason = reasonDialog.showAndWait();
            reason.ifPresent(r -> {
                int successCount = 0;
                String adminId = authService.getCurrentPerson().getId();

                for (ShipmentDTO shipment : shipments) {
                    try {
                        boolean success = shipmentService.changeStatus(shipment.getId(), newStatus, r, adminId);
                        if (success) successCount++;
                    } catch (Exception e) {
                        Logger.error("Failed to update shipment " + shipment.getId() + ": " + e.getMessage());
                    }
                }

                DialogUtil.showSuccess("Bulk Update Complete",
                    "Successfully updated " + successCount + " out of " + shipments.size() + " shipments");
                handleRefresh();
            });
        });
    }

    /**
     * Shows dialog to register an incident.
     */
    private void showRegisterIncidentDialog(ShipmentDTO shipment) {
        ChoiceDialog<IncidentType> typeDialog = new ChoiceDialog<>(IncidentType.DELAY, IncidentType.values());
        typeDialog.setTitle("Register Incident");
        typeDialog.setHeaderText("Register incident for shipment " + shipment.getId());
        typeDialog.setContentText("Select incident type:");

        Optional<IncidentType> typeResult = typeDialog.showAndWait();
        typeResult.ifPresent(type -> {
            TextInputDialog descDialog = new TextInputDialog();
            descDialog.setTitle("Incident Description");
            descDialog.setHeaderText("Describe the incident");
            descDialog.setContentText("Description:");

            Optional<String> descResult = descDialog.showAndWait();
            descResult.ifPresent(description -> {
                try {
                    String adminId = authService.getCurrentPerson().getId();
                    boolean success = shipmentService.registerIncident(shipment.getId(), type, description, adminId);
                    if (success) {
                        DialogUtil.showSuccess("Incident Registered", "Incident has been registered successfully");
                        handleRefresh();
                    }
                } catch (Exception e) {
                    Logger.error("Failed to register incident: " + e.getMessage());
                    DialogUtil.showError("Error", "Failed to register incident: " + e.getMessage());
                }
            });
        });
    }

    /**
     * Extracts email from combo box selection text.
     */
    private String extractEmail(String selection) {
        if (selection == null) return null;
        int start = selection.indexOf('(');
        int end = selection.indexOf(')');
        if (start == -1 || end == -1) return null;
        return selection.substring(start + 1, end);
    }

    /**
     * Applies a user filter to show only shipments for a specific user.
     * Called when navigating from ManageUsers.
     *
     * @param userEmail The email of the user to filter by
     */
    public void applyUserFilter(String userEmail) {
        if (userEmail == null || userEmail.isEmpty()) {
            Logger.warning("applyUserFilter called with null or empty email");
            return;
        }

        Logger.info("Applying user filter for email: " + userEmail);

        // Verify the user exists
        Optional<User> userOpt = userRepository.findByEmail(userEmail);
        if (userOpt.isEmpty()) {
            Logger.error("User not found for email: " + userEmail);
            DialogUtil.showError("Error", "No se encontró el usuario con email: " + userEmail);
            return;
        }

        // Set the email directly in the combo box (now we show only emails)
        filterUser.setValue(userEmail);

        // Apply the filter
        handleFilter();

        Logger.info("User filter applied successfully for: " + userEmail);
    }
}
