package barberiapp.repository;

import barberiapp.model.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, String> {
    /** Raíces activas (para dropdown público) */
    List<ProductCategory> findByParentIdIsNullAndActiveTrueOrderBySortOrderAsc();
    /** Hijos activos de un padre (para dropdown público) */
    List<ProductCategory> findByParentIdAndActiveTrueOrderBySortOrderAsc(String parentId);
    /** Todas las raíces (admin) */
    List<ProductCategory> findByParentIdIsNullOrderBySortOrderAsc();
    /** Todos los hijos de un padre (admin) */
    List<ProductCategory> findByParentIdOrderBySortOrderAsc(String parentId);
    /** Lista plana completa (admin) */
    List<ProductCategory> findAllByOrderBySortOrderAsc();
}
