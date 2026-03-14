package barberiapp.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class AppointmentRequest {
    private String userId; // Simulando usuario autenticado
    private String shopId;
    private Long barberId;
    private Long serviceId;
    private LocalDate date;
    private LocalTime time;
    private String locationType;
    private String paymentMethod;
    // Servicio a domicilio
    private String clientAddress;
    private Double clientLatitude;
    private Double clientLongitude;
    private Double homeDistanceKm;
    private Integer surchargeAmount;
    private Integer durationMinutes;
    /** Si true, descuenta un corte de la suscripción activa en el shop */
    private Boolean useSubscription;
    /** Productos de la tienda a agregar a esta cita (opcional) */
    private List<AppointmentProductItem> products;
}
