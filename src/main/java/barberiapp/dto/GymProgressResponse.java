package barberiapp.dto;

import barberiapp.model.GymProgressRecord;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class GymProgressResponse {
    private Long id;
    private Long memberId;
    private String shopId;
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
    private LocalDateTime createdAt;
    // Computed: IMC
    private Double bmi;

    public static GymProgressResponse from(GymProgressRecord p) {
        GymProgressResponse r = new GymProgressResponse();
        r.setId(p.getId());
        r.setMemberId(p.getMemberId());
        r.setShopId(p.getShopId());
        r.setRecordDate(p.getRecordDate());
        r.setWeightKg(p.getWeightKg());
        r.setHeightCm(p.getHeightCm());
        r.setBodyFatPct(p.getBodyFatPct());
        r.setChestCm(p.getChestCm());
        r.setWaistCm(p.getWaistCm());
        r.setHipsCm(p.getHipsCm());
        r.setBicepCm(p.getBicepCm());
        r.setThighCm(p.getThighCm());
        r.setNotes(p.getNotes());
        r.setCreatedAt(p.getCreatedAt());
        if (p.getWeightKg() != null && p.getHeightCm() != null && p.getHeightCm() > 0) {
            double heightM = p.getHeightCm() / 100.0;
            r.setBmi(Math.round((p.getWeightKg() / (heightM * heightM)) * 10.0) / 10.0);
        }
        return r;
    }
}
