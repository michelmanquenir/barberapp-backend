package barberiapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "barber_shops")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BarberShop {

    @Id
    @Column(nullable = false, length = 36)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private AppUser owner;

    @Column(unique = true, nullable = false, length = 100)
    private String slug;

    @Column(length = 500)
    private String address;

    private Double latitude;

    private Double longitude;

    @Builder.Default
    private Boolean active = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", length = 20)
    private ApprovalStatus approvalStatus;

    /** Indica si el negocio ofrece servicio a domicilio */
    @Builder.Default
    @Column(name = "home_service_enabled")
    private Boolean homeServiceEnabled = false;

    /** Precio por kilómetro para servicio a domicilio (viaje ida + vuelta) */
    @Builder.Default
    @Column(name = "price_per_km")
    private Integer pricePerKm = 0;

    /** ID de la categoría de negocio (ref. a business_categories) */
    @Column(name = "category_id", length = 36)
    private String categoryId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = LocalDateTime.now();
        if (active == null) active = true;
        if (homeServiceEnabled == null) homeServiceEnabled = false;
        if (pricePerKm == null) pricePerKm = 0;
        if (approvalStatus == null) approvalStatus = ApprovalStatus.PENDING;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
