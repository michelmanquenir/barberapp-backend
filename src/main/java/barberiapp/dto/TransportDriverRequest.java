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
    /** Email del conductor (opcional). Al crear, si se proporciona, se genera una cuenta de app. */
    private String email;
}
