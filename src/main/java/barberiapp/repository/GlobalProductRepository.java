package barberiapp.repository;

import barberiapp.model.GlobalProduct;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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

    /** Busca por barcode exacto */
    Optional<GlobalProduct> findByBarcodeAndActiveTrue(String barcode);

    /** Lista todos los activos (paginada) */
    List<GlobalProduct> findByActiveTrueOrderByNameAsc(Pageable pageable);
}
