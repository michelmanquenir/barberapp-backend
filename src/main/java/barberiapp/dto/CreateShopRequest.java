package barberiapp.dto;

import lombok.Data;

@Data
public class CreateShopRequest {
    private String name;
    private String description;
    private String slug;
}
