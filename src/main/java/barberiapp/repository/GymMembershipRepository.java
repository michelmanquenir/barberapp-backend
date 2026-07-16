package barberiapp.repository;

import barberiapp.model.GymMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface GymMembershipRepository extends JpaRepository<GymMembership, Long> {
    List<GymMembership> findByMemberIdOrderByCreatedAtDesc(Long memberId);
    Optional<GymMembership> findFirstByMemberIdAndStatusOrderByEndDateDesc(Long memberId, String status);
    List<GymMembership> findByShopIdAndStatusOrderByEndDateAsc(String shopId, String status);
    long countByShopIdAndStatus(String shopId, String status);
    /** Membresías activas cuya fecha de vencimiento ya pasó */
    List<GymMembership> findByShopIdAndStatusAndEndDateBefore(String shopId, String status, LocalDate date);
    /** Membresías activas de un miembro cuya fecha de vencimiento ya pasó */
    List<GymMembership> findByMemberIdAndStatusAndEndDateBefore(Long memberId, String status, LocalDate date);
}
