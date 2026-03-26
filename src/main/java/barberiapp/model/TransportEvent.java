package barberiapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "transport_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransportEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false, length = 36)
    private String shopId;

    @Column(name = "event_code", nullable = false, length = 50)
    private String eventCode;

    @Column(nullable = false)
    private String title;

    @Column(length = 500)
    private String address;

    @Column(name = "event_date")
    private LocalDateTime eventDate;

    @Column(name = "banner_image_url", columnDefinition = "TEXT")
    private String bannerImageUrl;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (active == null) active = true;
    }
}
