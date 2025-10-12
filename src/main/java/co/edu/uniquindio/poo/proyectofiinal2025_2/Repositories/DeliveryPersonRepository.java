package co.edu.uniquindio.poo.proyectofiinal2025_2.Repositories;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.DeliveryPerson;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Util.Adapter.LocalDateTimeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Manages the persistence and retrieval of DeliveryPerson entities using HashMaps for fast lookups.
 * Implements the Singleton pattern and saves data to a local JSON file.
 */
public class DeliveryPersonRepository {

    private static final String FILE_PATH = "data/delivery_persons.json";
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .setPrettyPrinting()
            .create();

    private static DeliveryPersonRepository instance;

    private final Map<String, DeliveryPerson> personsById;
    private final Map<String, DeliveryPerson> personsByEmail;
    private final Map<String, DeliveryPerson> personsByDocumentId;

    /**
     * Private constructor to enforce Singleton pattern.
     * Initializes maps and loads data from file.
     */
    private DeliveryPersonRepository() {
        this.personsById = new HashMap<>();
        this.personsByEmail = new HashMap<>();
        this.personsByDocumentId = new HashMap<>();
        loadFromFile();
        System.out.println("DeliveryPersonRepository initialized. Delivery persons loaded: " + personsById.size());
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

    /**
     * Saves the current list of delivery persons to the JSON file.
     */
    private void saveToFile() {
        try {
            File file = new File(FILE_PATH);
            File parentDir = file.getParentFile();

            if (parentDir != null && !parentDir.exists()) {
                if (!parentDir.mkdirs()) {
                    System.err.println("Failed to create directory: " + parentDir.getAbsolutePath());
                    return;
                }
            }

            List<DeliveryPerson> personList = new ArrayList<>(personsById.values());
            System.out.println("Saving " + personList.size() + " delivery persons to file...");

            try (FileWriter writer = new FileWriter(file, false)) {
                gson.toJson(personList, writer);
                writer.flush();
            }

            System.out.println("Delivery persons saved successfully to " + FILE_PATH);

        } catch (IOException e) {
            System.err.println("Error saving delivery persons to file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Loads the list of delivery persons from the JSON file.
     */
    private void loadFromFile() {
        File file = new File(FILE_PATH);

        if (!file.exists()) {
            System.out.println("No delivery_persons.json file found. Starting with empty repository.");
            return;
        }

        if (file.length() == 0) {
            System.out.println("delivery_persons.json file is empty. Starting with empty repository.");
            return;
        }

        try (Reader reader = new FileReader(file)) {
            Type listType = new TypeToken<ArrayList<DeliveryPerson>>() {}.getType();
            List<DeliveryPerson> loadedPersons = gson.fromJson(reader, listType);

            if (loadedPersons != null && !loadedPersons.isEmpty()) {
                System.out.println("Loading " + loadedPersons.size() + " delivery persons from file...");

                for (DeliveryPerson person : loadedPersons) {
                    if (person != null && person.getId() != null &&
                            person.getEmail() != null && person.getDocumentId() != null) {
                        personsById.put(person.getId(), person);
                        personsByEmail.put(person.getEmail().toLowerCase(), person);
                        personsByDocumentId.put(person.getDocumentId(), person);
                    } else {
                        System.err.println("Warning: Skipping corrupt delivery person entry in JSON file");
                    }
                }

                System.out.println("Successfully loaded " + personsById.size() + " delivery persons");
            } else {
                System.out.println("No valid delivery persons found in file");
            }
        } catch (IOException e) {
            System.err.println("Error loading delivery persons from file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Adds a new delivery person to the repository.
     * Updates the person if they already exist.
     *
     * @param person The delivery person to add.
     */
    public void addDeliveryPerson(DeliveryPerson person) {
        if (person == null) {
            System.err.println("ERROR: Cannot add null delivery person");
            return;
        }

        if (person.getId() == null) {
            System.err.println("ERROR: Delivery person ID is null for email: " + person.getEmail());
            return;
        }

        if (person.getEmail() == null) {
            System.err.println("ERROR: Delivery person email is null for ID: " + person.getId());
            return;
        }

        if (person.getDocumentId() == null) {
            System.err.println("ERROR: Delivery person document ID is null for ID: " + person.getId());
            return;
        }

        System.out.println("Adding delivery person: " + person.getEmail() + " (ID: " + person.getId() + ")");

        personsById.put(person.getId(), person);
        personsByEmail.put(person.getEmail().toLowerCase(), person);
        personsByDocumentId.put(person.getDocumentId(), person);

        System.out.println("Total delivery persons in memory: " + personsById.size());

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
        DeliveryPerson personToRemove = personsById.get(personId);
        if (personToRemove != null) {
            System.out.println("Removing delivery person: " + personToRemove.getEmail());
            personsById.remove(personId);
            personsByEmail.remove(personToRemove.getEmail().toLowerCase());
            personsByDocumentId.remove(personToRemove.getDocumentId());
            saveToFile();
        } else {
            System.err.println("Cannot remove: Delivery person not found with ID: " + personId);
        }
    }

    /**
     * Finds a delivery person by their unique ID.
     *
     * @param id The ID to search for.
     * @return An Optional containing the delivery person if found, empty otherwise.
     */
    public Optional<DeliveryPerson> findDeliveryPersonById(String id) {
        return Optional.ofNullable(personsById.get(id));
    }

    /**
     * Finds a delivery person by their email address.
     *
     * @param email The email to search for.
     * @return An Optional containing the delivery person if found, empty otherwise.
     */
    public Optional<DeliveryPerson> findDeliveryPersonByEmail(String email) {
        return Optional.ofNullable(personsByEmail.get(email.toLowerCase()));
    }

    /**
     * Finds a delivery person by their document ID.
     *
     * @param documentId The document ID to search for.
     * @return An Optional containing the delivery person if found, empty otherwise.
     */
    public Optional<DeliveryPerson> findDeliveryPersonByDocumentId(String documentId) {
        return Optional.ofNullable(personsByDocumentId.get(documentId));
    }

    /**
     * Retrieves all delivery persons stored in the repository.
     *
     * @return A new list containing all delivery persons.
     */
    public List<DeliveryPerson> getAllDeliveryPersons() {
        return new ArrayList<>(personsById.values());
    }

    /**
     * For debugging: prints all delivery persons currently in memory.
     */
    public void printAllDeliveryPersons() {
        System.out.println("=== Current Delivery Persons in Memory ===");
        personsById.values().forEach(person ->
                System.out.println("- " + person.getEmail() + " (ID: " + person.getId() +
                        ", Status: " + person.getAvailability() + ")")
        );
        System.out.println("Total: " + personsById.size() + " delivery persons");
    }
}