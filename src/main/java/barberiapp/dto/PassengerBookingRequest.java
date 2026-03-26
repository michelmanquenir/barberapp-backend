package barberiapp.dto;

import lombok.Data;

@Data
public class PassengerBookingRequest {
    private Long assignmentId;
    private String clientCommune;
    private Integer seatsBooked;
    private String notes;
}
