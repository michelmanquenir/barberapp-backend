package barberiapp.dto;

import lombok.Data;

@Data
public class TransportVehicleRequest {
    private String brand;
    private String model;
    private Integer year;
    private String licensePlate;
    private Integer passengerCapacity;
    private String commune;
    private Long driverId;
    private String imageUrl;
    private Boolean active;
}
