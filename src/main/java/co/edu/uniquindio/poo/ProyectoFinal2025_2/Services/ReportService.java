package co.edu.uniquindio.poo.ProyectoFinal2025_2.Services;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.*;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.ShipmentStatus;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Repositories.*;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.CsvUtility;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service dedicated to generating different types of reports and statistics.
 */
public class ReportService {

    // Repositories
    private final OrderRepository orderRepository = OrderRepository.getInstance();
    private final UserRepository userRepository = UserRepository.getInstance();
    private final DeliveryPersonRepository deliveryPersonRepository = DeliveryPersonRepository.getInstance();
    private final InvoiceRepository invoiceRepository = InvoiceRepository.getInstance();
    private final PaymentRepository paymentRepository = PaymentRepository.getInstance();
    private final ShipmentRepository shipmentRepository = ShipmentRepository.getInstance();

    // Formatters
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ==================================================================================
    // GENERAL STATISTICS
    // ==================================================================================

    /**
     * Calculates total revenue for a given date range.
     */
    public double calculateTotalRevenue(LocalDate from, LocalDate to) {
        return invoiceRepository.findAll().stream()
                .filter(invoice -> isInDateRange(invoice.getIssuedAt(), from, to))
                .mapToDouble(Invoice::getTotalAmount)
                .sum();
    }

    /**
     * Counts total shipments in date range.
     */
    public long countTotalShipments(LocalDate from, LocalDate to) {
        return shipmentRepository.findAll().stream()
                .filter(shipment -> isInDateRange(shipment.getCreatedAt(), from, to))
                .count();
    }

    /**
     * Calculates success rate (delivered shipments / total shipments).
     */
    public double calculateSuccessRate(LocalDate from, LocalDate to) {
        List<Shipment> shipments = shipmentRepository.findAll().stream()
                .filter(shipment -> isInDateRange(shipment.getCreatedAt(), from, to))
                .toList();

        if (shipments.isEmpty()) return 0.0;

        long delivered = shipments.stream()
                .filter(s -> s.getStatus() == ShipmentStatus.DELIVERED)
                .count();

        return (delivered * 100.0) / shipments.size();
    }

    /**
     * Counts active users (users who made orders in date range).
     */
    public long countActiveUsers(LocalDate from, LocalDate to) {
        return orderRepository.findAll().stream()
                .filter(order -> isInDateRange(order.getCreatedAt(), from, to))
                .map(Order::getUserId)
                .distinct()
                .count();
    }

    /**
     * Calculates average delivery time in hours.
     */
    public double calculateAverageDeliveryTime(LocalDate from, LocalDate to) {
        List<Shipment> deliveredShipments = shipmentRepository.findAll().stream()
                .filter(shipment -> shipment.getStatus() == ShipmentStatus.DELIVERED)
                .filter(shipment -> shipment.getDeliveredDate() != null)
                .filter(shipment -> isInDateRange(shipment.getCreatedAt(), from, to))
                .toList();

        if (deliveredShipments.isEmpty()) return 0.0;

        double totalHours = deliveredShipments.stream()
                .mapToLong(shipment -> ChronoUnit.HOURS.between(
                        shipment.getCreatedAt(),
                        shipment.getDeliveredDate()
                ))
                .average()
                .orElse(0.0);

        return totalHours;
    }

    // ==================================================================================
    // CHART DATA METHODS
    // ==================================================================================

    /**
     * Gets daily revenue for the last N days.
     */
    public Map<String, Double> getDailyRevenue(int days) {
        Map<String, Double> dailyRevenue = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dateStr = date.format(DATE_FORMATTER);

            double revenue = invoiceRepository.findAll().stream()
                    .filter(invoice -> invoice.getIssuedAt().toLocalDate().equals(date))
                    .mapToDouble(Invoice::getTotalAmount)
                    .sum();

            dailyRevenue.put(dateStr, revenue);
        }

        return dailyRevenue;
    }

    /**
     * Gets shipments grouped by status.
     */
    public Map<String, Long> getShipmentsByStatus(LocalDate from, LocalDate to) {
        return shipmentRepository.findAll().stream()
                .filter(shipment -> isInDateRange(shipment.getCreatedAt(), from, to))
                .collect(Collectors.groupingBy(
                        shipment -> shipment.getStatus().getDisplayName(),
                        Collectors.counting()
                ));
    }

    /**
     * Gets top delivery personnel by number of deliveries.
     */
    public Map<String, Long> getTopDeliveryPersonnel(LocalDate from, LocalDate to, int limit) {
        return shipmentRepository.findAll().stream()
                .filter(shipment -> shipment.getStatus() == ShipmentStatus.DELIVERED)
                .filter(shipment -> isInDateRange(shipment.getCreatedAt(), from, to))
                .filter(s -> s.getDeliveryPersonId() != null)
                .collect(Collectors.groupingBy(
                        s -> {
                            DeliveryPerson dp = deliveryPersonRepository.findDeliveryPersonById(s.getDeliveryPersonId()).orElse(null);
                            return dp != null ? dp.getName() + " " + dp.getLastName() : "Desconocido";
                        },
                        Collectors.counting()
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    /**
     * Gets shipments grouped by vehicle type (used instead of coverage area).
     */
    public Map<String, Long> getShipmentsByCoverageArea(LocalDate from, LocalDate to) {
        return shipmentRepository.findAll().stream()
                .filter(shipment -> isInDateRange(shipment.getCreatedAt(), from, to))
                .collect(Collectors.groupingBy(
                        s -> s.getVehicleType() != null ? s.getVehicleType().name() : "SIN_ASIGNAR",
                        Collectors.counting()
                ));
    }

    // ==================================================================================
    // REPORT GENERATION METHODS (PLACEHOLDER)
    // ==================================================================================

    /**
     * Generates general report in PDF format.
     * TODO: Implement when PDF generation utilities are ready.
     */
    public File generateGeneralReportPDF(LocalDate from, LocalDate to) {
        Logger.info("PDF generation not yet implemented - using CSV alternative");
        return generateGeneralReportCSV(from, to);
    }

    /**
     * Generates general report in CSV format.
     */
    public File generateGeneralReportCSV(LocalDate from, LocalDate to) {
        try {
            String fileName = "reportes/general_report_" + from + "_to_" + to + ".csv";

            List<String> headers = Arrays.asList(
                "Métrica", "Valor"
            );

            List<List<String>> rows = new ArrayList<>();
            rows.add(Arrays.asList("Total de Envíos", String.valueOf(countTotalShipments(from, to))));
            rows.add(Arrays.asList("Ingresos Totales", String.format("$%.2f", calculateTotalRevenue(from, to))));
            rows.add(Arrays.asList("Tasa de Éxito", String.format("%.2f%%", calculateSuccessRate(from, to))));
            rows.add(Arrays.asList("Usuarios Activos", String.valueOf(countActiveUsers(from, to))));
            rows.add(Arrays.asList("Tiempo Promedio de Entrega (horas)", String.format("%.2f", calculateAverageDeliveryTime(from, to))));

            return CsvUtility.writeCSV(fileName, headers, rows);
        } catch (Exception e) {
            Logger.error("Error generating general CSV report: " + e.getMessage());
            return null;
        }
    }

    /**
     * Generates financial report in PDF format.
     * TODO: Implement when PDF generation utilities are ready.
     */
    public File generateFinancialReportPDF(LocalDate from, LocalDate to) {
        Logger.info("PDF generation not yet implemented - using CSV alternative");
        return generateFinancialReportCSV(from, to);
    }

    /**
     * Generates financial report in CSV format.
     */
    public File generateFinancialReportCSV(LocalDate from, LocalDate to) {
        try {
            String fileName = "reportes/financial_report_" + from + "_to_" + to + ".csv";

            List<String> headers = Arrays.asList(
                "ID Factura", "ID Orden", "Usuario", "Monto Total", "Fecha Emisión"
            );

            List<List<String>> rows = new ArrayList<>();
            invoiceRepository.findAll().stream()
                    .filter(invoice -> isInDateRange(invoice.getIssuedAt(), from, to))
                    .forEach(invoice -> {
                        Order order = orderRepository.findById(invoice.getOrderId()).orElse(null);
                        User user = null;
                        if (order != null) {
                            user = userRepository.findById(order.getUserId()).orElse(null);
                        }
                        rows.add(Arrays.asList(
                                invoice.getId(),
                                invoice.getOrderId() != null ? invoice.getOrderId() : "N/A",
                                user != null ? user.getEmail() : "Desconocido",
                                String.format("$%.2f", invoice.getTotalAmount()),
                                invoice.getIssuedAt().format(DATETIME_FORMATTER)
                        ));
                    });

            return CsvUtility.writeCSV(fileName, headers, rows);
        } catch (Exception e) {
            Logger.error("Error generating financial CSV report: " + e.getMessage());
            return null;
        }
    }

    /**
     * Generates shipments report in PDF format.
     * TODO: Implement when PDF generation utilities are ready.
     */
    public File generateShipmentsReportPDF(LocalDate from, LocalDate to) {
        Logger.info("PDF generation not yet implemented - using CSV alternative");
        return generateShipmentsReportCSV(from, to);
    }

    /**
     * Generates shipments report in CSV format.
     */
    public File generateShipmentsReportCSV(LocalDate from, LocalDate to) {
        try {
            String fileName = "reportes/shipments_report_" + from + "_to_" + to + ".csv";

            List<String> headers = Arrays.asList(
                "ID Envío", "Usuario", "Estado", "Peso (kg)", "Costo Total", "Fecha Creación", "Fecha Entrega"
            );

            List<List<String>> rows = new ArrayList<>();
            shipmentRepository.findAll().stream()
                    .filter(shipment -> isInDateRange(shipment.getCreatedAt(), from, to))
                    .forEach(shipment -> {
                        User user = userRepository.findById(shipment.getUserId()).orElse(null);
                        rows.add(Arrays.asList(
                                shipment.getId(),
                                user != null ? user.getEmail() : "Desconocido",
                                shipment.getStatus().getDisplayName(),
                                String.format("%.2f", shipment.getWeightKg()),
                                String.format("$%.2f", shipment.getTotalCost()),
                                shipment.getCreatedAt().format(DATETIME_FORMATTER),
                                shipment.getDeliveredDate() != null ? shipment.getDeliveredDate().format(DATETIME_FORMATTER) : "Pendiente"
                        ));
                    });

            return CsvUtility.writeCSV(fileName, headers, rows);
        } catch (Exception e) {
            Logger.error("Error generating shipments CSV report: " + e.getMessage());
            return null;
        }
    }

    /**
     * Generates users report in PDF format.
     * TODO: Implement when PDF generation utilities are ready.
     */
    public File generateUsersReportPDF(LocalDate from, LocalDate to) {
        Logger.info("PDF generation not yet implemented - using CSV alternative");
        return generateUsersReportCSV(from, to);
    }

    /**
     * Generates users report in CSV format.
     */
    public File generateUsersReportCSV(LocalDate from, LocalDate to) {
        try {
            String fileName = "reportes/users_report_" + from + "_to_" + to + ".csv";

            List<String> headers = Arrays.asList(
                "ID Usuario", "Email", "Nombre", "Teléfono", "Envíos Totales"
            );

            List<List<String>> rows = new ArrayList<>();
            userRepository.getUsers().forEach(user -> {
                long shipmentCount = orderRepository.findAll().stream()
                        .filter(order -> order.getUserId().equals(user.getId()))
                        .filter(order -> isInDateRange(order.getCreatedAt(), from, to))
                        .count();

                rows.add(Arrays.asList(
                        user.getId(),
                        user.getEmail(),
                        user.getName() + " " + user.getLastName(),
                        user.getPhone() != null ? user.getPhone() : "N/A",
                        String.valueOf(shipmentCount)
                ));
            });

            return CsvUtility.writeCSV(fileName, headers, rows);
        } catch (Exception e) {
            Logger.error("Error generating users CSV report: " + e.getMessage());
            return null;
        }
    }

    /**
     * Generates delivery personnel report in PDF format.
     * TODO: Implement when PDF generation utilities are ready.
     */
    public File generateDeliveryPersonnelReportPDF(LocalDate from, LocalDate to) {
        Logger.info("PDF generation not yet implemented - using CSV alternative");
        return generateDeliveryPersonnelReportCSV(from, to);
    }

    /**
     * Generates delivery personnel report in CSV format.
     */
    public File generateDeliveryPersonnelReportCSV(LocalDate from, LocalDate to) {
        try {
            String fileName = "reportes/delivery_personnel_report_" + from + "_to_" + to + ".csv";

            List<String> headers = Arrays.asList(
                "ID Repartidor", "Nombre", "Email", "Estado", "Envíos Completados"
            );

            List<List<String>> rows = new ArrayList<>();
            deliveryPersonRepository.getAllDeliveryPersons().forEach(person -> {
                long completedShipments = shipmentRepository.findAll().stream()
                        .filter(s -> s.getDeliveryPersonId() != null && s.getDeliveryPersonId().equals(person.getId()))
                        .filter(s -> s.getStatus() == ShipmentStatus.DELIVERED)
                        .filter(s -> isInDateRange(s.getCreatedAt(), from, to))
                        .count();

                rows.add(Arrays.asList(
                        person.getId(),
                        person.getName() + " " + person.getLastName(),
                        person.getEmail(),
                        person.getAvailability().name(),
                        String.valueOf(completedShipments)
                ));
            });

            return CsvUtility.writeCSV(fileName, headers, rows);
        } catch (Exception e) {
            Logger.error("Error generating delivery personnel CSV report: " + e.getMessage());
            return null;
        }
    }

    // ==================================================================================
    // HELPER METHODS
    // ==================================================================================

    /**
     * Checks if a date-time is within the given date range.
     */
    private boolean isInDateRange(LocalDateTime dateTime, LocalDate from, LocalDate to) {
        LocalDate date = dateTime.toLocalDate();
        return !date.isBefore(from) && !date.isAfter(to);
    }
}
