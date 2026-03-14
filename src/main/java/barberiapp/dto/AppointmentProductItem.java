package barberiapp.dto;

import lombok.Data;

/**
 * DTO para representar un producto dentro de una solicitud de cita.
 */
@Data
public class AppointmentProductItem {
    /** ID del producto a agregar */
    private Long productId;
    /** Cantidad solicitada (mínimo 1) */
    private Integer quantity;
}
