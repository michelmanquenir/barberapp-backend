package barberiapp.repository;

import barberiapp.model.ApprovalStatus;
import barberiapp.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /** Todos los productos de una barbería (admin: incluye inactivos).
     *  LEFT JOIN FETCH globalProduct para evitar N+1 al construir los DTOs. */
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.globalProduct WHERE p.shopId = :shopId ORDER BY p.createdAt DESC")
    List<Product> findByShopIdOrderByCategoryAscNameAsc(@Param("shopId") String shopId);

    /** Solo productos activos de una barbería (vista pública sin filtro de aprobación — uso interno) */
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.globalProduct WHERE p.shopId = :shopId AND p.active = true ORDER BY p.createdAt DESC")
    List<Product> findByShopIdAndActiveTrueOrderByCategoryAscNameAsc(@Param("shopId") String shopId);

    /** Vista pública: solo activos Y aprobados */
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.globalProduct WHERE p.shopId = :shopId AND p.active = true AND (p.approvalStatus = :approved OR p.approvalStatus IS NULL) ORDER BY p.createdAt DESC")
    List<Product> findPublicApproved(@Param("shopId") String shopId, @Param("approved") ApprovalStatus approved);

    /** Super admin: todos los productos de todos los negocios */
    @Query("SELECT p FROM Product p ORDER BY p.createdAt DESC")
    List<Product> findAllOrderByCreatedAtDesc();

    /** Super admin: todos los productos filtrados por approvalStatus */
    @Query("SELECT p FROM Product p WHERE p.approvalStatus = :status ORDER BY p.createdAt DESC")
    List<Product> findByApprovalStatus(@Param("status") ApprovalStatus status);

    /** POS: buscar producto activo por código de barras local dentro de un negocio */
    Optional<Product> findByShopIdAndBarcodeAndActiveTrue(String shopId, String barcode);

    /** POS: buscar producto activo por barcode del catálogo global dentro de un negocio */
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.globalProduct gp WHERE p.shopId = :shopId AND gp.barcode = :barcode AND p.active = true")
    Optional<Product> findByShopIdAndGlobalBarcodeAndActive(@Param("shopId") String shopId, @Param("barcode") String barcode);

    // ── Validación de unicidad ────────────────────────────────────────────────

    /** Verifica si existe otro producto con el mismo nombre (case-insensitive) en el negocio */
    @Query("SELECT COUNT(p) > 0 FROM Product p WHERE p.shopId = :shopId AND LOWER(p.name) = LOWER(:name) AND p.id <> :excludeId")
    boolean existsByShopIdAndNameIgnoreCaseExcluding(@Param("shopId") String shopId, @Param("name") String name, @Param("excludeId") Long excludeId);

    /** Verifica si existe otro producto con el mismo código de barras en el negocio */
    @Query("SELECT COUNT(p) > 0 FROM Product p WHERE p.shopId = :shopId AND p.barcode = :barcode AND p.id <> :excludeId")
    boolean existsByShopIdAndBarcodeExcluding(@Param("shopId") String shopId, @Param("barcode") String barcode, @Param("excludeId") Long excludeId);

    /** Verifica si existe otro producto con el mismo SKU en el negocio */
    @Query("SELECT COUNT(p) > 0 FROM Product p WHERE p.shopId = :shopId AND p.sku = :sku AND p.id <> :excludeId")
    boolean existsByShopIdAndSkuExcluding(@Param("shopId") String shopId, @Param("sku") String sku, @Param("excludeId") Long excludeId);
}
