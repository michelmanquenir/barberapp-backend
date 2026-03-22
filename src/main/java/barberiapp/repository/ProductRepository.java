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

    /** Todos los productos de una barbería (admin: incluye inactivos) */
    List<Product> findByShopIdOrderByCategoryAscNameAsc(String shopId);

    /** Solo productos activos de una barbería (vista pública sin filtro de aprobación — uso interno) */
    List<Product> findByShopIdAndActiveTrueOrderByCategoryAscNameAsc(String shopId);

    /** Vista pública: solo activos Y aprobados (null se trata como ACTIVE para datos previos a la migración) */
    @Query("SELECT p FROM Product p WHERE p.shopId = :shopId AND p.active = true AND (p.approvalStatus = :approved OR p.approvalStatus IS NULL) ORDER BY p.category ASC, p.name ASC")
    List<Product> findPublicApproved(@Param("shopId") String shopId, @Param("approved") ApprovalStatus approved);

    /** Super admin: todos los productos de todos los negocios */
    @Query("SELECT p FROM Product p ORDER BY p.createdAt DESC")
    List<Product> findAllOrderByCreatedAtDesc();

    /** Super admin: todos los productos filtrados por approvalStatus */
    @Query("SELECT p FROM Product p WHERE p.approvalStatus = :status ORDER BY p.createdAt DESC")
    List<Product> findByApprovalStatus(@Param("status") ApprovalStatus status);

    /** POS: buscar producto activo por código de barras dentro de un negocio */
    Optional<Product> findByShopIdAndBarcodeAndActiveTrue(String shopId, String barcode);
}
