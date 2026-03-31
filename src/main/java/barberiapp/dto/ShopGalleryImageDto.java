package barberiapp.dto;

import barberiapp.model.ShopGalleryImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopGalleryImageDto {

    // ── Respuesta ─────────────────────────────────────────────────────────────
    private Long id;
    private String shopId;
    private String imageUrl;
    private String caption;
    private Integer displayOrder;
    private LocalDateTime createdAt;

    // ── Factory ───────────────────────────────────────────────────────────────
    public static ShopGalleryImageDto from(ShopGalleryImage img) {
        return ShopGalleryImageDto.builder()
                .id(img.getId())
                .shopId(img.getShopId())
                .imageUrl(img.getImageUrl())
                .caption(img.getCaption())
                .displayOrder(img.getDisplayOrder())
                .createdAt(img.getCreatedAt())
                .build();
    }
}
