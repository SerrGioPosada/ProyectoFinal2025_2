package co.edu.uniquindio.poo.proyectofiinal2025_2.Repositories;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Invoice;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * <p>Manages the persistence and retrieval of Invoice entities.</p>
 * <p>This class is implemented as a Singleton to ensure that there is only one
 * instance managing all invoice data in the application.</p>
 */
public class InvoiceRepository {

    private static InvoiceRepository instance;
    private final List<Invoice> invoices;

    private InvoiceRepository() {
        this.invoices = new ArrayList<>();
    }

    public static synchronized InvoiceRepository getInstance() {
        if (instance == null) {
            instance = new InvoiceRepository();
        }
        return instance;
    }

    public void addInvoice(Invoice invoice) {
        invoices.add(invoice);
    }

    public Optional<Invoice> findById(String id) {
        return invoices.stream()
                .filter(invoice -> invoice.getId().equals(id))
                .findFirst();
    }

    public List<Invoice> findAll() {
        return new ArrayList<>(invoices);
    }

    // In a real-world scenario, you might not update an invoice, but rather cancel and reissue.
}
