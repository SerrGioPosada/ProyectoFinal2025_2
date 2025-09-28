package co.edu.uniquindio.poo.proyectofiinal2025_2.Model;

import javafx.scene.image.Image;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a customer in the system, extending the {@link AuthenticablePerson} class.
 * <p>
 * A user is a customer with a profile image, and manages lists of
 * addresses, payment methods, and orders.
 * </p>
 */
public class User extends AuthenticablePerson {

    private Image profileImage;
    private List<Address> frequentAddresses; // Renamed from addresses
    private List<PaymentMethod> paymentMethods;
    private List<Order> orders;

    /**
     * Constructs a new user with the provided data.
     *
     * @param id           the unique identifier for the user
     * @param name         first name of the user
     * @param lastName     last name of the user
     * @param email        email address of the user
     * @param phone        phone number of the user
     * @param password     password for the user account (will be hashed)
     * @param profileImage profile image of the user
     */
    public User(String id, String name, String lastName, String email, String phone,
                String password, Image profileImage) {
        super(id, name, lastName, email, phone, password);
        this.profileImage = profileImage;
        this.frequentAddresses = new ArrayList<>();
        this.paymentMethods = new ArrayList<>();
        this.orders = new ArrayList<>();
    }

    // ======================
    // Getters
    // ======================

    public Image getProfileImage() {
        return profileImage;
    }

    public List<Address> getFrequentAddresses() {
        return frequentAddresses;
    }

    public List<PaymentMethod> getPaymentMethods() {
        return paymentMethods;
    }

    public List<Order> getOrders() {
        return orders;
    }

    // ======================
    // Setters
    // ======================

    public void setProfileImage(Image profileImage) {
        this.profileImage = profileImage;
    }

    public void setFrequentAddresses(List<Address> frequentAddresses) {
        this.frequentAddresses = frequentAddresses;
    }

    public void setPaymentMethods(List<PaymentMethod> paymentMethods) {
        this.paymentMethods = paymentMethods;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    // ======================
    // Utility methods
    // ======================

    /**
     * Adds a new frequent address to the user's list.
     *
     * @param address address to add
     */
    public void addFrequentAddress(Address address) {
        if (this.frequentAddresses == null) {
            this.frequentAddresses = new ArrayList<>();
        }
        this.frequentAddresses.add(address);
    }

    /**
     * Adds a new payment method to the user.
     *
     * @param method payment method to add
     */
    public void addPaymentMethod(PaymentMethod method) {
        this.paymentMethods.add(method);
    }

    /**
     * Adds a new order to the user.
     *
     * @param order order to add
     */
    public void addOrder(Order order) {
        this.orders.add(order);
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + getId() + '\'' +
                ", name='" + getName() + '\'' +
                ", lastName='" + getLastName() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", phone='" + getPhone() + '\'' +
                ", frequentAddresses=" + (frequentAddresses != null ? frequentAddresses.size() : "0") +
                ", paymentMethods=" + paymentMethods.size() +
                ", orders=" + orders.size() +
                '}';
    }
}
