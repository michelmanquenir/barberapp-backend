package barberiapp.repository;

import barberiapp.model.FinanceIncome;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FinanceIncomeRepository extends JpaRepository<FinanceIncome, Long> {

    List<FinanceIncome> findByUserIdOrderByDateDesc(String userId);

    // Incluye ingresos del mes actual + ingresos periódicos registrados en cualquier mes anterior
    @Query("""
        SELECT SUM(i.amount) FROM FinanceIncome i
        WHERE i.user.id = :userId
        AND (
            (i.date BETWEEN :from AND :to AND (i.recurring IS NULL OR i.recurring = false))
            OR (i.recurring = true AND i.date <= :to)
        )
        """)
    Double sumByUserIdAndDateBetween(@Param("userId") String userId,
                                    @Param("from") LocalDate from,
                                    @Param("to") LocalDate to);

    @Query("""
        SELECT i.type, SUM(i.amount) FROM FinanceIncome i
        WHERE i.user.id = :userId
        AND (
            (i.date BETWEEN :from AND :to AND (i.recurring IS NULL OR i.recurring = false))
            OR (i.recurring = true AND i.date <= :to)
        )
        GROUP BY i.type
        """)
    List<Object[]> sumByTypeAndPeriod(@Param("userId") String userId,
                                      @Param("from") LocalDate from,
                                      @Param("to") LocalDate to);
}
