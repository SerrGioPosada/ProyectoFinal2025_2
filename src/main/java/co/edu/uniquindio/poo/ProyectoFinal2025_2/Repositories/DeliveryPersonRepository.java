package co.edu.uniquindio.poo.ProyectoFinal2025_2.Repositories;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.DeliveryPerson;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.Logger;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel.StringUtil;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilRepository.GsonProvider;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilRepository.JsonFileHandler;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilRepository.RepositoryPaths;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilRepository.RepositoryValidator;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Manages the persistence and retrieval of DeliveryPerson entities using HashMaps for fast lookups by ID, email, and document ID.
 * <p>This class is implemented as a Singleton and saves data to a local JSON file.</p>
 */
public class DeliveryPersonRepository {

    // =================================================================================================================
    // CONSTANTS AND FIELDS
    // =================================================================================================================

    private final Gson gson = GsonProvider.createGsonWithPrettyPrinting();
    private static DeliveryPersonRepository instance;

    private final Map<String, DeliveryPerson> personsById;
    private final Map<String, DeliveryPerson> personsByEmail;
    private final Map<String, DeliveryPerson> personsByDocumentId;

    // =================================================================================================================
    // CONSTRUCTOR (Singleton)
    // =================================================================================================================

    /**
     * Private constructor to enforce Singleton pattern.
     * Initializes maps and loads data from file.
     */
    private DeliveryPersonRepository() {
        this.personsById = new HashMap<>();
        this.personsByEmail = new HashMap<>();
        this.personsByDocumentId = new HashMap<>();
        loadFromFile();
        Logger.info("DeliveryPersonRepository initialized. Delivery persons loaded: " + personsById.size());
    }

    /**
     * Returns the singleton instance of DeliveryPersonRepository.
     *
     * @return The singleton instance.
     */
    public static synchronized DeliveryPersonRepository getInstance() {
        if (instance == null) {
            instance = new DeliveryPersonRepository();
        }
        return instance;
    }

    // =================================================================================================================
    // FILE I/O OPERATIONS
    // =================================================================================================================

    /**
     * Saves the current list of delivery persons to the JSON file.
     */
    private void saveToFile() {
        List<DeliveryPerson> personList = new ArrayList<>(personsById.values());
        JsonFileHandler.saveToFile(RepositoryPaths.DELIVERY_PERSONS_PATH, personList, gson);
    }

    /**
     * Loads the list of delivery persons from the JSON file.
     */
    private void loadFromFile() {
        Type listType = new TypeToken<ArrayList<DeliveryPerson>>() {}.getType();
        Optional<List<DeliveryPerson>> loadedPersons = JsonFileHandler.loadFromFile(
                RepositoryPaths.DELIVERY_PERSONS_PATH,
                listType,
                gson
        );

        loadedPersons.ifPresent(persons -> {
            Logger.info("Loading " + persons.size() + " delivery persons from file...");
            for (DeliveryPerson person : persons) {
                if (RepositoryValidator.validateEntityWithIdAndEmail(person, person.getId(), person.getEmail(), "DeliveryPerson")) {
                    // Additional validation for document ID
                    if (StringUtil.isNullOrEmpty(person.getDocumentId())) {
                        Logger.warning("Warning: Skipping delivery person with null/empty document ID: " + person.getEmail());
                        continue;
                    }

                    personsById.put(person.getId(), person);
                    personsByEmail.put(person.getEmail().toLowerCase(), person);
                    personsByDocumentId.put(person.getDocumentId(), person);
                } else {
                    Logger.warning("Warning: Skipping corrupt delivery person entry in JSON file");
                }
            }
            Logger.info("Successfully loaded " + personsById.size() + " delivery persons");
        });
    }

    // =================================================================================================================
    // CRUD OPERATIONS
    // =================================================================================================================

    /**
     * Adds a new delivery person to the repository.
     * Updates the person if they already exist.
     *
     * @param person The delivery person to add.
     */
    public void addDeliveryPerson(DeliveryPerson person) {
        if (!RepositoryValidator.validateEntityWithIdAndEmail(person, person.getId(), person.getEmail(), "DeliveryPerson")) {
            return;
        }

        // Additional validation for document ID
        if (StringUtil.isNullOrEmpty(person.getDocumentId())) {
            Logger.error("RepositoryValidator: DeliveryPerson document ID is null or empty for ID: " + person.getId());
            return;
        }

        Logger.info("Adding delivery person: " + person.getEmail() + " (ID: " + person.getId() + ")");

        personsById.put(person.getId(), person);
        personsByEmail.put(person.getEmail().toLowerCase(), person);
        personsByDocumentId.put(person.getDocumentId(), person);

        Logger.info("Total delivery persons in memory: " + personsById.size());

        saveToFile();
    }

    /**
     * Updates an existing delivery person.
     * This is an alias for addDeliveryPerson since the implementation is the same.
     *
     * @param updatedPerson The delivery person with updated information.
     */
    public void updateDeliveryPerson(DeliveryPerson updatedPerson) {
        addDeliveryPerson(updatedPerson);
    }

    /**
     * Removes a delivery person from the repository by their ID.
     * The person is removed from all maps and changes are persisted.
     *
     * @param personId The ID of the delivery person to remove.
     */
    public void removeDeliveryPerson(String personId) {
        if (!RepositoryValidator.validateId(personId, "DeliveryPerson")) {
            return;
        }

        DeliveryPerson personToRemove = personsById.get(personId);
        if (personToRemove != null) {
            Logger.info("Removing delivery person: " + personToRemove.getEmail());
            personsById.remove(personId);
            personsByEmail.remove(personToRemove.getEmail().toLowerCase());
            personsByDocumentId.remove(personToRemove.getDocumentId());
            saveToFile();
        }
    }

    /**
     * Retrieves all delivery persons stored in the repository.
     *
     * @return A new list containing all delivery persons.
     */
    public List<DeliveryPerson> getAllDeliveryPersons() {
        return new ArrayList<>(personsById.values());
    }

    // =================================================================================================================
    // FINDER METHODS
    // =================================================================================================================

    /**
     * Finds a delivery person by their unique ID.
     *
     * @param id The ID to search for.
     * @return An Optional containing the delivery person if found, empty otherwise.
     */
    public Optional<DeliveryPerson> findDeliveryPersonById(String id) {
        if (!RepositoryValidator.validateId(id, "DeliveryPerson")) {
            return Optional.empty();
        }
        return Optional.ofNullable(personsById.get(id));
    }

    /**
     * Finds a delivery person by their email address.
     *
     * @param email The email to search for.
     * @return An Optional containing the delivery person if found, empty otherwise.
     */
    public Optional<DeliveryPerson> findDeliveryPersonByEmail(String email) {
        if (!RepositoryValidator.validateEmail(email, "DeliveryPerson")) {
            return Optional.empty();
        }
        return Optional.ofNullable(personsByEmail.get(email.toLowerCase()));
    }

    /**
     * Finds a delivery person by their document ID.
     *
     * @param documentId The document ID to search for.
     * @return An Optional containing the delivery person if found, empty otherwise.
     */
    public Optional<DeliveryPerson> findDeliveryPersonByDocumentId(String documentId) {
        if (StringUtil.isNullOrEmpty(documentId)) {
            Logger.error("RepositoryValidator: DeliveryPerson document ID is null or empty");
            return Optional.empty();
        }
        return Optional.ofNullable(personsByDocumentId.get(documentId));
    }

    /**
     * Gets a delivery person by their unique ID.
     * This is a convenience method that returns the delivery person directly or null if not found.
     *
     * @param id The ID to search for.
     * @return The delivery person if found, null otherwise.
     */
    public DeliveryPerson getDeliveryPersonById(String id) {
        return findDeliveryPersonById(id).orElse(null);
    }

    // =================================================================================================================
    // UTILITY AND DEBUG METHODS
    // =================================================================================================================

    /**
     * For debugging: prints all delivery persons currently in memory.
     */
    public void printAllDeliveryPersons() {
        Logger.info("=== Current Delivery Persons in Memory ===");
        personsById.values().forEach(person ->
                Logger.info("- " + person.getEmail() + " (ID: " + person.getId() +
                        ", Status: " + person.getAvailability() + ")")
        );
        Logger.info("Total: " + personsById.size() + " delivery persons");
    }
}
