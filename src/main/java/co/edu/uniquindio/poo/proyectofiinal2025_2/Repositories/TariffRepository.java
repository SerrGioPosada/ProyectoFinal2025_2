package co.edu.uniquindio.poo.proyectofiinal2025_2.Repositories;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Tariff;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * <p>Manages the persistence and retrieval of Tariff entities.</p>
 * <p>This class is implemented as a Singleton to ensure that there is only one
 * instance managing all tariff data in the application.</p>
 */
public class TariffRepository {

    private static TariffRepository instance;
    private final List<Tariff> tariffs;

    private TariffRepository() {
        this.tariffs = new ArrayList<>();
    }

    public static synchronized TariffRepository getInstance() {
        if (instance == null) {
            instance = new TariffRepository();
        }
        return instance;
    }

    public void addTariff(Tariff tariff) {
        tariffs.add(tariff);
    }

    public Optional<Tariff> findById(String id) {
        return tariffs.stream()
                .filter(tariff -> tariff.getId().equals(id))
                .findFirst();
    }

    public List<Tariff> findAll() {
        return new ArrayList<>(tariffs);
    }

    public void update(Tariff updatedTariff) {
        findById(updatedTariff.getId()).ifPresent(existingTariff -> {
            int index = tariffs.indexOf(existingTariff);
            tariffs.set(index, updatedTariff);
        });
    }

    public void delete(String id) {
        findById(id).ifPresent(tariffs::remove);
    }
}
