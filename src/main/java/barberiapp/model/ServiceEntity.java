package barberiapp.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

@Entity
@Table(name = "services")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer price;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    private Boolean active = true;

    /** Relación con la barbería dueña del servicio (null = servicio global legacy) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id")
    @JsonIgnore
    private BarberShop shop;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null)
            createdAt = LocalDateTime.now();
        if (active == null)
            active = true;
    }
}
