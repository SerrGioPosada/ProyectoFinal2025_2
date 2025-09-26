package co.edu.uniquindio.poo.proyectofiinal2025_2.Model;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Enums.Role;
import javafx.scene.image.Image;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a system user.
 * <p>
 * A user contains personal information such as name, last name,
 * email, phone, password, role, and address.
 * Additionally, it manages a profile image, multiple payment methods,
 * and shipments associated with the user.
 * </p>
 */
public class User {

    private String name;
    private String lastName;
    private String email;
    private String phone;
    private String password;
    private Role role;
    private Image profileImage;

    private Address address;
    private List<PaymentMethod> paymentMethods;
    private List<Shipment> shipments;

    /**
     * Constructs a new user with the provided data.
     *
     * @param name          first name of the user
     * @param lastName      last name of the user
     * @param email         email address of the user
     * @param phone         phone number of the user
     * @param password      password of the user
     * @param role          role of the user (e.g. ADMIN, CLIENT)
     * @param profileImage  profile image of the user
     * @param address       main address of the user
     */
    public User(String name, String lastName, String email, String phone,
                String password, Role role, Image profileImage,
                Address address) {
        this.name = name;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.role = role;
        this.profileImage = profileImage;
        this.address = address;
        this.paymentMethods = new ArrayList<>();
        this.shipments = new ArrayList<>();
    }

    // ======================
    // Getters
    // ======================

    public String getName() {
        return name;
    }
    public String getLastName() {
        return lastName;
    }
    public String getEmail() {
        return email;
    }
    public String getPhone() {
        return phone;
    }
    public String getPassword() {
        return password;
    }
    public Role getRole() {
        return role;
    }
    public Image getProfileImage() {
        return profileImage;
    }
    public Address getAddress() {
        return address;
    }
    public List<PaymentMethod> getPaymentMethods() {
        return paymentMethods;
    }
    public List<Shipment> getShipments() {
        return shipments;
    }

    // ======================
    // Setters
    // ======================

    public void setName(String name) {
        this.name = name;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public void setRole(Role role) {
        this.role = role;
    }
    public void setProfileImage(Image profileImage) {
        this.profileImage = profileImage;
    }
    public void setAddress(Address address) {
        this.address = address;
    }
    public void setPaymentMethods(List<PaymentMethod> paymentMethods) {
        this.paymentMethods = paymentMethods;
    }
    public void setShipments(List<Shipment> shipments) {
        this.shipments = shipments;
    }

    // ======================
    // Utility methods
    // ======================

    /**
     * Adds a new payment method to the user.
     *
     * @param method payment method to add
     */
    public void addPaymentMethod(PaymentMethod method) {
        this.paymentMethods.add(method);
    }

    /**
     * Adds a new shipment to the user.
     *
     * @param shipment shipment to add
     */
    public void addShipment(Shipment shipment) {
        this.shipments.add(shipment);
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", role=" + role +
                ", address=" + (address != null ? address.toString() : "null") +
                ", paymentMethods=" + paymentMethods.size() +
                ", shipments=" + shipments.size() +
                '}';
    }
}
