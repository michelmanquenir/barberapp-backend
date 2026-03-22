package barberiapp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Horario de trabajo de un barbero en un negocio específico.
 * Un barbero puede tener distintos turnos (días/horas) en distintas barberías.
 */
@Entity
@Table(
    name = "barber_schedules",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_barber_shop_day",
        columnNames = {"barber_id", "shop_id", "day_of_week"}
    )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BarberSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "barber_id", nullable = false)
    @JsonIgnoreProperties({"bio", "specialties", "imageUrl", "rating", "active", "userId", "createdAt"})
    private Barber barber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "shop_id", nullable = false)
    @JsonIgnoreProperties({"owner", "description", "slug", "address", "latitude", "longitude",
            "active", "approvalStatus", "homeServiceEnabled", "pricePerKm", "categoryId",
            "createdAt", "updatedAt"})
    private BarberShop shop;

    /**
     * Día de la semana en inglés: MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY.
     */
    @Column(name = "day_of_week", length = 10, nullable = false)
    private String dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (active == null)    active    = true;
    }
}
