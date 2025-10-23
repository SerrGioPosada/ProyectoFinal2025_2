package co.edu.uniquindio.poo.proyectofinal2025_2.Controller;

import co.edu.uniquindio.poo.proyectofinal2025_2.Model.Address;
import co.edu.uniquindio.poo.proyectofinal2025_2.Model.AdditionalService;
import co.edu.uniquindio.poo.proyectofinal2025_2.Model.Decorator.CostCalculator;
import co.edu.uniquindio.poo.proyectofinal2025_2.Model.Enums.ServiceType;
import co.edu.uniquindio.poo.proyectofinal2025_2.Model.User;
import co.edu.uniquindio.poo.proyectofinal2025_2.Model.dto.QuoteDTO;
import co.edu.uniquindio.poo.proyectofinal2025_2.Model.dto.QuoteResultDTO;
import co.edu.uniquindio.poo.proyectofinal2025_2.Model.dto.ShipmentDTO;
import co.edu.uniquindio.poo.proyectofinal2025_2.Repositories.AddressRepository;
import co.edu.uniquindio.poo.proyectofinal2025_2.Services.AuthenticationService;
import co.edu.uniquindio.poo.proyectofinal2025_2.Services.ShipmentService;
import co.edu.uniquindio.poo.proyectofinal2025_2.Services.TariffService;
import co.edu.uniquindio.poo.proyectofinal2025_2.Util.UtilModel.DistanceCalculator;
import co.edu.uniquindio.poo.proyectofinal2025_2.Util.UtilModel.Logger;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for creating new shipments.
 * Handles quote calculation and shipment creation with real-time cost updates.
 */
public class CreateShipmentController implements Initializable {

    // Address Selection
    @FXML private ComboBox<Address> cmbOrigin;
    @FXML private ComboBox<Address> cmbDestination;

    // Package Details
    @FXML private TextField txtWeight;
    @FXML private TextField txtHeight;
    @FXML private TextField txtWidth;
    @FXML private TextField txtLength;
    @FXML private Label lblVolume;

    // Additional Services
    @FXML private CheckBox chkInsurance;
    @FXML private CheckBox chkFragile;
    @FXML private CheckBox chkSignature;
    @FXML private CheckBox chkPriority;

    // Priority and Notes
    @FXML private Slider sliderPriority;
    @FXML private Label lblPriorityValue;
    @FXML private TextArea txtNotes;

    // Pickup Schedule
    @FXML private DatePicker dpPickupDate;
    @FXML private Spinner<Integer> spinHour;
    @FXML private Spinner<Integer> spinMinute;

    // Cost Display
    @FXML private Label lblBaseCost;
    @FXML private Label lblDistanceCost;
    @FXML private Label lblWeightCost;
    @FXML private Label lblVolumeCost;
    @FXML private Label lblServicesCost;
    @FXML private Label lblTotalCost;
    @FXML private Label lblEstimatedDelivery;

    // Services
    private final ShipmentService shipmentService = new ShipmentService();
    private final TariffService tariffService = new TariffService();
    private final AddressRepository addressRepository = AddressRepository.getInstance();
    private final AuthenticationService authService = AuthenticationService.getInstance();

    // Current quote
    private QuoteResultDTO currentQuote;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupAddressComboBoxes();
        setupNumericFields();
        setupPrioritySlider();
        setupPickupDateTime();
        setupCostListeners();
        loadUserAddresses();

        Logger.info("CreateShipmentController initialized");
    }

    /**
     * Sets up address combo boxes with custom formatting.
     */
    private void setupAddressComboBoxes() {
        // Set prompt text BEFORE converter
        cmbOrigin.setPromptText("Select origin address");
        cmbDestination.setPromptText("Select destination address");

        StringConverter<Address> addressConverter = new StringConverter<Address>() {
            @Override
            public String toString(Address address) {
                if (address == null) return "";
                return address.getAlias() + " - " + address.getStreet() + ", " + address.getCity();
            }

            @Override
            public Address fromString(String string) {
                return null;
            }
        };

        cmbOrigin.setConverter(addressConverter);
        cmbDestination.setConverter(addressConverter);

        // Add listeners for automatic quote calculation
        cmbOrigin.valueProperty().addListener((obs, oldVal, newVal) -> calculateQuoteIfReady());
        cmbDestination.valueProperty().addListener((obs, oldVal, newVal) -> calculateQuoteIfReady());
    }

    /**
     * Sets up numeric text fields with validation.
     */
    private void setupNumericFields() {
        addNumericValidation(txtWeight);
        addNumericValidation(txtHeight);
        addNumericValidation(txtWidth);
        addNumericValidation(txtLength);

        // Add listeners for automatic calculations
        txtWeight.textProperty().addListener((obs, oldVal, newVal) -> calculateQuoteIfReady());
        txtHeight.textProperty().addListener((obs, oldVal, newVal) -> updateVolume());
        txtWidth.textProperty().addListener((obs, oldVal, newVal) -> updateVolume());
        txtLength.textProperty().addListener((obs, oldVal, newVal) -> updateVolume());
    }

    /**
     * Adds numeric validation to a text field.
     */
    private void addNumericValidation(TextField field) {
        field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                field.setText(oldValue);
            }
        });
    }

    /**
     * Sets up the priority slider.
     */
    private void setupPrioritySlider() {
        sliderPriority.valueProperty().addListener((obs, oldVal, newVal) -> {
            int priority = newVal.intValue();
            lblPriorityValue.setText(String.valueOf(priority));
            calculateQuoteIfReady();
        });

        lblPriorityValue.setText("3");
    }

    /**
     * Sets up pickup date and time spinners.
     */
    private void setupPickupDateTime() {
        dpPickupDate.setValue(LocalDate.now().plusDays(1));

        SpinnerValueFactory<Integer> hourFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 12);
        SpinnerValueFactory<Integer> minuteFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0);

        spinHour.setValueFactory(hourFactory);
        spinMinute.setValueFactory(minuteFactory);
    }

    /**
     * Sets up listeners for cost-affecting checkboxes.
     */
    private void setupCostListeners() {
        chkInsurance.selectedProperty().addListener((obs, oldVal, newVal) -> calculateQuoteIfReady());
        chkFragile.selectedProperty().addListener((obs, oldVal, newVal) -> calculateQuoteIfReady());
        chkSignature.selectedProperty().addListener((obs, oldVal, newVal) -> calculateQuoteIfReady());
        chkPriority.selectedProperty().addListener((obs, oldVal, newVal) -> calculateQuoteIfReady());
    }

    /**
     * Loads the current user's addresses into combo boxes.
     */
    private void loadUserAddresses() {
        var currentPerson = authService.getCurrentPerson();
        if (currentPerson == null) {
            showError("No hay usuario autenticado");
            return;
        }

        // Check if current person is a User (not Admin)
        if (!(currentPerson instanceof User)) {
            showWarning("Los administradores deben crear envíos para usuarios específicos.\nPor favor, use la vista de Gestión de Envíos.");
            return;
        }

        User currentUser = (User) currentPerson;
        List<Address> addresses = currentUser.getFrequentAddresses();
        if (addresses == null || addresses.isEmpty()) {
            showWarning("No tienes direcciones guardadas. Por favor, agrega direcciones en tu perfil.");
            return;
        }

        cmbOrigin.getItems().setAll(addresses);
        cmbDestination.getItems().setAll(addresses);
    }

    /**
     * Updates the volume label based on dimensions.
     */
    private void updateVolume() {
        try {
            double height = parseDouble(txtHeight.getText());
            double width = parseDouble(txtWidth.getText());
            double length = parseDouble(txtLength.getText());

            if (height > 0 && width > 0 && length > 0) {
                double volumeM3 = (height * width * length) / 1000000.0;
                lblVolume.setText(String.format("%.4f m³", volumeM3));
                calculateQuoteIfReady();
            } else {
                lblVolume.setText("0.0 m³");
            }
        } catch (Exception e) {
            lblVolume.setText("0.0 m³");
        }
    }

    /**
     * Calculates quote if all required fields are filled.
     */
    @FXML
    private void handleCalculateQuote() {
        calculateQuoteIfReady();
    }

    /**
     * Calculates quote automatically if ready.
     */
    private void calculateQuoteIfReady() {
        try {
            // Check if all required fields are filled
            if (cmbOrigin.getValue() == null || cmbDestination.getValue() == null) {
                resetCostLabels();
                return;
            }

            double weight = parseDouble(txtWeight.getText());
            double height = parseDouble(txtHeight.getText());
            double width = parseDouble(txtWidth.getText());
            double length = parseDouble(txtLength.getText());

            if (weight <= 0 || height <= 0 || width <= 0 || length <= 0) {
                resetCostLabels();
                return;
            }

            // Build quote DTO
            QuoteDTO quoteDTO = new QuoteDTO();
            quoteDTO.setOriginId(cmbOrigin.getValue().getId());
            quoteDTO.setDestinationId(cmbDestination.getValue().getId());
            quoteDTO.setWeightKg(weight);
            quoteDTO.setHeightCm(height);
            quoteDTO.setWidthCm(width);
            quoteDTO.setLengthCm(length);
            quoteDTO.setPriority((int) sliderPriority.getValue());

            // Add selected services
            List<ServiceType> services = new ArrayList<>();
            if (chkInsurance.isSelected()) services.add(ServiceType.INSURANCE);
            if (chkFragile.isSelected()) services.add(ServiceType.FRAGILE);
            if (chkSignature.isSelected()) services.add(ServiceType.SIGNATURE_REQUIRED);
            if (chkPriority.isSelected()) services.add(ServiceType.PRIORITY);
            quoteDTO.setAdditionalServices(services);

            // Set pickup date
            LocalDateTime pickupDateTime = getPickupDateTime();
            quoteDTO.setRequestedPickupDate(pickupDateTime);

            // Calculate quote
            currentQuote = shipmentService.quoteShipment(quoteDTO);

            // Update UI
            updateCostLabels(currentQuote);

        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
            resetCostLabels();
        } catch (Exception e) {
            Logger.error("Error calculating quote: " + e.getMessage());
            resetCostLabels();
        }
    }

    /**
     * Updates cost labels with quote results.
     */
    private void updateCostLabels(QuoteResultDTO quote) {
        lblBaseCost.setText(formatCurrency(quote.getBaseCost()));
        lblDistanceCost.setText(formatCurrency(quote.getDistanceCost()));
        lblWeightCost.setText(formatCurrency(quote.getWeightCost()));
        lblVolumeCost.setText(formatCurrency(quote.getVolumeCost()));
        lblServicesCost.setText(formatCurrency(quote.getServicesCost() + quote.getPriorityCost()));
        lblTotalCost.setText(formatCurrency(quote.getTotalCost()));

        if (quote.getEstimatedDelivery() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            lblEstimatedDelivery.setText(quote.getEstimatedDelivery().format(formatter));
        } else {
            lblEstimatedDelivery.setText("--");
        }
    }

    /**
     * Resets cost labels to zero.
     */
    private void resetCostLabels() {
        lblBaseCost.setText("$0");
        lblDistanceCost.setText("$0");
        lblWeightCost.setText("$0");
        lblVolumeCost.setText("$0");
        lblServicesCost.setText("$0");
        lblTotalCost.setText("$0");
        lblEstimatedDelivery.setText("--");
        currentQuote = null;
    }

    /**
     * Handles shipment creation.
     */
    @FXML
    private void handleCreateShipment() {
        try {
            // Validate
            if (currentQuote == null) {
                showWarning("Por favor, calcula la cotización primero");
                return;
            }

            var currentPerson = authService.getCurrentPerson();
            if (currentPerson == null) {
                showError("No hay usuario autenticado");
                return;
            }

            // Check if current person is a User (not Admin)
            if (!(currentPerson instanceof User)) {
                showWarning("Los administradores no pueden crear envíos directamente.\nPor favor, use la vista de Gestión de Envíos.");
                return;
            }

            User currentUser = (User) currentPerson;

            // Build shipment DTO
            ShipmentDTO dto = new ShipmentDTO();
            dto.setUserId(currentUser.getId());
            dto.setOriginId(cmbOrigin.getValue().getId());
            dto.setDestinationId(cmbDestination.getValue().getId());
            dto.setWeightKg(parseDouble(txtWeight.getText()));
            dto.setHeightCm(parseDouble(txtHeight.getText()));
            dto.setWidthCm(parseDouble(txtWidth.getText()));
            dto.setLengthCm(parseDouble(txtLength.getText()));
            dto.setPriority((int) sliderPriority.getValue());
            dto.setUserNotes(txtNotes.getText());
            dto.setRequestedPickupDate(getPickupDateTime());

            // Add services
            List<AdditionalService> services = new ArrayList<>();
            double subtotal = currentQuote.getBaseCost() + currentQuote.getWeightCost() +
                            currentQuote.getVolumeCost() + currentQuote.getDistanceCost();

            if (chkInsurance.isSelected()) {
                services.add(new AdditionalService(ServiceType.INSURANCE, subtotal));
            }
            if (chkFragile.isSelected()) {
                services.add(new AdditionalService(ServiceType.FRAGILE, subtotal));
            }
            if (chkSignature.isSelected()) {
                services.add(new AdditionalService(ServiceType.SIGNATURE_REQUIRED, subtotal));
            }
            if (chkPriority.isSelected()) {
                services.add(new AdditionalService(ServiceType.PRIORITY, subtotal));
            }

            dto.setAdditionalServices(services);

            // Create shipment
            ShipmentDTO created = shipmentService.createShipment(dto);

            if (created != null) {
                showSuccess("Envío creado exitosamente con ID: " + created.getId());
                clearForm();
            } else {
                showError("Error al crear el envío");
            }

        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            Logger.error("Error creating shipment: " + e.getMessage());
            showError("Error inesperado al crear el envío");
        }
    }

    /**
     * Handles cancel button.
     */
    @FXML
    private void handleCancel() {
        // Close the window
        Stage stage = (Stage) cmbOrigin.getScene().getWindow();
        stage.close();
    }

    /**
     * Clears the form.
     */
    private void clearForm() {
        cmbOrigin.setValue(null);
        cmbDestination.setValue(null);
        txtWeight.clear();
        txtHeight.clear();
        txtWidth.clear();
        txtLength.clear();
        lblVolume.setText("0.0 m³");
        chkInsurance.setSelected(false);
        chkFragile.setSelected(false);
        chkSignature.setSelected(false);
        chkPriority.setSelected(false);
        sliderPriority.setValue(3);
        txtNotes.clear();
        dpPickupDate.setValue(LocalDate.now().plusDays(1));
        spinHour.getValueFactory().setValue(12);
        spinMinute.getValueFactory().setValue(0);
        resetCostLabels();
    }

    /**
     * Gets the pickup date-time from the form.
     */
    private LocalDateTime getPickupDateTime() {
        LocalDate date = dpPickupDate.getValue();
        if (date == null) {
            date = LocalDate.now().plusDays(1);
        }

        int hour = spinHour.getValue() != null ? spinHour.getValue() : 12;
        int minute = spinMinute.getValue() != null ? spinMinute.getValue() : 0;

        return LocalDateTime.of(date, LocalTime.of(hour, minute));
    }

    /**
     * Parses a double from a string, returns 0 if invalid.
     */
    private double parseDouble(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /**
     * Formats a value as currency.
     */
    private String formatCurrency(double value) {
        return String.format("$%,.0f", value);
    }

    /**
     * Shows an error alert.
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Shows a warning alert.
     */
    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Advertencia");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Shows a success alert.
     */
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Shows detailed cost breakdown using Decorator pattern.
     */
    @FXML
    private void handleShowDetailedBreakdown() {
        try {
            // Validate that we have all required data
            if (cmbOrigin.getValue() == null || cmbDestination.getValue() == null) {
                showWarning("Por favor, selecciona las direcciones de origen y destino");
                return;
            }

            double weight = parseDouble(txtWeight.getText());
            double height = parseDouble(txtHeight.getText());
            double width = parseDouble(txtWidth.getText());
            double length = parseDouble(txtLength.getText());

            if (weight <= 0 || height <= 0 || width <= 0 || length <= 0) {
                showWarning("Por favor, completa todos los datos del paquete");
                return;
            }

            // Calculate distance and volume
            double distance = DistanceCalculator.calculateDistance(
                cmbOrigin.getValue(),
                cmbDestination.getValue()
            );
            double volume = (height * width * length) / 1000000.0;

            // Get selected services
            List<ServiceType> services = new ArrayList<>();
            if (chkInsurance.isSelected()) services.add(ServiceType.INSURANCE);
            if (chkFragile.isSelected()) services.add(ServiceType.FRAGILE);
            if (chkSignature.isSelected()) services.add(ServiceType.SIGNATURE_REQUIRED);
            if (chkPriority.isSelected()) services.add(ServiceType.PRIORITY);

            // Get breakdown from TariffService using Decorator pattern
            var breakdown = tariffService.getCostBreakdown(
                distance,
                weight,
                volume,
                (int) sliderPriority.getValue(),
                services
            );

            // Build breakdown dialog
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Desglose Detallado de Costos");
            dialog.setHeaderText("Cálculo usando Patrón Decorator + Tariff");

            // Create content
            VBox content = new VBox(10);
            content.setPadding(new Insets(20));

            Label title = new Label("Desglose Línea por Línea:");
            title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            content.getChildren().add(title);

            // Add separator
            Separator sep1 = new Separator();
            content.getChildren().add(sep1);

            // Add each breakdown item
            double runningTotal = 0.0;
            for (CostCalculator.CostBreakdownItem item : breakdown) {
                runningTotal += item.getAmount();

                Label itemLabel = new Label(String.format("• %s: %s",
                    item.getDescription(),
                    formatCurrency(item.getAmount())
                ));
                itemLabel.setStyle("-fx-font-size: 12px;");
                content.getChildren().add(itemLabel);
            }

            // Add separator
            Separator sep2 = new Separator();
            content.getChildren().add(sep2);

            // Add total
            Label totalLabel = new Label(String.format("TOTAL: %s", formatCurrency(runningTotal)));
            totalLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #2196F3;");
            content.getChildren().add(totalLabel);

            // Add info about decorator pattern
            Separator sep3 = new Separator();
            content.getChildren().add(sep3);

            Label infoLabel = new Label(
                "💡 Este desglose se calcula usando:\n" +
                "   • Tariff: Plantilla de precios base\n" +
                "   • BaseShippingCost: Cálculo de costos básicos\n" +
                "   • Decorators: Servicios adicionales apilados"
            );
            infoLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
            infoLabel.setWrapText(true);
            content.getChildren().add(infoLabel);

            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
            dialog.showAndWait();

        } catch (Exception e) {
            Logger.error("Error showing detailed breakdown: " + e.getMessage());
            showError("Error al calcular el desglose detallado");
        }
    }
}