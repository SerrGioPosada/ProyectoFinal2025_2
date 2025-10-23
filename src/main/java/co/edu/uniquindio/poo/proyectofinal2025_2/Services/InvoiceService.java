package co.edu.uniquindio.poo.proyectofinal2025_2.Services;

import co.edu.uniquindio.poo.proyectofinal2025_2.Model.Invoice;
import co.edu.uniquindio.poo.proyectofinal2025_2.Model.LineItem;
import co.edu.uniquindio.poo.proyectofinal2025_2.Model.Order;
import co.edu.uniquindio.poo.proyectofinal2025_2.Model.Tariff;
import co.edu.uniquindio.poo.proyectofinal2025_2.Repositories.InvoiceRepository;
import co.edu.uniquindio.poo.proyectofinal2025_2.Repositories.TariffRepository;
import co.edu.uniquindio.poo.proyectofinal2025_2.Util.UtilService.IdGenerationUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>Provides business logic services related to invoice creation and management.</p>
 * <p>This service is responsible for calculating the total cost of an order based on
 * active tariffs and generating a corresponding immutable invoice.</p>
 */
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final TariffService tariffService;

    /**
     * Constructor with dependency injection for repositories and services.
     *
     * @param invoiceRepository The InvoiceRepository instance.
     * @param tariffService The TariffService instance.
     */
    public InvoiceService(InvoiceRepository invoiceRepository, TariffService tariffService) {
        this.invoiceRepository = invoiceRepository;
        this.tariffService = tariffService;
    }

    /**
     * Default constructor that uses singleton instances.
     * This provides backward compatibility and ease of use.
     */
    public InvoiceService() {
        this(InvoiceRepository.getInstance(), new TariffService(TariffRepository.getInstance()));
    }

    /**
     * Creates a new invoice for a given order.
     * <p>It calculates the total cost based on the active tariff and generates a detailed
     * invoice, which is then persisted.</p>
     *
     * @param order The order for which to create an invoice.
     * @return The newly created, immutable Invoice object.
     */
    public Invoice createInvoiceForOrder(Order order) {
        Tariff activeTariff = tariffService.getActiveTariff();
        List<LineItem> lineItems = new ArrayList<>();

        // --- Cost Calculation Logic --- //
        // In a real application, you would get weight/distance from the order details.
        double weightInKg = 5.0; // Placeholder
        double distanceInKm = 10.0; // Placeholder

        // 1. Add base cost
        lineItems.add(new LineItem("Base Shipping Cost", activeTariff.getBaseCost()));

        // 2. Add cost based on weight
        double weightCost = weightInKg * activeTariff.getCostPerKilogram();
        lineItems.add(new LineItem("Cost per Weight (" + weightInKg + " kg)", weightCost));

        // 3. Add cost based on distance
        double distanceCost = distanceInKm * activeTariff.getCostPerKilometer();
        lineItems.add(new LineItem("Cost per Distance (" + distanceInKm + " km)", distanceCost));

        // 4. Add priority surcharge if applicable (placeholder logic)
        boolean isPriority = true; // Placeholder
        if (isPriority) {
            lineItems.add(new LineItem("Priority Surcharge", activeTariff.getPrioritySurcharge()));
        }

        // Calculate total amount
        double totalAmount = lineItems.stream().mapToDouble(LineItem::getAmount).sum();

        // Create the immutable invoice object using the manual builder
        Invoice newInvoice = new Invoice.Builder()
                .withId(IdGenerationUtil.generateId())
                .withOrderId(order.getId())
                .withInvoiceNumber(IdGenerationUtil.generateInvoiceNumber())
                .withIssuedAt(LocalDateTime.now())
                .withTotalAmount(totalAmount)
                .withLineItems(lineItems)
                .build();

        // Persist the new invoice
        invoiceRepository.addInvoice(newInvoice);

        return newInvoice;
    }
}
