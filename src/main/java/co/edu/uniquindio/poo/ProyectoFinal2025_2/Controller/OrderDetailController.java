package co.edu.uniquindio.poo.ProyectoFinal2025_2.Controller;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.DeliveryPerson;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Order;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Payment;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.User;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.dto.ShipmentDTO;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Repositories.DeliveryPersonRepository;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Repositories.OrderRepository;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Repositories.PaymentRepository;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Repositories.UserRepository;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Services.ShipmentService;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilController.DialogUtil;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Controller for order details view.
 * Shows complete information about an order including related shipment, payment, and delivery person.
 */
public class OrderDetailController implements Initializable {

    // General Information
    @FXML private Label lblId;
    @FXML private Label lblStatus;
    @FXML private Label lblCreationDate;
    @FXML private Label lblShipmentId;
    @FXML private Label lblPaymentId;
    @FXML private Label lblInvoiceId;

    // User Information
    @FXML private Label lblUserName;
    @FXML private Label lblUserPhone;
    @FXML private Label lblUserEmail;

    // Addresses
    @FXML private Label lblOriginAddress;
    @FXML private Label lblOriginZone;
    @FXML private Label lblDestinationAddress;
    @FXML private Label lblDestinationZone;

    // Shipment Section
    @FXML private VBox shipmentSection;
    @FXML private Separator shipmentSeparator;
    @FXML private Button btnViewShipment;
    @FXML private Label lblShipmentStatus;
    @FXML private Label lblShipmentPriority;
    @FXML private Label lblWeight;
    @FXML private Label lblDistance;
    @FXML private Label lblEstimatedDate;
    @FXML private Label lblActualDate;

    // Payment Section
    @FXML private VBox paymentSection;
    @FXML private Separator paymentSeparator;
    @FXML private Label lblPaymentStatus;
    @FXML private Label lblPaymentMethod;
    @FXML private Label lblPaymentDate;
    @FXML private Label lblTotalAmount;

    // Delivery Person Section
    @FXML private VBox deliveryPersonSection;
    @FXML private Label lblDeliveryPersonName;
    @FXML private Label lblDeliveryPersonPhone;
    @FXML private Label lblDeliveryPersonZone;
    @FXML private Label lblDeliveryPersonDocumentId;

    // Repositories
    private final OrderRepository orderRepository = OrderRepository.getInstance();
    private final UserRepository userRepository = UserRepository.getInstance();
    private final PaymentRepository paymentRepository = PaymentRepository.getInstance();
    private final DeliveryPersonRepository deliveryPersonRepository = DeliveryPersonRepository.getInstance();

    // Services
    private final ShipmentService shipmentService = new ShipmentService();

    // Current order
    private Order currentOrder;
    private ShipmentDTO currentShipment;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Hide sections initially
        hideSection(shipmentSection, shipmentSeparator);
        hideSection(paymentSection, paymentSeparator);
        hideSection(deliveryPersonSection, null);

        Logger.info("OrderDetailController initialized");
    }

    /**
     * Loads and displays details for a specific order.
     * @param orderId Order ID
     */
    public void loadOrderDetails(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            DialogUtil.showError("Invalid ID", "Order ID is required");
            return;
        }

        Optional<Order> orderOpt = orderRepository.findById(orderId);

        if (!orderOpt.isPresent()) {
            DialogUtil.showError("Not Found", "Order with ID " + orderId + " not found");
            return;
        }

        currentOrder = orderOpt.get();
        displayOrderDetails();

        Logger.info("Loaded details for order: " + orderId);
    }

    /**
     * Displays all order details in the UI.
     */
    private void displayOrderDetails() {
        if (currentOrder == null) return;

        displayGeneralInfo();
        displayUserInfo();
        displayAddresses();
        displayShipmentInfo();
        displayPaymentInfo();
        displayDeliveryPersonInfo();
    }

    /**
     * Displays general order information.
     */
    private void displayGeneralInfo() {
        lblId.setText(currentOrder.getId());

        // Display status with color
        if (currentOrder.getStatus() != null) {
            lblStatus.setText(currentOrder.getStatus().getDisplayName());
            lblStatus.setStyle("-fx-text-fill: " + getStatusColor(currentOrder.getStatus().name()) + "; -fx-font-weight: bold;");
        }

        if (currentOrder.getCreatedAt() != null) {
            lblCreationDate.setText(currentOrder.getCreatedAt().format(DATE_FORMATTER));
        }

        lblShipmentId.setText(currentOrder.getShipmentId() != null ? currentOrder.getShipmentId() : "N/A");
        lblPaymentId.setText(currentOrder.getPaymentId() != null ? currentOrder.getPaymentId() : "N/A");
        lblInvoiceId.setText(currentOrder.getInvoiceId() != null ? currentOrder.getInvoiceId() : "N/A");
    }

    /**
     * Displays user information.
     */
    private void displayUserInfo() {
        if (currentOrder.getUserId() == null) return;

        Optional<User> userOpt = userRepository.findById(currentOrder.getUserId());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            lblUserName.setText(user.getName() + " " + user.getLastName());
            lblUserPhone.setText(user.getPhone() != null ? user.getPhone() : "N/A");
            lblUserEmail.setText(user.getEmail());
        } else {
            lblUserName.setText("Unknown User");
            lblUserPhone.setText("N/A");
            lblUserEmail.setText(currentOrder.getUserId());
        }
    }

    /**
     * Displays address information.
     */
    private void displayAddresses() {
        if (currentOrder.getOrigin() != null) {
            lblOriginAddress.setText(formatAddress(currentOrder.getOrigin()));
            lblOriginZone.setText("Zip Code: " + (currentOrder.getOrigin().getZipCode() != null ?
                currentOrder.getOrigin().getZipCode() : "N/A"));
        }

        if (currentOrder.getDestination() != null) {
            lblDestinationAddress.setText(formatAddress(currentOrder.getDestination()));
            lblDestinationZone.setText("Zip Code: " + (currentOrder.getDestination().getZipCode() != null ?
                currentOrder.getDestination().getZipCode() : "N/A"));
        }
    }

    /**
     * Displays shipment information if exists.
     */
    private void displayShipmentInfo() {
        if (currentOrder.getShipmentId() == null) {
            hideSection(shipmentSection, shipmentSeparator);
            return;
        }

        Optional<ShipmentDTO> shipmentOpt = shipmentService.getShipment(currentOrder.getShipmentId());
        if (!shipmentOpt.isPresent()) {
            hideSection(shipmentSection, shipmentSeparator);
            return;
        }

        currentShipment = shipmentOpt.get();

        lblShipmentStatus.setText(currentShipment.getStatusDisplayName());
        lblShipmentStatus.setStyle("-fx-text-fill: " + currentShipment.getStatusColor() + "; -fx-font-weight: bold;");
        lblShipmentPriority.setText(String.valueOf(currentShipment.getPriority()));
        lblWeight.setText(String.format("%.2f kg", currentShipment.getWeightKg()));
        lblDistance.setText(String.format("%.2f km", currentShipment.getDistanceKm()));

        if (currentShipment.getEstimatedDeliveryDate() != null) {
            lblEstimatedDate.setText(currentShipment.getEstimatedDeliveryDate().format(DATE_FORMATTER));
        } else {
            lblEstimatedDate.setText("--");
        }

        if (currentShipment.getActualDeliveryDate() != null) {
            lblActualDate.setText(currentShipment.getActualDeliveryDate().format(DATE_FORMATTER));
        } else {
            lblActualDate.setText("--");
        }

        showSection(shipmentSection, shipmentSeparator);
    }

    /**
     * Displays payment information if exists.
     */
    private void displayPaymentInfo() {
        if (currentOrder.getPaymentId() == null) {
            hideSection(paymentSection, paymentSeparator);
            return;
        }

        Optional<Payment> paymentOpt = paymentRepository.findById(currentOrder.getPaymentId());
        if (!paymentOpt.isPresent()) {
            hideSection(paymentSection, paymentSeparator);
            return;
        }

        Payment payment = paymentOpt.get();

        if (payment.getStatus() != null) {
            lblPaymentStatus.setText(payment.getStatus().getDisplayName());
            lblPaymentStatus.setStyle("-fx-text-fill: " + getPaymentStatusColor(payment.getStatus().name()) + "; -fx-font-weight: bold;");
        }

        if (payment.getPaymentMethod() != null && payment.getPaymentMethod().getType() != null) {
            String methodText = payment.getPaymentMethod().getType().name();
            if (payment.getPaymentMethod().getProvider() != null) {
                methodText += " - " + payment.getPaymentMethod().getProvider().name();
            }
            lblPaymentMethod.setText(methodText);
        } else {
            lblPaymentMethod.setText("N/A");
        }

        if (payment.getDate() != null) {
            lblPaymentDate.setText(payment.getDate().format(DATE_FORMATTER));
        } else {
            lblPaymentDate.setText("--");
        }

        lblTotalAmount.setText(String.format("$%,.2f", payment.getAmount()));

        showSection(paymentSection, paymentSeparator);
    }

    /**
     * Displays delivery person information if assigned.
     */
    private void displayDeliveryPersonInfo() {
        if (currentOrder.getDeliveryPersonId() == null) {
            hideSection(deliveryPersonSection, null);
            return;
        }

        DeliveryPerson deliveryPerson = deliveryPersonRepository.getDeliveryPersonById(currentOrder.getDeliveryPersonId());
        if (deliveryPerson == null) {
            hideSection(deliveryPersonSection, null);
            return;
        }

        lblDeliveryPersonName.setText(deliveryPerson.getName() + " " + deliveryPerson.getLastName());
        lblDeliveryPersonPhone.setText(deliveryPerson.getPhone() != null ? deliveryPerson.getPhone() : "N/A");
        lblDeliveryPersonZone.setText(deliveryPerson.getCoverageArea() != null ?
            deliveryPerson.getCoverageArea().getDisplayName() : "N/A");
        lblDeliveryPersonDocumentId.setText(deliveryPerson.getDocumentId() != null ?
            deliveryPerson.getDocumentId() : "N/A");

        showSection(deliveryPersonSection, null);
    }

    // ===========================
    // Button Handlers
    // ===========================

    @FXML
    private void handleBack() {
        Stage stage = (Stage) lblId.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handlePrint() {
        if (currentOrder == null) {
            DialogUtil.showWarning("Sin Datos", "No hay orden cargada para imprimir");
            return;
        }

        try {
            // Generate PDF with order details
            String fileName = "order_detail_" + currentOrder.getId() + "_" + System.currentTimeMillis();
            String title = "Detalles de la Orden";
            String subtitle = "ID: " + currentOrder.getId() + " | Estado: " + currentOrder.getStatus().getDisplayName();

            // Prepare headers and data
            List<String> headers = Arrays.asList("Campo", "Valor");
            List<List<String>> rows = new ArrayList<>();

            // General Information
            rows.add(Arrays.asList("ID Orden", currentOrder.getId()));
            rows.add(Arrays.asList("Estado", currentOrder.getStatus().getDisplayName()));
            rows.add(Arrays.asList("Fecha Creación", currentOrder.getCreatedAt() != null ?
                currentOrder.getCreatedAt().format(DATE_FORMATTER) : "N/A"));

            // User Information
            if (currentOrder.getUserId() != null) {
                co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.User user =
                    userRepository.findById(currentOrder.getUserId()).orElse(null);
                if (user != null) {
                    rows.add(Arrays.asList("Usuario", user.getName() + " " + user.getLastName()));
                    rows.add(Arrays.asList("Email", user.getEmail()));
                    rows.add(Arrays.asList("Teléfono", user.getPhone() != null ? user.getPhone() : "N/A"));
                }
            }

            // Addresses
            if (currentOrder.getOrigin() != null) {
                rows.add(Arrays.asList("Dirección Origen",
                    currentOrder.getOrigin().getStreet() + ", " +
                    currentOrder.getOrigin().getCity() + ", " +
                    currentOrder.getOrigin().getState()));
            }
            if (currentOrder.getDestination() != null) {
                rows.add(Arrays.asList("Dirección Destino",
                    currentOrder.getDestination().getStreet() + ", " +
                    currentOrder.getDestination().getCity() + ", " +
                    currentOrder.getDestination().getState()));
            }

            // IDs Relacionados
            rows.add(Arrays.asList("ID Envío", currentOrder.getShipmentId() != null ? currentOrder.getShipmentId() : "N/A"));
            rows.add(Arrays.asList("ID Pago", currentOrder.getPaymentId() != null ? currentOrder.getPaymentId() : "N/A"));
            rows.add(Arrays.asList("ID Factura", currentOrder.getInvoiceId() != null ? currentOrder.getInvoiceId() : "N/A"));

            // Generate PDF
            File pdfFile = co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.PdfUtility.generatePdfReport(
                fileName, title, subtitle, headers, rows);

            if (pdfFile != null && pdfFile.exists()) {
                DialogUtil.showSuccess("PDF Generado",
                    "El PDF se generó exitosamente:\n" + pdfFile.getAbsolutePath());
                Logger.info("Order detail PDF generated: " + pdfFile.getAbsolutePath());
            } else {
                DialogUtil.showError("Error", "No se pudo generar el PDF");
            }
        } catch (Exception e) {
            Logger.error("Error generating order detail PDF: " + e.getMessage());
            DialogUtil.showError("Error", "Error al generar PDF: " + e.getMessage());
        }
    }

    @FXML
    private void handleViewShipment() {
        if (currentOrder.getShipmentId() == null) {
            DialogUtil.showWarning("No Shipment", "This order does not have an associated shipment yet");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/co/edu/uniquindio/poo/ProyectoFinal2025_2/View/ShipmentDetail.fxml")
            );
            Parent root = loader.load();

            ShipmentDetailController controller = loader.getController();
            controller.loadShipmentDetails(currentOrder.getShipmentId());

            Stage stage = new Stage();
            stage.setTitle("Detalles del Envío");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root, 650, 800));
            stage.showAndWait();

        } catch (Exception e) {
            Logger.error("Error loading shipment details: " + e.getMessage());
            DialogUtil.showError("Error", "Failed to load shipment details: " + e.getMessage());
        }
    }

    // ===========================
    // Helper Methods
    // ===========================

    private void hideSection(VBox section, Separator separator) {
        if (section != null) {
            section.setVisible(false);
            section.setManaged(false);
        }
        if (separator != null) {
            separator.setVisible(false);
            separator.setManaged(false);
        }
    }

    private void showSection(VBox section, Separator separator) {
        if (section != null) {
            section.setVisible(true);
            section.setManaged(true);
        }
        if (separator != null) {
            separator.setVisible(true);
            separator.setManaged(true);
        }
    }

    private String formatAddress(co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Address address) {
        if (address == null) return "N/A";

        StringBuilder sb = new StringBuilder();
        if (address.getStreet() != null) sb.append(address.getStreet()).append(", ");
        if (address.getCity() != null) sb.append(address.getCity()).append(", ");
        if (address.getState() != null) sb.append(address.getState()).append(", ");
        if (address.getZipCode() != null) sb.append(address.getZipCode());

        return sb.toString().replaceAll(", $", "");
    }

    private String getStatusColor(String status) {
        switch (status) {
            case "AWAITING_PAYMENT":
            case "PENDING":
                return "#ffc107"; // Yellow
            case "APPROVED":
            case "PROCESSING":
                return "#17a2b8"; // Blue
            case "READY_FOR_SHIPMENT":
            case "IN_TRANSIT":
                return "#007bff"; // Blue
            case "DELIVERED":
            case "COMPLETED":
                return "#28a745"; // Green
            case "CANCELLED":
            case "REJECTED":
                return "#dc3545"; // Red
            default:
                return "#6c757d"; // Gray
        }
    }

    private String getPaymentStatusColor(String status) {
        switch (status) {
            case "PENDING":
                return "#ffc107"; // Yellow
            case "COMPLETED":
                return "#28a745"; // Green
            case "FAILED":
            case "REFUNDED":
                return "#dc3545"; // Red
            default:
                return "#6c757d"; // Gray
        }
    }
}
