package barberiapp.controller;

import barberiapp.dto.ReviewRequest;
import barberiapp.dto.ReviewResponse;
import barberiapp.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // ─── Crear reseña ──────────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<?> createReview(@RequestBody ReviewRequest request) {
        try {
            String currentUserId = getCurrentUserId();
            ReviewResponse review = reviewService.createReview(request, currentUserId);
            return ResponseEntity.ok(review);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── Consultas públicas ────────────────────────────────────────────────────

    /** Reseñas de clientes sobre un barbero — público */
    @GetMapping("/barber/{barberId}")
    public List<ReviewResponse> getBarberReviews(@PathVariable Long barberId) {
        return reviewService.getBarberReviews(barberId);
    }

    /** Reseñas de clientes sobre una barbería — público */
    @GetMapping("/shop/{shopId}")
    public List<ReviewResponse> getShopReviews(@PathVariable String shopId) {
        return reviewService.getShopReviews(shopId);
    }

    // ─── Consultas privadas ────────────────────────────────────────────────────

    /** Reseñas de barberos sobre un cliente — solo cliente/barberos */
    @GetMapping("/client/{userId}")
    public ResponseEntity<?> getClientReviews(@PathVariable String userId) {
        try {
            List<ReviewResponse> reviews = reviewService.getClientReviews(userId);
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** Todas las reseñas de una cita — requiere auth */
    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<?> getAppointmentReviews(@PathVariable Long appointmentId) {
        try {
            List<ReviewResponse> reviews = reviewService.getAppointmentReviews(appointmentId);
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── Helper ────────────────────────────────────────────────────────────────

    private String getCurrentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
