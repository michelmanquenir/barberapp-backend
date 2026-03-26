package barberiapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "passenger_bookings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PassengerBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "assignment_id", nullable = false)
    private Long assignmentId;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Column(name = "passenger_name", nullable = false)
    private String passengerName;

    @Column(name = "client_commune", length = 100)
    private String clientCommune;

    @Builder.Default
    @Column(name = "seats_booked", nullable = false)
    private Integer seatsBooked = 1;

    @Builder.Default
    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null) status = "PENDING";
        if (seatsBooked == null) seatsBooked = 1;
    }
}
