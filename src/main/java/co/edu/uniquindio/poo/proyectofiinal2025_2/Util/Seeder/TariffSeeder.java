package co.edu.uniquindio.poo.proyectofiinal2025_2.Util.Seeder;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Tariff;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Repositories.TariffRepository;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Util.UtilModel.Logger;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Util.UtilService.IdGenerationUtil;

/**
 * Seeds default tariff data into the system.
 * <p>Creates a default tariff if none exists in the repository.</p>
 */
public class TariffSeeder {

    private static final TariffRepository tariffRepository = TariffRepository.getInstance();

    /**
     * Seeds a default tariff into the system if no tariffs exist.
     */
    public static void seedDefaultTariff() {
        if (tariffRepository.findAll().isEmpty()) {
            Logger.info("No tariffs found. Creating default tariff...");

            Tariff defaultTariff = new Tariff.Builder()
                .withId(IdGenerationUtil.generateId())
                .withDescription("Tarifa Estándar 2025")
                .withBaseCost(10000.0)              // $10,000 base cost
                .withCostPerKilometer(1500.0)       // $1,500 per km
                .withCostPerKilogram(2000.0)        // $2,000 per kg
                .withPrioritySurcharge(0.10)        // 10% surcharge per priority level above 3
                .build();

            tariffRepository.addTariff(defaultTariff);
            Logger.info("✓ Default tariff created successfully: " + defaultTariff.getId());
        } else {
            Logger.info("Tariffs already exist. Skipping seed.");
        }
    }
}
