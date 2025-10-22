package co.edu.uniquindio.poo.proyectofiinal2025_2.Controller;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Address;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.User;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Services.AuthenticationService;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Util.UtilController.DialogUtil;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Util.UtilModel.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.UUID;

/**
 * Controller for managing user's frequent addresses.
 */
public class ManageAddressesController implements Initializable {

    @FXML private TableView<Address> addressesTable;
    @FXML private TableColumn<Address, String> colLabel;
    @FXML private TableColumn<Address, String> colStreet;
    @FXML private TableColumn<Address, String> colCity;
    @FXML private TableColumn<Address, String> colState;
    @FXML private TableColumn<Address, String> colPostalCode;
    @FXML private TableColumn<Address, String> colDefault;

    @FXML private VBox emptyStateBox;
    @FXML private Label lblAddressCount;

    private final AuthenticationService authService = AuthenticationService.getInstance();
    private User currentUser;
    private ObservableList<Address> addressesData;
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

        addressesTable.setItems(addressesData);
        updateUI();
    }

    private void updateUI() {
        boolean isEmpty = addressesData.isEmpty();
        addressesTable.setVisible(!isEmpty);
        addressesTable.setManaged(!isEmpty);
        emptyStateBox.setVisible(isEmpty);
        emptyStateBox.setManaged(isEmpty);
        lblAddressCount.setText(String.valueOf(addressesData.size()));
    }

    @FXML
    private void handleAddAddress() {
        Dialog<Address> dialog = createAddressDialog(null);
        Optional<Address> result = dialog.showAndWait();

        result.ifPresent(address -> {
            address.setId(UUID.randomUUID().toString());
            currentUser.addFrequentAddress(address);
            addressesData.add(address);
            updateUI();
            // TODO: Save to repository
            DialogUtil.showSuccess("Exito", "Direccion agregada correctamente");
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

        result.ifPresent(address -> {
            int index = addressesData.indexOf(selected);
            addressesData.set(index, address);
            currentUser.getFrequentAddresses().set(index, address);
            // TODO: Save to repository
            DialogUtil.showSuccess("Exito", "Direccion actualizada correctamente");
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
            addressesData.remove(selected);
            currentUser.getFrequentAddresses().remove(selected);
            updateUI();
            // TODO: Save to repository
            DialogUtil.showSuccess("Exito", "Direccion eliminada correctamente");
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
                getClass().getResource("/co/edu/uniquindio/poo/proyectofiinal2025_2/Style.css").toExternalForm()
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

        if (existingAddress != null) {
            txtAlias.setText(existingAddress.getAlias());
            txtStreet.setText(existingAddress.getStreet());
            txtCity.setText(existingAddress.getCity());
            txtState.setText(existingAddress.getState());
            txtZipCode.setText(existingAddress.getZipCode());
        }

        grid.add(lblAlias, 0, 0);
        grid.add(txtAlias, 1, 0);
        grid.add(lblStreet, 0, 1);
        grid.add(txtStreet, 1, 1);
        grid.add(lblCity, 0, 2);
        grid.add(txtCity, 1, 2);
        grid.add(lblState, 0, 3);
        grid.add(txtState, 1, 3);
        grid.add(lblZipCode, 0, 4);
        grid.add(txtZipCode, 1, 4);

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
                return address;
            }
            return null;
        });

        return dialog;
    }
}
