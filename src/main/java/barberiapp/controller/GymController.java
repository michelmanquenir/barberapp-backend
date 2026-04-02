package barberiapp.controller;

import barberiapp.dto.*;
import barberiapp.service.GymService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/gym/shops/{shopId}")
public class GymController {

    private final GymService gymService;

    // ─── MIEMBROS ─────────────────────────────────────────────────────────────

    @GetMapping("/members")
    public ResponseEntity<?> getMembers(@PathVariable String shopId) {
        try {
            return ResponseEntity.ok(gymService.getMembers(shopId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/members/{memberId}")
    public ResponseEntity<?> getMember(@PathVariable String shopId,
                                        @PathVariable Long memberId) {
        try {
            return ResponseEntity.ok(gymService.getMember(memberId, shopId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/members")
    public ResponseEntity<?> createMember(@PathVariable String shopId,
                                           @RequestBody GymMemberRequest req) {
        try {
            return ResponseEntity.ok(gymService.createMember(shopId, req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/members/{memberId}")
    public ResponseEntity<?> updateMember(@PathVariable String shopId,
                                           @PathVariable Long memberId,
                                           @RequestBody GymMemberRequest req) {
        try {
            return ResponseEntity.ok(gymService.updateMember(memberId, shopId, req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/members/{memberId}")
    public ResponseEntity<?> deleteMember(@PathVariable String shopId,
                                           @PathVariable Long memberId) {
        try {
            gymService.deleteMember(memberId, shopId);
            return ResponseEntity.ok(Map.of("message", "Miembro eliminado"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── MEMBRESÍAS ───────────────────────────────────────────────────────────

    @GetMapping("/members/{memberId}/memberships")
    public ResponseEntity<?> getMemberships(@PathVariable String shopId,
                                             @PathVariable Long memberId) {
        try {
            return ResponseEntity.ok(gymService.getMemberships(memberId, shopId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/members/{memberId}/memberships")
    public ResponseEntity<?> createMembership(@PathVariable String shopId,
                                               @PathVariable Long memberId,
                                               @RequestBody GymMembershipRequest req) {
        try {
            return ResponseEntity.ok(gymService.createMembership(memberId, shopId, req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/memberships/{membershipId}")
    public ResponseEntity<?> updateMembership(@PathVariable String shopId,
                                               @PathVariable Long membershipId,
                                               @RequestBody GymMembershipRequest req) {
        try {
            return ResponseEntity.ok(gymService.updateMembership(membershipId, shopId, req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── ASISTENCIA ───────────────────────────────────────────────────────────

    @PostMapping("/attendance")
    public ResponseEntity<?> checkIn(@PathVariable String shopId,
                                      @RequestBody GymAttendanceRequest req) {
        try {
            return ResponseEntity.ok(gymService.checkIn(shopId, req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/attendance/today")
    public ResponseEntity<?> getTodayAttendance(@PathVariable String shopId) {
        try {
            return ResponseEntity.ok(gymService.getTodayAttendance(shopId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/members/{memberId}/attendance")
    public ResponseEntity<?> getMemberAttendance(@PathVariable String shopId,
                                                  @PathVariable Long memberId) {
        try {
            return ResponseEntity.ok(gymService.getMemberAttendance(memberId, shopId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/attendance/{attendanceId}")
    public ResponseEntity<?> deleteAttendance(@PathVariable String shopId,
                                               @PathVariable Long attendanceId) {
        try {
            gymService.deleteAttendance(attendanceId, shopId);
            return ResponseEntity.ok(Map.of("message", "Asistencia eliminada"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── PROGRESO ─────────────────────────────────────────────────────────────

    @GetMapping("/members/{memberId}/progress")
    public ResponseEntity<?> getProgress(@PathVariable String shopId,
                                          @PathVariable Long memberId) {
        try {
            return ResponseEntity.ok(gymService.getProgress(memberId, shopId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/members/{memberId}/progress")
    public ResponseEntity<?> addProgress(@PathVariable String shopId,
                                          @PathVariable Long memberId,
                                          @RequestBody GymProgressRequest req) {
        try {
            return ResponseEntity.ok(gymService.addProgress(memberId, shopId, req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/members/{memberId}/progress/{progressId}")
    public ResponseEntity<?> updateProgress(@PathVariable String shopId,
                                             @PathVariable Long memberId,
                                             @PathVariable Long progressId,
                                             @RequestBody GymProgressRequest req) {
        try {
            return ResponseEntity.ok(gymService.updateProgress(progressId, memberId, shopId, req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/members/{memberId}/progress/{progressId}")
    public ResponseEntity<?> deleteProgress(@PathVariable String shopId,
                                             @PathVariable Long memberId,
                                             @PathVariable Long progressId) {
        try {
            gymService.deleteProgress(progressId, memberId, shopId);
            return ResponseEntity.ok(Map.of("message", "Registro eliminado"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── STATS ────────────────────────────────────────────────────────────────

    @GetMapping("/stats")
    public ResponseEntity<?> getStats(@PathVariable String shopId) {
        try {
            return ResponseEntity.ok(gymService.getStats(shopId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private String getCurrentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
