package barberiapp.controller;

import barberiapp.model.ProductCategory;
import barberiapp.repository.ProductCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ProductCategoryController {

    private final ProductCategoryRepository repo;

    // ── Público: listado de categorías activas (para el dropdown de productos) ──

    @GetMapping("/api/product-categories")
    public List<ProductCategory> listActive() {
        return repo.findByActiveTrueOrderBySortOrderAsc();
    }

    // ── Super Admin ────────────────────────────────────────────────────────────

    @GetMapping("/api/super-admin/product-categories")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public List<ProductCategory> listAll() {
        return repo.findAllByOrderBySortOrderAsc();
    }

    @PostMapping("/api/super-admin/product-categories")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ProductCategory> create(@RequestBody Map<String, Object> body) {
        ProductCategory cat = ProductCategory.builder()
                .id(UUID.randomUUID().toString())
                .name(str(body, "name"))
                .icon(str(body, "icon"))
                .sortOrder(intVal(body, "sortOrder", 0))
                .active(true)
                .build();
        return ResponseEntity.ok(repo.save(cat));
    }

    @PutMapping("/api/super-admin/product-categories/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ProductCategory> update(@PathVariable String id,
                                                  @RequestBody Map<String, Object> body) {
        ProductCategory cat = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));

        if (body.containsKey("name") && str(body, "name") != null)
            cat.setName(str(body, "name"));
        if (body.containsKey("icon"))
            cat.setIcon(str(body, "icon"));
        if (body.containsKey("sortOrder"))
            cat.setSortOrder(intVal(body, "sortOrder", cat.getSortOrder()));
        if (body.containsKey("active"))
            cat.setActive(Boolean.TRUE.equals(body.get("active")));

        return ResponseEntity.ok(repo.save(cat));
    }

    @DeleteMapping("/api/super-admin/product-categories/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        repo.findById(id).ifPresent(cat -> {
            cat.setActive(false);
            repo.save(cat);
        });
        return ResponseEntity.noContent().build();
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private String str(Map<String, Object> body, String key) {
        Object v = body.get(key);
        return v instanceof String s ? s.trim() : null;
    }

    private int intVal(Map<String, Object> body, String key, int fallback) {
        Object v = body.get(key);
        if (v instanceof Number n) return n.intValue();
        return fallback;
    }
}
