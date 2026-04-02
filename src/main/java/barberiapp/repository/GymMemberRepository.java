package barberiapp.repository;

import barberiapp.model.GymMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface GymMemberRepository extends JpaRepository<GymMember, Long> {
    List<GymMember> findByShopIdOrderByNameAsc(String shopId);
    List<GymMember> findByShopIdAndStatusOrderByNameAsc(String shopId, String status);
    Optional<GymMember> findByIdAndShopId(Long id, String shopId);
    long countByShopId(String shopId);
    long countByShopIdAndStatus(String shopId, String status);
}
