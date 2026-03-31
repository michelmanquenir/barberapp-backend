package barberiapp.controller;

import barberiapp.dto.ShopGalleryImageDto;
import barberiapp.service.ShopGalleryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/shops/{shopId}/gallery")
@RequiredArgsConstructor
public class ShopGalleryController {

    private final ShopGalleryService galleryService;

    // ── GET público ───────────────────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<List<ShopGalleryImageDto>> getGallery(
            @PathVariable String shopId) {
        return ResponseEntity.ok(galleryService.getImages(shopId));
    }

    // ── POST — agregar imagen (dueño autenticado) ─────────────────────────────
    @PostMapping
    public ResponseEntity<ShopGalleryImageDto> addImage(
            @PathVariable String shopId,
            @RequestBody Map<String, String> body) {
        String userId  = userId();
        String imageUrl = body.get("imageUrl");
        String caption  = body.getOrDefault("caption", null);

        if (imageUrl == null || imageUrl.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        ShopGalleryImageDto dto = galleryService.addImage(userId, shopId, imageUrl, caption);
        return ResponseEntity.ok(dto);
    }

    // ── DELETE — eliminar imagen (dueño autenticado) ──────────────────────────
    @DeleteMapping("/{imageId}")
    public ResponseEntity<Void> deleteImage(
            @PathVariable String shopId,
            @PathVariable Long imageId) {
        galleryService.deleteImage(userId(), shopId, imageId);
        return ResponseEntity.noContent().build();
    }

    // ── PATCH — actualizar caption (dueño autenticado) ────────────────────────
    @PatchMapping("/{imageId}")
    public ResponseEntity<ShopGalleryImageDto> updateCaption(
            @PathVariable String shopId,
            @PathVariable Long imageId,
            @RequestBody Map<String, String> body) {
        String caption = body.getOrDefault("caption", "");
        ShopGalleryImageDto dto = galleryService.updateCaption(userId(), shopId, imageId, caption);
        return ResponseEntity.ok(dto);
    }

    // ── Helper ────────────────────────────────────────────────────────────────
    private String userId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
