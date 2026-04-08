package barberiapp.repository;

import barberiapp.model.GymClassEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface GymClassEnrollmentRepository extends JpaRepository<GymClassEnrollment, Long> {
    List<GymClassEnrollment> findByClassIdOrderByEnrolledAtAsc(Long classId);
    List<GymClassEnrollment> findByMemberIdAndShopId(Long memberId, String shopId);
    Optional<GymClassEnrollment> findByClassIdAndMemberId(Long classId, Long memberId);
    boolean existsByClassIdAndMemberId(Long classId, Long memberId);
    long countByClassId(Long classId);
    void deleteByClassId(Long classId);
}
