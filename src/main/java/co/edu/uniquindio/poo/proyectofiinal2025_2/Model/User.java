package co.edu.uniquindio.poo.proyectofiinal2025_2.Model;

import javafx.scene.image.Image;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a customer in the system, extending the {@link AuthenticablePerson} class.
 * <p>
 * A user is a customer with a profile image, and manages lists of
 * addresses, payment methods, and orders.
 * </p>
 */

@Getter
@Setter
@ToString(callSuper = true)
@SuperBuilder

public class User extends AuthenticablePerson {

    private Image profileImage;                  // Profile picture of the user
    private List<Address> frequentAddresses;     // List of addresses frequently used by the user
    private List<PaymentMethod> paymentMethods;  // Payment methods registered by the user
    private List<Order> orders;                  // Orders placed by the user

    /**
     * Default constructor for Lombok's @SuperBuilder.
     * Initializes lists to avoid NullPointerExceptions.
     */
    private User() {
        super();
        this.frequentAddresses = new ArrayList<>();
        this.paymentMethods = new ArrayList<>();
        this.orders = new ArrayList<>();
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
        if (this.paymentMethods == null) {
            this.paymentMethods = new ArrayList<>();
        }
        this.paymentMethods.add(method);
    }

    /**
     * Adds a new order to the user.
     *
     * @param order order to add
     */
    public void addOrder(Order order) {
        if (this.orders == null) {
            this.orders = new ArrayList<>();
        }
        this.orders.add(order);
    }
}
