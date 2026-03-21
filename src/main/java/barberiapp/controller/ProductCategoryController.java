package barberiapp.controller;

import barberiapp.dto.ProductCategoryTree;
import barberiapp.model.ProductCategory;
import barberiapp.repository.ProductCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class ProductCategoryController {

    private final ProductCategoryRepository repo;

    // ── Público: árbol de categorías activas ──────────────────────────────────

    @GetMapping("/api/product-categories")
    public List<ProductCategoryTree> listTree() {
        return repo.findByParentIdIsNullAndActiveTrueOrderBySortOrderAsc()
                .stream()
                .map(parent -> {
                    List<ProductCategoryTree> children = repo
                            .findByParentIdAndActiveTrueOrderBySortOrderAsc(parent.getId())
                            .stream()
                            .map(child -> ProductCategoryTree.from(child, List.of()))
                            .collect(Collectors.toList());
                    return ProductCategoryTree.from(parent, children);
                })
                .collect(Collectors.toList());
    }

    // ── Super Admin: lista plana completa ─────────────────────────────────────

    @GetMapping("/api/super-admin/product-categories")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public List<ProductCategory> listAll() {
        return repo.findAllByOrderBySortOrderAsc();
    }

    @PostMapping("/api/super-admin/product-categories")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ProductCategory> create(@RequestBody Map<String, Object> body) {
        String parentId = str(body, "parentId");
        // Validar que el padre existe si se envió
        if (parentId != null && !repo.existsById(parentId)) {
            return ResponseEntity.badRequest().build();
        }
        ProductCategory cat = ProductCategory.builder()
                .id(UUID.randomUUID().toString())
                .name(str(body, "name"))
                .icon(str(body, "icon"))
                .parentId(parentId)
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
        if (body.containsKey("parentId"))
            cat.setParentId(str(body, "parentId")); // null = promover a raíz
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
            // Desactivar también todas las subcategorías
            repo.findByParentIdOrderBySortOrderAsc(id).forEach(child -> {
                child.setActive(false);
                repo.save(child);
            });
        });
        return ResponseEntity.noContent().build();
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private String str(Map<String, Object> body, String key) {
        if (!body.containsKey(key)) return null;
        Object v = body.get(key);
        return v instanceof String s && !s.isBlank() ? s.trim() : null;
    }

    private int intVal(Map<String, Object> body, String key, int fallback) {
        Object v = body.get(key);
        if (v instanceof Number n) return n.intValue();
        return fallback;
    }
}
