package barberiapp.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "gym_classes")
@Data
public class GymClass {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String shopId;

    @Column(nullable = false)
    private String name;

    private String classType; // boxing, zumba, sparring, funcional, tecnica, libre, otro

    private String instructorName;

    @Column(nullable = false)
    private String dayOfWeek; // MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY

    @Column(nullable = false)
    private String startTime; // "09:00"

    @Column(nullable = false)
    private String endTime; // "10:00"

    private Integer maxCapacity;

    private String description;

    private String color; // hex color for calendar display

    @Column(nullable = false)
    private Boolean active = true;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
