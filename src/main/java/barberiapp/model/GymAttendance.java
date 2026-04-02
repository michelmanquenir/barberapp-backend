package barberiapp.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "gym_attendance")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class GymAttendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "shop_id", nullable = false, length = 36)
    private String shopId;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Column(name = "check_in_time")
    private LocalTime checkInTime;

    /** boxing | sparring | cardio | funcional | tecnica | libre */
    @Column(name = "class_type", length = 50)
    private String classType;

    @Column(name = "is_trial_class")
    @Builder.Default
    private Boolean isTrialClass = false;

    @Column(name = "notes", length = 300)
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (attendanceDate == null) attendanceDate = LocalDate.now();
        if (checkInTime == null) checkInTime = LocalTime.now().withSecond(0).withNano(0);
    }
}
