package barberiapp.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "gym_memberships")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class GymMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "shop_id", nullable = false, length = 36)
    private String shopId;

    @Column(name = "plan_name", length = 100)
    private String planName;

    @Column(name = "monthly_price")
    private Integer monthlyPrice;

    /** null = ilimitadas */
    @Column(name = "visits_allowed")
    private Integer visitsAllowed;

    @Column(name = "visits_used")
    @Builder.Default
    private Integer visitsUsed = 0;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    /** active | expired | cancelled */
    @Column(length = 20)
    @Builder.Default
    private String status = "active";

    /** paid | pending | overdue */
    @Column(name = "payment_status", length = 20)
    @Builder.Default
    private String paymentStatus = "pending";

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (startDate == null) startDate = LocalDate.now();
        if (endDate == null) endDate = LocalDate.now().plusMonths(1);
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
