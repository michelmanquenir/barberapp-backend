package barberiapp.repository;

import barberiapp.model.BarberGalleryImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BarberGalleryImageRepository extends JpaRepository<BarberGalleryImage, Long> {
    List<BarberGalleryImage> findByGalleryIdOrderByCreatedAtDesc(Long galleryId);
}
