package barberiapp.repository;

import barberiapp.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /** Reseñas de clientes sobre un barbero (CLIENT_TO_BARBER) */
    List<Review> findByTargetBarberIdAndReviewTypeOrderByCreatedAtDesc(Long targetBarberId, String reviewType);

    /** Reseñas de clientes sobre una barbería (CLIENT_TO_SHOP) */
    List<Review> findByTargetShopIdAndReviewTypeOrderByCreatedAtDesc(String targetShopId, String reviewType);

    /** Reseñas de barberos sobre un cliente (BARBER_TO_CLIENT) */
    List<Review> findByTargetUserIdAndReviewTypeOrderByCreatedAtDesc(String targetUserId, String reviewType);

    /** Todas las reseñas de una cita específica */
    List<Review> findByAppointmentId(Long appointmentId);

    /** Verifica si ya existe una reseña de ese tipo para esa cita */
    boolean existsByAppointmentIdAndReviewType(Long appointmentId, String reviewType);
}
