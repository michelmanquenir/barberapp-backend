package barberiapp.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ID de la cita (nullable si es reseña de pedido) */
    @Column(name = "appointment_id")
    private Long appointmentId;

    /** ID del pedido (nullable si es reseña de cita) */
    @Column(name = "order_id")
    private Long orderId;

    /**
     * Tipo de reseña:
     *   CLIENT_TO_BARBER  → cliente evalúa al barbero  (pública)
     *   CLIENT_TO_SHOP    → cliente evalúa la barbería (pública)
     *   BARBER_TO_CLIENT  → barbero evalúa al cliente  (semi-privada)
     */
    @Column(name = "review_type", length = 30, nullable = false)
    private String reviewType;

    /** ID del usuario que deja la reseña (Profile.id = UUID String) */
    @Column(name = "reviewer_user_id", nullable = false, length = 36)
    private String reviewerUserId;

    @Column(name = "reviewer_name", length = 100)
    private String reviewerName;

    /** Para CLIENT_TO_BARBER */
    @Column(name = "target_barber_id")
    private Long targetBarberId;

    /** Para CLIENT_TO_SHOP */
    @Column(name = "target_shop_id", length = 36)
    private String targetShopId;

    /** Para BARBER_TO_CLIENT (userId del cliente) */
    @Column(name = "target_user_id", length = 36)
    private String targetUserId;

    /** Rating del 1 al 5 */
    @Column(nullable = false)
    private Integer rating;

    /** Comentario opcional (max ~500 chars en DB) */
    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
