package co.edu.uniquindio.poo.proyectofinal2025_2.Controller;

import co.edu.uniquindio.poo.proyectofinal2025_2.Model.dto.UserSummaryDTO;
import co.edu.uniquindio.poo.proyectofinal2025_2.Services.UserService;
import co.edu.uniquindio.poo.proyectofinal2025_2.Util.UtilController.DialogUtil;
import co.edu.uniquindio.poo.proyectofinal2025_2.Util.UtilModel.Logger;
import co.edu.uniquindio.poo.proyectofinal2025_2.Util.UtilModel.StringUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Circle;

/**
 * Controller for the user management view (ManageUsers.fxml), accessible by administrators.
 * <p>
 * Responsibilities include:
 * <ul>
 *     <li>Displaying a list of all registered users in a table.</li>
 *     <li>Providing functionality to search and filter the user list.</li>
 *     <li>Enabling actions such as adding, deleting, and enabling/disabling users.</li>
 *     <li>Displaying summary statistics about the user base.</li>
 *     <li>Navigating to other views (e.g., add user, view orders) via the {@link IndexController}.</li>
 * </ul>
 * </p>
 */
public class ManageUsersController {

    // =================================================================================================================
    // FXML Fields
    // =================================================================================================================

    private final UserService userService = UserService.getInstance();
    @FXML
    private AnchorPane rootPane;
    @FXML
    private TableView<UserSummaryDTO> tableUsers;
    @FXML
    private TableColumn<UserSummaryDTO, String> colImage;
    @FXML
    private TableColumn<UserSummaryDTO, String> colName;
    @FXML
    private TableColumn<UserSummaryDTO, String> colLastName;
    @FXML
    private TableColumn<UserSummaryDTO, String> colEmail;
    @FXML
    private TableColumn<UserSummaryDTO, String> colPhone;
    @FXML
    private TableColumn<UserSummaryDTO, Integer> colOrders;
    @FXML
    private TableColumn<UserSummaryDTO, Integer> colAddresses;
    @FXML
    private TableColumn<UserSummaryDTO, Boolean> colStatus;
    @FXML
    private TextField txtSearch;
    @FXML
    private Label lblTotalUsers;
    @FXML
    private Label lblActiveUsers;
    @FXML
    private Label lblInactiveUsers;
    @FXML
    private Button btnAddUser;
    @FXML
    private Button btnViewOrders;
    @FXML
    private Button btnViewAddresses;
    @FXML
    private Button btnToggleStatus;

    // =================================================================================================================
    // Dependencies and State
    // =================================================================================================================

    @FXML
    private Button btnDeleteUser;
    private ObservableList<UserSummaryDTO> usersList;
    private FilteredList<UserSummaryDTO> filteredUsers;
    private IndexController indexController;

    // =================================================================================================================
    // Initialization
    // =================================================================================================================

    /**
     * Initializes the controller after its root element has been completely processed.
     * <p>
     * This method sets up the table columns, loads users, configures the search filter,
     * sets up selection listeners, and updates the user statistics labels.
     * </p>
     */
    @FXML
    public void initialize() {
        setupTableColumns();
        loadUsers();
        setupSearchFilter();
        setupSelectionListener();
        setupClickOutsideListener();
        updateStatistics();
    }

    /**
     * Injects the main controller reference to enable navigation to other views.
     *
     * @param indexController The main {@link IndexController} instance.
     */
    public void setIndexController(IndexController indexController) {
        Logger.info("IndexController has been set in ManageUsersController.");
        this.indexController = indexController;
    }

    // =================================================================================================================
    // Public API Methods
    // =================================================================================================================

    /**
     * Refreshes the user data from the service and updates the table and statistics.
     * <p>
     * This method can be called externally whenever user data has been modified
     * to ensure the table and statistics remain up-to-date.
     * </p>
     */
    public void refreshUsers() {
        Logger.info("Refreshing user data...");
        loadUsers();
        updateStatistics();
    }

    // =================================================================================================================
    // Event Handlers
    // =================================================================================================================

    /**
     * Handles the "Add User" button click, opening the signup view.
     */
    @FXML
    private void handleAddUser() {
        if (indexController == null) {
            Logger.error("Cannot load signup view: IndexController is null.");
            return;
        }
        indexController.loadView("Signup.fxml");
    }

    /**
     * Handles the "View Orders" button click.
     * <p>
     * Navigates to ShipmentManagement view with a filter applied for the selected user's shipments.
     * </p>
     */
    @FXML
    private void handleViewOrders() {
        UserSummaryDTO selected = tableUsers.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Logger.info("View Orders button clicked for user: " + selected.getFullName());

        // Navigate to ShipmentManagement and pass the user email for filtering
        if (indexController != null) {
            indexController.loadViewWithUserFilter("ShipmentManagement.fxml", selected.getEmail());
        }
    }

    /**
     * Handles the "View Addresses" button click.
     * <p>
     * Currently shows a placeholder dialog indicating the feature is in development.
     * </p>
     */
    @FXML
    private void handleViewAddresses() {
        UserSummaryDTO selected = tableUsers.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Logger.info("View Addresses button clicked for user: " + selected.getFullName());
        DialogUtil.showInfo("Función en Desarrollo", "La libreta de direcciones para " + selected.getFullName() + " estará disponible pronto.");
    }

    /**
     * Toggles the active/inactive status of the selected user.
     * <p>
     * Prompts for confirmation, updates the service, refreshes the table,
     * and updates the statistics and status button text.
     * </p>
     */
    @FXML
    private void handleToggleStatus() {
        UserSummaryDTO selected = tableUsers.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        boolean isCurrentlyActive = selected.isActive();
        String action = isCurrentlyActive ? "inhabilitar" : "habilitar";

        boolean confirmed = DialogUtil.showConfirmation(
                "Confirmar Acción",
                "¿Estás seguro de que quieres " + action + " este usuario?",
                "Usuario: " + selected.getFullName()
        );

        if (!confirmed) return;

        boolean success = isCurrentlyActive
                ? userService.disableUser(selected.getId())
                : userService.enableUser(selected.getId());

        if (!success) {
            DialogUtil.showError("Error al " + action + " el usuario.");
            return;
        }

        selected.setActive(!isCurrentlyActive);
        tableUsers.refresh();
        updateStatistics();

        // Update button text to reflect new state
        btnToggleStatus.setText(selected.isActive() ? "Inhabilitar" : "Habilitar");

        DialogUtil.showSuccess("Usuario " + (isCurrentlyActive ? "inhabilitado" : "habilitado") + " exitosamente.");
    }

    /**
     * Deletes the selected user after confirming the action.
     * <p>
     * Updates the table, statistics, and displays success or error messages.
     * </p>
     */
    @FXML
    private void handleDeleteUser() {
        UserSummaryDTO selected = tableUsers.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        boolean confirmed = DialogUtil.showWarningConfirmation(
                "Confirmar Eliminación",
                "¿Estás seguro de que quieres eliminar este usuario?",
                "Esta acción NO se puede deshacer.\nUsuario: " + selected.getFullName()
        );

        if (!confirmed) return;

        boolean success = userService.deleteUser(selected.getId());

        if (!success) {
            DialogUtil.showError("Error al eliminar el usuario.");
            return;
        }

        usersList.remove(selected);
        updateStatistics();
        DialogUtil.showSuccess("Usuario eliminado exitosamente.");
    }

    // =================================================================================================================
    // Private Setup & Logic Methods
    // =================================================================================================================

    /**
     * Configures the table columns, including custom cell factories for images and status badges.
     */
    private void setupTableColumns() {
        colImage.setCellValueFactory(cellData -> cellData.getValue().profileImagePathProperty());
        colImage.setCellFactory(col -> new TableCell<>() {
            private final ImageView imageView = new ImageView();
            private final Circle clip = new Circle(20, 20, 20);

            @Override
            protected void updateItem(String imagePath, boolean empty) {
                super.updateItem(imagePath, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }

                Image image = DialogUtil.loadUserImage(imagePath, 40, 40);
                if (image != null) {
                    imageView.setImage(image);
                    imageView.setClip(clip);
                    setGraphic(imageView);
                } else {
                    setGraphic(null);
                }
                setAlignment(Pos.CENTER);
            }
        });

        colName.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        colLastName.setCellValueFactory(cellData -> cellData.getValue().lastNameProperty());
        colEmail.setCellValueFactory(cellData -> cellData.getValue().emailProperty());
        colPhone.setCellValueFactory(cellData -> cellData.getValue().phoneProperty());
        colOrders.setCellValueFactory(cellData -> cellData.getValue().orderCountProperty().asObject());
        colAddresses.setCellValueFactory(cellData -> cellData.getValue().addressCountProperty().asObject());
        colOrders.setStyle("-fx-alignment: CENTER;");
        colAddresses.setStyle("-fx-alignment: CENTER;");

        colStatus.setCellValueFactory(cellData -> cellData.getValue().isActiveProperty());
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean active, boolean empty) {
                super.updateItem(active, empty);
                if (empty || active == null) {
                    setGraphic(null);
                    return;
                }
                setGraphic(DialogUtil.createStatusBadge(active));
                setAlignment(Pos.CENTER);
            }
        });
    }

    /**
     * Loads the users from the service and populates the observable and filtered lists.
     * <p>
     * If this is the first load, creates the lists; otherwise updates the existing list.
     * </p>
     */
    private void loadUsers() {
        Logger.info("Loading users from UserService...");
        java.util.List<UserSummaryDTO> summaryList = userService.getAllUsersSummary();
        Logger.info("Loaded " + summaryList.size() + " users.");

        if (usersList == null) {
            usersList = FXCollections.observableArrayList(summaryList);
            filteredUsers = new FilteredList<>(usersList, p -> true);
            tableUsers.setItems(filteredUsers);
        } else {
            usersList.setAll(summaryList);
        }
    }

    /**
     * Configures the search filter to update the filtered list and statistics dynamically.
     */
    private void setupSearchFilter() {
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredUsers.setPredicate(user -> {
                if (StringUtil.isNullOrEmpty(newValue)) return true;
                String filter = newValue.toLowerCase();
                return user.getName().toLowerCase().contains(filter) ||
                        user.getLastName().toLowerCase().contains(filter) ||
                        user.getEmail().toLowerCase().contains(filter);
            });
            updateStatistics();
        });
    }

    /**
     * Sets up the selection listener for the table.
     * <p>
     * Updates button states and toggle button text depending on whether a user is selected.
     * </p>
     */
    private void setupSelectionListener() {
        tableUsers.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;

            btnViewOrders.setDisable(!hasSelection);
            btnViewAddresses.setDisable(!hasSelection);
            btnToggleStatus.setDisable(!hasSelection);
            btnDeleteUser.setDisable(!hasSelection);

            if (hasSelection) {
                btnToggleStatus.setText(newSelection.isActive() ? "Inhabilitar" : "Habilitar");
            }
        });
    }

    /**
     * Sets up listener to deselect table rows when clicking outside the table.
     */
    private void setupClickOutsideListener() {
        if (rootPane != null) {
            rootPane.setOnMouseClicked(event -> {
                // Check if the click target is not the table or any of its children
                if (!isNodeOrChildOf(event.getTarget(), tableUsers)) {
                    tableUsers.getSelectionModel().clearSelection();
                }
            });
        }
    }

    /**
     * Checks if a node is the target or a child of the target.
     */
    private boolean isNodeOrChildOf(Object target, javafx.scene.Node node) {
        if (!(target instanceof javafx.scene.Node)) return false;
        javafx.scene.Node current = (javafx.scene.Node) target;
        while (current != null) {
            if (current == node) return true;
            current = current.getParent();
        }
        return false;
    }

    /**
     * Updates the statistics labels for total, active, and inactive users.
     */
    private void updateStatistics() {
        long total = filteredUsers.size();
        long active = filteredUsers.stream().filter(UserSummaryDTO::isActive).count();
        long inactive = total - active;

        lblTotalUsers.setText(String.valueOf(total));
        lblActiveUsers.setText(String.valueOf(active));
        lblInactiveUsers.setText(String.valueOf(inactive));
    }
}
