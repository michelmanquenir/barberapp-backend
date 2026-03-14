package barberiapp.repository;

import barberiapp.model.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, Long> {
    List<ServiceEntity> findByActiveTrue();
    List<ServiceEntity> findByActiveTrueOrActiveIsNull();

    /** Servicios activos de una barbería específica */
    List<ServiceEntity> findByShopIdAndActiveTrue(String shopId);
}
