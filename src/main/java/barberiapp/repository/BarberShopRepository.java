package barberiapp.repository;

import barberiapp.model.BarberShop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BarberShopRepository extends JpaRepository<BarberShop, String> {
    List<BarberShop> findByOwnerId(String ownerId);
    List<BarberShop> findByOwnerIdAndActiveTrue(String ownerId);
    Optional<BarberShop> findBySlug(String slug);
}
