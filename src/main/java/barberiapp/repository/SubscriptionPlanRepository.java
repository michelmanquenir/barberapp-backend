package barberiapp.repository;

import barberiapp.model.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {
    List<SubscriptionPlan> findByShopIdOrderByPriceAsc(String shopId);
    List<SubscriptionPlan> findByShopIdAndActiveTrueOrderByPriceAsc(String shopId);
}
