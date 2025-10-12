package co.edu.uniquindio.poo.proyectofiinal2025_2.Controller;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.dto.UserSummaryDTO;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Services.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;

import java.io.InputStream;
import java.util.Optional;

/**
 * Controller for the user management view (ManageUsers.fxml), accessible by administrators.
 * <p>
 * This class is responsible for:
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

    @FXML private TableView<UserSummaryDTO> tableUsers;
    @FXML private TableColumn<UserSummaryDTO, String> colImage;
    @FXML private TableColumn<UserSummaryDTO, String> colName;
    @FXML private TableColumn<UserSummaryDTO, String> colLastName;
    @FXML private TableColumn<UserSummaryDTO, String> colEmail;
    @FXML private TableColumn<UserSummaryDTO, String> colPhone;
    @FXML private TableColumn<UserSummaryDTO, Integer> colOrders;
    @FXML private TableColumn<UserSummaryDTO, Integer> colAddresses;
    @FXML private TableColumn<UserSummaryDTO, Boolean> colStatus;

    @FXML private TextField txtSearch;
    @FXML private Label lblTotalUsers;
    @FXML private Label lblActiveUsers;
    @FXML private Label lblInactiveUsers;

    @FXML private Button btnAddUser;
    @FXML private Button btnViewOrders;
    @FXML private Button btnViewAddresses;
    @FXML private Button btnToggleStatus;
    @FXML private Button btnDeleteUser;

    // =================================================================================================================
    // Dependencies and State
    // =================================================================================================================

    private final UserService userService = UserService.getInstance();
    private ObservableList<UserSummaryDTO> usersList;
    private FilteredList<UserSummaryDTO> filteredUsers;
    private IndexController indexController;

    // =================================================================================================================
    // Initialization
    // =================================================================================================================

    /**
     * Initializes the controller after its root element has been completely processed.
     */
    @FXML
    public void initialize() {
        setupTableColumns();
        loadUsers();
        setupSearchFilter();
        setupSelectionListener();
        updateStatistics();
    }

    /**
     * Injected by the parent controller to establish communication for navigation.
     * @param indexController The main application controller.
     */
    public void setIndexController(IndexController indexController) {
        System.out.println("IndexController has been set in ManageUsersController.");
        this.indexController = indexController;
    }

    // =================================================================================================================
    // Public API Methods
    // =================================================================================================================

    /**
     * Reloads the user data from the service and refreshes the table and statistics.
     * This method can be called from other controllers after a change has been made.
     */
    public void refreshUsers() {
        System.out.println("Refreshing user data...");
        loadUsers();
        updateStatistics();
    }

    // =================================================================================================================
    // Event Handlers
    // =================================================================================================================

    @FXML
    private void handleAddUser() {
        if (indexController == null) {
            System.err.println("Cannot load signup view: IndexController is null.");
            return;
        }
        indexController.loadView("Signup.fxml");
    }

    @FXML
    private void handleViewOrders() {
        UserSummaryDTO selected = tableUsers.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        System.out.println("View Orders button clicked for user: " + selected.getFullName());
        showInfo("Función en Desarrollo", "El historial de pedidos para " + selected.getFullName() + " estará disponible pronto.");
        // TODO: Implement navigation to user orders view: indexController.loadUserOrdersView(selected.getId());
    }

    @FXML
    private void handleViewAddresses() {
        UserSummaryDTO selected = tableUsers.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        System.out.println("View Addresses button clicked for user: " + selected.getFullName());
        showInfo("Función en Desarrollo", "La libreta de direcciones para " + selected.getFullName() + " estará disponible pronto.");
        // TODO: Implement navigation to user addresses view: indexController.loadUserAddressesView(selected.getId());
    }

    @FXML
    private void handleToggleStatus() {
        UserSummaryDTO selected = tableUsers.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        boolean isCurrentlyActive = selected.isActive();
        String action = isCurrentlyActive ? "inhabilitar" : "habilitar";

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmar Acción");
        confirmation.setHeaderText("¿Estás seguro de que quieres " + action + " este usuario?");
        confirmation.setContentText("Usuario: " + selected.getFullName());

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        System.out.println("User confirmed action. Proceeding to " + action + " user.");
        boolean success = isCurrentlyActive
                ? userService.disableUser(selected.getId())
                : userService.enableUser(selected.getId());

        if (!success) {
            showError("Error al " + action + " el usuario.");
            return;
        }

        selected.setActive(!isCurrentlyActive);
        tableUsers.refresh();
        updateStatistics();
        showSuccess("Usuario " + (isCurrentlyActive ? "inhabilitado" : "habilitado") + " exitosamente.");
    }

    @FXML
    private void handleDeleteUser() {
        UserSummaryDTO selected = tableUsers.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        System.out.println("Delete User button clicked for user: " + selected.getFullName());

        Alert confirmation = new Alert(Alert.AlertType.WARNING);
        confirmation.setTitle("Confirmar Eliminación");
        confirmation.setHeaderText("¿Estás seguro de que quieres eliminar este usuario?");
        confirmation.setContentText("Esta acción NO se puede deshacer.\nUsuario: " + selected.getFullName());

        ButtonType btnConfirm = new ButtonType("Eliminar", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancel = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmation.getButtonTypes().setAll(btnConfirm, btnCancel);

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isEmpty() || result.get() != btnConfirm) {
            System.out.println("User cancelled deletion.");
            return;
        }

        System.out.println("User confirmed deletion. Proceeding to delete user.");
        boolean success = userService.deleteUser(selected.getId());

        if (!success) {
            showError("Error al eliminar el usuario.");
            return;
        }

        usersList.remove(selected);
        updateStatistics();
        showSuccess("Usuario eliminado exitosamente.");
    }

    // =================================================================================================================
    // Private Setup & Logic Methods
    // =================================================================================================================

    /**
     * Configures the cell value factories and cell factories for each table column.
     */
    private void setupTableColumns() {

        // Image column: Displays a circular avatar.
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

                Image image = null;
                try {
                    if (imagePath != null && !imagePath.isEmpty()) {
                        image = new Image("file:" + imagePath, 40, 40, true, true);
                    } else {
                        InputStream defaultImageStream = getClass().getResourceAsStream("/co/edu/uniquindio/poo/proyectofiinal2025_2/Images/default-userImage.png");
                        if (defaultImageStream != null) {
                            image = new Image(defaultImageStream, 40, 40, true, true);
                        }
                    }

                    if (image != null && !image.isError()) {
                        imageView.setImage(image);
                        imageView.setClip(clip);
                        setGraphic(imageView);
                    } else {
                        setGraphic(null);
                    }
                } catch (Exception e) {
                    System.err.println("Error loading image for table cell: " + e.getMessage());
                    setGraphic(null);
                }
                setAlignment(Pos.CENTER);
            }
        });

        // Standard text columns.
        colName.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        colLastName.setCellValueFactory(cellData -> cellData.getValue().lastNameProperty());
        colEmail.setCellValueFactory(cellData -> cellData.getValue().emailProperty());
        colPhone.setCellValueFactory(cellData -> cellData.getValue().phoneProperty());

        // Numeric columns, centered.
        colOrders.setCellValueFactory(cellData -> cellData.getValue().orderCountProperty().asObject());
        colAddresses.setCellValueFactory(cellData -> cellData.getValue().addressCountProperty().asObject());
        colOrders.setStyle("-fx-alignment: CENTER;");
        colAddresses.setStyle("-fx-alignment: CENTER;");

        // Status column: Displays a styled badge (Active/Inactive).
        colStatus.setCellValueFactory(cellData -> cellData.getValue().isActiveProperty());
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean active, boolean empty) {
                super.updateItem(active, empty);
                if (empty || active == null) {
                    setGraphic(null);
                    return;
                }
                Label badge = new Label(active ? "Activo" : "Inactivo");
                String style = active
                        ? "-fx-background-color: #d4edda; -fx-text-fill: #155724; -fx-padding: 5 10; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;"
                        : "-fx-background-color: #f8d7da; -fx-text-fill: #721c24; -fx-padding: 5 10; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;";
                badge.setStyle(style);
                setGraphic(badge);
                setAlignment(Pos.CENTER);
            }
        });
    }

    /**
     * Fetches user data from the UserService, populates the observable list,
     * and sets up the filtered list for the table.
     */
    private void loadUsers() {
        System.out.println("Loading users from UserService...");
        java.util.List<UserSummaryDTO> summaryList = userService.getAllUsersSummary();
        System.out.println("Loaded " + summaryList.size() + " users.");

        usersList = FXCollections.observableArrayList(summaryList);
        filteredUsers = new FilteredList<>(usersList, p -> true);
        tableUsers.setItems(filteredUsers);
    }

    /**
     * Sets up a listener on the search text field to filter the table data in real-time.
     */
    private void setupSearchFilter() {
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredUsers.setPredicate(user -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return user.getName().toLowerCase().contains(lowerCaseFilter) ||
                        user.getLastName().toLowerCase().contains(lowerCaseFilter) ||
                        user.getEmail().toLowerCase().contains(lowerCaseFilter);
            });
            updateStatistics();
        });
    }

    /**
     * Sets up a listener on the table's selection model to enable/disable action buttons.
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
     * Recalculates and updates the user statistics labels (Total, Active, Inactive).
     */
    private void updateStatistics() {
        long total = filteredUsers.size();
        long active = filteredUsers.stream().filter(UserSummaryDTO::isActive).count();
        long inactive = total - active;

        lblTotalUsers.setText(String.valueOf(total));
        lblActiveUsers.setText(String.valueOf(active));
        lblInactiveUsers.setText(String.valueOf(inactive));
    }

    // =================================================================================================================
    // Dialog and Alert Helpers
    // =================================================================================================================

    /**
     * Displays a success information dialog.
     * @param message The message to display.
     */
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Displays an error dialog.
     * @param message The message to display.
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Displays a general information dialog.
     * @param title The title of the dialog window.
     * @param message The message to display.
     */
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
