package co.edu.uniquindio.poo.ProyectoFinal2025_2.Repositories;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Invoice;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.Adapter.LocalDateTimeAdapter;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * <p>Manages the persistence and retrieval of Invoice entities with JSON persistence.</p>
 * <p>This class is implemented as a Singleton to ensure that there is only one
 * instance managing all invoice data in the application.</p>
 */
public class InvoiceRepository {

    private static InvoiceRepository instance;
    private final List<Invoice> invoices;
    private static final String FILE_PATH = "data/invoices.json";
    private final Gson gson;

    private InvoiceRepository() {
        this.invoices = new ArrayList<>();
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .setPrettyPrinting()
                .create();
        loadFromFile();
    }

    public static synchronized InvoiceRepository getInstance() {
        if (instance == null) {
            instance = new InvoiceRepository();
        }
        return instance;
    }

    /**
     * Loads invoices from JSON file.
     */
    private void loadFromFile() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            Logger.info("Invoice file not found, starting with empty list");
            return;
        }

        try (FileReader reader = new FileReader(file)) {
            Type listType = new TypeToken<ArrayList<Invoice>>(){}.getType();
            List<Invoice> loadedInvoices = gson.fromJson(reader, listType);
            if (loadedInvoices != null) {
                invoices.addAll(loadedInvoices);
                Logger.info("Loaded " + invoices.size() + " invoices from file");
            }
        } catch (IOException e) {
            Logger.error("Error loading invoices from file: " + e.getMessage());
        }
    }

    /**
     * Saves invoices to JSON file.
     */
    private void saveToFile() {
        try {
            File file = new File(FILE_PATH);
            file.getParentFile().mkdirs();

            try (FileWriter writer = new FileWriter(file)) {
                gson.toJson(invoices, writer);
                Logger.info("Saved " + invoices.size() + " invoices to file");
            }
        } catch (IOException e) {
            Logger.error("Error saving invoices to file: " + e.getMessage());
        }
    }

    public void addInvoice(Invoice invoice) {
        invoices.add(invoice);
        saveToFile();
        Logger.info("Invoice added and saved: " + invoice.getId());
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
