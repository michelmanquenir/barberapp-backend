package barberiapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "transport_vehicles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransportVehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false, length = 36)
    private String shopId;

    @Column(nullable = false, length = 100)
    private String brand;

    @Column(nullable = false, length = 100)
    private String model;

    private Integer year;

    @Column(name = "license_plate", length = 20)
    private String licensePlate;

    @Builder.Default
    @Column(name = "passenger_capacity", nullable = false)
    private Integer passengerCapacity = 4;

    @Column(length = 100)
    private String commune;

    @Column(name = "driver_id")
    private Long driverId;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (active == null) active = true;
        if (passengerCapacity == null) passengerCapacity = 4;
    }
}
