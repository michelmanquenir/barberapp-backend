package barberiapp.repository;

import barberiapp.model.GymPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GymPlanRepository extends JpaRepository<GymPlan, Long> {
    List<GymPlan> findByShopIdOrderByPriceAsc(String shopId);
    Optional<GymPlan> findByIdAndShopId(Long id, String shopId);
}
