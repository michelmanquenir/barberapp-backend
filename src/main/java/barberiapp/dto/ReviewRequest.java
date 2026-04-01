package barberiapp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequest {

    private Long appointmentId;

    /** ID del pedido (para reseñas de compras) */
    private Long orderId;

    /** CLIENT_TO_BARBER | CLIENT_TO_SHOP | BARBER_TO_CLIENT */
    private String reviewType;

    // Targets (solo uno aplica según reviewType)
    private Long targetBarberId;
    private String targetShopId;
    private String targetUserId;

    /** 1–5 */
    private Integer rating;

    /** Opcional */
    private String comment;
}
