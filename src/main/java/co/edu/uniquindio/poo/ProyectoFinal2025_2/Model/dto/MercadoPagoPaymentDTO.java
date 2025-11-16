package co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Mercado Pago payment information.
 * Contains the result of a processed payment.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MercadoPagoPaymentDTO {
    private Long paymentId;
    private String status;            // approved, pending, rejected, etc.
    private String statusDetail;
    private String orderId;
    private double transactionAmount;
    private String currency;
    private String paymentMethodId;
    private String paymentTypeId;
    private String payerEmail;
    private LocalDateTime dateCreated;
    private LocalDateTime dateApproved;
    private String externalReference;
}
