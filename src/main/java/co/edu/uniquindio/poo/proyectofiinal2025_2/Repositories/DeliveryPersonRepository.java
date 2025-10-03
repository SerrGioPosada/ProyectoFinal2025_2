package co.edu.uniquindio.poo.proyectofiinal2025_2.Repositories;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.DeliveryPerson;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Manages the persistence and retrieval of DeliveryPerson entities using HashMaps for fast lookups.
 * <p>
 * Implements the Singleton pattern and saves data to a local JSON file.
 * </p>
 */
public class DeliveryPersonRepository {

    private static final String FILE_PATH = "data/delivery_persons.json";
    private final Gson gson = new Gson();

    private static DeliveryPersonRepository instance;

    private final Map<String, DeliveryPerson> personsById;
    private final Map<String, DeliveryPerson> personsByEmail;
    private final Map<String, DeliveryPerson> personsByDocumentId;

    private DeliveryPersonRepository() {
        this.personsById = new HashMap<>();
        this.personsByEmail = new HashMap<>();
        this.personsByDocumentId = new HashMap<>();
        loadFromFile();
    }

    public static synchronized DeliveryPersonRepository getInstance() {
        if (instance == null) {
            instance = new DeliveryPersonRepository();
        }
        return instance;
    }

    // ======================
    // Private file handling
    // ======================

    private void saveToFile() {
        try (Writer writer = new FileWriter(FILE_PATH)) {

            gson.toJson(personsById.values(), writer);
        } catch (IOException e) {
            System.err.println("Error saving delivery persons to file: " + e.getMessage());
        }
    }

    private void loadFromFile() {
        File file = new File(FILE_PATH);
        if (file.exists() && file.length() > 0) {
            try (Reader reader = new FileReader(file)) {
                Type listType = new TypeToken<ArrayList<DeliveryPerson>>() {}.getType();
                List<DeliveryPerson> loadedPersons = gson.fromJson(reader, listType);
                if (loadedPersons != null) {

                    for (DeliveryPerson person : loadedPersons) {
                        personsById.put(person.getId(), person);
                        personsByEmail.put(person.getEmail().toLowerCase(), person);
                        personsByDocumentId.put(person.getDocumentId(), person);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error loading delivery persons from file: " + e.getMessage());
            }
        }
    }

    // ======================
    // Handling methods
    // ======================

    public void addDeliveryPerson(DeliveryPerson person) {

        personsById.put(person.getId(), person);
        personsByEmail.put(person.getEmail().toLowerCase(), person);
        personsByDocumentId.put(person.getDocumentId(), person);
        saveToFile();
    }

    public void updateDeliveryPerson(DeliveryPerson updatedPerson) {
        addDeliveryPerson(updatedPerson);
    }

    // ======================
    // Query methods
    // ======================

    public Optional<DeliveryPerson> findDeliveryPersonById(String id) {
        return Optional.ofNullable(personsById.get(id));
    }

    public Optional<DeliveryPerson> findDeliveryPersonByEmail(String email) {
        return Optional.ofNullable(personsByEmail.get(email.toLowerCase()));
    }

    /**
     * Finds a delivery person by their document ID (cédula).
     *
     * @param documentId the document ID to search for
     * @return an {@link Optional} containing the delivery person if found, or empty otherwise
     */
    public Optional<DeliveryPerson> findDeliveryPersonByDocumentId(String documentId) {
        return Optional.ofNullable(personsByDocumentId.get(documentId));
    }

    public List<DeliveryPerson> getAllDeliveryPersons() {
        return new ArrayList<>(personsById.values());
    }
}
