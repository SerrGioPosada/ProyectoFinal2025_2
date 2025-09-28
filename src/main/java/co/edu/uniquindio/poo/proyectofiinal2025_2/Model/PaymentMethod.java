package co.edu.uniquindio.poo.proyectofiinal2025_2.Model;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Enums.PaymentMethodType;
import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Enums.PaymentProvider;

/**
 * Represents a payment method that a user can register.
 * It contains the type (e.g., Credit Card, Cash),
 * and the provider (e.g., Visa, PayPal).
 */
public class PaymentMethod {

    private String id;                       // Unique identifier for the payment method
    private PaymentMethodType type;          // Enum type (Credit Card, Debit Card, Cash, etc.)
    private PaymentProvider provider;        // Enum provider (Visa, PayPal, etc.)
    private String accountNumber;            // For cards/accounts (masked for security)

    /**
     * Constructs a new PaymentMethod.
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

    // ======================
    // Getters
    // ======================

    public String getId() {
        return id;
    }

    public PaymentMethodType getType() {
        return type;
    }

    public PaymentProvider getProvider() {
        return provider;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    // ======================
    // Setters
    // ======================

    public void setId(String id) {
        this.id = id;
    }

    public void setType(PaymentMethodType type) {
        this.type = type;
    }

    public void setProvider(PaymentProvider provider) {
        this.provider = provider;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    // ======================
    // ToString
    // ======================

    @Override
    public String toString() {
        return "PaymentMethod{" +
                "id='" + id + '\'' +
                ", type=" + type +
                ", provider=" + provider +
                ", accountNumber='" + (accountNumber != null ? "****" + accountNumber.substring(accountNumber.length() - 4) : "N/A") + '\'' +
                '}';
    }
}
