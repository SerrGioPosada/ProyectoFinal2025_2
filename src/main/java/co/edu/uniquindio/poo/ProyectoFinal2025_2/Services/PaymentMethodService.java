package co.edu.uniquindio.poo.ProyectoFinal2025_2.Services;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.PaymentMethod;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.PaymentMethodType;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.PaymentProvider;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Repositories.PaymentMethodRepository;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilService.IdGenerationUtil;

import java.util.List;
import java.util.Optional;

/**
 * Provides business logic services related to payment methods management.
 * <p>This service handles CRUD operations for user payment methods.</p>
 */
public class PaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;

    /**
     * Constructor with dependency injection for the repository.
     *
     * @param paymentMethodRepository The PaymentMethodRepository instance.
     */
    public PaymentMethodService(PaymentMethodRepository paymentMethodRepository) {
        this.paymentMethodRepository = paymentMethodRepository;
    }

    /**
     * Default constructor that uses singleton instance.
     */
    public PaymentMethodService() {
        this(PaymentMethodRepository.getInstance());
    }

    // ===========================
    // CRUD Operations
    // ===========================

    /**
     * Creates a new payment method for a user.
     *
     * @param userId        The ID of the user.
     * @param type          The type of payment method.
     * @param provider      The payment provider.
     * @param accountNumber The account or card number (will be masked).
     * @return The created PaymentMethod object.
     */
    public PaymentMethod createPaymentMethod(String userId, PaymentMethodType type, PaymentProvider provider, String accountNumber) {
        // Mask the account number for security (show only last 4 digits)
        String maskedAccountNumber = maskAccountNumber(accountNumber);

        PaymentMethod newMethod = new PaymentMethod.Builder()
                .withId(IdGenerationUtil.generateId())
                .withUserId(userId)
                .withType(type)
                .withProvider(provider)
                .withAccountNumber(maskedAccountNumber)
                .build();

        paymentMethodRepository.addPaymentMethod(newMethod);
        return newMethod;
    }

    /**
     * Updates an existing payment method.
     *
     * @param id            The ID of the payment method to update.
     * @param type          The new type of payment method.
     * @param provider      The new payment provider.
     * @param accountNumber The new account or card number.
     * @return The updated PaymentMethod object.
     * @throws IllegalArgumentException if the payment method is not found.
     */
    public PaymentMethod updatePaymentMethod(String id, PaymentMethodType type, PaymentProvider provider, String accountNumber) {
        PaymentMethod existingMethod = paymentMethodRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payment method not found with ID: " + id));

        // Mask the account number
        String maskedAccountNumber = maskAccountNumber(accountNumber);

        existingMethod.setType(type);
        existingMethod.setProvider(provider);
        existingMethod.setAccountNumber(maskedAccountNumber);

        paymentMethodRepository.updatePaymentMethod(existingMethod);
        return existingMethod;
    }

    /**
     * Deletes a payment method.
     *
     * @param id The ID of the payment method to delete.
     */
    public void deletePaymentMethod(String id) {
        paymentMethodRepository.deletePaymentMethod(id);
    }

    /**
     * Retrieves a payment method by ID.
     *
     * @param id The ID of the payment method.
     * @return An Optional containing the payment method if found.
     */
    public Optional<PaymentMethod> getPaymentMethodById(String id) {
        return paymentMethodRepository.findById(id);
    }

    /**
     * Retrieves all payment methods for a specific user.
     *
     * @param userId The ID of the user.
     * @return A list of payment methods.
     */
    public List<PaymentMethod> getPaymentMethodsByUserId(String userId) {
        return paymentMethodRepository.findByUserId(userId);
    }

    /**
     * Retrieves all payment methods in the system.
     *
     * @return A list of all payment methods.
     */
    public List<PaymentMethod> getAllPaymentMethods() {
        return paymentMethodRepository.findAll();
    }

    // ===========================
    // Helper Methods
    // ===========================

    /**
     * Masks an account number showing only the last 4 digits.
     * Example: "1234567890123456" becomes "************3456"
     *
     * @param accountNumber The original account number.
     * @return The masked account number.
     */
    private String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() <= 4) {
            return accountNumber;
        }

        int visibleDigits = 4;
        int maskedLength = accountNumber.length() - visibleDigits;
        String maskedPart = "*".repeat(maskedLength);
        String visiblePart = accountNumber.substring(accountNumber.length() - visibleDigits);

        return maskedPart + visiblePart;
    }
}
