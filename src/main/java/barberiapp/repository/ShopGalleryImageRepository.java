package barberiapp.repository;

import barberiapp.model.ShopGalleryImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShopGalleryImageRepository extends JpaRepository<ShopGalleryImage, Long> {

    List<ShopGalleryImage> findByShopIdOrderByDisplayOrderAscCreatedAtAsc(String shopId);

    Optional<ShopGalleryImage> findByIdAndShopId(Long id, String shopId);

    long countByShopId(String shopId);
}
