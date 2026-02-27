package barberiapp.repository;

import barberiapp.model.BarberShopMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BarberShopMemberRepository extends JpaRepository<BarberShopMember, Long> {
    List<BarberShopMember> findByShopIdAndActiveTrue(String shopId);
    List<BarberShopMember> findByBarberId(Long barberId);
    boolean existsByShopIdAndBarberId(String shopId, Long barberId);
    void deleteByShopIdAndBarberId(String shopId, Long barberId);
}
