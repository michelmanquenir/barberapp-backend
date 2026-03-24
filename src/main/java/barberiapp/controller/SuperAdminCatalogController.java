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

/**
 * Super-admin endpoints para gestionar el catálogo global de productos.
 *
 * GET  /api/super-admin/global-products?q=&page=0&size=20  → listado paginado (incluye inactivos)
 *
 * Crear / Actualizar / Toggle se hacen reutilizando:
 *   POST /api/admin/global-products
 *   PUT  /api/admin/global-products/{id}   (ahora acepta "active" en el body)
 *
 * Estos endpoints están bajo /api/super-admin/** → requieren rol SUPER_ADMIN (SecurityConfig).
 */
@RestController
@RequiredArgsConstructor
public class SuperAdminCatalogController {

    private final GlobalProductRepository repo;

    /**
     * Listado paginado de todos los productos del catálogo global,
     * incluyendo los inactivos. Soporta búsqueda por nombre o barcode.
     */
    @GetMapping("/api/super-admin/global-products")
    public Map<String, Object> listAll(
            @RequestParam(defaultValue = "")   String q,
            @RequestParam(defaultValue = "0")  int    page,
            @RequestParam(defaultValue = "20") int    size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        Page<GlobalProduct> result = repo.searchAll(q.trim(), pageable);

        return Map.of(
                "content",       result.getContent().stream().map(GlobalProductDto::from).toList(),
                "totalElements", result.getTotalElements(),
                "totalPages",    result.getTotalPages(),
                "page",          result.getNumber()
        );
    }
}
