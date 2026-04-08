package barberiapp.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "gym_class_enrollments",
       uniqueConstraints = @UniqueConstraint(columnNames = {"class_id", "member_id"}))
@Data
public class GymClassEnrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "class_id", nullable = false)
    private Long classId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private String shopId;

    @CreationTimestamp
    private LocalDateTime enrolledAt;
}
