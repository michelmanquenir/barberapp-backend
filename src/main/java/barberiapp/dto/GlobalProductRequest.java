package barberiapp.dto;

import lombok.Data;

@Data
public class GlobalProductRequest {
    private String name;
    private String description;
    private String category;
    private String imageUrl;
    private String barcode;
    private String sku;
    private Boolean active;
}
