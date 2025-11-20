package co.edu.uniquindio.poo.ProyectoFinal2025_2.Model;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.PaymentMethodType;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.PaymentProvider;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a payment method that a user can register.
 * It contains the type (e.g., Credit Card, Cash),
 * and the provider (e.g., Visa, PayPal).
 */
@Getter
@Setter
public class PaymentMethod {

    private String id;                       // Unique identifier for the payment method
    private String userId;                   // ID of the user who owns this payment method
    private PaymentMethodType type;          // Enum type (Credit Card, Debit Card, Cash, etc.)
    private PaymentProvider provider;        // Enum provider (Visa, PayPal, etc.)
    private String accountNumber;            // For cards/accounts (masked for security)

    /**
     * Default constructor.
     */
    public PaymentMethod() {
    }

    /**
     * Constructs a new PaymentMethod with the specified details.
     *
     * @param id            unique identifier for this payment method
     * @param userId        ID of the user who owns this payment method
     * @param type          type of payment method (enum)
     * @param provider      provider or brand (enum)
     * @param accountNumber account or card number (can be masked)
     */
    public PaymentMethod(String id, String userId, PaymentMethodType type, PaymentProvider provider, String accountNumber) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.provider = provider;
        this.accountNumber = accountNumber;
    }

    /**
     * Private constructor for the builder pattern.
     * @param builder The builder instance to construct from.
     */
    private PaymentMethod(Builder builder) {
        this.id = builder.id;
        this.userId = builder.userId;
        this.type = builder.type;
        this.provider = builder.provider;
        this.accountNumber = builder.accountNumber;
    }

    // ======================================
    //               BUILDER
    // ======================================

    /**
     * Static builder class for creating PaymentMethod instances.
     */
    public static class Builder {
        private String id;
        private String userId;
        private PaymentMethodType type;
        private PaymentProvider provider;
        private String accountNumber;

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withUserId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder withType(PaymentMethodType type) {
            this.type = type;
            return this;
        }

        public Builder withProvider(PaymentProvider provider) {
            this.provider = provider;
            return this;
        }

        public Builder withAccountNumber(String accountNumber) {
            this.accountNumber = accountNumber;
            return this;
        }

        /**
         * Creates a new PaymentMethod instance from the builder's properties.
         * @return A new PaymentMethod instance.
         */
        public PaymentMethod build() {
            return new PaymentMethod(this);
        }
    }

    /**
     * Returns a user-friendly string representation of the payment method.
     * Used for display in selection dialogs.
     */
    @Override
    public String toString() {
        String providerName = provider != null ? provider.toString().replace("_", " ") : "N/A";
        String typeName = type != null ? type.toString().replace("_", " ") : "N/A";
        return String.format("%s - %s (%s)", providerName, accountNumber != null ? accountNumber : "N/A", typeName);
    }
}
