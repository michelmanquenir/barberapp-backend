package barberiapp.service;

import barberiapp.dto.ShopProductRequest;
import barberiapp.dto.ShopProductResponse;
import barberiapp.model.ApprovalStatus;
import barberiapp.model.BarberShop;
import barberiapp.model.GlobalProduct;
import barberiapp.model.Product;
import barberiapp.repository.BarberShopRepository;
import barberiapp.repository.GlobalProductRepository;
import barberiapp.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final BarberShopRepository shopRepository;
    private final GlobalProductRepository globalProductRepository;

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
        BarberShop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new IllegalArgumentException("Negocio no encontrado"));

        if (shop.getApprovalStatus() == ApprovalStatus.PENDING) {
            throw new IllegalArgumentException("El negocio está pendiente de aprobación.");
        }
        if (shop.getApprovalStatus() == ApprovalStatus.REJECTED) {
            throw new IllegalArgumentException("El negocio ha sido rechazado. No puedes agregar productos.");
        }
        if (req.getSalePrice() == null || req.getSalePrice() < 0)
            throw new IllegalArgumentException("El precio de venta es requerido");

        Product p = new Product();
        p.setShopId(shopId);
        applyRequest(p, req);

        // Validación: necesita nombre propio O estar vinculado al catálogo
        if (p.getGlobalProduct() == null && (p.getName() == null || p.getName().isBlank()))
            throw new IllegalArgumentException("El nombre es requerido cuando no se vincula a un producto del catálogo");

        // Productos del catálogo global ya fueron revisados por el super admin:
        // se aprueban automáticamente sin necesidad de revisión adicional.
        if (p.getGlobalProduct() != null) {
            p.setApprovalStatus(ApprovalStatus.ACTIVE);
        }

        return ShopProductResponse.from(productRepository.save(p));
    }

    @Transactional
    public ShopProductResponse updateProduct(Long productId, ShopProductRequest req) {
        Product p = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        applyRequest(p, req);
        return ShopProductResponse.from(productRepository.save(p));
    }

    /** Desactiva un producto (soft delete). */
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
        // Si viene un globalProductId, vincular al catálogo y limpiar campos locales
        if (req.getGlobalProductId() != null) {
            GlobalProduct gp = globalProductRepository.findById(req.getGlobalProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Producto del catálogo no encontrado"));
            p.setGlobalProduct(gp);
            // Limpiar campos locales (la info pública viene del catálogo)
            p.setName(null);
            p.setDescription(null);
            p.setCategory(null);
            p.setImageUrl(null);
            p.setBarcode(null);
            p.setSku(null);
        } else {
            // Producto local: aplicar los campos del request
            p.setGlobalProduct(null);
            if (req.getName()        != null) p.setName(req.getName().trim());
            if (req.getDescription() != null) p.setDescription(req.getDescription().trim().isEmpty() ? null : req.getDescription().trim());
            if (req.getCategory()    != null) p.setCategory(req.getCategory().trim().isEmpty() ? null : req.getCategory().trim());
            if (req.getImageUrl()    != null) p.setImageUrl(req.getImageUrl().trim().isEmpty() ? null : req.getImageUrl().trim());
            if (req.getBarcode()     != null) p.setBarcode(req.getBarcode().trim().isEmpty() ? null : req.getBarcode().trim());
            if (req.getSku()         != null) p.setSku(req.getSku().trim().isEmpty() ? null : req.getSku().trim());
        }

        // Campos exclusivos del negocio: siempre se aplican
        if (req.getPurchasePrice() != null) p.setPurchasePrice(req.getPurchasePrice());
        if (req.getSalePrice()     != null) p.setSalePrice(req.getSalePrice());
        if (req.getStock()         != null) p.setStock(Math.max(0, req.getStock()));
        if (req.getActive()        != null) p.setActive(req.getActive());
    }
}
