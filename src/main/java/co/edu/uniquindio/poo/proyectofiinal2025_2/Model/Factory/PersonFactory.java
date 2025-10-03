package co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Factory;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.dto.PersonCreationData;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Admin;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.DeliveryPerson;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Enums.PersonType;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Person;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.User;

/**
 * A factory class responsible for creating different types of Person objects
 * (User, Admin, etc.) by encapsulating the instantiation logic.
 * It uses the Lombok @SuperBuilder pattern internally for complex object construction.
 */
public class PersonFactory {

    /**
     * Creates a Person instance based on the specified type and data.
     * This method uses the Lombok-generated @SuperBuilder for instantiation.
     *
     * @param type The type of person to create (USER, ADMIN, etc.).
     * @param data The PersonCreationData object containing all possible attributes.
     * @return The newly created Person object.
     * @throws IllegalArgumentException if the person type is invalid.
     */
    public static Person createPerson(PersonType type, PersonCreationData data) {
        switch (type) {
            case USER:
                return User.builder()
                        .id(data.getId())
                        .name(data.getName())
                        .lastName(data.getLastName())
                        .email(data.getEmail())
                        .phone(data.getPhone())
                        .password(data.getPassword())
                        .profileImage(data.getProfileImage())
                        .build();

            case ADMIN:
                return Admin.builder()
                        .id(data.getId())
                        .name(data.getName())
                        .lastName(data.getLastName())
                        .email(data.getEmail())
                        .phone(data.getPhone())
                        .password(data.getPassword())
                        .employeeId(data.getEmployeeId())
                        .permissionLevel(data.getPermissionLevel())
                        .build();

            case DELIVERY_PERSON:
                return DeliveryPerson.builder()
                        .id(data.getId())
                        .name(data.getName())
                        .lastName(data.getLastName())
                        .email(data.getEmail())
                        .phone(data.getPhone())
                        .password(data.getPassword())
                        .documentId(data.getDocumentId())
                        .availability(data.getAvailability())
                        .assignedVehicle(data.getAssignedVehicle())
                        .coverageArea(data.getCoverageArea())
                        .build();

            default:
                throw new IllegalArgumentException("Invalid PersonType: " + type);
        }
    }
}
