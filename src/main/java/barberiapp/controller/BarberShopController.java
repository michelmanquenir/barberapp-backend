package barberiapp.controller;

import barberiapp.dto.BarberShopResponse;
import barberiapp.dto.CreateShopRequest;
import barberiapp.model.Barber;
import barberiapp.service.BarberShopService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/shops")
public class BarberShopController {

    private final BarberShopService barberShopService;

    public BarberShopController(BarberShopService barberShopService) {
        this.barberShopService = barberShopService;
    }

    /** GET /api/shops/my — negocios del usuario autenticado */
    @GetMapping("/my")
    public ResponseEntity<List<BarberShopResponse>> getMyShops() {
        String userId = getCurrentUserId();
        return ResponseEntity.ok(barberShopService.getShopsByOwner(userId));
    }

    /** POST /api/shops — crear negocio */
    @PostMapping
    public ResponseEntity<?> createShop(@RequestBody CreateShopRequest req) {
        try {
            String userId = getCurrentUserId();
            BarberShopResponse response = barberShopService.createShop(userId, req);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** GET /api/shops/{slug} — info pública del negocio */
    @GetMapping("/{slug}")
    public ResponseEntity<?> getShopBySlug(@PathVariable String slug) {
        try {
            return ResponseEntity.ok(barberShopService.getShopBySlug(slug));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** GET /api/shops/{slug}/barbers — barberos del negocio (público) */
    @GetMapping("/{slug}/barbers")
    public ResponseEntity<List<Barber>> getShopBarbers(@PathVariable String slug) {
        BarberShopResponse shop = barberShopService.getShopBySlug(slug);
        return ResponseEntity.ok(shop.getBarbers());
    }

    /** POST /api/shops/{shopId}/members/{barberId} — agregar barbero */
    @PostMapping("/{shopId}/members/{barberId}")
    public ResponseEntity<?> addBarber(@PathVariable String shopId, @PathVariable Long barberId) {
        try {
            String userId = getCurrentUserId();
            barberShopService.addBarberToShop(shopId, barberId, userId);
            return ResponseEntity.ok(Map.of("message", "Barbero agregado al negocio"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** DELETE /api/shops/{shopId}/members/{barberId} — quitar barbero */
    @DeleteMapping("/{shopId}/members/{barberId}")
    public ResponseEntity<?> removeBarber(@PathVariable String shopId, @PathVariable Long barberId) {
        try {
            String userId = getCurrentUserId();
            barberShopService.removeBarberFromShop(shopId, barberId, userId);
            return ResponseEntity.ok(Map.of("message", "Barbero removido del negocio"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private String getCurrentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
