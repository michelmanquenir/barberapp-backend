package barberiapp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "finance_incomes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinanceIncome {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Profile user;

    @Column(nullable = false, length = 20)
    private String type; // SALARY, EXTRA

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private LocalDate date;

    /**
     * Si es true, el ingreso se repite cada mes (ej: sueldo, arriendo recibido).
     * Nullable en BD para compatibilidad con filas existentes (null = false).
     */
    @Column
    private Boolean recurring;

    /**
     * Cantidad de meses durante los cuales se recibirá este ingreso.
     * Solo relevante cuando recurring = false y se quiere un ingreso temporal.
     */
    @Column(name = "duration_months")
    private Integer durationMonths;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /** Devuelve false si recurring es null (filas antiguas sin el campo). */
    public boolean isRecurring() {
        return Boolean.TRUE.equals(recurring);
    }

    @PrePersist
    protected void onCreate() {
        if (date == null) date = LocalDate.now();
        if (recurring == null) recurring = false;
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
