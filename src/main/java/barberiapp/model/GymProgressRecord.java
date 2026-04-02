package barberiapp.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "gym_progress_records")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class GymProgressRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "shop_id", nullable = false, length = 36)
    private String shopId;

    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    @Column(name = "weight_kg")
    private Double weightKg;

    @Column(name = "height_cm")
    private Double heightCm;

    @Column(name = "body_fat_pct")
    private Double bodyFatPct;

    @Column(name = "chest_cm")
    private Double chestCm;

    @Column(name = "waist_cm")
    private Double waistCm;

    @Column(name = "hips_cm")
    private Double hipsCm;

    @Column(name = "bicep_cm")
    private Double bicepCm;

    @Column(name = "thigh_cm")
    private Double thighCm;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (recordDate == null) recordDate = LocalDate.now();
    }
}
