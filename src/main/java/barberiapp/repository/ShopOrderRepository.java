package barberiapp.repository;

import barberiapp.model.ShopOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShopOrderRepository extends JpaRepository<ShopOrder, Long> {

    /** Pedidos de un cliente ordenados por fecha descendente */
    List<ShopOrder> findByClientUserIdOrderByCreatedAtDesc(String clientUserId);

    /** Pedidos de un negocio ordenados por fecha descendente */
    List<ShopOrder> findByShopIdOrderByCreatedAtDesc(String shopId);
}
