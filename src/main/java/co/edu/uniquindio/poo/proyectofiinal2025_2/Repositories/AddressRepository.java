package co.edu.uniquindio.poo.proyectofiinal2025_2.Repositories;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Address;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Manages the persistence and retrieval of Address entities.</p>
 * <p>This class is implemented as a Singleton to ensure that there is only one
 * instance managing all address data in the application.</p>
 */
public class AddressRepository {

    private static AddressRepository instance;
    private final List<Address> addresses;

    private AddressRepository() {
        this.addresses = new ArrayList<>();
    }

    public static synchronized AddressRepository getInstance() {
        if (instance == null) {
            instance = new AddressRepository();
        }
        return instance;
    }

    public void addAddress(Address address) {
        addresses.add(address);
    }

    public List<Address> findAll() {
        return new ArrayList<>(addresses);
    }

    // In a real application, you would have findById, update, and delete methods.
}
