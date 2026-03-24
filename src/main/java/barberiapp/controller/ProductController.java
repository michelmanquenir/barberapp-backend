package barberiapp.controller;

import barberiapp.dto.ShopProductRequest;
import barberiapp.dto.ShopProductResponse;
import barberiapp.repository.ProductRepository;
import barberiapp.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductRepository productRepository;

    // ── Público ──────────────────────────────────────────────────────────────────

    /**
     * GET /api/shops/{shopId}/products
     * Productos activos de una barbería (para clientes).
     */
    @GetMapping("/api/shops/{shopId}/products")
    public List<ShopProductResponse> getPublicProducts(@PathVariable String shopId) {
        return productService.getPublicProducts(shopId);
    }

    // ── Admin ────────────────────────────────────────────────────────────────────

    /**
     * GET /api/admin/shops/{shopId}/products
     * Todos los productos de la barbería (activos + inactivos).
     */
    @GetMapping("/api/admin/shops/{shopId}/products")
    public List<ShopProductResponse> getAdminProducts(@PathVariable String shopId) {
        return productService.getAdminProducts(shopId);
    }

    /**
     * POST /api/admin/shops/{shopId}/products
     * Crea un nuevo producto para la barbería.
     */
    @PostMapping("/api/admin/shops/{shopId}/products")
    public ResponseEntity<?> createProduct(@PathVariable String shopId,
                                            @RequestBody ShopProductRequest req) {
        try {
            return ResponseEntity.ok(productService.createProduct(shopId, req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PUT /api/admin/products/{productId}
     * Actualiza nombre, precio, stock, etc.
     */
    @PutMapping("/api/admin/products/{productId}")
    public ResponseEntity<?> updateProduct(@PathVariable Long productId,
                                            @RequestBody ShopProductRequest req) {
        try {
            return ResponseEntity.ok(productService.updateProduct(productId, req));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PATCH /api/admin/products/{productId}/stock?delta=N
     * Ajusta el stock (positivo = ingreso, negativo = venta/baja).
     */
    @PatchMapping("/api/admin/products/{productId}/stock")
    public ResponseEntity<?> adjustStock(@PathVariable Long productId,
                                          @RequestParam int delta) {
        try {
            return ResponseEntity.ok(productService.adjustStock(productId, delta));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /api/admin/products/{productId}
     * Desactiva un producto (soft delete).
     */
    @DeleteMapping("/api/admin/products/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long productId) {
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/admin/shops/{shopId}/products/barcode/{barcode}
     * Busca un producto activo por código de barras.
     * Busca primero en barcode local, luego en barcode del catálogo global vinculado.
     */
    @GetMapping("/api/admin/shops/{shopId}/products/barcode/{barcode}")
    public ResponseEntity<?> getByBarcode(@PathVariable String shopId,
                                           @PathVariable String barcode) {
        // 1. Barcode local
        var local = productRepository.findByShopIdAndBarcodeAndActiveTrue(shopId, barcode);
        if (local.isPresent()) return ResponseEntity.ok(ShopProductResponse.from(local.get()));

        // 2. Barcode del catálogo global vinculado
        var global = productRepository.findByShopIdAndGlobalBarcodeAndActive(shopId, barcode);
        return global
                .map(p -> ResponseEntity.ok(ShopProductResponse.from(p)))
                .orElse(ResponseEntity.notFound().build());
    }
}
