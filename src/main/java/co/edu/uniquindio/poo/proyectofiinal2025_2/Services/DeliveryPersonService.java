package co.edu.uniquindio.poo.proyectofiinal2025_2.Services;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.DeliveryPerson;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Enums.AvailabilityStatus;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Shipment;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Repositories.DeliveryPersonRepository;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Util.PasswordUtility;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>Provides business logic services related to delivery personnel.</p>
 * <p>This service class encapsulates the logic for registering delivery persons,
 * updating their status, and managing their shipments, ensuring passwords are securely hashed.</p>
 */
public class DeliveryPersonService {

    private final DeliveryPersonRepository deliveryPersonRepository;

    /**
     * Constructs a new DeliveryPersonService with a repository dependency.
     *
     * @param deliveryPersonRepository The repository for managing delivery person data.
     */
    public DeliveryPersonService(DeliveryPersonRepository deliveryPersonRepository) {
        this.deliveryPersonRepository = deliveryPersonRepository;
    }

    /**
     * Registers a new delivery person, hashing their password for secure storage.
     *
     * @param person The delivery person object containing plain text password to register.
     * @return true if registration is successful, false if the email already exists.
     */
    public boolean registerDeliveryPerson(DeliveryPerson person) {
        if (deliveryPersonRepository.findDeliveryPersonByEmail(person.getEmail()).isPresent()) {
            return false; // Email already registered
        }

        // Security: Hash the plain text password before saving the delivery person.
        String hashedPassword = PasswordUtility.hashPassword(person.getPassword());
        person.setPassword(hashedPassword);

        deliveryPersonRepository.addDeliveryPerson(person);
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
        if (personOpt.isPresent()) {
            DeliveryPerson person = personOpt.get();
            person.setAvailability(newStatus);
            deliveryPersonRepository.updateDeliveryPerson(person);
            return true;
        }
        return false;
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
     * Assigns a shipment to a delivery person and updates their status.
     *
     * @param person   The delivery person to assign the shipment to.
     * @param shipment The shipment to be assigned.
     */
    public void assignShipment(DeliveryPerson person, Shipment shipment) {
        if (person.getAvailability() == AvailabilityStatus.AVAILABLE) {
            person.addShipment(shipment);
            person.setAvailability(AvailabilityStatus.IN_TRANSIT);
            deliveryPersonRepository.updateDeliveryPerson(person);
        } else {
            throw new IllegalStateException("Cannot assign shipment: Delivery person " + person.getName() + " is not available.");
        }
    }
}
