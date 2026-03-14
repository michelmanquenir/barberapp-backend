package barberiapp.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO para que un barbero agende una cita en nombre de un cliente.
 * Bypassa la restricción de 15 minutos de anticipación.
 */
@Data
public class BarberBookingRequest {

    /** ID del negocio donde se realizará el servicio */
    private String shopId;

    /** ID del servicio seleccionado */
    private Long serviceId;

    /** ID del barbero que realizará el servicio */
    private Long barberId;

    /** Fecha de la cita */
    private LocalDate date;

    /** Hora de la cita */
    private LocalTime time;

    /** Nombre del cliente (walk-in o referencia; puede ser null si clientUserId está presente) */
    private String clientName;

    /** ID de perfil del cliente si tiene cuenta registrada (opcional) */
    private String clientUserId;

    /** Método de pago: cash | transfer */
    private String paymentMethod;

    /** Ubicación: barbershop | home */
    private String locationType;

    // Servicio a domicilio
    private String clientAddress;
    private Double clientLatitude;
    private Double clientLongitude;
    private Double homeDistanceKm;
    private Integer surchargeAmount;
}
