package barberiapp.controller;

import barberiapp.dto.GalleryImageRequest;
import barberiapp.dto.GalleryRequest;
import barberiapp.dto.GalleryResponse;
import barberiapp.service.BarberGalleryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BarberGalleryController {

    private final BarberGalleryService galleryService;

    // ── Endpoints privados (barbero autenticado) ────────────────────────────────

    /** GET /api/barbers/me/galleries — mis galerías */
    @GetMapping("/api/barbers/me/galleries")
    public List<GalleryResponse> getMyGalleries() {
        String userId = currentUserId();
        return galleryService.getMyGalleries(userId);
    }

    /** GET /api/barbers/me/galleries/{id} — galería con imágenes */
    @GetMapping("/api/barbers/me/galleries/{id}")
    public ResponseEntity<GalleryResponse> getGallery(@PathVariable Long id) {
        String userId = currentUserId();
        return galleryService.getGallery(userId, id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** POST /api/barbers/me/galleries — crear galería */
    @PostMapping("/api/barbers/me/galleries")
    public ResponseEntity<GalleryResponse> createGallery(@RequestBody GalleryRequest req) {
        String userId = currentUserId();
        GalleryResponse created = galleryService.createGallery(userId, req);
        return ResponseEntity.ok(created);
    }

    /** PUT /api/barbers/me/galleries/{id} — actualizar galería */
    @PutMapping("/api/barbers/me/galleries/{id}")
    public ResponseEntity<GalleryResponse> updateGallery(@PathVariable Long id,
                                                         @RequestBody GalleryRequest req) {
        String userId = currentUserId();
        return galleryService.updateGallery(userId, id, req)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** DELETE /api/barbers/me/galleries/{id} — eliminar galería */
    @DeleteMapping("/api/barbers/me/galleries/{id}")
    public ResponseEntity<Void> deleteGallery(@PathVariable Long id) {
        String userId = currentUserId();
        boolean deleted = galleryService.deleteGallery(userId, id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    /** POST /api/barbers/me/galleries/{id}/images — agregar imagen */
    @PostMapping("/api/barbers/me/galleries/{id}/images")
    public ResponseEntity<GalleryResponse.GalleryImageDto> addImage(@PathVariable Long id,
                                                                      @RequestBody GalleryImageRequest req) {
        String userId = currentUserId();
        return galleryService.addImage(userId, id, req)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** DELETE /api/barbers/me/galleries/{galleryId}/images/{imageId} — eliminar imagen */
    @DeleteMapping("/api/barbers/me/galleries/{galleryId}/images/{imageId}")
    public ResponseEntity<Void> deleteImage(@PathVariable Long galleryId,
                                            @PathVariable Long imageId) {
        String userId = currentUserId();
        boolean deleted = galleryService.deleteImage(userId, galleryId, imageId);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    // ── Endpoints públicos ─────────────────────────────────────────────────────

    /**
     * GET /api/barbers/{barberId}/galleries — galerías públicas de un barbero.
     * Parámetro opcional ?shopId=xxx  filtra por barbería.
     * ?shopId=general  devuelve solo las galerías sin barbería asociada.
     */
    @GetMapping("/api/barbers/{barberId}/galleries")
    public List<GalleryResponse> getPublicGalleries(
            @PathVariable Long barberId,
            @RequestParam(required = false) String shopId) {
        return galleryService.getPublicGalleries(barberId, shopId);
    }

    // ── helper ─────────────────────────────────────────────────────────────────

    private String currentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
