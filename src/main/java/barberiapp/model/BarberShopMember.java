package barberiapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "barber_shop_members")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BarberShopMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private BarberShop shop;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "barber_id", nullable = false)
    private Barber barber;

    @Builder.Default
    private Boolean active = true;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @PrePersist
    protected void onCreate() {
        if (joinedAt == null) joinedAt = LocalDateTime.now();
        if (active == null) active = true;
    }
}
