package barberiapp.repository;

import barberiapp.model.ApprovalStatus;
import barberiapp.model.BarberShop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BarberShopRepository extends JpaRepository<BarberShop, String> {
    List<BarberShop> findByOwnerId(String ownerId);
    List<BarberShop> findByOwnerIdAndActiveTrue(String ownerId);
    Optional<BarberShop> findBySlug(String slug);
    List<BarberShop> findByActiveTrue();

    /** Vista pública: solo negocios activos Y aprobados (null se trata como ACTIVE para datos previos a la migración) */
    @Query("SELECT s FROM BarberShop s WHERE s.active = true AND (s.approvalStatus = :approved OR s.approvalStatus IS NULL)")
    List<BarberShop> findPublicApproved(@Param("approved") ApprovalStatus approved);
}
