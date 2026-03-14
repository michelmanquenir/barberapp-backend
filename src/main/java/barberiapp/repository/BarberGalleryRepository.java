package barberiapp.repository;

import barberiapp.model.BarberGallery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BarberGalleryRepository extends JpaRepository<BarberGallery, Long> {
    /** Todas las galerías de un barbero (uso del barbero autenticado) */
    List<BarberGallery> findByBarberIdOrderByCreatedAtDesc(Long barberId);

    /** Solo las galerías visibles de un barbero (uso público/cliente) */
    List<BarberGallery> findByBarberIdAndHiddenFalseOrderByCreatedAtDesc(Long barberId);
}
