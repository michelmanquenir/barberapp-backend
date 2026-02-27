package barberiapp.dto;

import lombok.Data;

@Data
public class CreateBarberRequest {
    private String name;
    private String bio;
    private String imageUrl;
}
