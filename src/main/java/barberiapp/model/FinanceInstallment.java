package barberiapp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "finance_installments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinanceInstallment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Profile user;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;

    @Column(name = "installment_amount", nullable = false)
    private Double installmentAmount;

    @Column(name = "total_installments", nullable = false)
    private Integer totalInstallments;

    @Column(name = "paid_installments", nullable = false)
    private Integer paidInstallments;

    @Column(name = "due_day")
    private Integer dueDay; // día del mes en que vence cada cuota (1-31)

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (paidInstallments == null) paidInstallments = 0;
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
