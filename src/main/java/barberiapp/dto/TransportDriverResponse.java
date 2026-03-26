package barberiapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransportDriverResponse {
    private Long id;
    private String shopId;
    private String name;
    private String phone;
    private String licenseNumber;
    private String licenseImageUrl;
    private String notes;
    private Boolean active;
    private LocalDateTime createdAt;
}
