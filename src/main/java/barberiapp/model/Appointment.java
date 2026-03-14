package barberiapp.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Profile user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barber_id")
    private Barber barber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private ServiceEntity service;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private LocalTime time;

    @Column(length = 20)
    private String status = "pending"; // pending, confirmed, completed, cancelled, no_show

    @Column(length = 20)
    private String location = "barbershop"; // barbershop, home

    @Column(name = "payment_method", length = 20)
    private String paymentMethod = "cash"; // cash, transfer

    @Column(name = "total_price", nullable = false)
    private Integer totalPrice;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /** Nombre del cliente para citas walk-in agendadas por el barbero (puede ser null) */
    @Column(name = "client_name", length = 100)
    private String clientName;

    /** true cuando el barbero agenda la cita en nombre de un cliente */
    @Column(name = "booked_by_barber")
    private Boolean bookedByBarber = false;

    // ── Servicio a domicilio ────────────────────────────────────────────────────

    /** Dirección del cliente para servicio a domicilio */
    @Column(name = "client_address", length = 500)
    private String clientAddress;

    @Column(name = "client_latitude")
    private Double clientLatitude;

    @Column(name = "client_longitude")
    private Double clientLongitude;

    /** Distancia en km (línea recta) entre el negocio y el cliente */
    @Column(name = "home_distance_km")
    private Double homeDistanceKm;

    /** Recargo por servicio a domicilio (en pesos/moneda local) */
    @Column(name = "surcharge_amount")
    private Integer surchargeAmount = 0;

    /** Duración estimada en minutos: 30 normal, 180 para servicio a domicilio */
    @Column(name = "duration_minutes")
    private Integer durationMinutes = 30;

    /** Negocio donde se agendó la cita (puede ser null en datos anteriores) */
    @Column(name = "shop_id", length = 36)
    private String shopId;

    /**
     * ID de la suscripción usada para pagar esta cita.
     * Null si el pago fue en efectivo, transferencia, wallet, etc.
     */
    @Column(name = "subscription_id")
    private Long subscriptionId;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null)
            createdAt = LocalDateTime.now();
        if (status == null)
            status = "pending";
        if (location == null)
            location = "barbershop";
        if (paymentMethod == null)
            paymentMethod = "cash";
        if (bookedByBarber == null)
            bookedByBarber = false;
        if (surchargeAmount == null)
            surchargeAmount = 0;
        if (durationMinutes == null)
            durationMinutes = 30;
    }
}
