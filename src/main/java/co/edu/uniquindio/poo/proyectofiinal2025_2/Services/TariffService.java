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

    /**
     * Constructs a new TariffService with a repository dependency.
     *
     * @param tariffRepository The repository for managing tariff data.
     */
    public TariffService(TariffRepository tariffRepository) {
        this.tariffRepository = tariffRepository;
    }

    /**
     * Gets the currently active tariff.
     * <p>In a real application, this method would contain logic to determine which
     * tariff to use based on the order details (e.g., priority, location).</p>
     *
     * @return The active Tariff.
     */
    public Tariff getActiveTariff() {
        // For now, we assume there is only one tariff. A real implementation
        // would have more complex logic to select the correct one.
        return tariffRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("No active tariff found in the system."));
    }
}
