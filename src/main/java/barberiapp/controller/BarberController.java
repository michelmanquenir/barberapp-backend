package barberiapp.controller;

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
}
