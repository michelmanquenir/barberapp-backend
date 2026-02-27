package barberiapp.controller;

import barberiapp.dto.CreateBarberRequest;
import barberiapp.model.Barber;
import barberiapp.service.BarberService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    /** POST /api/barbers — crear perfil de barbero (autenticado) */
    @PostMapping
    public ResponseEntity<Barber> createBarber(@RequestBody CreateBarberRequest req) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        Barber barber = barberService.createBarberProfile(userId, req.getName(), req.getBio(), req.getImageUrl());
        return ResponseEntity.ok(barber);
    }
}
