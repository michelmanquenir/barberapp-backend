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
    private List<OrderItemRequest> items;
}
