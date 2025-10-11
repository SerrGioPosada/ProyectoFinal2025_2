package co.edu.uniquindio.poo.proyectofiinal2025_2.Services;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Tariff;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Repositories.TariffRepository;

/**
 * <p>Provides business logic services related to tariffs and pricing.</p>
 * <p>This service is responsible for retrieving the correct tariff rules that should
 * be applied to an order for cost calculation.</p>
 */
public class TariffService {

    private final TariffRepository tariffRepository;

    public TariffService(TariffRepository tariffRepository) {
        this.tariffRepository = tariffRepository;
    }

    // ===========================
    // Tariff Retrieval
    // ===========================

    /**
     * Gets the currently active tariff.
     * <p>In a real application, this method would contain logic to determine which
     * tariff to use based on the order details (e.g., priority, location).</p>
     *
     * @return The active Tariff.
     */
    public Tariff getActiveTariff() {
        return tariffRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("No active tariff found in the system."));
    }
}

