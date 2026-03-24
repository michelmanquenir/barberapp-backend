package barberiapp.controller;

import barberiapp.dto.GlobalProductDto;
import barberiapp.dto.GlobalProductRequest;
import barberiapp.model.GlobalProduct;
import barberiapp.repository.GlobalProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Endpoints para el catálogo global de productos.
 *
 * GET  /api/admin/global-products?q=monster&limit=10   → búsqueda
 * GET  /api/admin/global-products/barcode/{code}        → buscar por barcode exacto
 * POST /api/admin/global-products                       → crear nueva entrada en el catálogo
 * PUT  /api/admin/global-products/{id}                  → actualizar entrada del catálogo
 */
@RestController
@RequiredArgsConstructor
public class GlobalProductController {

    private final GlobalProductRepository repo;

    /** Busca productos en el catálogo global (por nombre o barcode). */
    @GetMapping("/api/admin/global-products")
    public List<GlobalProductDto> search(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "20") int limit) {

        if (q.isBlank()) {
            return repo.findByActiveTrueOrderByNameAsc(PageRequest.of(0, limit))
                    .stream().map(GlobalProductDto::from).toList();
        }
        return repo.search(q.trim(), PageRequest.of(0, limit))
                .stream().map(GlobalProductDto::from).toList();
    }

    /** Busca un producto en el catálogo por código de barras exacto. */
    @GetMapping("/api/admin/global-products/barcode/{barcode}")
    public ResponseEntity<?> findByBarcode(@PathVariable String barcode) {
        return repo.findByBarcodeAndActiveTrue(barcode)
                .map(gp -> ResponseEntity.ok(GlobalProductDto.from(gp)))
                .orElse(ResponseEntity.notFound().build());
    }

    /** Crea una nueva entrada en el catálogo global. */
    @PostMapping("/api/admin/global-products")
    public ResponseEntity<?> create(@RequestBody GlobalProductRequest req) {
        if (req.getName() == null || req.getName().isBlank())
            return ResponseEntity.badRequest().body(Map.of("error", "El nombre es requerido"));

        // Barcode debe ser único si se provee
        if (req.getBarcode() != null && !req.getBarcode().isBlank()) {
            String bc = req.getBarcode().trim();
            if (repo.findByBarcodeAndActiveTrue(bc).isPresent())
                return ResponseEntity.badRequest().body(
                    Map.of("error", "Ya existe un producto en el catálogo con ese código de barras"));
        }

        GlobalProduct gp = GlobalProduct.builder()
                .name(req.getName().trim())
                .description(req.getDescription() != null && !req.getDescription().isBlank() ? req.getDescription().trim() : null)
                .category(req.getCategory() != null && !req.getCategory().isBlank() ? req.getCategory().trim() : null)
                .imageUrl(req.getImageUrl() != null && !req.getImageUrl().isBlank() ? req.getImageUrl().trim() : null)
                .barcode(req.getBarcode() != null && !req.getBarcode().isBlank() ? req.getBarcode().trim() : null)
                .sku(req.getSku() != null && !req.getSku().isBlank() ? req.getSku().trim() : null)
                .active(true)
                .build();

        return ResponseEntity.ok(GlobalProductDto.from(repo.save(gp)));
    }

    /** Actualiza nombre, imagen, descripción, etc. de una entrada del catálogo. */
    @PutMapping("/api/admin/global-products/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody GlobalProductRequest req) {
        GlobalProduct gp = repo.findById(id)
                .orElse(null);
        if (gp == null) return ResponseEntity.notFound().build();

        if (req.getName() != null && !req.getName().isBlank()) gp.setName(req.getName().trim());
        if (req.getDescription() != null) gp.setDescription(req.getDescription().trim().isEmpty() ? null : req.getDescription().trim());
        if (req.getCategory()    != null) gp.setCategory(req.getCategory().trim().isEmpty() ? null : req.getCategory().trim());
        if (req.getImageUrl()    != null) gp.setImageUrl(req.getImageUrl().trim().isEmpty() ? null : req.getImageUrl().trim());
        if (req.getSku()         != null) gp.setSku(req.getSku().trim().isEmpty() ? null : req.getSku().trim());

        return ResponseEntity.ok(GlobalProductDto.from(repo.save(gp)));
    }
}
