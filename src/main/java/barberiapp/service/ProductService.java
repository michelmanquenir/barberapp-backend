package barberiapp.service;

import barberiapp.dto.ShopProductRequest;
import barberiapp.dto.ShopProductResponse;
import barberiapp.model.ApprovalStatus;
import barberiapp.model.Product;
import barberiapp.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    // ── Consultas ───────────────────────────────────────────────────────────────

    /** Admin: todos los productos de la barbería (activos e inactivos) */
    public List<ShopProductResponse> getAdminProducts(String shopId) {
        return productRepository.findByShopIdOrderByCategoryAscNameAsc(shopId)
                .stream()
                .map(ShopProductResponse::from)
                .toList();
    }

    /** Público: solo productos activos Y aprobados de la barbería */
    public List<ShopProductResponse> getPublicProducts(String shopId) {
        return productRepository.findPublicApproved(shopId, ApprovalStatus.ACTIVE)
                .stream()
                .map(ShopProductResponse::from)
                .toList();
    }

    // ── CRUD ────────────────────────────────────────────────────────────────────

    @Transactional
    public ShopProductResponse createProduct(String shopId, ShopProductRequest req) {
        if (req.getName() == null || req.getName().isBlank())
            throw new IllegalArgumentException("El nombre es requerido");
        if (req.getSalePrice() == null || req.getSalePrice() < 0)
            throw new IllegalArgumentException("El precio de venta es requerido");

        Product p = new Product();
        p.setShopId(shopId);
        applyRequest(p, req);
        return ShopProductResponse.from(productRepository.save(p));
    }

    @Transactional
    public ShopProductResponse updateProduct(Long productId, ShopProductRequest req) {
        Product p = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        applyRequest(p, req);
        return ShopProductResponse.from(productRepository.save(p));
    }

    /** Desactiva un producto (soft delete) */
    @Transactional
    public void deleteProduct(Long productId) {
        Product p = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        p.setActive(false);
        productRepository.save(p);
    }

    /**
     * Ajusta el stock de un producto.
     * delta positivo = ingreso; negativo = venta o baja.
     */
    @Transactional
    public ShopProductResponse adjustStock(Long productId, int delta) {
        Product p = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        int newStock = Math.max(0, (p.getStock() != null ? p.getStock() : 0) + delta);
        p.setStock(newStock);
        return ShopProductResponse.from(productRepository.save(p));
    }

    // ── helper ──────────────────────────────────────────────────────────────────

    private void applyRequest(Product p, ShopProductRequest req) {
        if (req.getName()          != null) p.setName(req.getName().trim());
        if (req.getDescription()   != null) p.setDescription(req.getDescription().trim().isEmpty() ? null : req.getDescription().trim());
        if (req.getCategory()      != null) p.setCategory(req.getCategory().trim().isEmpty() ? null : req.getCategory().trim());
        if (req.getImageUrl()      != null) p.setImageUrl(req.getImageUrl().trim().isEmpty() ? null : req.getImageUrl().trim());
        if (req.getPurchasePrice() != null) p.setPurchasePrice(req.getPurchasePrice());
        if (req.getSalePrice()     != null) p.setSalePrice(req.getSalePrice());
        if (req.getStock()         != null) p.setStock(Math.max(0, req.getStock()));
        if (req.getActive()        != null) p.setActive(req.getActive());
    }
}
