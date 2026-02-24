package barberiapp.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class AppointmentRequest {
    private String userId; // Simulando usuario autenticado
    private Long barberId;
    private Long serviceId;
    private LocalDate date;
    private LocalTime time;
    private String locationType;
    private String paymentMethod;
}
