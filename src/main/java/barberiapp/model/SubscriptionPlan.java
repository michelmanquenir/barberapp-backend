package barberiapp.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "subscription_plans")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Barbería a la que pertenece este plan */
    @Column(name = "shop_id", nullable = false, length = 36)
    private String shopId;

    /** Nombre de la barbería (desnormalizado) */
    @Column(name = "shop_name", length = 150)
    private String shopName;

    /** Nombre del plan (ej: "Plan Básico", "Plan Premium") */
    @Column(nullable = false, length = 100)
    private String name;

    /** Descripción opcional del plan */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** Precio mensual en moneda local (ej: pesos) */
    @Column(nullable = false)
    private Integer price;

    /** Cantidad de cortes permitidos en 30 días */
    @Column(name = "cuts_per_period", nullable = false)
    private Integer cutsPerPeriod;

    /** Si false, el plan no acepta nuevas suscripciones */
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
