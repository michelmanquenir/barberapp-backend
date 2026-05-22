package barberiapp.dto;

import lombok.Data;

@Data
public class UpdateOrderStatusRequest {
    /** pending | confirmed | ready | delivered | cancelled */
    private String status;
    /** Motivo de cancelación (solo cuando status = "cancelled") */
    private String cancellationReason;
}
