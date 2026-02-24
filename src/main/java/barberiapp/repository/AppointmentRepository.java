package barberiapp.repository;

import barberiapp.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByUserIdOrderByDateDescTimeDesc(String userId);

    List<Appointment> findByUserIdAndStatusOrderByDateDescTimeDesc(String userId, String status);
}
