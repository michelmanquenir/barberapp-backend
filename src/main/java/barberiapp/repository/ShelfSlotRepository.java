package barberiapp.repository;

import barberiapp.model.ShelfSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShelfSlotRepository extends JpaRepository<ShelfSlot, Long> {

    List<ShelfSlot> findByShelfIdOrderByCodeAsc(Long shelfId);

    /** Carga el slot junto con su estantería (para verificar shopId) */
    @Query("SELECT ss FROM ShelfSlot ss JOIN FETCH ss.shelf WHERE ss.id = :id")
    Optional<ShelfSlot> findByIdWithShelf(@Param("id") Long id);
}
