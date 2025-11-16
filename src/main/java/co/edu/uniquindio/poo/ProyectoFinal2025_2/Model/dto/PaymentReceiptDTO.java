package co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.dto;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.PaymentStatus;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.PaymentMethodType;
import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Enums.PaymentProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for displaying payment receipt information to users.
 * Contains all necessary information for a payment receipt view.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentReceiptDTO {
    private String paymentId;
    private String invoiceId;
    private String invoiceNumber;
    private String orderId;
    private double amount;
    private LocalDateTime paymentDate;
    private PaymentStatus status;
    private PaymentMethodType paymentMethodType;
    private PaymentProvider paymentProvider;
    private String accountNumber;
}
