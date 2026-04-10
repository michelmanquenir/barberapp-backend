package barberiapp.dto;

import lombok.Data;

@Data
public class PassengerBookingRequest {
    private Long assignmentId;
    private String clientCommune;
    private Integer seatsBooked;
    private String notes;
    /** EFECTIVO | TRANSFERENCIA | TARJETA */
    private String paymentMethod;
    /** Tarifa total calculada en frontend (fare x seats) */
    private Integer totalFare;
}
