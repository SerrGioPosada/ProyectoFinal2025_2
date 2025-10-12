package co.edu.uniquindio.poo.proyectofiinal2025_2.Model.dto;

import javafx.beans.property.*;

/**
 * A Data Transfer Object (DTO) designed for displaying user summary information in UI components like TableView.
 * <p>
 * This class uses JavaFX properties (e.g., {@link StringProperty}) to allow for easy data binding
 * with JavaFX controls. It provides a flattened, read-only view of a user's most important data points.
 * </p>
 */
public class UserSummaryDTO {

    // =================================================================================================================
    // Properties
    // =================================================================================================================

    private final StringProperty id;
    private final StringProperty name;
    private final StringProperty lastName;
    private final StringProperty email;
    private final StringProperty phone;
    private final IntegerProperty orderCount;
    private final IntegerProperty addressCount;
    private final StringProperty profileImagePath;
    private final BooleanProperty isActive;

    // =================================================================================================================
    // Constructor
    // =================================================================================================================

    /**
     * Constructs a new UserSummaryDTO with all required data.
     *
     * @param id The unique identifier of the user.
     * @param name The user's first name.
     * @param lastName The user's last name.
     * @param email The user's email address.
     * @param phone The user's phone number.
     * @param orderCount The total number of orders placed by the user.
     * @param addressCount The total number of addresses registered by the user.
     * @param profileImagePath The file path to the user's profile image.
     * @param isActive The current status of the user's account.
     */
    public UserSummaryDTO(String id, String name, String lastName, String email,
                          String phone, int orderCount, int addressCount,
                          String profileImagePath, boolean isActive) {
        this.id = new SimpleStringProperty(id);
        this.name = new SimpleStringProperty(name);
        this.lastName = new SimpleStringProperty(lastName);
        this.email = new SimpleStringProperty(email);
        this.phone = new SimpleStringProperty(phone);
        this.orderCount = new SimpleIntegerProperty(orderCount);
        this.addressCount = new SimpleIntegerProperty(addressCount);
        this.profileImagePath = new SimpleStringProperty(profileImagePath);
        this.isActive = new SimpleBooleanProperty(isActive);
    }

    // =================================================================================================================
    // JavaFX Property Accessors (for data binding)
    // =================================================================================================================

    public StringProperty idProperty() { return id; }
    public StringProperty nameProperty() { return name; }
    public StringProperty lastNameProperty() { return lastName; }
    public StringProperty emailProperty() { return email; }
    public StringProperty phoneProperty() { return phone; }
    public IntegerProperty orderCountProperty() { return orderCount; }
    public IntegerProperty addressCountProperty() { return addressCount; }
    public StringProperty profileImagePathProperty() { return profileImagePath; }
    public BooleanProperty isActiveProperty() { return isActive; }

    // =================================================================================================================
    // Standard Getters (for direct data access)
    // =================================================================================================================

    public String getId() { return id.get(); }
    public String getName() { return name.get(); }
    public String getLastName() { return lastName.get(); }
    public String getEmail() { return email.get(); }
    public String getPhone() { return phone.get(); }
    public int getOrderCount() { return orderCount.get(); }
    public int getAddressCount() { return addressCount.get(); }
    public String getProfileImagePath() { return profileImagePath.get(); }
    public boolean isActive() { return isActive.get(); }

    // =================================================================================================================
    // Standard Setters
    // =================================================================================================================

    public void setId(String id) { this.id.set(id); }
    public void setName(String name) { this.name.set(name); }
    public void setLastName(String lastName) { this.lastName.set(lastName); }
    public void setEmail(String email) { this.email.set(email); }
    public void setPhone(String phone) { this.phone.set(phone); }
    public void setOrderCount(int orderCount) { this.orderCount.set(orderCount); }
    public void setAddressCount(int addressCount) { this.addressCount.set(addressCount); }
    public void setProfileImagePath(String path) { this.profileImagePath.set(path); }
    public void setActive(boolean active) { this.isActive.set(active); }

    // =================================================================================================================
    // Utility Methods
    // =================================================================================================================

    /**
     * Returns the full name of the user by combining the first and last names.
     *
     * @return A string representing the user's full name.
     */
    public String getFullName() {
        return name.get() + " " + lastName.get();
    }
}
