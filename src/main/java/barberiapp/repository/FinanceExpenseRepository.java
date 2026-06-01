package barberiapp.repository;

import barberiapp.model.FinanceExpense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FinanceExpenseRepository extends JpaRepository<FinanceExpense, Long> {

    List<FinanceExpense> findByUserIdOrderByDateDesc(String userId);

    // Incluye gastos del mes actual + gastos periódicos registrados en cualquier mes anterior
    @Query("""
        SELECT SUM(e.amount) FROM FinanceExpense e
        WHERE e.user.id = :userId
        AND (
            (e.date BETWEEN :from AND :to AND (e.recurring IS NULL OR e.recurring = false))
            OR (e.recurring = true AND e.date <= :to)
        )
        """)
    Double sumByUserIdAndDateBetween(@Param("userId") String userId,
                                     @Param("from") LocalDate from,
                                     @Param("to") LocalDate to);

    @Query("""
        SELECT e.category, SUM(e.amount) FROM FinanceExpense e
        WHERE e.user.id = :userId
        AND (
            (e.date BETWEEN :from AND :to AND (e.recurring IS NULL OR e.recurring = false))
            OR (e.recurring = true AND e.date <= :to)
        )
        GROUP BY e.category
        """)
    List<Object[]> sumByCategoryAndPeriod(@Param("userId") String userId,
                                           @Param("from") LocalDate from,
                                           @Param("to") LocalDate to);
}
