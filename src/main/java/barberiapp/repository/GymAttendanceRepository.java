package barberiapp.repository;

import barberiapp.model.GymAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface GymAttendanceRepository extends JpaRepository<GymAttendance, Long> {
    List<GymAttendance> findByMemberIdOrderByAttendanceDateDescCreatedAtDesc(Long memberId);
    List<GymAttendance> findByShopIdAndAttendanceDateOrderByCheckInTimeAsc(String shopId, LocalDate date);
    List<GymAttendance> findByMemberIdAndAttendanceDateBetweenOrderByAttendanceDateAsc(Long memberId, LocalDate from, LocalDate to);
    long countByMemberId(Long memberId);
    long countByMemberIdAndIsTrialClass(Long memberId, Boolean isTrialClass);
    boolean existsByMemberIdAndAttendanceDate(Long memberId, LocalDate date);
}
