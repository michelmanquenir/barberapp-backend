package barberiapp.dto;

import lombok.Data;

@Data
public class CreateServiceRequest {
    private String name;
    private String description;
    private Integer price;
    private Integer durationMinutes;
}
