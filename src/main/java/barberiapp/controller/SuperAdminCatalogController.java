package barberiapp.controller;

import barberiapp.dto.GlobalProductDto;
import barberiapp.model.GlobalProduct;
import barberiapp.repository.GlobalProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

/**
 * Super-admin endpoints para gestionar el catálogo global de productos.
 *
 * GET  /api/super-admin/global-products?q=&page=0&size=20&active=all&sortBy=name&sortDir=asc
 *
 * active  : all | active | inactive
 * sortBy  : name | id | category
 * sortDir : asc | desc
 */
@RestController
@RequiredArgsConstructor
public class SuperAdminCatalogController {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("name", "id", "category");

    private final GlobalProductRepository repo;

    @GetMapping("/api/super-admin/global-products")
    public Map<String, Object> listAll(
            @RequestParam(defaultValue = "")       String q,
            @RequestParam(defaultValue = "0")      int    page,
            @RequestParam(defaultValue = "20")     int    size,
            @RequestParam(defaultValue = "all")    String active,
            @RequestParam(defaultValue = "name")   String sortBy,
            @RequestParam(defaultValue = "asc")    String sortDir) {

        String sortField = ALLOWED_SORT_FIELDS.contains(sortBy) ? sortBy : "name";
        Sort.Direction dir = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sortField));

        String qt = q.trim();
        Page<GlobalProduct> result = switch (active) {
            case "active"   -> repo.searchAllByActive(qt, true,  pageable);
            case "inactive" -> repo.searchAllByActive(qt, false, pageable);
            default         -> repo.searchAll(qt, pageable);
        };

        return Map.of(
                "content",       result.getContent().stream().map(GlobalProductDto::from).toList(),
                "totalElements", result.getTotalElements(),
                "totalPages",    result.getTotalPages(),
                "page",          result.getNumber()
        );
    }
}
