package barberiapp.dto;

import barberiapp.model.GymPlan;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GymPlanResponse {
    private Long          id;
    private String        name;
    private String        description;
    private Integer       price;
    private Integer       durationMonths;
    private Integer       visitsAllowed;
    private Boolean       active;
    private LocalDateTime createdAt;

    public static GymPlanResponse from(GymPlan p) {
        GymPlanResponse r = new GymPlanResponse();
        r.setId(p.getId());
        r.setName(p.getName());
        r.setDescription(p.getDescription());
        r.setPrice(p.getPrice());
        r.setDurationMonths(p.getDurationMonths());
        r.setVisitsAllowed(p.getVisitsAllowed());
        r.setActive(p.getActive());
        r.setCreatedAt(p.getCreatedAt());
        return r;
    }
}
