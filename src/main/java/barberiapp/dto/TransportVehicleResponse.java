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
public class TransportVehicleResponse {
    private Long id;
    private String shopId;
    private String brand;
    private String model;
    private Integer year;
    private String licensePlate;
    private Integer passengerCapacity;
    private String commune;
    private String imageUrl;
    private Boolean active;
    private LocalDateTime createdAt;
}
