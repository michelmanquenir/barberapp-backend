package barberiapp.dto;

import lombok.Data;

@Data
public class SubscriptionPlanRequest {
    private String name;
    private String description;
    private Integer price;
    private Integer cutsPerPeriod;
    private Boolean active;
}
