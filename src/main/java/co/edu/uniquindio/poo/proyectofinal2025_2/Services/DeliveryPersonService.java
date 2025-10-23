package co.edu.uniquindio.poo.proyectofinal2025_2.Services;

import co.edu.uniquindio.poo.proyectofinal2025_2.Model.DeliveryPerson;
import co.edu.uniquindio.poo.proyectofinal2025_2.Model.Enums.AvailabilityStatus;
import co.edu.uniquindio.poo.proyectofinal2025_2.Model.Enums.PersonType;
import co.edu.uniquindio.poo.proyectofinal2025_2.Model.Factory.PersonFactory;
import co.edu.uniquindio.poo.proyectofinal2025_2.Model.Shipment;
import co.edu.uniquindio.poo.proyectofinal2025_2.Model.dto.PersonCreationData;
import co.edu.uniquindio.poo.proyectofinal2025_2.Repositories.DeliveryPersonRepository;
import co.edu.uniquindio.poo.proyectofinal2025_2.Util.UtilModel.PasswordUtility;
import co.edu.uniquindio.poo.proyectofinal2025_2.Util.UtilModel.ValidationUtil;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Provides business logic services related to delivery personnel.
 * This service class encapsulates the logic for registering delivery persons,
 * updating their status, and managing their shipments, ensuring passwords are securely hashed.
 */
public class DeliveryPersonService {

    private static DeliveryPersonService instance;
    private final DeliveryPersonRepository deliveryPersonRepository;

    /**
     * Package-private constructor for testing and dependency injection.
     *
     * @param deliveryPersonRepository The DeliveryPersonRepository instance to use.
     */
    DeliveryPersonService(DeliveryPersonRepository deliveryPersonRepository) {
        this.deliveryPersonRepository = deliveryPersonRepository;
    }

    /**
     * Private constructor to enforce Singleton pattern.
     * Delegates to the dependency injection constructor.
     */
    private DeliveryPersonService() {
        this(DeliveryPersonRepository.getInstance());
    }

    /**
     * Returns the single instance of DeliveryPersonService.
     *
     * @return The singleton instance.
     */
    public static synchronized DeliveryPersonService getInstance() {
        if (instance == null) {
            instance = new DeliveryPersonService();
        }
        return instance;
    }

    /**
     * Allows setting a custom instance for testing purposes.
     *
     * @param customInstance The custom DeliveryPersonService instance.
     */
    static void setInstance(DeliveryPersonService customInstance) {
        instance = customInstance;
    }

    /**
     * Orchestrates the registration of a new delivery person from raw creation data.
     * This method handles the entire registration process:
     * 1. Validates that the email is not already in use.
     * 2. Calls the PersonFactory to create a new DeliveryPerson object.
     * 3. Hashes the delivery person's password for secure storage.
     * 4. Persists the new delivery person to the repository.
     *
     * @param data The PersonCreationData DTO containing the delivery person's raw information.
     * @return true if registration is successful, false if the email already exists.
     */
    public boolean registerDeliveryPerson(PersonCreationData data) {
        if (deliveryPersonRepository.findDeliveryPersonByEmail(data.getEmail()).isPresent()) {
            return false;
        }

        DeliveryPerson newDeliveryPerson = (DeliveryPerson) PersonFactory.createPerson(
                PersonType.DELIVERY_PERSON, data
        );

        String hashedPassword = PasswordUtility.hashPassword(newDeliveryPerson.getPassword());
        newDeliveryPerson.setPassword(hashedPassword);
        deliveryPersonRepository.addDeliveryPerson(newDeliveryPerson);

        return true;
    }

    /**
     * Updates the availability status of a specific delivery person.
     *
     * @param personId  The ID of the delivery person to update.
     * @param newStatus The new availability status.
     * @return true if the update was successful, false otherwise.
     */
    public boolean updateAvailability(String personId, AvailabilityStatus newStatus) {
        Optional<DeliveryPerson> personOpt = deliveryPersonRepository.findDeliveryPersonById(personId);

        if (personOpt.isEmpty()) {
            return false;
        }

        DeliveryPerson person = personOpt.get();
        person.setAvailability(newStatus);
        deliveryPersonRepository.updateDeliveryPerson(person);
        return true;
    }

    /**
     * Finds all delivery persons who are currently available.
     *
     * @return A list of all delivery persons with the status AVAILABLE.
     */
    public List<DeliveryPerson> findAvailableDeliveryPersons() {
        return deliveryPersonRepository.getAllDeliveryPersons().stream()
                .filter(person -> person.getAvailability() == AvailabilityStatus.AVAILABLE)
                .collect(Collectors.toList());
    }

    /**
     * Finds all delivery persons currently in transit.
     *
     * @return A list of all delivery persons with the status IN_TRANSIT.
     */
    public List<DeliveryPerson> findDeliveryPersonsInTransit() {
        return deliveryPersonRepository.getAllDeliveryPersons().stream()
                .filter(person -> person.getAvailability() == AvailabilityStatus.IN_TRANSIT)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all delivery persons in the system.
     *
     * @return A list of all delivery persons.
     */
    public List<DeliveryPerson> getAllDeliveryPersons() {
        return deliveryPersonRepository.getAllDeliveryPersons();
    }

    /**
     * Finds a delivery person by their unique ID.
     *
     * @param personId The ID of the delivery person.
     * @return An Optional containing the delivery person if found, empty otherwise.
     */
    public Optional<DeliveryPerson> findDeliveryPersonById(String personId) {
        return deliveryPersonRepository.findDeliveryPersonById(personId);
    }

    /**
     * Assigns a shipment to a delivery person and updates their status to IN_TRANSIT.
     * Validates that both person and shipment are not null and that the person is available.
     *
     * @param person   The delivery person to assign the shipment to.
     * @param shipment The shipment to be assigned.
     * @throws IllegalArgumentException if person or shipment is null.
     * @throws IllegalStateException    if the delivery person is not available.
     */
    public void assignShipment(DeliveryPerson person, Shipment shipment) {
        ValidationUtil.requireNonNull(person, "Delivery person cannot be null");
        ValidationUtil.requireNonNull(shipment, "Shipment cannot be null");

        if (person.getAvailability() != AvailabilityStatus.AVAILABLE) {
            throw new IllegalStateException(
                    "Cannot assign shipment: Delivery person " + person.getName() + " is not available"
            );
        }

        person.addShipment(shipment);
        person.setAvailability(AvailabilityStatus.IN_TRANSIT);
        deliveryPersonRepository.updateDeliveryPerson(person);
    }

    /**
     * Completes a shipment for a delivery person.
     * Removes the shipment from the person's list and sets them back to AVAILABLE if no shipments remain.
     *
     * @param person   The delivery person who completed the shipment.
     * @param shipment The shipment that was completed.
     * @return true if the shipment was found and removed, false otherwise.
     */
    public boolean completeShipment(DeliveryPerson person, Shipment shipment) {
        if (person == null || shipment == null) {
            return false;
        }

        boolean removed = person.getAssignedShipments().remove(shipment);

        if (removed && person.getAssignedShipments().isEmpty()) {
            person.setAvailability(AvailabilityStatus.AVAILABLE);
            deliveryPersonRepository.updateDeliveryPerson(person);
        }

        return removed;
    }

    /**
     * Removes a delivery person from the system.
     *
     * @param personId The ID of the delivery person to remove.
     * @return true if removal was successful, false if person not found.
     */
    public boolean removeDeliveryPerson(String personId) {
        if (deliveryPersonRepository.findDeliveryPersonById(personId).isEmpty()) {
            return false;
        }

        deliveryPersonRepository.removeDeliveryPerson(personId);
        return true;
    }
}