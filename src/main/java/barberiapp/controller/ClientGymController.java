package barberiapp.controller;

import barberiapp.service.GymService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/gym")
public class ClientGymController {

    private final GymService gymService;

    public ClientGymController(GymService gymService) {
        this.gymService = gymService;
    }

    @GetMapping("/my-memberships")
    public ResponseEntity<?> getMyMemberships() {
        try {
            String userId = SecurityContextHolder.getContext().getAuthentication().getName();
            return ResponseEntity.ok(gymService.getMyGymMemberships(userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/checkin")
    public ResponseEntity<?> selfCheckIn(@RequestBody Map<String, String> body) {
        try {
            String userId = SecurityContextHolder.getContext().getAuthentication().getName();
            String shopId = body.get("shopId");
            if (shopId == null || shopId.isBlank())
                return ResponseEntity.badRequest().body(Map.of("error", "shopId requerido"));
            return ResponseEntity.ok(gymService.selfCheckIn(userId, shopId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── Clases — cliente ─────────────────────────────────────────────────────

    @GetMapping("/{shopId}/my-class-enrollments")
    public ResponseEntity<?> getMyClassEnrollments(@PathVariable String shopId) {
        try {
            String userId = SecurityContextHolder.getContext().getAuthentication().getName();
            return ResponseEntity.ok(gymService.getMyClassEnrollments(userId, shopId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{shopId}/classes/{classId}/enroll")
    public ResponseEntity<?> selfEnroll(
            @PathVariable String shopId,
            @PathVariable Long classId) {
        try {
            String userId = SecurityContextHolder.getContext().getAuthentication().getName();
            gymService.selfEnrollInClass(userId, shopId, classId);
            return ResponseEntity.ok(Map.of("enrolled", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{shopId}/classes/{classId}/enroll")
    public ResponseEntity<?> selfUnenroll(
            @PathVariable String shopId,
            @PathVariable Long classId) {
        try {
            String userId = SecurityContextHolder.getContext().getAuthentication().getName();
            gymService.selfUnenrollFromClass(userId, shopId, classId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
