package barberiapp.dto;

import barberiapp.model.Product;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ShopProductResponse {

    private Long id;
    private String shopId;

    // Campos públicos resueltos (pueden venir del catálogo global o ser locales)
    private String name;
    private String description;
    private String category;
    private String imageUrl;
    private String barcode;
    private String sku;

    // Campos exclusivos de cada negocio
    private Integer purchasePrice;
    private Integer salePrice;
    private Integer stock;
    private Boolean active;
    private String approvalStatus;
    private LocalDateTime createdAt;

    // Catálogo global
    private Long globalProductId;

    // ── Calculados ──────────────────────────────────────────────────────────────

    /** Ganancia por unidad = salePrice - purchasePrice */
    private Integer profit;

    /** Margen de ganancia en porcentaje */
    private Integer profitMarginPct;

    /** true si el stock está en nivel crítico (< 5 unidades) */
    private Boolean lowStock;

    // ── Factory ─────────────────────────────────────────────────────────────────

    public static ShopProductResponse from(Product p) {
        ShopProductResponse r = new ShopProductResponse();
        r.setId(p.getId());
        r.setShopId(p.getShopId());

        // Campos públicos: resolución desde catálogo o valores locales
        r.setName(p.getResolvedName());
        r.setDescription(p.getResolvedDescription());
        r.setCategory(p.getResolvedCategory());
        r.setImageUrl(p.getResolvedImageUrl());
        r.setBarcode(p.getResolvedBarcode());
        r.setSku(p.getResolvedSku());

        // Campos exclusivos del negocio
        r.setPurchasePrice(p.getPurchasePrice());
        r.setSalePrice(p.getSalePrice());
        r.setStock(p.getStock() != null ? p.getStock() : 0);
        r.setActive(p.getActive() != null ? p.getActive() : true);
        r.setApprovalStatus(p.getApprovalStatus() != null ? p.getApprovalStatus().name() : "ACTIVE");
        r.setCreatedAt(p.getCreatedAt());
        r.setProfit(p.getProfit());
        r.setProfitMarginPct(p.getProfitMarginPct());
        r.setLowStock(p.getStock() != null && p.getStock() < 5);

        // ID del catálogo global (si está vinculado)
        r.setGlobalProductId(p.getGlobalProduct() != null ? p.getGlobalProduct().getId() : null);

        return r;
    }
}
