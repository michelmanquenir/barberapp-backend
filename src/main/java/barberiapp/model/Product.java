package barberiapp.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Barbería a la que pertenece el producto */
    @Column(name = "shop_id", length = 36, nullable = false)
    private String shopId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** Precio de compra (costo) */
    @Column(name = "purchase_price")
    private Integer purchasePrice;

    /** Precio de venta al público */
    @Column(name = "sale_price", nullable = false)
    private Integer salePrice;

    @Column(length = 100)
    private String category;

    @Column(columnDefinition = "integer default 0")
    private Integer stock = 0;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(columnDefinition = "boolean default true")
    private Boolean active = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", length = 20)
    private ApprovalStatus approvalStatus;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // ── Campos calculados (no persistidos) ─────────────────────────────────────

    /** Ganancia por unidad = precio venta - precio compra */
    @Transient
    public Integer getProfit() {
        if (salePrice == null || purchasePrice == null) return null;
        return salePrice - purchasePrice;
    }

    /** Margen de ganancia en porcentaje (0-100) */
    @Transient
    public Integer getProfitMarginPct() {
        Integer profit = getProfit();
        if (profit == null || salePrice == null || salePrice == 0) return null;
        return (int) Math.round((profit * 100.0) / salePrice);
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (stock == null)  stock  = 0;
        if (active == null) active = true;
        if (approvalStatus == null) approvalStatus = ApprovalStatus.PENDING;
    }
}
