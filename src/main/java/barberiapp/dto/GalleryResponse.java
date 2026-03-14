package barberiapp.dto;

import barberiapp.model.BarberGallery;
import barberiapp.model.BarberGalleryImage;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class GalleryResponse {
    private Long id;
    private String name;
    private String description;
    private String coverUrl;
    private String shopId;
    private String shopName;
    private Boolean hidden;
    private int imageCount;
    private LocalDateTime createdAt;
    private List<GalleryImageDto> images;

    @Data
    public static class GalleryImageDto {
        private Long id;
        private String imageUrl;
        private String caption;
        private LocalDateTime createdAt;

        public static GalleryImageDto from(BarberGalleryImage img) {
            GalleryImageDto dto = new GalleryImageDto();
            dto.setId(img.getId());
            dto.setImageUrl(img.getImageUrl());
            dto.setCaption(img.getCaption());
            dto.setCreatedAt(img.getCreatedAt());
            return dto;
        }
    }

    public static GalleryResponse from(BarberGallery gallery, boolean includeImages) {
        GalleryResponse dto = new GalleryResponse();
        dto.setId(gallery.getId());
        dto.setName(gallery.getName());
        dto.setDescription(gallery.getDescription());
        dto.setCoverUrl(gallery.getCoverUrl());
        dto.setShopId(gallery.getShopId());
        dto.setShopName(gallery.getShopName());
        dto.setHidden(gallery.getHidden() != null ? gallery.getHidden() : false);
        dto.setCreatedAt(gallery.getCreatedAt());
        dto.setImageCount(gallery.getImages() != null ? gallery.getImages().size() : 0);
        if (includeImages && gallery.getImages() != null) {
            dto.setImages(gallery.getImages().stream().map(GalleryImageDto::from).toList());
        }
        return dto;
    }
}
