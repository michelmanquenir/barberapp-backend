package barberiapp.repository;

import barberiapp.model.BarberShopMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BarberShopMemberRepository extends JpaRepository<BarberShopMember, Long> {
    List<BarberShopMember> findByShopIdAndActiveTrue(String shopId);
    List<BarberShopMember> findByBarberId(Long barberId);
    boolean existsByShopIdAndBarberIdAndActiveTrue(String shopId, Long barberId);
    Optional<BarberShopMember> findByShopIdAndBarberId(String shopId, Long barberId);
    void deleteByShopIdAndBarberId(String shopId, Long barberId);
}
