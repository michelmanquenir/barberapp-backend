package barberiapp.repository;

import barberiapp.model.TransportDriver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransportDriverRepository extends JpaRepository<TransportDriver, Long> {
    List<TransportDriver> findByShopIdOrderByNameAsc(String shopId);
    Optional<TransportDriver> findByIdAndShopId(Long id, String shopId);
}
