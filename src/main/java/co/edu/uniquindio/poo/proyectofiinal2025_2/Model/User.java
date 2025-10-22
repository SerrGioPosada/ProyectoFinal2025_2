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
    private boolean isActive; // ✅ Campo para habilitar/inhabilitar cuenta

    /**
     * Default constructor.
     * Initializes lists to avoid NullPointerExceptions.
     */
    public User() {
        super();
        this.frequentAddresses = new ArrayList<>();
        this.paymentMethods = new ArrayList<>();
        this.orders = new ArrayList<>();
        this.isActive = true; // ✅ Por defecto, las cuentas están activas
    }

    /**
     * Protected constructor for the builder pattern.
     * @param builder The builder instance to construct from.
     */
    protected User(Builder builder) {
        super(builder);
        this.frequentAddresses = new ArrayList<>();
        this.paymentMethods = new ArrayList<>();
        this.orders = new ArrayList<>();
        this.isActive = builder.isActive; // ✅ Obtener del builder
    }

    // ======================================
    //               BUILDER
    // ======================================

    /**
     * Concrete builder for creating User instances.
     */
    public static class Builder extends AuthenticablePerson.Builder<Builder> {

        private boolean isActive = true; // ✅ Campo en el builder

        /**
         * Sets whether the user account is active.
         * @param isActive true if active, false if disabled.
         * @return The builder instance for chaining.
         */
        public Builder withIsActive(boolean isActive) {
            this.isActive = isActive;
            return this;
        }

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

    /**
     * Checks if the user account is active.
     * @return true if active, false if disabled.
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Sets the active status of the user account.
     * @param active true to enable, false to disable.
     */
    public void setActive(boolean active) {
        this.isActive = active;
    }
}