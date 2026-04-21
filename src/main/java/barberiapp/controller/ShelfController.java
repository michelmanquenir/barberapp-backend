package barberiapp.controller;

import barberiapp.dto.*;
import barberiapp.service.ShelfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ShelfController {

    private final ShelfService shelfService;

    /**
     * GET /api/admin/shops/{shopId}/shelves
     * Lista todas las estanterías del negocio con conteo de slots ocupados.
     */
    @GetMapping("/api/admin/shops/{shopId}/shelves")
    public ResponseEntity<List<ShelfResponse>> getShelves(@PathVariable String shopId) {
        return ResponseEntity.ok(shelfService.getShelves(shopId));
    }

    /**
     * GET /api/admin/shops/{shopId}/shelves/{shelfId}/grid
     * Devuelve la grilla completa de una estantería (slots + producto en cada uno).
     */
    @GetMapping("/api/admin/shops/{shopId}/shelves/{shelfId}/grid")
    public ResponseEntity<?> getGrid(@PathVariable String shopId,
                                      @PathVariable Long shelfId) {
        try {
            return ResponseEntity.ok(shelfService.getGrid(shopId, shelfId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * POST /api/admin/shops/{shopId}/shelves
     * Crea una estantería y genera sus slots automáticamente.
     */
    @PostMapping("/api/admin/shops/{shopId}/shelves")
    public ResponseEntity<?> createShelf(@PathVariable String shopId,
                                          @RequestBody ShelfRequest req) {
        try {
            return ResponseEntity.ok(shelfService.createShelf(shopId, req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PUT /api/admin/shops/{shopId}/shelves/{shelfId}
     * Actualiza nombre y descripción de una estantería.
     */
    @PutMapping("/api/admin/shops/{shopId}/shelves/{shelfId}")
    public ResponseEntity<?> updateShelf(@PathVariable String shopId,
                                          @PathVariable Long shelfId,
                                          @RequestBody ShelfRequest req) {
        try {
            return ResponseEntity.ok(shelfService.updateShelf(shopId, shelfId, req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /api/admin/shops/{shopId}/shelves/{shelfId}
     * Elimina la estantería y desvincula los productos que tenían slots en ella.
     */
    @DeleteMapping("/api/admin/shops/{shopId}/shelves/{shelfId}")
    public ResponseEntity<?> deleteShelf(@PathVariable String shopId,
                                          @PathVariable Long shelfId) {
        try {
            shelfService.deleteShelf(shopId, shelfId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PATCH /api/admin/shops/{shopId}/shelf-slots/{slotId}/label
     * Actualiza la etiqueta descriptiva de un slot (ej: "Lapiceras azules").
     */
    @PatchMapping("/api/admin/shops/{shopId}/shelf-slots/{slotId}/label")
    public ResponseEntity<?> updateSlotLabel(@PathVariable String shopId,
                                              @PathVariable Long slotId,
                                              @RequestBody ShelfSlotLabelRequest req) {
        try {
            return ResponseEntity.ok(shelfService.updateSlotLabel(shopId, slotId, req.getLabel()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
