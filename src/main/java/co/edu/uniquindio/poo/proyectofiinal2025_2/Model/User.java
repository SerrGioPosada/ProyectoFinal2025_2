package co.edu.uniquindio.poo.proyectofiinal2025_2.Model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a customer in the system, extending the {@link AuthenticablePerson} class.
 * <p>
 * A user manages lists of addresses, payment methods, and orders.
 * The profile image is inherited from AuthenticablePerson.
 * </p>
 */
@Getter
@Setter
@ToString(callSuper = true)
public class User extends AuthenticablePerson {

    private List<Address> frequentAddresses;
    private List<PaymentMethod> paymentMethods;
    private List<Order> orders;

    /**
     * Default constructor.
     * Initializes lists to avoid NullPointerExceptions.
     */
    public User() {
        super();
        this.frequentAddresses = new ArrayList<>();
        this.paymentMethods = new ArrayList<>();
        this.orders = new ArrayList<>();
    }

    /**
     * Protected constructor for the builder pattern.
     * @param builder The builder instance to construct from.
     */
    protected User(Builder builder) {
        super(builder);
        this.frequentAddresses = new ArrayList<>(); // Always initialize lists
        this.paymentMethods = new ArrayList<>();
        this.orders = new ArrayList<>();
    }

    // ======================================
    //               BUILDER
    // ======================================

    /**
     * Concrete builder for creating User instances.
     */
    public static class Builder extends AuthenticablePerson.Builder<Builder> {

        /**
         * Returns the concrete builder instance (part of the CRTP pattern).
         * @return The concrete builder instance.
         */
        @Override
        protected Builder self() {
            return this;
        }

        /**
         * Creates a new User instance from the builder's properties.
         * @return A new User instance.
         */
        @Override
        public User build() {
            return new User(this);
        }
    }

    // ======================================
    //           UTILITY METHODS
    // ======================================

    /**
     * Adds a new frequent address to the user's list.
     *
     * @param address The Address object to add.
     */
    public void addFrequentAddress(Address address) {
        if (this.frequentAddresses == null) {
            this.frequentAddresses = new ArrayList<>();
        }
        this.frequentAddresses.add(address);
    }

    /**
     * Adds a new payment method to the user's list.
     *
     * @param method The PaymentMethod object to add.
     */
    public void addPaymentMethod(PaymentMethod method) {
        if (this.paymentMethods == null) {
            this.paymentMethods = new ArrayList<>();
        }
        this.paymentMethods.add(method);
    }

    /**
     * Adds a new order to the user's list.
     *
     * @param order The Order object to add.
     */
    public void addOrder(Order order) {
        if (this.orders == null) {
            this.orders = new ArrayList<>();
        }
        this.orders.add(order);
    }
}
