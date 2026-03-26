package barberiapp.repository;

import barberiapp.model.TransportEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransportEventRepository extends JpaRepository<TransportEvent, Long> {
    List<TransportEvent> findByShopIdOrderByEventDateAsc(String shopId);
    List<TransportEvent> findByShopIdAndActiveOrderByEventDateAsc(String shopId, boolean active);
    Optional<TransportEvent> findByIdAndShopId(Long id, String shopId);
}
