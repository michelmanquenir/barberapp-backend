package barberiapp.dto;

import lombok.Data;

@Data
public class TransportEventRequest {
    private String eventCode;
    private String title;
    private String address;
    private Double latitude;
    private Double longitude;
    private String eventDate;
    private String bannerImageUrl;
    private Double pricePerKm;
    private Boolean active;
}
