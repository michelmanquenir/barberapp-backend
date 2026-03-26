package barberiapp.repository;

import barberiapp.model.TransportVehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransportVehicleRepository extends JpaRepository<TransportVehicle, Long> {
    List<TransportVehicle> findByShopIdOrderByBrandAsc(String shopId);
    List<TransportVehicle> findByShopIdAndActiveOrderByBrandAsc(String shopId, boolean active);
    Optional<TransportVehicle> findByIdAndShopId(Long id, String shopId);
}
