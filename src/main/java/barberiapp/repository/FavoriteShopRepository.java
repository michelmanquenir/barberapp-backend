package barberiapp.repository;

import barberiapp.model.FavoriteShop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteShopRepository extends JpaRepository<FavoriteShop, Long> {
    List<FavoriteShop> findByUserId(String userId);
    Optional<FavoriteShop> findByUserIdAndShopId(String userId, String shopId);
    boolean existsByUserIdAndShopId(String userId, String shopId);
}
