package barberiapp.service;

import barberiapp.dto.ReviewRequest;
import barberiapp.dto.ReviewResponse;
import barberiapp.model.Appointment;
import barberiapp.model.Barber;
import barberiapp.model.Profile;
import barberiapp.model.Review;
import barberiapp.repository.AppointmentRepository;
import barberiapp.repository.BarberRepository;
import barberiapp.repository.BarberShopMemberRepository;
import barberiapp.repository.ProfileRepository;
import barberiapp.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final AppointmentRepository appointmentRepository;
    private final BarberRepository barberRepository;
    private final BarberShopMemberRepository barberShopMemberRepository;
    private final ProfileRepository profileRepository;

    // ─── Crear reseña ──────────────────────────────────────────────────────────

    @Transactional
    public ReviewResponse createReview(ReviewRequest req, String currentUserId) {
        // Validar rating
        if (req.getRating() == null || req.getRating() < 1 || req.getRating() > 5) {
            throw new IllegalArgumentException("El rating debe ser entre 1 y 5");
        }

        // Validar reviewType
        String type = req.getReviewType();
        if (type == null || (!type.equals("CLIENT_TO_BARBER") && !type.equals("CLIENT_TO_SHOP") && !type.equals("BARBER_TO_CLIENT"))) {
            throw new IllegalArgumentException("Tipo de reseña inválido");
        }

        // Obtener la cita
        Appointment appointment = appointmentRepository.findById(req.getAppointmentId())
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada"));

        // Solo se puede reseñar citas completadas
        if (!"completed".equals(appointment.getStatus())) {
            throw new IllegalArgumentException("Solo puedes reseñar citas completadas");
        }

        // Validar permisos según el tipo de reseña
        if (type.equals("CLIENT_TO_BARBER") || type.equals("CLIENT_TO_SHOP")) {
            // Solo el cliente de la cita puede dejar esta reseña
            if (appointment.getUser() == null || !appointment.getUser().getId().equals(currentUserId)) {
                throw new IllegalArgumentException("No tienes permiso para dejar esta reseña");
            }
        } else if (type.equals("BARBER_TO_CLIENT")) {
            // Puede dejar la reseña: el propio barbero O el dueño de un negocio al que pertenece ese barbero
            boolean isTheBarber = appointment.getBarber() != null
                    && currentUserId.equals(appointment.getBarber().getUserId());
            boolean isShopOwner = false;
            if (!isTheBarber && appointment.getBarber() != null) {
                isShopOwner = barberShopMemberRepository
                        .findByBarberId(appointment.getBarber().getId())
                        .stream()
                        .filter(m -> Boolean.TRUE.equals(m.getActive()))
                        .anyMatch(m -> m.getShop().getOwner() != null
                                && currentUserId.equals(m.getShop().getOwner().getId()));
            }
            if (!isTheBarber && !isShopOwner) {
                throw new IllegalArgumentException("No tienes permiso para dejar esta reseña");
            }
        }

        // Verificar que no exista duplicado
        if (reviewRepository.existsByAppointmentIdAndReviewType(req.getAppointmentId(), type)) {
            throw new IllegalArgumentException("Ya existe una reseña de este tipo para esta cita");
        }

        // Obtener nombre del reviewer
        String reviewerName = "Anónimo";
        Profile reviewer = profileRepository.findById(currentUserId).orElse(null);
        if (reviewer != null && reviewer.getFullName() != null) {
            reviewerName = reviewer.getFullName();
        }

        // Crear y guardar la reseña
        Review review = new Review();
        review.setAppointmentId(req.getAppointmentId());
        review.setReviewType(type);
        review.setReviewerUserId(currentUserId);
        review.setReviewerName(reviewerName);
        review.setRating(req.getRating());
        review.setComment(req.getComment() != null ? req.getComment().trim() : null);

        // Asignar target según tipo
        if (type.equals("CLIENT_TO_BARBER")) {
            Long barberId = req.getTargetBarberId() != null ? req.getTargetBarberId()
                    : (appointment.getBarber() != null ? appointment.getBarber().getId() : null);
            review.setTargetBarberId(barberId);
        } else if (type.equals("CLIENT_TO_SHOP")) {
            // Si no se envía targetShopId, intentar resolverlo desde la membresía del barbero
            String shopId = req.getTargetShopId();
            if (shopId == null && appointment.getBarber() != null) {
                shopId = barberShopMemberRepository
                        .findByBarberId(appointment.getBarber().getId())
                        .stream()
                        .filter(m -> Boolean.TRUE.equals(m.getActive()))
                        .map(m -> m.getShop().getId())
                        .findFirst()
                        .orElse(null);
            }
            review.setTargetShopId(shopId);
        } else { // BARBER_TO_CLIENT
            String clientUserId = req.getTargetUserId() != null ? req.getTargetUserId()
                    : (appointment.getUser() != null ? appointment.getUser().getId() : null);
            review.setTargetUserId(clientUserId);
        }

        review = reviewRepository.save(review);

        // Actualizar el rating del barbero si es una reseña CLIENT_TO_BARBER
        if (type.equals("CLIENT_TO_BARBER") && review.getTargetBarberId() != null) {
            updateBarberRating(review.getTargetBarberId());
        }

        return toResponse(review);
    }

    // ─── Consultas ─────────────────────────────────────────────────────────────

    public List<ReviewResponse> getBarberReviews(Long barberId) {
        return reviewRepository
                .findByTargetBarberIdAndReviewTypeOrderByCreatedAtDesc(barberId, "CLIENT_TO_BARBER")
                .stream().map(this::toResponse).toList();
    }

    public List<ReviewResponse> getShopReviews(String shopId) {
        return reviewRepository
                .findByTargetShopIdAndReviewTypeOrderByCreatedAtDesc(shopId, "CLIENT_TO_SHOP")
                .stream().map(this::toResponse).toList();
    }

    public List<ReviewResponse> getClientReviews(String userId) {
        return reviewRepository
                .findByTargetUserIdAndReviewTypeOrderByCreatedAtDesc(userId, "BARBER_TO_CLIENT")
                .stream().map(this::toResponse).toList();
    }

    public List<ReviewResponse> getAppointmentReviews(Long appointmentId) {
        return reviewRepository
                .findByAppointmentId(appointmentId)
                .stream().map(this::toResponse).toList();
    }

    // ─── Actualizar rating del barbero ─────────────────────────────────────────

    private void updateBarberRating(Long barberId) {
        List<Review> barberReviews = reviewRepository
                .findByTargetBarberIdAndReviewTypeOrderByCreatedAtDesc(barberId, "CLIENT_TO_BARBER");
        if (barberReviews.isEmpty()) return;
        double avg = barberReviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(5.0);
        barberRepository.findById(barberId).ifPresent(barber -> {
            barber.setRating(Math.round(avg * 10.0) / 10.0);
            barberRepository.save(barber);
        });
    }

    // ─── Mapper ────────────────────────────────────────────────────────────────

    private ReviewResponse toResponse(Review r) {
        ReviewResponse res = new ReviewResponse();
        res.setId(r.getId());
        res.setAppointmentId(r.getAppointmentId());
        res.setReviewType(r.getReviewType());
        res.setReviewerUserId(r.getReviewerUserId());
        res.setReviewerName(r.getReviewerName());
        res.setTargetBarberId(r.getTargetBarberId());
        res.setTargetShopId(r.getTargetShopId());
        res.setTargetUserId(r.getTargetUserId());
        res.setRating(r.getRating());
        res.setComment(r.getComment());
        res.setCreatedAt(r.getCreatedAt());
        return res;
    }
}
