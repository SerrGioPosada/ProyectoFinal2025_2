package co.edu.uniquindio.poo.ProyectoFinal2025_2.Controller;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Address;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.AdditionalService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.ServiceType;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.VehicleType;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.User;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.dto.OrderDetailDTO;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.dto.QuoteDTO;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.dto.QuoteResultDTO;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Order;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.AuthenticationService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.OrderService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.ShipmentService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.DialogUtil;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.DistanceCalculator;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.VehicleSelector;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;

/**
 * Controller for the multi-step shipment creation wizard.
 * Provides a professional step-by-step experience for creating shipments.
 */
public class CreateShipmentWizardController implements Initializable {

    // =================================================================================================================
    // Step Indicators
    // =================================================================================================================
    @FXML private VBox step1Container;
    @FXML private VBox step2Container;
    @FXML private VBox step3Container;
    @FXML private VBox step4Container;
    @FXML private VBox step5Container;

    // =================================================================================================================
    // Form Containers
    // =================================================================================================================
    @FXML private VBox step1Form;
    @FXML private VBox step2Form;
    @FXML private VBox step3Form;
    @FXML private VBox step4Form;
    @FXML private VBox step5Form;

    // =================================================================================================================
    // Navigation Buttons
    // =================================================================================================================
    @FXML private Button btnPrevious;
    @FXML private Button btnNext;
    @FXML private Button btnConfirm;
    @FXML private Button btnCancel;

    // =================================================================================================================
    // Step 1: Package Details
    // =================================================================================================================
    @FXML private TextField txtWeight;
    @FXML private TextField txtHeight;
    @FXML private TextField txtWidth;
    @FXML private TextField txtLength;
    @FXML private Label lblVolumeCalculated;
    @FXML private Label lblWeightError;
    @FXML private Label lblHeightError;
    @FXML private Label lblWidthError;
    @FXML private Label lblLengthError;

    // Floating labels for Step 1
    @FXML private Label lblWeightFloat;
    @FXML private Label lblHeightFloat;
    @FXML private Label lblWidthFloat;
    @FXML private Label lblLengthFloat;

    // =================================================================================================================
    // Step 2: Origin Address
    // =================================================================================================================
    @FXML private ComboBox<Address> cmbOriginSavedAddresses;

    // Floating labels for Step 2 (Origin)
    @FXML private Label lblOriginAliasFloat;
    @FXML private Label lblOriginStreetFloat;
    @FXML private Label lblOriginCityFloat;
    @FXML private Label lblOriginStateFloat;
    @FXML private Label lblOriginZipCodeFloat;
    @FXML private Label lblOriginCountryFloat;
    @FXML private Label lblOriginLatitudeFloat;
    @FXML private Label lblOriginLongitudeFloat;
    @FXML private TextField txtOriginAlias;
    @FXML private TextField txtOriginStreet;
    @FXML private TextField txtOriginCity;
    @FXML private TextField txtOriginState;
    @FXML private TextField txtOriginZipCode;
    @FXML private TextField txtOriginCountry;
    @FXML private TextField txtOriginLatitude;
    @FXML private TextField txtOriginLongitude;
    @FXML private Label lblOriginStreetError;
    @FXML private Label lblOriginCityError;

    // =================================================================================================================
    // Step 3: Destination Address
    // =================================================================================================================
    @FXML private ComboBox<Address> cmbDestinationSavedAddresses;

    // Floating labels for Step 3 (Destination)
    @FXML private Label lblDestinationAliasFloat;
    @FXML private Label lblDestinationStreetFloat;
    @FXML private Label lblDestinationCityFloat;
    @FXML private Label lblDestinationStateFloat;
    @FXML private Label lblDestinationZipCodeFloat;
    @FXML private Label lblDestinationCountryFloat;
    @FXML private Label lblDestinationLatitudeFloat;
    @FXML private Label lblDestinationLongitudeFloat;

    @FXML private TextField txtDestinationAlias;
    @FXML private TextField txtDestinationStreet;
    @FXML private TextField txtDestinationCity;
    @FXML private TextField txtDestinationState;
    @FXML private TextField txtDestinationZipCode;
    @FXML private TextField txtDestinationCountry;
    @FXML private TextField txtDestinationLatitude;
    @FXML private TextField txtDestinationLongitude;
    @FXML private Label lblDestinationStreetError;
    @FXML private Label lblDestinationCityError;

    // =================================================================================================================
    // Step 4: Additional Services
    // =================================================================================================================
    @FXML private CheckBox chkInsurance;
    @FXML private CheckBox chkFragile;
    @FXML private CheckBox chkSignature;
    @FXML private CheckBox chkPriority;
    @FXML private Label lblInsuranceCost;
    @FXML private Label lblPriorityCost;
    @FXML private ComboBox<co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.VehicleType> cmbVehicleType;
    @FXML private Label lblVehicleRecommendation;
    @FXML private Label lblVehicleValidation;
    @FXML private TextArea txtNotes;
    @FXML private DatePicker datePickup;
    @FXML private Spinner<Integer> spinnerHour;
    @FXML private Spinner<Integer> spinnerMinute;

    // =================================================================================================================
    // Step 5: Summary
    // =================================================================================================================
    @FXML private Label lblSummaryWeight;
    @FXML private Label lblSummaryDimensions;
    @FXML private Label lblSummaryVolume;
    @FXML private Label lblSummaryOrigin;
    @FXML private Label lblSummaryDestination;
    @FXML private Label lblSummaryServices;
    @FXML private Label lblQuoteBaseCost;
    @FXML private Label lblQuoteDistanceCost;
    @FXML private Label lblQuoteWeightCost;
    @FXML private Label lblQuoteVolumeCost;
    @FXML private Label lblQuoteServicesCost;
    @FXML private Label lblQuoteTotalCost;
    @FXML private Label lblEstimatedDelivery;

    // =================================================================================================================
    // Services and State
    // =================================================================================================================
    private final ShipmentService shipmentService = new ShipmentService();
    private final AuthenticationService authService = AuthenticationService.getInstance();
    private final OrderService orderService = new OrderService();
    private int currentStep = 1;
    private QuoteResultDTO currentQuote;
    private IndexController indexController;
    private boolean comesFromMyShipments = false;

    // =================================================================================================================
    // Initialization
    // =================================================================================================================

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupFloatingLabels();
        setupNumericFields();
        setupVolumeCalculation();
        setupPickupTime();
        setupServiceCostUpdates();
        setupVehicleSelector();
        setupSavedAddresses();
        setupNotesTextArea();
        updateStepIndicators();
        updateNavigationButtons();

        // Initialize service cost labels with default values
        updateServiceCosts();

        Logger.info("CreateShipmentWizardController initialized");
    }

    /**
     * Sets up the saved addresses ComboBoxes and their listeners.
     */
    private void setupSavedAddresses() {
        var currentPerson = authService.getCurrentPerson();
        if (currentPerson instanceof User) {
            User currentUser = (User) currentPerson;
            List<Address> addresses = currentUser.getFrequentAddresses();

            if (addresses != null && !addresses.isEmpty()) {
                // Setup origin addresses ComboBox
                cmbOriginSavedAddresses.setItems(FXCollections.observableArrayList(addresses));
                cmbOriginSavedAddresses.setPromptText("Seleccione dirección guardada");

                // Custom StringConverter for display
                javafx.util.StringConverter<Address> addressConverter = new javafx.util.StringConverter<Address>() {
                    @Override
                    public String toString(Address address) {
                        if (address == null) return "";
                        String alias = address.getAlias() != null ? address.getAlias() : "Sin nombre";
                        return String.format("%s - %s, %s", alias, address.getStreet(), address.getCity());
                    }

                    @Override
                    public Address fromString(String string) {
                        return null;
                    }
                };

                cmbOriginSavedAddresses.setConverter(addressConverter);

                // Custom button cell to ensure prompt text shows with visible colors
                cmbOriginSavedAddresses.setButtonCell(new ListCell<Address>() {
                    @Override
                    protected void updateItem(Address item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setStyle("-fx-text-fill: #4a5568 !important; -fx-font-weight: normal;");
                        } else {
                            setText(addressConverter.toString(item));
                            setStyle("-fx-text-fill: #1a202c !important; -fx-font-weight: bold;");
                        }
                    }
                });

                // Setup destination addresses ComboBox
                cmbDestinationSavedAddresses.setItems(FXCollections.observableArrayList(addresses));
                cmbDestinationSavedAddresses.setPromptText("Seleccione dirección guardada");
                cmbDestinationSavedAddresses.setConverter(addressConverter);

                // Custom button cell to ensure prompt text shows with visible colors
                cmbDestinationSavedAddresses.setButtonCell(new ListCell<Address>() {
                    @Override
                    protected void updateItem(Address item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setStyle("-fx-text-fill: #4a5568 !important; -fx-font-weight: normal;");
                        } else {
                            setText(addressConverter.toString(item));
                            setStyle("-fx-text-fill: #1a202c !important; -fx-font-weight: bold;");
                        }
                    }
                });

                // Add listener for origin address selection
                cmbOriginSavedAddresses.valueProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        fillOriginAddressFields(newVal);
                    }
                });

                // Add listener for destination address selection
                cmbDestinationSavedAddresses.valueProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        fillDestinationAddressFields(newVal);
                    }
                });
            }
        }
    }

    /**
     * Fills the origin address fields with the selected address.
     */
    private void fillOriginAddressFields(Address address) {
        txtOriginAlias.setText(address.getAlias() != null ? address.getAlias() : "");
        txtOriginStreet.setText(address.getStreet());
        txtOriginCity.setText(address.getCity());
        txtOriginState.setText(address.getState() != null ? address.getState() : "");
        txtOriginZipCode.setText(address.getZipCode() != null ? address.getZipCode() : "");
        txtOriginCountry.setText(address.getCountry() != null ? address.getCountry() : "Colombia");

        if (address.getLatitude() != null) {
            txtOriginLatitude.setText(String.valueOf(address.getLatitude()));
        } else {
            txtOriginLatitude.setText("");
        }

        if (address.getLongitude() != null) {
            txtOriginLongitude.setText(String.valueOf(address.getLongitude()));
        } else {
            txtOriginLongitude.setText("");
        }
    }

    /**
     * Sets up the notes TextArea to be auto-expanding with a maximum height.
     * The TextArea grows as the user types, up to a maximum of 150px, then shows scrollbar.
     */
    private void setupNotesTextArea() {
        if (txtNotes != null) {
            // Set initial height
            txtNotes.setPrefHeight(70);
            txtNotes.setMinHeight(70);
            txtNotes.setMaxHeight(150);

            // Add listener to auto-expand
            txtNotes.textProperty().addListener((obs, oldVal, newVal) -> {
                // Calculate required height based on content
                // Approximate: each line is ~20px, add padding
                String text = newVal != null ? newVal : "";
                int lineCount = text.split("\n").length;

                // Add extra line for wrapping text
                if (text.length() > 0) {
                    // Rough estimate: 50 characters per line at current width
                    int estimatedWrappedLines = (int) Math.ceil(text.length() / 50.0);
                    lineCount = Math.max(lineCount, estimatedWrappedLines);
                }

                // Calculate height: base padding (10) + lines * line height (20)
                double calculatedHeight = 10 + (lineCount * 20);

                // Clamp between min and max
                double newHeight = Math.max(70, Math.min(150, calculatedHeight));

                txtNotes.setPrefHeight(newHeight);
            });

            Logger.info("Notes TextArea auto-expansion configured");
        }
    }

    /**
     * Fills the destination address fields with the selected address.
     */
    private void fillDestinationAddressFields(Address address) {
        txtDestinationAlias.setText(address.getAlias() != null ? address.getAlias() : "");
        txtDestinationStreet.setText(address.getStreet());
        txtDestinationCity.setText(address.getCity());
        txtDestinationState.setText(address.getState() != null ? address.getState() : "");
        txtDestinationZipCode.setText(address.getZipCode() != null ? address.getZipCode() : "");
        txtDestinationCountry.setText(address.getCountry() != null ? address.getCountry() : "Colombia");

        if (address.getLatitude() != null) {
            txtDestinationLatitude.setText(String.valueOf(address.getLatitude()));
        } else {
            txtDestinationLatitude.setText("");
        }

        if (address.getLongitude() != null) {
            txtDestinationLongitude.setText(String.valueOf(address.getLongitude()));
        } else {
            txtDestinationLongitude.setText("");
        }
    }

    /**
     * Sets up floating label animations for all text fields.
     */
    private void setupFloatingLabels() {
        // Step 1: Package detail fields
        if (txtWeight != null && lblWeightFloat != null) {
            setupFieldListeners(txtWeight, lblWeightFloat, "Peso (kg)");
        }
        if (txtHeight != null && lblHeightFloat != null) {
            setupFieldListeners(txtHeight, lblHeightFloat, "Alto (cm)");
        }
        if (txtWidth != null && lblWidthFloat != null) {
            setupFieldListeners(txtWidth, lblWidthFloat, "Ancho (cm)");
        }
        if (txtLength != null && lblLengthFloat != null) {
            setupFieldListeners(txtLength, lblLengthFloat, "Largo (cm)");
        }

        // Step 2: Origin address fields
        if (txtOriginAlias != null && lblOriginAliasFloat != null) {
            setupFieldListeners(txtOriginAlias, lblOriginAliasFloat, "Alias");
        }
        if (txtOriginStreet != null && lblOriginStreetFloat != null) {
            setupFieldListeners(txtOriginStreet, lblOriginStreetFloat, "Calle");
        }
        if (txtOriginCity != null && lblOriginCityFloat != null) {
            setupFieldListeners(txtOriginCity, lblOriginCityFloat, "Ciudad");
        }
        if (txtOriginState != null && lblOriginStateFloat != null) {
            setupFieldListeners(txtOriginState, lblOriginStateFloat, "Departamento");
        }
        if (txtOriginZipCode != null && lblOriginZipCodeFloat != null) {
            setupFieldListeners(txtOriginZipCode, lblOriginZipCodeFloat, "Código postal");
        }
        if (txtOriginCountry != null && lblOriginCountryFloat != null) {
            setupFieldListeners(txtOriginCountry, lblOriginCountryFloat, "País");
        }
        if (txtOriginLatitude != null && lblOriginLatitudeFloat != null) {
            setupFieldListeners(txtOriginLatitude, lblOriginLatitudeFloat, "Latitud");
        }
        if (txtOriginLongitude != null && lblOriginLongitudeFloat != null) {
            setupFieldListeners(txtOriginLongitude, lblOriginLongitudeFloat, "Longitud");
        }

        // Step 3: Destination address fields
        if (txtDestinationAlias != null && lblDestinationAliasFloat != null) {
            setupFieldListeners(txtDestinationAlias, lblDestinationAliasFloat, "Alias");
        }
        if (txtDestinationStreet != null && lblDestinationStreetFloat != null) {
            setupFieldListeners(txtDestinationStreet, lblDestinationStreetFloat, "Calle");
        }
        if (txtDestinationCity != null && lblDestinationCityFloat != null) {
            setupFieldListeners(txtDestinationCity, lblDestinationCityFloat, "Ciudad");
        }
        if (txtDestinationState != null && lblDestinationStateFloat != null) {
            setupFieldListeners(txtDestinationState, lblDestinationStateFloat, "Departamento");
        }
        if (txtDestinationZipCode != null && lblDestinationZipCodeFloat != null) {
            setupFieldListeners(txtDestinationZipCode, lblDestinationZipCodeFloat, "Código postal");
        }
        if (txtDestinationCountry != null && lblDestinationCountryFloat != null) {
            setupFieldListeners(txtDestinationCountry, lblDestinationCountryFloat, "País");
        }
        if (txtDestinationLatitude != null && lblDestinationLatitudeFloat != null) {
            setupFieldListeners(txtDestinationLatitude, lblDestinationLatitudeFloat, "Latitud");
        }
        if (txtDestinationLongitude != null && lblDestinationLongitudeFloat != null) {
            setupFieldListeners(txtDestinationLongitude, lblDestinationLongitudeFloat, "Longitud");
        }
    }

    /**
     * Attaches focus and text listeners to a field to create the floating label effect.
     * The label stays visible when the field contains text, even after losing focus.
     *
     * @param field  The text input control (TextField).
     * @param label  The floating label associated with the field.
     * @param prompt The prompt text to restore when the field is empty and unfocused.
     */
    private void setupFieldListeners(TextField field, Label label, String prompt) {
        label.setOpacity(0);
        label.setTranslateY(35);

        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            String text = field.getText();
            if (newVal) {
                // On focus: always animate label up
                animateFloatingLabel(label, field, true, prompt);
            } else if (text == null || text.isEmpty()) {
                // On blur: only hide label if field is empty
                animateFloatingLabel(label, field, false, prompt);
            }
            // If field has text, label stays visible (don't animate down)
        });

        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty() && label.getOpacity() < 0.5) {
                // If text is added and label is hidden, show it
                animateFloatingLabel(label, field, true, prompt);
            } else if ((newVal == null || newVal.isEmpty()) && !field.isFocused()) {
                // If text is cleared and field is not focused, hide label
                animateFloatingLabel(label, field, false, prompt);
            }
        });
    }

    /**
     * Animates a floating label up or down based on the focus state.
     *
     * @param label      The label to animate.
     * @param field      The associated text field.
     * @param moveUp     True to move the label up, false to move it down.
     * @param promptText The original prompt text to restore.
     */
    private void animateFloatingLabel(Label label, TextField field, boolean moveUp, String promptText) {
        TranslateTransition translate = new TranslateTransition(Duration.millis(200), label);
        FadeTransition fade = new FadeTransition(Duration.millis(200), label);

        if (moveUp) {
            translate.setToY(0);
            fade.setToValue(1.0);
            field.setPromptText(null);
        } else {
            translate.setToY(35);
            fade.setToValue(0.0);
            fade.setOnFinished(e -> field.setPromptText(promptText));
        }
        translate.play();
        fade.play();
    }

    /**
     * Sets up numeric validation for text fields.
     */
    private void setupNumericFields() {
        addNumericValidation(txtWeight);
        addNumericValidation(txtHeight);
        addNumericValidation(txtWidth);
        addNumericValidation(txtLength);
        addNumericValidation(txtOriginLatitude);
        addNumericValidation(txtOriginLongitude);
        addNumericValidation(txtDestinationLatitude);
        addNumericValidation(txtDestinationLongitude);
    }

    /**
     * Adds numeric validation to a text field.
     */
    private void addNumericValidation(TextField field) {
        field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("-?\\d*\\.?\\d*")) {
                field.setText(oldValue);
            }
        });
    }

    /**
     * Sets up automatic volume calculation.
     */
    private void setupVolumeCalculation() {
        txtHeight.textProperty().addListener((obs, old, newVal) -> updateVolumeCalculation());
        txtWidth.textProperty().addListener((obs, old, newVal) -> updateVolumeCalculation());
        txtLength.textProperty().addListener((obs, old, newVal) -> updateVolumeCalculation());
    }

    /**
     * Updates the volume calculation label.
     */
    private void updateVolumeCalculation() {
        try {
            double height = parseDouble(txtHeight.getText());
            double width = parseDouble(txtWidth.getText());
            double length = parseDouble(txtLength.getText());

            if (height > 0 && width > 0 && length > 0) {
                double volumeM3 = (height * width * length) / 1000000.0;
                lblVolumeCalculated.setText(String.format("%.4f m³", volumeM3));
            } else {
                lblVolumeCalculated.setText("0.0 m³");
            }
        } catch (Exception e) {
            lblVolumeCalculated.setText("0.0 m³");
        }
    }

    /**
     * Sets up the pickup time Spinners with validation.
     */
    private void setupPickupTime() {
        // Set default pickup date to tomorrow
        datePickup.setValue(LocalDate.now().plusDays(1));

        // Setup hour spinner (0-23)
        SpinnerValueFactory<Integer> hourFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 12);
        spinnerHour.setValueFactory(hourFactory);
        spinnerHour.setEditable(true);

        // Setup minute spinner (0-59)
        SpinnerValueFactory<Integer> minuteFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0);
        spinnerMinute.setValueFactory(minuteFactory);
        spinnerMinute.setEditable(true);

        // Add text formatter for hour spinner to enforce numeric input with proper range
        TextFormatter<Integer> hourFormatter = new TextFormatter<>(hourFactory.getConverter(), hourFactory.getValue());
        spinnerHour.getEditor().setTextFormatter(hourFormatter);
        hourFactory.valueProperty().bindBidirectional(hourFormatter.valueProperty());

        // Add text formatter for minute spinner to enforce numeric input with proper range
        TextFormatter<Integer> minuteFormatter = new TextFormatter<>(minuteFactory.getConverter(), minuteFactory.getValue());
        spinnerMinute.getEditor().setTextFormatter(minuteFormatter);
        minuteFactory.valueProperty().bindBidirectional(minuteFormatter.valueProperty());

        // Add validation on focus lost for hour
        spinnerHour.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                commitEditorText(spinnerHour);
            }
        });

        // Add validation on focus lost for minute
        spinnerMinute.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                commitEditorText(spinnerMinute);
            }
        });
    }

    /**
     * Commits the editor text of a spinner, validating and correcting if necessary.
     */
    private void commitEditorText(Spinner<Integer> spinner) {
        try {
            String text = spinner.getEditor().getText();
            int value = Integer.parseInt(text);
            SpinnerValueFactory<Integer> factory = spinner.getValueFactory();

            // Check bounds
            if (value < ((SpinnerValueFactory.IntegerSpinnerValueFactory) factory).getMin()) {
                value = ((SpinnerValueFactory.IntegerSpinnerValueFactory) factory).getMin();
            } else if (value > ((SpinnerValueFactory.IntegerSpinnerValueFactory) factory).getMax()) {
                value = ((SpinnerValueFactory.IntegerSpinnerValueFactory) factory).getMax();
            }

            factory.setValue(value);
            spinner.getEditor().setText(String.format("%02d", value));
        } catch (NumberFormatException e) {
            // If invalid, reset to current value
            spinner.getEditor().setText(String.format("%02d", spinner.getValue()));
        }
    }

    /**
     * Sets up listeners to update service costs dynamically.
     */
    private void setupServiceCostUpdates() {
        chkInsurance.selectedProperty().addListener((obs, old, selected) -> updateServiceCosts());
        chkPriority.selectedProperty().addListener((obs, old, selected) -> updateServiceCosts());
    }

    /**
     * Sets up the vehicle selector ComboBox with all vehicle types and automatic selection logic.
     */
    private void setupVehicleSelector() {
        // Populate vehicle types
        cmbVehicleType.setItems(FXCollections.observableArrayList(VehicleType.values()));

        // Custom cell factory for display
        cmbVehicleType.setCellFactory(lv -> new ListCell<VehicleType>() {
            @Override
            protected void updateItem(VehicleType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(VehicleSelector.getCapacityInfo(item));
                }
            }
        });

        // Custom button cell
        cmbVehicleType.setButtonCell(new ListCell<VehicleType>() {
            @Override
            protected void updateItem(VehicleType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-text-fill: #4a5568 !important; -fx-font-weight: normal;");
                } else {
                    setText(VehicleSelector.getVehicleTypeName(item));
                    setStyle("-fx-text-fill: #1a202c !important; -fx-font-weight: bold;");
                }
            }
        });

        // Listener for automatic vehicle recommendation when weight/volume changes
        txtWeight.textProperty().addListener((obs, old, newVal) -> updateVehicleRecommendation());
        txtHeight.textProperty().addListener((obs, old, newVal) -> updateVehicleRecommendation());
        txtWidth.textProperty().addListener((obs, old, newVal) -> updateVehicleRecommendation());
        txtLength.textProperty().addListener((obs, old, newVal) -> updateVehicleRecommendation());

        // Listener for service changes (priority/fragile affects vehicle selection)
        chkPriority.selectedProperty().addListener((obs, old, selected) -> updateVehicleRecommendation());
        chkFragile.selectedProperty().addListener((obs, old, selected) -> updateVehicleRecommendation());

        // Listener for manual vehicle selection - validate compatibility
        cmbVehicleType.valueProperty().addListener((obs, old, newVal) -> validateVehicleSelection());
    }

    /**
     * Updates the vehicle recommendation based on current package weight, volume, and services.
     */
    private void updateVehicleRecommendation() {
        try {
            double weight = parseDouble(txtWeight.getText());
            double volume = calculateVolume();
            boolean isPriority = chkPriority.isSelected();
            boolean isFragile = chkFragile.isSelected();

            if (weight <= 0 || volume <= 0) {
                lblVehicleRecommendation.setText("");
                return;
            }

            // Get recommended vehicle
            VehicleType recommended = VehicleSelector.selectVehicleType(weight, volume, isPriority, isFragile);

            // Set recommended vehicle as selected
            cmbVehicleType.setValue(recommended);

            // Show recommendation reason
            String reason = VehicleSelector.getRecommendationReason(recommended, isPriority, isFragile);
            lblVehicleRecommendation.setText(reason);
            lblVehicleRecommendation.setStyle("-fx-text-fill: #28a745; -fx-font-weight: normal;");

        } catch (Exception e) {
            lblVehicleRecommendation.setText("");
        }
    }

    /**
     * Validates the manually selected vehicle type against package constraints and services.
     */
    private void validateVehicleSelection() {
        VehicleType selected = cmbVehicleType.getValue();
        if (selected == null) {
            lblVehicleValidation.setText("");
            return;
        }

        try {
            double weight = parseDouble(txtWeight.getText());
            double volume = calculateVolume();
            boolean isPriority = chkPriority.isSelected();
            boolean isFragile = chkFragile.isSelected();

            if (weight <= 0 || volume <= 0) {
                lblVehicleValidation.setText("");
                return;
            }

            // Get comprehensive validation
            String error = VehicleSelector.getComprehensiveValidation(selected, weight, volume, isPriority, isFragile);

            if (error != null) {
                lblVehicleValidation.setText(error);
                lblVehicleValidation.setStyle("-fx-text-fill: #d9534f; -fx-font-weight: bold;");
            } else {
                lblVehicleValidation.setText("✓ Vehículo compatible con el envío");
                lblVehicleValidation.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
            }

        } catch (Exception e) {
            lblVehicleValidation.setText("");
        }
    }

    /**
     * Calculates the volume from height, width, and length fields.
     * @return volume in cubic meters
     */
    private double calculateVolume() {
        try {
            double height = parseDouble(txtHeight.getText()) / 100.0; // cm to m
            double width = parseDouble(txtWidth.getText()) / 100.0;
            double length = parseDouble(txtLength.getText()) / 100.0;
            return height * width * length;
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * Updates the service cost labels based on current quote.
     */
    private void updateServiceCosts() {
        // These will be calculated properly when quote is generated in step 5
        // For now, just show the base amounts
        lblInsuranceCost.setText(chkInsurance.isSelected() ? "5% del costo base" : "$0");
        lblPriorityCost.setText(chkPriority.isSelected() ? "15% del costo base" : "$0");
    }

    /**
     * Sets the IndexController reference for navigation.
     */
    public void setIndexController(IndexController indexController) {
        this.indexController = indexController;
    }

    /**
     * Enables the cancel button (called when coming from MyShipments view).
     */
    public void enableCancelButton() {
        this.comesFromMyShipments = true;
        if (btnCancel != null) {
            btnCancel.setVisible(true);
            btnCancel.setManaged(true);
        }
    }

    // =================================================================================================================
    // Navigation
    // =================================================================================================================

    /**
     * Handles the Next button click.
     */
    @FXML
    private void handleNext() {
        if (!validateCurrentStep()) {
            return;
        }

        currentStep++;
        updateStepDisplay();
        updateStepIndicators();
        updateNavigationButtons();

        // Calculate quote when reaching step 5
        if (currentStep == 5) {
            populateSummary();
            calculateQuote();
        }
    }

    /**
     * Handles the Previous button click.
     */
    @FXML
    private void handlePrevious() {
        currentStep--;
        updateStepDisplay();
        updateStepIndicators();
        updateNavigationButtons();
    }

    /**
     * Handles the Confirm button click - shows payment options dialog.
     */
    @FXML
    private void handleConfirm() {
        if (currentQuote == null) {
            DialogUtil.showError("Error", "No se pudo calcular la cotización");
            return;
        }

        // Show dialog with payment options
        Alert paymentOptions = new Alert(Alert.AlertType.CONFIRMATION);
        paymentOptions.setTitle("Opciones de Pago");
        paymentOptions.setHeaderText("¿Cómo desea proceder con el pago?");
        paymentOptions.setContentText("Seleccione una opción:");

        // Apply custom styling to the dialog
        DialogPane dialogPane = paymentOptions.getDialogPane();
        dialogPane.getStylesheets().add(
            getClass().getResource("/co/edu/uniquindio/poo/ProyectoFinal2025_2/Style.css").toExternalForm()
        );
        dialogPane.getStyleClass().add("custom-dialog");

        ButtonType btnPayNow = new ButtonType("Continuar con el Pago");
        ButtonType btnPayLater = new ButtonType("Pagar Después");
        ButtonType btnCancel = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);

        paymentOptions.getButtonTypes().setAll(btnPayNow, btnPayLater, btnCancel);

        // Style the buttons
        dialogPane.lookupButton(btnPayNow).getStyleClass().addAll("btn-primary");
        dialogPane.lookupButton(btnPayLater).getStyleClass().addAll("btn-secondary");
        dialogPane.lookupButton(btnCancel).getStyleClass().addAll("btn-danger");

        paymentOptions.showAndWait().ifPresent(response -> {
            if (response == btnPayNow) {
                handlePayNow();
            } else if (response == btnPayLater) {
                handlePayLater();
            }
            // If cancel, do nothing
        });
    }

    /**
     * Handles "Continuar con el Pago" - creates order and opens payment window.
     */
    private void handlePayNow() {
        try {
            openCheckoutWindow();
        } catch (Exception e) {
            Logger.error("Error opening checkout: " + e.getMessage());
            e.printStackTrace();
            DialogUtil.showError("Error", "Error al proceder al checkout: " + e.getMessage());
        }
    }

    /**
     * Handles "Pagar Después" - creates order in AWAITING_PAYMENT status and shows in MyShipments.
     */
    private void handlePayLater() {
        try {
            var currentPerson = authService.getCurrentPerson();
            if (currentPerson == null || !(currentPerson instanceof User)) {
                DialogUtil.showError("Error", "Usuario no autenticado");
                return;
            }

            User currentUser = (User) currentPerson;

            // Build addresses
            Address origin = buildOriginAddress();
            Address destination = buildDestinationAddress();

            // Build OrderDetailDTO (same as openCheckoutWindow)
            OrderDetailDTO orderDetail = buildOrderDetail(currentUser, origin, destination);

            // Create the order in AWAITING_PAYMENT status
            Order createdOrder = orderService.initiateOrderCreation(
                orderDetail.getUserId(),
                orderDetail.getOrigin(),
                orderDetail.getDestination()
            );

            Logger.info("Order created with ID: " + createdOrder.getId() + " in AWAITING_PAYMENT status");

            // Show success message
            DialogUtil.showSuccess("Orden Creada",
                "Su orden ha sido creada exitosamente.\n\n" +
                "ID de Orden: " + createdOrder.getId() + "\n" +
                "Estado: Pendiente de Pago\n\n" +
                "Puede completar el pago desde 'Mis Envíos'.");

            // Navigate to MyShipments and filter by this order
            navigateToMyShipmentsWithOrder(createdOrder.getId());

        } catch (Exception e) {
            Logger.error("Error creating order for pay later: " + e.getMessage());
            e.printStackTrace();
            DialogUtil.showError("Error", "Error al crear la orden: " + e.getMessage());
        }
    }

    /**
     * Handles the Cancel button click - resets wizard to step 1 and clears all data.
     */
    @FXML
    private void handleCancelWizard() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancelar Creación de Envío");
        confirm.setHeaderText("¿Desea cancelar la creación del envío?");
        confirm.setContentText("Se perderán todos los datos ingresados y regresará al paso 1.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                resetWizard();
                Logger.info("Wizard cancelled and reset to step 1");
            }
        });
    }

    /**
     * Handles the Cancel button click.
     * Navigates back to the dashboard instead of closing the application.
     */
    @FXML
    private void handleCancel() {
        try {
            Logger.info("Cancel button clicked in CreateShipmentWizard");

            // Check if comes from MyShipments
            if (comesFromMyShipments && indexController != null) {
                Logger.info("Navigating back to MyShipments");
                co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.NavigationUtil.navigate(
                    indexController,
                    "MyShipments.fxml",
                    MyShipmentsController.class
                );
            } else if (indexController != null) {
                // We're embedded in dashboard - navigate back
                var currentPerson = authService.getCurrentPerson();
                if (currentPerson instanceof User) {
                    Logger.info("Navigating back to UserDashboard");
                    indexController.loadView("UserDashboard.fxml");
                } else {
                    Logger.info("Navigating back to AdminDashboard");
                    indexController.loadView("AdminDashboard.fxml");
                }
            } else {
                // We're in standalone mode - close the window
                Logger.info("Closing standalone wizard window");
                if (step1Form != null && step1Form.getScene() != null) {
                    Stage stage = (Stage) step1Form.getScene().getWindow();
                    if (stage != null) {
                        stage.close();
                    }
                }
            }
        } catch (Exception e) {
            Logger.error("Error handling cancel in CreateShipmentWizard: " + e.getMessage());
        }
    }

    /**
     * Updates which step form is visible.
     */
    private void updateStepDisplay() {
        step1Form.setVisible(currentStep == 1);
        step1Form.setManaged(currentStep == 1);

        step2Form.setVisible(currentStep == 2);
        step2Form.setManaged(currentStep == 2);

        step3Form.setVisible(currentStep == 3);
        step3Form.setManaged(currentStep == 3);

        step4Form.setVisible(currentStep == 4);
        step4Form.setManaged(currentStep == 4);

        step5Form.setVisible(currentStep == 5);
        step5Form.setManaged(currentStep == 5);
    }

    /**
     * Updates step indicator visual states.
     */
    private void updateStepIndicators() {
        updateStepIndicator(step1Container, 1);
        updateStepIndicator(step2Container, 2);
        updateStepIndicator(step3Container, 3);
        updateStepIndicator(step4Container, 4);
        updateStepIndicator(step5Container, 5);
    }

    /**
     * Updates a single step indicator.
     */
    private void updateStepIndicator(VBox container, int stepNumber) {
        StackPane circle = (StackPane) container.getChildren().get(0);

        if (stepNumber == currentStep) {
            // Active step
            circle.getStyleClass().clear();
            circle.getStyleClass().add("step-circle-active");
            container.setOpacity(1.0);
        } else if (stepNumber < currentStep) {
            // Completed step
            circle.getStyleClass().clear();
            circle.getStyleClass().add("step-circle-completed");
            container.setOpacity(1.0);
        } else {
            // Future step
            circle.getStyleClass().clear();
            circle.getStyleClass().add("step-circle-inactive");
            container.setOpacity(0.5);
        }
    }

    /**
     * Updates navigation button visibility and text.
     */
    private void updateNavigationButtons() {
        // Previous button
        btnPrevious.setVisible(currentStep > 1);
        btnPrevious.setManaged(currentStep > 1);

        // Next button
        btnNext.setVisible(currentStep < 5);
        btnNext.setManaged(currentStep < 5);

        // Confirm button
        btnConfirm.setVisible(currentStep == 5);
        btnConfirm.setManaged(currentStep == 5);
    }

    // =================================================================================================================
    // Validation
    // =================================================================================================================

    /**
     * Validates the current step.
     */
    private boolean validateCurrentStep() {
        clearErrors();

        switch (currentStep) {
            case 1:
                return validateStep1();
            case 2:
                return validateStep2();
            case 3:
                return validateStep3();
            case 4:
                return validateStep4();
            default:
                return true;
        }
    }

    /**
     * Validates step 1 (package details).
     */
    private boolean validateStep1() {
        boolean isValid = true;

        double weight = parseDouble(txtWeight.getText());
        if (weight <= 0) {
            showError(lblWeightError, "El peso debe ser mayor a 0");
            isValid = false;
        }

        double height = parseDouble(txtHeight.getText());
        if (height <= 0) {
            showError(lblHeightError, "El alto debe ser mayor a 0");
            isValid = false;
        }

        double width = parseDouble(txtWidth.getText());
        if (width <= 0) {
            showError(lblWidthError, "El ancho debe ser mayor a 0");
            isValid = false;
        }

        double length = parseDouble(txtLength.getText());
        if (length <= 0) {
            showError(lblLengthError, "El largo debe ser mayor a 0");
            isValid = false;
        }

        return isValid;
    }

    /**
     * Validates step 2 (origin address).
     */
    private boolean validateStep2() {
        boolean isValid = true;

        if (isEmpty(txtOriginStreet)) {
            showError(lblOriginStreetError, "La dirección es requerida");
            isValid = false;
        }

        if (isEmpty(txtOriginCity)) {
            showError(lblOriginCityError, "La ciudad es requerida");
            isValid = false;
        }

        return isValid;
    }

    /**
     * Validates step 3 (destination address).
     */
    private boolean validateStep3() {
        boolean isValid = true;

        if (isEmpty(txtDestinationStreet)) {
            showError(lblDestinationStreetError, "La dirección es requerida");
            isValid = false;
        }

        if (isEmpty(txtDestinationCity)) {
            showError(lblDestinationCityError, "La ciudad es requerida");
            isValid = false;
        }

        return isValid;
    }

    /**
     * Validates step 4 (additional services).
     */
    private boolean validateStep4() {
        // All fields are optional in step 4
        return true;
    }

    /**
     * Shows an error message on a label.
     */
    private void showError(Label label, String message) {
        if (label != null) {
            label.setText(message);
            label.setVisible(true);
            label.setManaged(true);
        }
    }

    /**
     * Clears all error labels.
     */
    private void clearErrors() {
        if (lblWeightError != null) lblWeightError.setVisible(false);
        if (lblHeightError != null) lblHeightError.setVisible(false);
        if (lblWidthError != null) lblWidthError.setVisible(false);
        if (lblLengthError != null) lblLengthError.setVisible(false);
        if (lblOriginStreetError != null) lblOriginStreetError.setVisible(false);
        if (lblOriginCityError != null) lblOriginCityError.setVisible(false);
        if (lblDestinationStreetError != null) lblDestinationStreetError.setVisible(false);
        if (lblDestinationCityError != null) lblDestinationCityError.setVisible(false);
    }

    // =================================================================================================================
    // Summary and Quote Calculation
    // =================================================================================================================

    /**
     * Populates the summary in step 5.
     */
    private void populateSummary() {
        // Package details
        lblSummaryWeight.setText(txtWeight.getText() + " kg");
        lblSummaryDimensions.setText(String.format("%s x %s x %s cm",
            txtWidth.getText(), txtLength.getText(), txtHeight.getText()));
        lblSummaryVolume.setText(lblVolumeCalculated.getText());

        // Addresses
        lblSummaryOrigin.setText(formatAddress(
            txtOriginStreet.getText(),
            txtOriginCity.getText(),
            txtOriginState.getText()
        ));

        lblSummaryDestination.setText(formatAddress(
            txtDestinationStreet.getText(),
            txtDestinationCity.getText(),
            txtDestinationState.getText()
        ));

        // Services
        List<String> services = new ArrayList<>();
        if (chkInsurance.isSelected()) services.add("🛡️ Seguro");
        if (chkFragile.isSelected()) services.add("📦 Frágil");
        if (chkSignature.isSelected()) services.add("✍️ Firma Requerida");
        if (chkPriority.isSelected()) services.add("⚡ Prioritario");

        if (services.isEmpty()) {
            lblSummaryServices.setText("Ninguno seleccionado");
        } else {
            lblSummaryServices.setText(String.join(", ", services));
        }
    }

    /**
     * Calculates the quote for the shipment.
     */
    private void calculateQuote() {
        try {
            // Build addresses
            Address origin = buildOriginAddress();
            Address destination = buildDestinationAddress();

            // Build quote DTO
            QuoteDTO quoteDTO = new QuoteDTO();
            quoteDTO.setOrigin(origin);
            quoteDTO.setDestination(destination);
            quoteDTO.setWeightKg(parseDouble(txtWeight.getText()));
            quoteDTO.setHeightCm(parseDouble(txtHeight.getText()));
            quoteDTO.setWidthCm(parseDouble(txtWidth.getText()));
            quoteDTO.setLengthCm(parseDouble(txtLength.getText()));
            quoteDTO.setPriority(3); // Default priority

            // Add selected services
            List<ServiceType> services = new ArrayList<>();
            if (chkInsurance.isSelected()) services.add(ServiceType.INSURANCE);
            if (chkFragile.isSelected()) services.add(ServiceType.FRAGILE);
            if (chkSignature.isSelected()) services.add(ServiceType.SIGNATURE_REQUIRED);
            if (chkPriority.isSelected()) services.add(ServiceType.PRIORITY);
            quoteDTO.setAdditionalServices(services);

            // Set pickup date
            quoteDTO.setRequestedPickupDate(getPickupDateTime());

            // Calculate quote
            currentQuote = shipmentService.quoteShipment(quoteDTO);

            // Update cost labels
            lblQuoteBaseCost.setText(formatCurrency(currentQuote.getBaseCost()));
            lblQuoteDistanceCost.setText(formatCurrency(currentQuote.getDistanceCost()));
            lblQuoteWeightCost.setText(formatCurrency(currentQuote.getWeightCost()));
            lblQuoteVolumeCost.setText(formatCurrency(currentQuote.getVolumeCost()));
            lblQuoteServicesCost.setText(formatCurrency(currentQuote.getServicesCost() + currentQuote.getPriorityCost()));
            lblQuoteTotalCost.setText(formatCurrency(currentQuote.getTotalCost()));

            if (currentQuote.getEstimatedDelivery() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                lblEstimatedDelivery.setText("Fecha estimada de entrega: " +
                    currentQuote.getEstimatedDelivery().format(formatter));
            }

        } catch (Exception e) {
            Logger.error("Error calculating quote: " + e.getMessage());
            DialogUtil.showError("Error", "Error al calcular la cotización: " + e.getMessage());
        }
    }

    // =================================================================================================================
    // Checkout
    // =================================================================================================================

    /**
     * Opens the checkout window with the order details.
     */
    /**
     * Builds OrderDetailDTO from current wizard data.
     */
    private OrderDetailDTO buildOrderDetail(User currentUser, Address origin, Address destination) {
        OrderDetailDTO orderDetail = new OrderDetailDTO();
        orderDetail.setUserId(currentUser.getId());
        orderDetail.setOrigin(origin);
        orderDetail.setDestination(destination);

        // Package details
        double height = parseDouble(txtHeight.getText());
        double width = parseDouble(txtWidth.getText());
        double length = parseDouble(txtLength.getText());
        orderDetail.setWeightKg(parseDouble(txtWeight.getText()));
        orderDetail.setHeightCm(height);
        orderDetail.setWidthCm(width);
        orderDetail.setLengthCm(length);
        orderDetail.setVolumeM3((height * width * length) / 1000000.0);

        // Shipment details
        orderDetail.setPriority(3);
        orderDetail.setUserNotes(txtNotes.getText());
        orderDetail.setRequestedPickupDate(getPickupDateTime());
        orderDetail.setEstimatedDelivery(currentQuote.getEstimatedDelivery());

        // Services
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
        orderDetail.setAdditionalServices(services);

        // Cost breakdown
        orderDetail.setBaseCost(currentQuote.getBaseCost());
        orderDetail.setDistanceCost(currentQuote.getDistanceCost());
        orderDetail.setWeightCost(currentQuote.getWeightCost());
        orderDetail.setVolumeCost(currentQuote.getVolumeCost());
        orderDetail.setServicesCost(currentQuote.getServicesCost());
        orderDetail.setPriorityCost(currentQuote.getPriorityCost());
        orderDetail.setTotalCost(currentQuote.getTotalCost());

        // Distance
        orderDetail.setDistanceKm(DistanceCalculator.calculateDistance(origin, destination));

        return orderDetail;
    }

    private void openCheckoutWindow() {
        try {
            var currentPerson = authService.getCurrentPerson();
            if (currentPerson == null || !(currentPerson instanceof User)) {
                DialogUtil.showError("Error", "Usuario no autenticado");
                return;
            }

            User currentUser = (User) currentPerson;

            // Build addresses
            Address origin = buildOriginAddress();
            Address destination = buildDestinationAddress();

            // Build OrderDetailDTO using new method
            OrderDetailDTO orderDetail = buildOrderDetail(currentUser, origin, destination);

            // Create the order directly (skip Checkout popup as per user request)
            Order createdOrder = orderService.initiateOrderCreation(
                orderDetail.getUserId(),
                orderDetail.getOrigin(),
                orderDetail.getDestination()
            );

            Logger.info("Order created with ID: " + createdOrder.getId());

            // Open payment window directly
            // The wizard will close after payment is completed or cancelled
            openPaymentWindow(createdOrder, orderDetail);

        } catch (Exception e) {
            Logger.error("Error opening checkout window: " + e.getMessage());
            e.printStackTrace();
            DialogUtil.showError("Error", "Error al abrir ventana de checkout: " + e.getMessage());
        }
    }

    /**
     * Opens the payment processor selection view in the Index (not as a popup).
     * User can choose between:
     * - Mercado Pago (real payment processing with WebView)
     * - App Payment (simulated payment)
     */
    private void openPaymentWindow(Order createdOrder, OrderDetailDTO orderDetail) {
        try {
            if (indexController == null) {
                Logger.error("IndexController not set, cannot open payment selection");
                DialogUtil.showError("Error", "No se puede proceder al pago en este momento");
                return;
            }

            // Load payment processor selection view in the Index
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "/co/edu/uniquindio/poo/ProyectoFinal2025_2/View/PaymentProcessorSelection.fxml"));

            // Load the view
            indexController.getContentArea().getChildren().clear();
            indexController.getContentArea().getChildren().add(loader.load());

            // Get controller and pass necessary data
            PaymentProcessorSelectionController selectionController = loader.getController();
            selectionController.setIndexController(indexController);
            selectionController.setWizardController(this);
            selectionController.setOrder(createdOrder, orderDetail);

            Logger.info("Payment processor selection view loaded in Index");

        } catch (Exception e) {
            Logger.error("Error opening payment selection view: " + e.getMessage());
            e.printStackTrace();
            DialogUtil.showError("Error", "Error al abrir selección de pago: " + e.getMessage());
        }
    }

    /**
     * Returns to the last step of the wizard (step 5 - review).
     */
    public void returnToLastStep() {
        currentStep = 5;
        updateStepDisplay();
        updateStepIndicators();
        updateNavigationButtons();
        Logger.info("Returned to wizard last step (review)");
    }

    /**
     * Navigates to MyShipments and filters by the given order ID.
     */
    private void navigateToMyShipmentsWithOrder(String orderId) {
        if (indexController == null) {
            Logger.warning("IndexController not set, cannot navigate to MyShipments");
            return;
        }

        try {
            Logger.info("Navigating to MyShipments with filter for order: " + orderId);

            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/co/edu/uniquindio/poo/ProyectoFinal2025_2/View/MyShipments.fxml")
            );

            indexController.getContentArea().getChildren().clear();
            indexController.getContentArea().getChildren().add(loader.load());

            MyShipmentsController controller = loader.getController();
            controller.filterByOrderId(orderId);

            Logger.info("Successfully navigated to MyShipments with order filter: " + orderId);
        } catch (Exception e) {
            Logger.error("Error navigating to MyShipments: " + e.getMessage());
            e.printStackTrace();
            // Fallback to simple navigation
            indexController.loadView("MyShipments.fxml");
        }
    }

    /**
     * Resets the wizard to step 1 and clears all form data.
     */
    public void resetWizard() {
        // Reset to step 1
        currentStep = 1;
        updateStepDisplay();
        updateStepIndicators();
        updateNavigationButtons();

        // Clear all form fields
        clearAllFormFields();

        // Reset quote
        currentQuote = null;

        Logger.info("Wizard reset to step 1, all data cleared");
    }

    /**
     * Clears all form fields in the wizard.
     */
    private void clearAllFormFields() {
        // Step 1: Package details
        txtWeight.clear();
        txtHeight.clear();
        txtWidth.clear();
        txtLength.clear();
        lblVolumeCalculated.setText("0.00 m³");

        // Step 2: Origin address
        txtOriginAlias.clear();
        txtOriginStreet.clear();
        txtOriginCity.clear();
        txtOriginState.clear();
        txtOriginCountry.setText("Colombia");
        txtOriginZipCode.clear();
        txtOriginLatitude.clear();
        txtOriginLongitude.clear();
        if (cmbOriginSavedAddresses != null) {
            cmbOriginSavedAddresses.getSelectionModel().clearSelection();
        }

        // Step 3: Destination address
        txtDestinationAlias.clear();
        txtDestinationStreet.clear();
        txtDestinationCity.clear();
        txtDestinationState.clear();
        txtDestinationCountry.setText("Colombia");
        txtDestinationZipCode.clear();
        txtDestinationLatitude.clear();
        txtDestinationLongitude.clear();
        if (cmbDestinationSavedAddresses != null) {
            cmbDestinationSavedAddresses.getSelectionModel().clearSelection();
        }

        // Step 4: Pickup & Additional Services
        if (datePickup != null) {
            datePickup.setValue(LocalDate.now().plusDays(1));
        }
        if (spinnerHour != null) {
            spinnerHour.getValueFactory().setValue(12);
        }
        if (spinnerMinute != null) {
            spinnerMinute.getValueFactory().setValue(0);
        }
        if (chkFragile != null) chkFragile.setSelected(false);
        if (chkInsurance != null) chkInsurance.setSelected(false);
        if (chkSignature != null) chkSignature.setSelected(false);
        if (chkPriority != null) chkPriority.setSelected(false);

        // Clear errors
        clearErrors();
    }

    // =================================================================================================================
    // Helper Methods
    // =================================================================================================================

    /**
     * Builds an Address object from origin form fields.
     */
    private Address buildOriginAddress() {
        return new Address.Builder()
            .withId(UUID.randomUUID().toString())
            .withAlias(txtOriginAlias.getText().trim().isEmpty() ? null : txtOriginAlias.getText().trim())
            .withStreet(txtOriginStreet.getText().trim())
            .withCity(txtOriginCity.getText().trim())
            .withState(txtOriginState.getText().trim().isEmpty() ? null : txtOriginState.getText().trim())
            .withCountry(txtOriginCountry.getText().trim().isEmpty() ? "Colombia" : txtOriginCountry.getText().trim())
            .withZipCode(txtOriginZipCode.getText().trim().isEmpty() ? null : txtOriginZipCode.getText().trim())
            .withLatitude(parseDoubleOrNull(txtOriginLatitude.getText()))
            .withLongitude(parseDoubleOrNull(txtOriginLongitude.getText()))
            .build();
    }

    /**
     * Builds an Address object from destination form fields.
     */
    private Address buildDestinationAddress() {
        return new Address.Builder()
            .withId(UUID.randomUUID().toString())
            .withAlias(txtDestinationAlias.getText().trim().isEmpty() ? null : txtDestinationAlias.getText().trim())
            .withStreet(txtDestinationStreet.getText().trim())
            .withCity(txtDestinationCity.getText().trim())
            .withState(txtDestinationState.getText().trim().isEmpty() ? null : txtDestinationState.getText().trim())
            .withCountry(txtDestinationCountry.getText().trim().isEmpty() ? "Colombia" : txtDestinationCountry.getText().trim())
            .withZipCode(txtDestinationZipCode.getText().trim().isEmpty() ? null : txtDestinationZipCode.getText().trim())
            .withLatitude(parseDoubleOrNull(txtDestinationLatitude.getText()))
            .withLongitude(parseDoubleOrNull(txtDestinationLongitude.getText()))
            .build();
    }

    /**
     * Gets the pickup date-time from the form.
     */
    private LocalDateTime getPickupDateTime() {
        LocalDate date = datePickup.getValue();
        if (date == null) {
            date = LocalDate.now().plusDays(1);
        }

        int hour = spinnerHour.getValue() != null ? spinnerHour.getValue() : 12;
        int minute = spinnerMinute.getValue() != null ? spinnerMinute.getValue() : 0;

        return LocalDateTime.of(date, LocalTime.of(hour, minute));
    }

    /**
     * Formats an address for display.
     */
    private String formatAddress(String street, String city, String state) {
        StringBuilder sb = new StringBuilder();
        if (street != null && !street.isEmpty()) {
            sb.append(street);
        }
        if (city != null && !city.isEmpty()) {
            if (sb.length() > 0) sb.append("\n");
            sb.append(city);
        }
        if (state != null && !state.isEmpty()) {
            sb.append(", ").append(state);
        }
        return sb.toString();
    }

    /**
     * Checks if a text field is empty.
     */
    private boolean isEmpty(TextField field) {
        return field == null || field.getText() == null || field.getText().trim().isEmpty();
    }

    /**
     * Parses a double from text, returns 0 if invalid.
     */
    private double parseDouble(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
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

    /**
     * Formats a value as currency.
     */
    private String formatCurrency(double value) {
        return String.format("$%,.0f", value);
    }
}
