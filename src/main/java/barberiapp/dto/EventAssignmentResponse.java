package barberiapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventAssignmentResponse {
    private Long id;
    private Long eventId;
    private TransportVehicleResponse vehicle;
    private TransportDriverResponse driver;
    private int bookedSeats;
    private int availableSeats;
}
