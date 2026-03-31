package barberiapp.dto;

import barberiapp.model.BarberShop;
import barberiapp.model.FavoriteShop;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO seguro para serializar FavoriteShop sin exponer relaciones lazy
 * (BarberShop.owner es LAZY → LazyInitializationException si se serializa la entidad directamente).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteShopDto {

    private Long id;
    private LocalDateTime createdAt;

    // ── Datos del negocio (sin owner ni barbers para evitar lazy issues) ──────
    private ShopSummary shop;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShopSummary {
        private String id;
        private String name;
        private String description;
        private String slug;
        private String address;
        private Double latitude;
        private Double longitude;
        private Boolean active;
        private Boolean homeServiceEnabled;
        private Integer pricePerKm;
        private String categoryId;
        private String approvalStatus;
    }

    // ── Factory ───────────────────────────────────────────────────────────────
    public static FavoriteShopDto from(FavoriteShop fav) {
        BarberShop s = fav.getShop();
        ShopSummary shopSummary = null;
        if (s != null) {
            shopSummary = ShopSummary.builder()
                    .id(s.getId())
                    .name(s.getName())
                    .description(s.getDescription())
                    .slug(s.getSlug())
                    .address(s.getAddress())
                    .latitude(s.getLatitude())
                    .longitude(s.getLongitude())
                    .active(s.getActive())
                    .homeServiceEnabled(s.getHomeServiceEnabled())
                    .pricePerKm(s.getPricePerKm())
                    .categoryId(s.getCategoryId())
                    .approvalStatus(s.getApprovalStatus() != null ? s.getApprovalStatus().name() : null)
                    .build();
        }
        return FavoriteShopDto.builder()
                .id(fav.getId())
                .createdAt(fav.getCreatedAt())
                .shop(shopSummary)
                .build();
    }
}
