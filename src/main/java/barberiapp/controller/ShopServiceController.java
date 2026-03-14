package barberiapp.controller;

import barberiapp.dto.CreateServiceRequest;
import barberiapp.model.ServiceEntity;
import barberiapp.service.ServiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/shops/{shopId}/services")
@RequiredArgsConstructor
public class ShopServiceController {

    private final ServiceService serviceService;

    /** GET /api/shops/{shopId}/services — público */
    @GetMapping
    public ResponseEntity<List<ServiceEntity>> getShopServices(@PathVariable String shopId) {
        return ResponseEntity.ok(serviceService.getServicesByShop(shopId));
    }

    /** POST /api/shops/{shopId}/services — crear servicio (solo dueño) */
    @PostMapping
    public ResponseEntity<?> createService(
            @PathVariable String shopId,
            @RequestBody CreateServiceRequest req) {
        try {
            String ownerId = getCurrentUserId();
            ServiceEntity created = serviceService.createService(shopId, ownerId, req);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** PUT /api/shops/{shopId}/services/{serviceId} — editar servicio (solo dueño) */
    @PutMapping("/{serviceId}")
    public ResponseEntity<?> updateService(
            @PathVariable String shopId,
            @PathVariable Long serviceId,
            @RequestBody CreateServiceRequest req) {
        try {
            String ownerId = getCurrentUserId();
            ServiceEntity updated = serviceService.updateService(serviceId, shopId, ownerId, req);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** DELETE /api/shops/{shopId}/services/{serviceId} — eliminar servicio (solo dueño) */
    @DeleteMapping("/{serviceId}")
    public ResponseEntity<?> deleteService(
            @PathVariable String shopId,
            @PathVariable Long serviceId) {
        try {
            String ownerId = getCurrentUserId();
            serviceService.deleteService(serviceId, shopId, ownerId);
            return ResponseEntity.ok(Map.of("message", "Servicio eliminado"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private String getCurrentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
