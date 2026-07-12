package barberiapp.dto;

import lombok.Data;

@Data
public class GymPlanRequest {
    private String  name;
    private String  description;
    private Integer price;
    private Integer durationMonths;
    private Integer visitsAllowed;
    private Boolean active;
}
