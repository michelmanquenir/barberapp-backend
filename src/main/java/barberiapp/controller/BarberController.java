package barberiapp.controller;

import barberiapp.dto.BarberShopResponse;
import barberiapp.dto.CreateBarberAccountRequest;
import barberiapp.dto.CreateBarberRequest;
import barberiapp.model.Barber;
import barberiapp.service.BarberService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/barbers")
public class BarberController {

    private final BarberService barberService;

    public BarberController(BarberService barberService) {
        this.barberService = barberService;
    }

    /** GET /api/barbers?shopId= — listar barberos (todos o filtrados por negocio) */
    @GetMapping
    public List<Barber> getBarbers(@RequestParam(required = false) String shopId) {
        if (shopId != null && !shopId.isBlank()) {
            return barberService.getBarbersByShop(shopId);
        }
        return barberService.getActiveBarbers();
    }

    /** GET /api/barbers/me — perfil del barbero autenticado */
    @GetMapping("/me")
    public ResponseEntity<Barber> getMyProfile() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return barberService.getMyProfile(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** GET /api/barbers/me/shops — negocios donde trabaja el barbero autenticado */
    @GetMapping("/me/shops")
    public ResponseEntity<?> getMyShops() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            List<BarberShopResponse> shops = barberService.getMyShops(userId);
            return ResponseEntity.ok(shops);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** GET /api/barbers/search?q= — buscar barberos registrados por nombre */
    @GetMapping("/search")
    public List<Barber> searchBarbers(@RequestParam(defaultValue = "") String q) {
        return barberService.searchBarbers(q);
    }

    /** PATCH /api/barbers/me/image — actualizar imagen del barbero autenticado */
    @PatchMapping("/me/image")
    public ResponseEntity<Barber> updateMyImage(@RequestBody Map<String, String> body) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        String imageUrl = body.get("imageUrl");
        return barberService.updateBarberImage(userId, imageUrl)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** POST /api/barbers — crear perfil de barbero (autenticado) */
    @PostMapping
    public ResponseEntity<Barber> createBarber(@RequestBody CreateBarberRequest req) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        Barber barber = barberService.createBarberProfile(userId, req.getName(), req.getBio(), req.getImageUrl());
        return ResponseEntity.ok(barber);
    }

    // ─── Admin: gestión de cuentas de empleados ───────────────────────────────

    /**
     * POST /api/admin/shops/{shopId}/barbers/{barberId}/account
     * Crea una cuenta de app para un profesional y la vincula al perfil de barbero.
     * Solo el dueño del negocio puede invocar este endpoint.
     */
    @PostMapping("/admin/shops/{shopId}/barbers/{barberId}/account")
    public ResponseEntity<?> createBarberAccount(
            @PathVariable String shopId,
            @PathVariable Long barberId,
            @RequestBody CreateBarberAccountRequest req) {
        String requesterId = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            if (req.getEmail() == null || req.getEmail().isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "El email es obligatorio"));
            }
            Barber barber = barberService.createAccountForBarber(shopId, barberId, requesterId, req.getEmail(), req.getRut());
            return ResponseEntity.ok(barber);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /api/admin/shops/{shopId}/barbers/{barberId}/account
     * Desvincula la cuenta de app del perfil de barbero.
     * La cuenta de usuario NO se elimina — solo se rompe el vínculo.
     * Solo el dueño del negocio puede invocar este endpoint.
     */
    @DeleteMapping("/admin/shops/{shopId}/barbers/{barberId}/account")
    public ResponseEntity<?> unlinkBarberAccount(
            @PathVariable String shopId,
            @PathVariable Long barberId) {
        String requesterId = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            Barber barber = barberService.unlinkAccountFromBarber(shopId, barberId, requesterId);
            return ResponseEntity.ok(barber);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
