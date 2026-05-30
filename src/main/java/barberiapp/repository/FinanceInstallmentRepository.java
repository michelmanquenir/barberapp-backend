package barberiapp.repository;

import barberiapp.model.FinanceInstallment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FinanceInstallmentRepository extends JpaRepository<FinanceInstallment, Long> {

    List<FinanceInstallment> findByUserIdOrderByCreatedAtDesc(String userId);

    @Query("SELECT SUM(i.installmentAmount * (i.totalInstallments - i.paidInstallments)) FROM FinanceInstallment i WHERE i.user.id = :userId AND i.paidInstallments < i.totalInstallments")
    Double sumPendingAmountByUserId(@Param("userId") String userId);
}
