package barberiapp.repository;

import barberiapp.model.GymMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface GymMembershipRepository extends JpaRepository<GymMembership, Long> {
    List<GymMembership> findByMemberIdOrderByCreatedAtDesc(Long memberId);
    Optional<GymMembership> findFirstByMemberIdAndStatusOrderByEndDateDesc(Long memberId, String status);
    List<GymMembership> findByShopIdAndStatusOrderByEndDateAsc(String shopId, String status);
    long countByShopIdAndStatus(String shopId, String status);
}
