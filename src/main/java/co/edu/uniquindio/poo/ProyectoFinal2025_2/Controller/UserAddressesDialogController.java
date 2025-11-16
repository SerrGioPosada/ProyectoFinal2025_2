package co.edu.uniquindio.poo.ProyectoFinal2025_2.Controller;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Address;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.User;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Repositories.UserRepository;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.UserService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.DialogUtil;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

/**
 * Controller for the User Addresses Dialog (UserAddressesDialog.fxml).
 * Displays all addresses for a specific user with options to delete addresses.
 */
public class UserAddressesDialogController {

    // =================================================================================================================
    // FXML Fields
    // =================================================================================================================

    @FXML
    private Label lblUserName;

    @FXML
    private Label lblFullName;

    @FXML
    private Label lblEmail;

    @FXML
    private Label lblPhone;

    @FXML
    private Label lblAddressCount;

    @FXML
    private VBox vboxAddresses;

    // =================================================================================================================
    // Dependencies and State
    // =================================================================================================================

    private final UserService userService = UserService.getInstance();
    private final UserRepository userRepository = UserRepository.getInstance();
    private User currentUser;
    private Runnable onAddressDeleted;

    // =================================================================================================================
    // Public API Methods
    // =================================================================================================================

    /**
     * Initializes the dialog with user data.
     *
     * @param user The user whose addresses to display
     */
    public void setUser(User user) {
        this.currentUser = user;
        loadUserInfo();
        loadAddresses();
    }

    /**
     * Sets a callback to be executed when an address is deleted.
     *
     * @param callback The callback runnable
     */
    public void setOnAddressDeleted(Runnable callback) {
        this.onAddressDeleted = callback;
    }

    // =================================================================================================================
    // Event Handlers
    // =================================================================================================================

    /**
     * Handles the close button action.
     */
    @FXML
    private void handleClose() {
        Stage stage = (Stage) lblUserName.getScene().getWindow();
        stage.close();
    }

    /**
     * Handles mouse enter event on close button.
     */
    @FXML
    private void onCloseButtonHover(javafx.scene.input.MouseEvent event) {
        if (event.getSource() instanceof Button btn) {
            btn.getStyleClass().remove("addresses-close-btn");
            btn.getStyleClass().add("addresses-close-btn-hover");
        }
    }

    /**
     * Handles mouse exit event on close button.
     */
    @FXML
    private void onCloseButtonExit(javafx.scene.input.MouseEvent event) {
        if (event.getSource() instanceof Button btn) {
            btn.getStyleClass().remove("addresses-close-btn-hover");
            btn.getStyleClass().add("addresses-close-btn");
        }
    }

    /**
     * Handles deletion of a specific address.
     */
    private void handleDeleteAddress(Address address) {
        boolean confirmed = DialogUtil.showWarningConfirmation(
                "Confirmar Eliminación",
                "¿Estás seguro de que quieres eliminar esta dirección?",
                "Alias: " + (address.getAlias() != null ? address.getAlias() : "Sin alias") + "\n" +
                        address.getStreet() + ", " + address.getCity()
        );

        if (!confirmed) return;

        try {
            // Remove address from user
            currentUser.getFrequentAddresses().remove(address);

            // Save updated user using repository
            userRepository.updateUser(currentUser);

            // Refresh the addresses list
            loadAddresses();

            // Notify parent if callback is set
            if (onAddressDeleted != null) {
                onAddressDeleted.run();
            }

            DialogUtil.showSuccess("Dirección eliminada exitosamente.");
            Logger.info("Address deleted for user: " + currentUser.getEmail());

        } catch (Exception e) {
            Logger.error("Error deleting address", e);
            DialogUtil.showError("Error al eliminar la dirección: " + e.getMessage());
        }
    }

    // =================================================================================================================
    // Private Methods
    // =================================================================================================================

    /**
     * Loads the user information into the header labels.
     */
    private void loadUserInfo() {
        if (currentUser == null) return;

        lblUserName.setText("Direcciones de " + currentUser.getName() + " " + currentUser.getLastName());
        lblFullName.setText(currentUser.getName() + " " + currentUser.getLastName());
        lblEmail.setText(currentUser.getEmail());
        lblPhone.setText(currentUser.getPhone() != null ? currentUser.getPhone() : "No especificado");
    }

    /**
     * Loads and displays all addresses for the current user.
     */
    private void loadAddresses() {
        vboxAddresses.getChildren().clear();

        if (currentUser == null || currentUser.getFrequentAddresses() == null) {
            lblAddressCount.setText("0 direcciones");
            showEmptyState();
            return;
        }

        List<Address> addresses = currentUser.getFrequentAddresses();
        lblAddressCount.setText(addresses.size() + " dirección" + (addresses.size() != 1 ? "es" : ""));

        if (addresses.isEmpty()) {
            showEmptyState();
            return;
        }

        for (Address address : addresses) {
            vboxAddresses.getChildren().add(createAddressCard(address));
        }

        Logger.info("Loaded " + addresses.size() + " addresses for user: " + currentUser.getEmail());
    }

    /**
     * Creates a styled card for displaying an address.
     */
    private VBox createAddressCard(Address address) {
        VBox card = new VBox(10);
        card.getStyleClass().add("address-card");
        card.setPadding(new Insets(15));

        // Header with alias and delete button
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        String aliasText = address.getAlias() != null ? address.getAlias() : "Sin alias";
        Label lblAlias = new Label("📍 " + aliasText);
        lblAlias.getStyleClass().add("address-alias");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnDelete = new Button("Eliminar");
        btnDelete.getStyleClass().add("address-delete-btn");
        btnDelete.setOnMouseEntered(e -> {
            btnDelete.getStyleClass().remove("address-delete-btn");
            btnDelete.getStyleClass().add("address-delete-btn-hover");
        });
        btnDelete.setOnMouseExited(e -> {
            btnDelete.getStyleClass().remove("address-delete-btn-hover");
            btnDelete.getStyleClass().add("address-delete-btn");
        });
        btnDelete.setOnAction(e -> handleDeleteAddress(address));

        header.getChildren().addAll(lblAlias, spacer, btnDelete);

        // Address details
        VBox details = new VBox(8);
        details.setPadding(new Insets(10, 0, 0, 0));

        Label lblStreet = new Label("• " + address.getStreet());
        lblStreet.getStyleClass().add("address-street");

        String cityStateZip = address.getCity() +
                (address.getState() != null ? ", " + address.getState() : "") +
                (address.getZipCode() != null ? " " + address.getZipCode() : "");
        Label lblCityState = new Label("  " + cityStateZip);
        lblCityState.getStyleClass().add("address-city");

        if (address.getCountry() != null) {
            Label lblCountry = new Label("  " + address.getCountry());
            lblCountry.getStyleClass().add("address-city");
            details.getChildren().addAll(lblStreet, lblCityState, lblCountry);
        } else {
            details.getChildren().addAll(lblStreet, lblCityState);
        }

        // Coordinates (if available)
        if (address.getLatitude() != null && address.getLongitude() != null) {
            Label lblCoordinates = new Label(String.format("Coordenadas: %.6f, %.6f",
                    address.getLatitude(), address.getLongitude()));
            lblCoordinates.getStyleClass().add("address-coordinates");
            details.getChildren().add(lblCoordinates);
        }

        card.getChildren().addAll(header, new Separator(), details);

        return card;
    }

    /**
     * Shows an empty state message when no addresses are found.
     */
    private void showEmptyState() {
        VBox emptyState = new VBox(15);
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setPadding(new Insets(60));
        emptyState.getStyleClass().add("addresses-empty-state");

        Label lblMessage = new Label("No hay direcciones registradas");
        lblMessage.getStyleClass().add("addresses-empty-message");

        Label lblSubMessage = new Label("Este usuario no tiene direcciones guardadas");
        lblSubMessage.getStyleClass().add("addresses-empty-submessage");

        emptyState.getChildren().addAll(lblMessage, lblSubMessage);
        vboxAddresses.getChildren().add(emptyState);
    }
}
