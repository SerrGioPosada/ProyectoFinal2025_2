package co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Mercado Pago payment preference information.
 * Contains the data needed to create a checkout preference.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MercadoPagoPreferenceDTO {
    private String preferenceId;
    private String initPoint;        // URL para checkout web
    private String sandboxInitPoint;  // URL para checkout de pruebas
    private String orderId;
    private String title;
    private String description;
    private double totalAmount;
    private String currency;
    private String payerEmail;
}
