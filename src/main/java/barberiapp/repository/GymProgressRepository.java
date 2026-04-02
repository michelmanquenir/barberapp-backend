package barberiapp.repository;

import barberiapp.model.GymProgressRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface GymProgressRepository extends JpaRepository<GymProgressRecord, Long> {
    List<GymProgressRecord> findByMemberIdOrderByRecordDateDesc(Long memberId);
    Optional<GymProgressRecord> findFirstByMemberIdOrderByRecordDateDesc(Long memberId);
    Optional<GymProgressRecord> findByIdAndMemberId(Long id, Long memberId);
}
