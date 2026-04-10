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
public class PassengerBookingResponse {
    private Long id;
    private EventAssignmentResponse assignment;
    private String passengerName;
    private String clientCommune;
    private Integer seatsBooked;
    private String status;
    private String notes;
    private String paymentMethod;
    private Integer totalFare;
    private Integer amountPaid;
    private LocalDateTime createdAt;
    // Event details (denormalized for convenience)
    private String eventTitle;
    private LocalDateTime eventDate;
    private String eventAddress;
}
