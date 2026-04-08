package barberiapp.repository;

import barberiapp.model.GymClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface GymClassRepository extends JpaRepository<GymClass, Long> {
    List<GymClass> findByShopIdOrderByDayOfWeekAscStartTimeAsc(String shopId);
    List<GymClass> findByShopIdAndActiveOrderByDayOfWeekAscStartTimeAsc(String shopId, Boolean active);
    Optional<GymClass> findByIdAndShopId(Long id, String shopId);
}
