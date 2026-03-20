package barberiapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    /** Nullable: el producto puede ser eliminado después de realizarse el pedido */
    @Column(name = "product_id")
    private Long productId;

    /** Snapshot del nombre del producto al momento del pedido */
    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;

    /** Snapshot del precio de venta al momento del pedido */
    @Column(name = "unit_price", nullable = false)
    private Integer unitPrice;

    @Column(nullable = false)
    private Integer quantity;

    /** unitPrice × quantity */
    @Column(nullable = false)
    private Integer subtotal;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
