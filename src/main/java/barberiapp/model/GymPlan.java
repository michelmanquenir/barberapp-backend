package barberiapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "gym_plans")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class GymPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false, length = 36)
    private String shopId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** Precio en moneda local (CLP) */
    @Column(nullable = false)
    private Integer price;

    /** Duración en meses (1, 3, 6, 12…) */
    @Column(name = "duration_months", nullable = false)
    private Integer durationMonths;

    /** null = visitas ilimitadas */
    @Column(name = "visits_allowed")
    private Integer visitsAllowed;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (active == null)    active    = true;
    }
}
