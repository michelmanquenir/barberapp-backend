package barberiapp.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class GymProgressRequest {
    private LocalDate recordDate;
    private Double weightKg;
    private Double heightCm;
    private Double bodyFatPct;
    private Double chestCm;
    private Double waistCm;
    private Double hipsCm;
    private Double bicepCm;
    private Double thighCm;
    private String notes;
}
