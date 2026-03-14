package barberiapp.repository;

import barberiapp.model.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {

    /** Todas las suscripciones de un usuario */
    List<UserSubscription> findByUserIdOrderByCreatedAtDesc(String userId);

    /** Suscripción activa de un usuario en una barbería */
    @Query("""
        SELECT s FROM UserSubscription s
        WHERE s.userId = :userId
          AND s.shopId = :shopId
          AND s.status = 'active'
          AND s.endDate >= :today
        ORDER BY s.createdAt DESC
        """)
    Optional<UserSubscription> findActiveByUserAndShop(
            @Param("userId") String userId,
            @Param("shopId") String shopId,
            @Param("today") LocalDate today);

    /** Todas las suscripciones activas de una barbería (para el admin) */
    @Query("""
        SELECT s FROM UserSubscription s
        WHERE s.shopId = :shopId
          AND s.status = 'active'
          AND s.endDate >= :today
        ORDER BY s.createdAt DESC
        """)
    List<UserSubscription> findActiveByShop(
            @Param("shopId") String shopId,
            @Param("today") LocalDate today);

    /** Todas las suscripciones de una barbería (historial) */
    List<UserSubscription> findByShopIdOrderByCreatedAtDesc(String shopId);
}
