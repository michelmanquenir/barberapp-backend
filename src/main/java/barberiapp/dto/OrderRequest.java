package barberiapp.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {
    private String shopId;
    /** pickup | delivery */
    private String deliveryType;
    /** cash | transfer */
    private String paymentMethod;
    /** Requerido si deliveryType = "delivery" */
    private String clientAddress;
    private String notes;
    /** Recargo de delivery calculado en frontend (pricePerKm × km × 2). 0 o null si es retiro */
    private Integer deliveryFee;
    /** Origen: "web" (cliente online) | "pos" (caja / punto de venta) */
    private String source;
    /** ID del barbero/profesional asignado para hacer el delivery */
    private Long assignedBarberId;
    /** Fecha-hora ISO 8601 acordada para el delivery (ej. "2026-03-25T09:00:00") */
    private String scheduledAt;
    private List<OrderItemRequest> items;
}
