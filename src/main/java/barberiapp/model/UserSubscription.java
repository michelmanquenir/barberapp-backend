package barberiapp.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_subscriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ID del cliente suscrito (Profile.id) */
    @Column(name = "user_id", nullable = false)
    private String userId;

    /** Nombre del cliente (desnormalizado) */
    @Column(name = "user_name", length = 150)
    private String userName;

    /** ID de la barbería */
    @Column(name = "shop_id", nullable = false, length = 36)
    private String shopId;

    /** Nombre de la barbería (desnormalizado) */
    @Column(name = "shop_name", length = 150)
    private String shopName;

    /** Plan al que se suscribió */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    private SubscriptionPlan plan;

    /** Nombre del plan al momento de la suscripción (desnormalizado) */
    @Column(name = "plan_name", length = 100)
    private String planName;

    /** Fecha de inicio de la suscripción */
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /** Fecha de vencimiento: startDate + 30 días */
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    /** Cantidad de cortes ya usados en este período */
    @Column(name = "cuts_used", nullable = false)
    private Integer cutsUsed = 0;

    /** Cantidad de cortes permitidos (copiado del plan al suscribirse) */
    @Column(name = "cuts_allowed", nullable = false)
    private Integer cutsAllowed;

    /** Precio cobrado al momento de la suscripción */
    @Column(name = "price_charged", nullable = false)
    private Integer priceCharged;

    /**
     * Estado: "active" | "expired" | "cancelled"
     * Se actualiza automáticamente según endDate.
     */
    @Column(nullable = false, length = 20)
    private String status = "active";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (cutsUsed == null)  cutsUsed  = 0;
        if (status == null)    status    = "active";
    }

    /** Verifica si la suscripción está activa y tiene cortes disponibles */
    @Transient
    public boolean isUsable() {
        return "active".equals(status)
               && LocalDate.now().isBefore(endDate.plusDays(1))
               && cutsUsed < cutsAllowed;
    }

    /** Calcula el estado real según la fecha actual */
    @Transient
    public String computedStatus() {
        if ("cancelled".equals(status)) return "cancelled";
        return LocalDate.now().isAfter(endDate) ? "expired" : "active";
    }
}
