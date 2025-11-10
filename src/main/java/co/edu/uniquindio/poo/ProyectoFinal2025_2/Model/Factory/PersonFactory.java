package co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Factory;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.dto.PersonCreationData;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Admin;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.DeliveryPerson;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.PersonType;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Person;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.User;

/**
 * A factory class responsible for creating different types of Person objects
 * (User, Admin, etc.) by encapsulating the instantiation logic.
 * It uses a manual, inheritance-aware Builder pattern internally for complex object construction.
 */

public class PersonFactory {

    /**
     * Creates a Person instance based on the specified type and data.
     * This method uses the manually implemented Builder pattern for instantiation.
     *
     * @param type The type of person to create (USER, ADMIN, etc.).
     * @param data The PersonCreationData object containing all possible attributes.
     * @return The newly created Person object.
     * @throws IllegalArgumentException if the person type is invalid.
     */
    public static Person createPerson(PersonType type, PersonCreationData data) {
        return switch (type) {
            case USER -> new User.Builder()
                    .withId(data.getId())
                    .withName(data.getName())
                    .withLastName(data.getLastName())
                    .withEmail(data.getEmail())
                    .withPhone(data.getPhone())
                    .withPassword(data.getPassword())
                    .withProfileImagePath(data.getProfileImagePath())
                    .withIsActive(data.isActive())
                    .build();

            case ADMIN -> new Admin.Builder()
                    .withId(data.getId())
                    .withName(data.getName())
                    .withLastName(data.getLastName())
                    .withEmail(data.getEmail())
                    .withPhone(data.getPhone())
                    .withPassword(data.getPassword())
                    .withProfileImagePath(data.getProfileImagePath())
                    .withEmployeeId(data.getEmployeeId())
                    .withPermissionLevel(data.getPermissionLevel())
                    .build();

            case DELIVERY_PERSON -> {
                DeliveryPerson.Builder builder = new DeliveryPerson.Builder()
                        .withId(data.getId())
                        .withName(data.getName())
                        .withLastName(data.getLastName())
                        .withEmail(data.getEmail())
                        .withPhone(data.getPhone())
                        .withPassword(data.getPassword())
                        .withProfileImagePath(data.getProfileImagePath())
                        .withDocumentId(data.getDocumentId())
                        .withAvailability(data.getAvailability())
                        .withCoverageArea(data.getCoverageArea());

                // If assigned vehicle is provided, extract its plate
                if (data.getAssignedVehicle() != null) {
                    builder.withActiveVehiclePlate(data.getAssignedVehicle().getPlate());
                }

                yield builder.build();
            }
        };
    }
}
