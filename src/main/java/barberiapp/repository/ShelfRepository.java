package barberiapp.repository;

import barberiapp.model.Shelf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShelfRepository extends JpaRepository<Shelf, Long> {

    List<Shelf> findByShopIdOrderByNameAsc(String shopId);

    Optional<Shelf> findByIdAndShopId(Long id, String shopId);
}
