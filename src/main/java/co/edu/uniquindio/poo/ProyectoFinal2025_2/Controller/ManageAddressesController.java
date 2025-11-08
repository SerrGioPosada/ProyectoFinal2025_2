package co.edu.uniquindio.poo.ProyectoFinal2025_2.Controller;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Address;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.User;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.AddressService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.AuthenticationService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.DialogUtil;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for managing user's frequent addresses.
 */
public class ManageAddressesController implements Initializable {

    @FXML private AnchorPane rootPane;
    @FXML private TableView<Address> addressesTable;
    @FXML private TableColumn<Address, String> colLabel;
    @FXML private TableColumn<Address, String> colStreet;
    @FXML private TableColumn<Address, String> colCity;
    @FXML private TableColumn<Address, String> colState;
    @FXML private TableColumn<Address, String> colPostalCode;
    @FXML private TableColumn<Address, String> colDefault;

    @FXML private VBox emptyStateBox;
    @FXML private Label lblAddressCount;
    @FXML private TextField txtSearch;
    @FXML private Button btnEditAddress;
    @FXML private Button btnDeleteAddress;

    private final AuthenticationService authService = AuthenticationService.getInstance();
    private final AddressService addressService = AddressService.getInstance();
    private User currentUser;
    private ObservableList<Address> addressesData;
    private FilteredList<Address> filteredAddresses;
    private IndexController indexController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        currentUser = (User) authService.getCurrentPerson();

        if (currentUser == null) {
            Logger.error("No user logged in");
            DialogUtil.showError("Error", "No hay usuario autenticado");
            return;
        }

        setupTable();
        loadAddresses();
        setupSearchFilter();
        setupSelectionListener();
        setupClickOutsideListener();
        Logger.info("ManageAddressesController initialized");
    }

    private void setupTable() {
        colLabel.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAlias()));
        colStreet.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStreet()));
        colCity.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCity()));
        colState.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getState()));
        colPostalCode.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getZipCode()));
        colDefault.setCellValueFactory(data -> new SimpleStringProperty("--")); // TODO: implement default flag
    }

    private void loadAddresses() {
        if (currentUser.getFrequentAddresses() == null) {
            addressesData = FXCollections.observableArrayList();
        } else {
            addressesData = FXCollections.observableArrayList(currentUser.getFrequentAddresses());
        }

        if (filteredAddresses == null) {
            filteredAddresses = new FilteredList<>(addressesData, p -> true);
            addressesTable.setItems(filteredAddresses);
        } else {
            addressesData.setAll(currentUser.getFrequentAddresses() != null ? currentUser.getFrequentAddresses() : FXCollections.emptyObservableList());
        }

        updateUI();
    }

    /**
     * Configures the search filter to update the filtered list dynamically.
     */
    private void setupSearchFilter() {
        if (txtSearch != null) {
            txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
                filteredAddresses.setPredicate(address -> {
                    if (newValue == null || newValue.trim().isEmpty()) {
                        return true;
                    }
                    String filter = newValue.toLowerCase();
                    return address.getAlias().toLowerCase().contains(filter) ||
                           address.getStreet().toLowerCase().contains(filter) ||
                           address.getCity().toLowerCase().contains(filter);
                });
                updateUI();
            });
        }
    }

    /**
     * Sets up the selection listener for the table.
     */
    private void setupSelectionListener() {
        addressesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            btnEditAddress.setDisable(!hasSelection);
            btnDeleteAddress.setDisable(!hasSelection);
        });
    }

    /**
     * Sets up listener to deselect table rows when clicking outside the table.
     */
    private void setupClickOutsideListener() {
        if (rootPane != null) {
            rootPane.setOnMouseClicked(event -> {
                if (!isNodeOrChildOf(event.getTarget(), addressesTable)) {
                    addressesTable.getSelectionModel().clearSelection();
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

    private void updateUI() {
        boolean isEmpty = filteredAddresses.isEmpty();
        addressesTable.setVisible(!isEmpty);
        addressesTable.setManaged(!isEmpty);
        emptyStateBox.setVisible(isEmpty);
        emptyStateBox.setManaged(isEmpty);
        lblAddressCount.setText(String.valueOf(filteredAddresses.size()));
    }

    @FXML
    private void handleAddAddress() {
        Dialog<Address> dialog = createAddressDialog(null);
        Optional<Address> result = dialog.showAndWait();

        result.ifPresent(address -> {
            try {
                addressService.addAddress(currentUser, address);
                addressesData.add(address);
                updateUI();
                DialogUtil.showSuccess("Exito", "Direccion agregada correctamente");
            } catch (IllegalArgumentException e) {
                DialogUtil.showError("Error", e.getMessage());
            } catch (Exception e) {
                Logger.error("Error adding address: " + e.getMessage());
                DialogUtil.showError("Error", "No se pudo agregar la direccion: " + e.getMessage());
            }
        });
    }

    @FXML
    private void handleEditAddress() {
        Address selected = addressesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtil.showWarning("Advertencia", "Selecciona una direccion para editar");
            return;
        }

        Dialog<Address> dialog = createAddressDialog(selected);
        Optional<Address> result = dialog.showAndWait();

        result.ifPresent(updatedAddress -> {
            try {
                addressService.updateAddress(currentUser, selected, updatedAddress);
                int index = addressesData.indexOf(selected);
                addressesData.set(index, updatedAddress);
                DialogUtil.showSuccess("Exito", "Direccion actualizada correctamente");
            } catch (IllegalArgumentException e) {
                DialogUtil.showError("Error", e.getMessage());
            } catch (Exception e) {
                Logger.error("Error updating address: " + e.getMessage());
                DialogUtil.showError("Error", "No se pudo actualizar la direccion: " + e.getMessage());
            }
        });
    }

    @FXML
    private void handleDeleteAddress() {
        Address selected = addressesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtil.showWarning("Advertencia", "Selecciona una direccion para eliminar");
            return;
        }

        boolean confirmed = DialogUtil.showConfirmation(
            "Confirmar Eliminacion",
            "Deseas eliminar la direccion '" + selected.getAlias() + "'?"
        );

        if (confirmed) {
            try {
                addressService.deleteAddress(currentUser, selected);
                addressesData.remove(selected);
                updateUI();
                DialogUtil.showSuccess("Exito", "Direccion eliminada correctamente");
            } catch (Exception e) {
                Logger.error("Error deleting address: " + e.getMessage());
                DialogUtil.showError("Error", "No se pudo eliminar la direccion: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleBack() {
        if (indexController != null) {
            // Navigate back to profile
            if (authService.isCurrentPersonAdmin()) {
                indexController.loadView("AdminProfile.fxml");
            } else {
                indexController.loadView("UserProfile.fxml");
            }
        } else {
            // Fallback: close window if opened as modal
            Stage stage = (Stage) addressesTable.getScene().getWindow();
            stage.close();
        }
    }

    /**
     * Sets the IndexController reference for navigation.
     */
    public void setIndexController(IndexController indexController) {
        this.indexController = indexController;
    }

    private Dialog<Address> createAddressDialog(Address existingAddress) {
        Dialog<Address> dialog = new Dialog<>();
        dialog.setTitle(existingAddress == null ? "Nueva Direccion" : "Editar Direccion");
        dialog.setHeaderText(existingAddress == null ? "Agregar direccion frecuente" : "Modificar direccion");

        // Apply CSS styles
        try {
            dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/co/edu/uniquindio/poo/ProyectoFinal2025_2/Style.css").toExternalForm()
            );
        } catch (Exception e) {
            Logger.error("Failed to load stylesheet for dialog: " + e.getMessage());
        }

        ButtonType saveButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setStyle("-fx-padding: 20;");

        // Create labels with style class
        Label lblAlias = new Label("Etiqueta:");
        lblAlias.getStyleClass().add("form-label");
        Label lblStreet = new Label("Direccion:");
        lblStreet.getStyleClass().add("form-label");
        Label lblCity = new Label("Ciudad:");
        lblCity.getStyleClass().add("form-label");
        Label lblState = new Label("Departamento:");
        lblState.getStyleClass().add("form-label");
        Label lblZipCode = new Label("Codigo Postal:");
        lblZipCode.getStyleClass().add("form-label");
        Label lblCountry = new Label("Pais:");
        lblCountry.getStyleClass().add("form-label");
        Label lblLatitude = new Label("Latitud (Opcional):");
        lblLatitude.getStyleClass().add("form-label");
        Label lblLongitude = new Label("Longitud (Opcional):");
        lblLongitude.getStyleClass().add("form-label");

        // Create text fields with style class
        TextField txtAlias = new TextField();
        txtAlias.setPromptText("Ej: Casa, Trabajo, etc.");
        txtAlias.getStyleClass().add("form-input");
        txtAlias.setPrefWidth(300);

        TextField txtStreet = new TextField();
        txtStreet.setPromptText("Calle y numero");
        txtStreet.getStyleClass().add("form-input");
        txtStreet.setPrefWidth(300);

        TextField txtCity = new TextField();
        txtCity.setPromptText("Ciudad");
        txtCity.getStyleClass().add("form-input");
        txtCity.setPrefWidth(300);

        TextField txtState = new TextField();
        txtState.setPromptText("Departamento");
        txtState.getStyleClass().add("form-input");
        txtState.setPrefWidth(300);

        TextField txtZipCode = new TextField();
        txtZipCode.setPromptText("Codigo postal");
        txtZipCode.getStyleClass().add("form-input");
        txtZipCode.setPrefWidth(300);

        TextField txtCountry = new TextField();
        txtCountry.setPromptText("Colombia");
        txtCountry.setText("Colombia"); // Default value
        txtCountry.getStyleClass().add("form-input");
        txtCountry.setPrefWidth(300);

        TextField txtLatitude = new TextField();
        txtLatitude.setPromptText("Ej: 4.536389");
        txtLatitude.getStyleClass().add("form-input");
        txtLatitude.setPrefWidth(300);

        TextField txtLongitude = new TextField();
        txtLongitude.setPromptText("Ej: -75.671111");
        txtLongitude.getStyleClass().add("form-input");
        txtLongitude.setPrefWidth(300);

        // Add numeric validation for coordinates
        addNumericValidation(txtLatitude);
        addNumericValidation(txtLongitude);

        // Create hint labels for coordinates
        Label lblLatitudeHint = new Label("Para integracion con mapas");
        lblLatitudeHint.getStyleClass().add("form-label-small");
        lblLatitudeHint.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 11px; -fx-font-style: italic;");

        Label lblLongitudeHint = new Label("Para integracion con mapas");
        lblLongitudeHint.getStyleClass().add("form-label-small");
        lblLongitudeHint.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 11px; -fx-font-style: italic;");

        if (existingAddress != null) {
            txtAlias.setText(existingAddress.getAlias());
            txtStreet.setText(existingAddress.getStreet());
            txtCity.setText(existingAddress.getCity());
            txtState.setText(existingAddress.getState());
            txtZipCode.setText(existingAddress.getZipCode());
            txtCountry.setText(existingAddress.getCountry() != null ? existingAddress.getCountry() : "Colombia");
            if (existingAddress.getLatitude() != null) {
                txtLatitude.setText(String.valueOf(existingAddress.getLatitude()));
            }
            if (existingAddress.getLongitude() != null) {
                txtLongitude.setText(String.valueOf(existingAddress.getLongitude()));
            }
        }

        // Add components to grid
        int row = 0;
        grid.add(lblAlias, 0, row);
        grid.add(txtAlias, 1, row++);
        grid.add(lblStreet, 0, row);
        grid.add(txtStreet, 1, row++);
        grid.add(lblCity, 0, row);
        grid.add(txtCity, 1, row++);
        grid.add(lblState, 0, row);
        grid.add(txtState, 1, row++);
        grid.add(lblZipCode, 0, row);
        grid.add(txtZipCode, 1, row++);
        grid.add(lblCountry, 0, row);
        grid.add(txtCountry, 1, row++);
        grid.add(lblLatitude, 0, row);
        VBox latBox = new VBox(5, txtLatitude, lblLatitudeHint);
        grid.add(latBox, 1, row++);
        grid.add(lblLongitude, 0, row);
        VBox longBox = new VBox(5, txtLongitude, lblLongitudeHint);
        grid.add(longBox, 1, row++);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setMinWidth(500);

        // Style buttons
        dialog.getDialogPane().lookupButton(saveButtonType).getStyleClass().addAll("btn-primary");
        dialog.getDialogPane().lookupButton(ButtonType.CANCEL).getStyleClass().addAll("btn-secondary");

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Address address = existingAddress != null ? existingAddress : new Address();
                address.setAlias(txtAlias.getText());
                address.setStreet(txtStreet.getText());
                address.setCity(txtCity.getText());
                address.setState(txtState.getText());
                address.setZipCode(txtZipCode.getText());
                address.setCountry(txtCountry.getText());

                // Parse coordinates (optional)
                address.setLatitude(parseDoubleOrNull(txtLatitude.getText()));
                address.setLongitude(parseDoubleOrNull(txtLongitude.getText()));

                return address;
            }
            return null;
        });

        return dialog;
    }

    /**
     * Adds numeric validation to a text field (allows negative numbers and decimals).
     */
    private void addNumericValidation(TextField field) {
        field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("-?\\d*\\.?\\d*")) {
                field.setText(oldValue);
            }
        });
    }

    /**
     * Parses a double from text, returns null if invalid or empty.
     */
    private Double parseDoubleOrNull(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
