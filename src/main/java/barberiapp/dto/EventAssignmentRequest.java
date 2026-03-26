package barberiapp.dto;

import lombok.Data;

@Data
public class EventAssignmentRequest {
    private Long vehicleId;
    private Long driverId;
}
