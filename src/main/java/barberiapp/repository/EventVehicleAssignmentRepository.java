package barberiapp.repository;

import barberiapp.model.EventVehicleAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventVehicleAssignmentRepository extends JpaRepository<EventVehicleAssignment, Long> {
    List<EventVehicleAssignment> findByEventId(Long eventId);
    List<EventVehicleAssignment> findByVehicleId(Long vehicleId);
    List<EventVehicleAssignment> findByDriverId(Long driverId);
    Optional<EventVehicleAssignment> findByEventIdAndVehicleId(Long eventId, Long vehicleId);
    Optional<EventVehicleAssignment> findByEventIdAndDriverId(Long eventId, Long driverId);
}
