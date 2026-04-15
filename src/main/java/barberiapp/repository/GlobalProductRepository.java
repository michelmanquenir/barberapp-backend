package barberiapp.repository;

import barberiapp.model.GlobalProduct;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;

@Repository
public interface GlobalProductRepository extends JpaRepository<GlobalProduct, Long> {

    /** Busca por nombre (like) o barcode exacto, solo activos */
    @Query("""
        SELECT gp FROM GlobalProduct gp
        WHERE gp.active = true
          AND (
            LOWER(gp.name) LIKE LOWER(CONCAT('%', :q, '%'))
            OR gp.barcode = :q
          )
        ORDER BY gp.name ASC
        """)
    List<GlobalProduct> search(@Param("q") String q, Pageable pageable);

    /** Busca por barcode exacto (solo activos — para vincular desde negocios) */
    Optional<GlobalProduct> findByBarcodeAndActiveTrue(String barcode);

    /** Busca por barcode exacto (todos, incluyendo inactivos — para validar unicidad) */
    Optional<GlobalProduct> findByBarcode(String barcode);

    /** Lista todos los activos (paginada) */
    List<GlobalProduct> findByActiveTrueOrderByNameAsc(Pageable pageable);

    /**
     * Super admin: busca en TODOS los productos (incluyendo inactivos), paginado.
     * Si q está vacío devuelve todos.
     */
    @Query(value = """
            SELECT gp FROM GlobalProduct gp
            WHERE :q = ''
               OR LOWER(gp.name) LIKE LOWER(CONCAT('%', :q, '%'))
               OR gp.barcode = :q
            """,
            countQuery = """
            SELECT COUNT(gp) FROM GlobalProduct gp
            WHERE :q = ''
               OR LOWER(gp.name) LIKE LOWER(CONCAT('%', :q, '%'))
               OR gp.barcode = :q
            """)
    Page<GlobalProduct> searchAll(@Param("q") String q, Pageable pageable);
}
