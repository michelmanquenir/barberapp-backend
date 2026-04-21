package barberiapp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

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

    /**
     * Vínculo opcional al catálogo global.
     * Cuando está presente, nombre/imagen/barcode/sku/descripción/categoría
     * se heredan del GlobalProduct. Precio y stock son siempre propios.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "global_product_id")
    private GlobalProduct globalProduct;

    /** Nombre local (usado solo cuando globalProduct == null). */
    @Column(nullable = true)
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

    /** Código de barras del producto (EAN-13, UPC-A, Code128, etc.) */
    @Column(length = 50)
    private String barcode;

    /** SKU / código interno del negocio */
    @Column(length = 50)
    private String sku;

    /**
     * Posición en bodega/estantería (opcional).
     * Si es null, el producto no tiene ubicación asignada.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shelf_slot_id")
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ShelfSlot shelfSlot;

    @Column(columnDefinition = "boolean default true")
    private Boolean active = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", length = 20)
    private ApprovalStatus approvalStatus;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // ── Resolvers: devuelven el valor del catálogo global si está vinculado ────

    @Transient
    public String getResolvedName() {
        return globalProduct != null ? globalProduct.getName() : name;
    }
    @Transient
    public String getResolvedDescription() {
        return globalProduct != null ? globalProduct.getDescription() : description;
    }
    @Transient
    public String getResolvedCategory() {
        return globalProduct != null ? globalProduct.getCategory() : category;
    }
    @Transient
    public String getResolvedImageUrl() {
        return globalProduct != null ? globalProduct.getImageUrl() : imageUrl;
    }
    @Transient
    public String getResolvedBarcode() {
        return globalProduct != null ? globalProduct.getBarcode() : barcode;
    }
    @Transient
    public String getResolvedSku() {
        return globalProduct != null ? globalProduct.getSku() : sku;
    }

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
