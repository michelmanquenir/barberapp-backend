package barberiapp.repository;

import barberiapp.model.Barber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BarberRepository extends JpaRepository<Barber, Long> {
    List<Barber> findByActiveTrue();
    Optional<Barber> findByUserId(String userId);
    List<Barber> findByNameContainingIgnoreCaseAndActiveTrue(String name);
}
