package barberiapp.dto;

import lombok.Data;

@Data
public class TransportDriverRequest {
    private String name;
    private String phone;
    private String licenseNumber;
    private String licenseImageUrl;
    private String notes;
    private Boolean active;
}
