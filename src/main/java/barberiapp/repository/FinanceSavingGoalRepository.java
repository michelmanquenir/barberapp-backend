package barberiapp.repository;

import barberiapp.model.FinanceSavingGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FinanceSavingGoalRepository extends JpaRepository<FinanceSavingGoal, Long> {

    List<FinanceSavingGoal> findByUserIdOrderByCreatedAtDesc(String userId);

    @Query("SELECT SUM(g.currentAmount) FROM FinanceSavingGoal g WHERE g.user.id = :userId")
    Double sumCurrentAmountByUserId(@Param("userId") String userId);
}
