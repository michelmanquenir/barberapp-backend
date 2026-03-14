package barberiapp.dto;

import lombok.Data;

@Data
public class CreateShopRequest {
    private String name;
    private String description;
    private String slug;
    private String address;
    private Double latitude;
    private Double longitude;
    private Boolean homeServiceEnabled;
    private Integer pricePerKm;
    private String categoryId;
}
