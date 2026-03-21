package barberiapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "shop_orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", length = 36, nullable = false)
    private String shopId;

    @Column(name = "client_user_id", length = 36, nullable = false)
    private String clientUserId;

    /** Snapshot del nombre del cliente al momento del pedido */
    @Column(name = "client_name", length = 100)
    private String clientName;

    /** pending | confirmed | ready | delivered | cancelled */
    @Builder.Default
    @Column(length = 20, nullable = false)
    private String status = "pending";

    /** pickup | delivery */
    @Builder.Default
    @Column(name = "delivery_type", length = 20, nullable = false)
    private String deliveryType = "pickup";

    /** cash | transfer */
    @Builder.Default
    @Column(name = "payment_method", length = 20, nullable = false)
    private String paymentMethod = "cash";

    /** Dirección de entrega, null si es retiro en local */
    @Column(name = "client_address", length = 500)
    private String clientAddress;

    @Column(name = "total_price", nullable = false)
    private Integer totalPrice;

    /** Recargo de delivery en centavos (0 si es retiro) */
    @Builder.Default
    @Column(name = "delivery_fee", nullable = false)
    private Integer deliveryFee = 0;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null) status = "pending";
        if (deliveryType == null) deliveryType = "pickup";
        if (paymentMethod == null) paymentMethod = "cash";
    }
}
