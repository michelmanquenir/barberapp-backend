package barberiapp.repository;

import barberiapp.model.PassengerBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PassengerBookingRepository extends JpaRepository<PassengerBooking, Long> {
    List<PassengerBooking> findByAssignmentIdOrderByCreatedAtDesc(Long assignmentId);
    List<PassengerBooking> findByUserIdOrderByCreatedAtDesc(String userId);
    int countByAssignmentIdAndStatusNot(Long assignmentId, String status);
    Optional<PassengerBooking> findByIdAndUserId(Long id, String userId);

    @Query("SELECT COALESCE(SUM(b.seatsBooked), 0) FROM PassengerBooking b WHERE b.assignmentId = :assignmentId AND b.status <> 'CANCELLED'")
    int sumBookedSeatsByAssignmentId(@Param("assignmentId") Long assignmentId);

    List<PassengerBooking> findByAssignmentIdIn(List<Long> assignmentIds);

    /**
     * Batch: total de asientos reservados por assignmentId (excluye canceladas).
     * Retorna List de Object[] donde [0] = assignmentId (Long), [1] = sum (Long).
     */
    @Query("SELECT b.assignmentId, COALESCE(SUM(b.seatsBooked), 0) " +
           "FROM PassengerBooking b " +
           "WHERE b.assignmentId IN :assignmentIds AND b.status <> 'CANCELLED' " +
           "GROUP BY b.assignmentId")
    List<Object[]> sumBookedSeatsByAssignmentIds(@Param("assignmentIds") List<Long> assignmentIds);
}
