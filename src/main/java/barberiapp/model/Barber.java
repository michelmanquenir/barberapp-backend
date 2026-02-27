package barberiapp.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "barbers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Barber {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(precision = 10)
    private Double rating;

    @Column(columnDefinition = "JSON")
    private String specialties; // Or map it properly if needed, depending on Postgres dialect support for JSON

    private Boolean active = true;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null)
            createdAt = LocalDateTime.now();
        if (rating == null)
            rating = 5.00;
        if (active == null)
            active = true;
    }
}
