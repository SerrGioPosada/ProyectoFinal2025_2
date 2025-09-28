package co.edu.uniquindio.poo.proyectofiinal2025_2.Repositories;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.DeliveryPerson;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * <p>Manages the persistence and retrieval of DeliveryPerson entities.</p>
 * <p>This class is implemented as a Singleton to ensure that there is only one
 * instance managing all delivery person data in the application.</p>
 */
public class DeliveryPersonRepository {

    private static DeliveryPersonRepository instance;
    private final List<DeliveryPerson> deliveryPersons;

    /**
     * Private constructor to initialize the repository. It is part of the Singleton pattern.
     */
    private DeliveryPersonRepository() {
        this.deliveryPersons = new ArrayList<>();
    }

    /**
     * Returns the single instance of the repository.
     *
     * @return The singleton instance of DeliveryPersonRepository.
     */
    public static synchronized DeliveryPersonRepository getInstance() {
        if (instance == null) {
            instance = new DeliveryPersonRepository();
        }
        return instance;
    }

    /**
     * Adds a new delivery person to the repository.
     *
     * @param person The DeliveryPerson to add.
     */
    public void addDeliveryPerson(DeliveryPerson person) {
        deliveryPersons.add(person);
    }

    /**
     * Finds a delivery person by their unique ID.
     *
     * @param id The ID of the delivery person to find.
     * @return An Optional containing the found DeliveryPerson, or empty if not found.
     */
    public Optional<DeliveryPerson> findDeliveryPersonById(String id) {
        return deliveryPersons.stream()
                .filter(person -> person.getId().equals(id))
                .findFirst();
    }

    /**
     * Finds a delivery person by their email address.
     *
     * @param email The email of the delivery person to find.
     * @return An Optional containing the found DeliveryPerson, or empty if not found.
     */
    public Optional<DeliveryPerson> findDeliveryPersonByEmail(String email) {
        return deliveryPersons.stream()
                .filter(person -> person.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    /**
     * Retrieves all delivery persons from the repository.
     *
     * @return A list of all delivery persons.
     */
    public List<DeliveryPerson> getAllDeliveryPersons() {
        return new ArrayList<>(deliveryPersons);
    }

    /**
     * Updates the details of an existing delivery person.
     *
     * @param updatedPerson The delivery person with updated information.
     */
    public void updateDeliveryPerson(DeliveryPerson updatedPerson) {
        findDeliveryPersonById(updatedPerson.getId()).ifPresent(existingPerson -> {
            int index = deliveryPersons.indexOf(existingPerson);
            deliveryPersons.set(index, updatedPerson);
        });
    }
}
