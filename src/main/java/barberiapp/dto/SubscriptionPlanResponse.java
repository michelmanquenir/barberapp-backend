package barberiapp.dto;

import barberiapp.model.SubscriptionPlan;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SubscriptionPlanResponse {
    private Long id;
    private String shopId;
    private String shopName;
    private String name;
    private String description;
    private Integer price;
    private Integer cutsPerPeriod;
    private Boolean active;
    private LocalDateTime createdAt;

    public static SubscriptionPlanResponse from(SubscriptionPlan p) {
        SubscriptionPlanResponse dto = new SubscriptionPlanResponse();
        dto.setId(p.getId());
        dto.setShopId(p.getShopId());
        dto.setShopName(p.getShopName());
        dto.setName(p.getName());
        dto.setDescription(p.getDescription());
        dto.setPrice(p.getPrice());
        dto.setCutsPerPeriod(p.getCutsPerPeriod());
        dto.setActive(p.getActive());
        dto.setCreatedAt(p.getCreatedAt());
        return dto;
    }
}
