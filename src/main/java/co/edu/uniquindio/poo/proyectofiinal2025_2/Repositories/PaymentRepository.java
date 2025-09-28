package co.edu.uniquindio.poo.proyectofiinal2025_2.Repositories;

import co.edu.uniquindio.poo.proyectofiinal2025_2.Model.Payment;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * <p>Manages the persistence and retrieval of Payment entities.</p>
 * <p>This class is implemented as a Singleton to ensure that there is only one
 * instance managing all payment data in the application.</p>
 */
public class PaymentRepository {

    private static PaymentRepository instance;
    private final List<Payment> payments;

    private PaymentRepository() {
        this.payments = new ArrayList<>();
    }

    public static synchronized PaymentRepository getInstance() {
        if (instance == null) {
            instance = new PaymentRepository();
        }
        return instance;
    }

    public void addPayment(Payment payment) {
        payments.add(payment);
    }

    // Payments often don't have a simple unique ID, so find methods might be more complex.
    // For this example, we'll assume they can be retrieved but not easily searched.
    public List<Payment> findAll() {
        return new ArrayList<>(payments);
    }

    // Update and delete methods would be implemented based on specific business needs.
}
