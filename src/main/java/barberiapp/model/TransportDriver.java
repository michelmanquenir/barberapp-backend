package barberiapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "transport_drivers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransportDriver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false, length = 36)
    private String shopId;

    @Column(nullable = false)
    private String name;

    @Column(length = 20)
    private String phone;

    @Column(name = "license_number", length = 50)
    private String licenseNumber;

    @Column(name = "license_image_url", columnDefinition = "TEXT")
    private String licenseImageUrl;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    /** Email del conductor (opcional). Si se proporciona al crear, se genera una cuenta de app. */
    @Column(length = 200)
    private String email;

    /** userId vinculado de la app (null = sin cuenta). */
    @Column(name = "user_id", length = 36)
    private String userId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (active == null) active = true;
    }
}
