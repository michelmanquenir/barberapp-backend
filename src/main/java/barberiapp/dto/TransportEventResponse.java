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
public class TransportEventResponse {
    private Long id;
    private String shopId;
    private String eventCode;
    private String title;
    private String address;
    private Double latitude;
    private Double longitude;
    private LocalDateTime eventDate;
    private String bannerImageUrl;
    private Double pricePerKm;
    private Boolean active;
    private LocalDateTime createdAt;
    private int vehicleCount;
}
