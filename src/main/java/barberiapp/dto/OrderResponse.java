package barberiapp.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponse {
    private Long id;
    private String shopId;
    private String shopName;
    private String clientUserId;
    private String clientName;
    private String status;
    private String deliveryType;
    private String paymentMethod;
    private String clientAddress;
    private Integer totalPrice;
    private String notes;
    private LocalDateTime createdAt;
    private List<OrderItemResponse> items;
}
