package co.edu.uniquindio.poo.proyectofinal2025_2.Model;

import co.edu.uniquindio.poo.proyectofinal2025_2.Model.Enums.PaymentMethodType;
import co.edu.uniquindio.poo.proyectofinal2025_2.Model.Enums.PaymentProvider;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Represents a payment method that a user can register.
 * It contains the type (e.g., Credit Card, Cash),
 * and the provider (e.g., Visa, PayPal).
 */
@Getter
@Setter
@ToString
public class PaymentMethod {

    private String id;                       // Unique identifier for the payment method
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
     * @param type          type of payment method (enum)
     * @param provider      provider or brand (enum)
     * @param accountNumber account or card number (can be masked)
     */
    public PaymentMethod(String id, PaymentMethodType type, PaymentProvider provider, String accountNumber) {
        this.id = id;
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
        private PaymentMethodType type;
        private PaymentProvider provider;
        private String accountNumber;

        public Builder withId(String id) {
            this.id = id;
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
}
