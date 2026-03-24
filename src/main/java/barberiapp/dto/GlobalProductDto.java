package barberiapp.dto;

import barberiapp.model.GlobalProduct;
import lombok.Data;

import java.time.LocalDateTime;

/** DTO para los endpoints del catálogo global de productos. */
@Data
public class GlobalProductDto {

    private Long id;
    private String name;
    private String description;
    private String category;
    private String imageUrl;
    private String barcode;
    private String sku;
    private Boolean active;
    private LocalDateTime createdAt;

    public static GlobalProductDto from(GlobalProduct gp) {
        GlobalProductDto d = new GlobalProductDto();
        d.setId(gp.getId());
        d.setName(gp.getName());
        d.setDescription(gp.getDescription());
        d.setCategory(gp.getCategory());
        d.setImageUrl(gp.getImageUrl());
        d.setBarcode(gp.getBarcode());
        d.setSku(gp.getSku());
        d.setActive(gp.getActive());
        d.setCreatedAt(gp.getCreatedAt());
        return d;
    }
}
