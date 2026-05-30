package barberiapp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "finance_expenses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinanceExpense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Profile user;

    @Column(nullable = false, length = 50)
    private String category; // COMIDA, TRANSPORTE, OCIO, SALUD, EDUCACION, HOGAR, OTRO

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private LocalDate date;

    /**
     * Si es true, el gasto se repite cada mes (ej: streaming, gym).
     */
    @Column(nullable = false)
    private Boolean recurring = false;

    /**
     * Número de cuota actual (ej: 10).
     * Solo se usa cuando el gasto es parte de un plan en cuotas.
     */
    @Column(name = "installment_number")
    private Integer installmentNumber;

    /**
     * Total de cuotas del plan (ej: 30).
     * Solo se usa cuando el gasto es parte de un plan en cuotas.
     */
    @Column(name = "installment_total")
    private Integer installmentTotal;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (date == null) date = LocalDate.now();
        if (recurring == null) recurring = false;
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
