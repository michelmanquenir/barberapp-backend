package barberiapp.controller;

import barberiapp.dto.AppointmentRequest;
import barberiapp.dto.BarberBookingRequest;
import barberiapp.model.Appointment;
import barberiapp.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    /**
     * GET /api/appointments/booked-barbers?shopId=&date=&time=&durationMinutes=
     * Devuelve barberos que tienen conflicto de agenda con el slot solicitado.
     * durationMinutes: 30 para servicio normal, 180 para domicilio (default 30)
     */
    @GetMapping("/booked-barbers")
    public List<Long> getBookedBarbers(
            @RequestParam String shopId,
            @RequestParam String date,
            @RequestParam String time,
            @RequestParam(defaultValue = "30") int durationMinutes) {
        LocalDate localDate = LocalDate.parse(date);
        LocalTime localTime = LocalTime.parse(time);
        return appointmentService.getBookedBarberIds(shopId, localDate, localTime, durationMinutes);
    }

    @GetMapping
    public List<Appointment> getUserAppointments(@RequestParam String userId) {
        return appointmentService.getUserAppointments(userId);
    }

    @PostMapping
    public ResponseEntity<?> createAppointment(@RequestBody AppointmentRequest request) {
        try {
            Appointment appointment = new Appointment();
            appointment.setDate(request.getDate());
            appointment.setTime(request.getTime());
            appointment.setLocation(request.getLocationType());
            appointment.setPaymentMethod(request.getPaymentMethod());
            appointment.setShopId(request.getShopId());
            // Domicilio
            appointment.setClientAddress(request.getClientAddress());
            appointment.setClientLatitude(request.getClientLatitude());
            appointment.setClientLongitude(request.getClientLongitude());
            appointment.setHomeDistanceKm(request.getHomeDistanceKm());
            appointment.setSurchargeAmount(request.getSurchargeAmount());
            appointment.setDurationMinutes(request.getDurationMinutes());

            boolean useSubscription = Boolean.TRUE.equals(request.getUseSubscription());
            Appointment saved = appointmentService.createAppointment(
                    appointment,
                    request.getUserId(),
                    request.getBarberId(),
                    request.getServiceId(),
                    useSubscription,
                    request.getProducts());
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** POST /api/appointments/barber — barbero agenda cita para un cliente (sin restricción de 15 min) */
    @PostMapping("/barber")
    public ResponseEntity<?> createBarberAppointment(@RequestBody BarberBookingRequest request) {
        try {
            String requesterId = getCurrentUserId();
            Appointment appointment = appointmentService.createBarberAppointment(request, requesterId);
            return ResponseEntity.ok(appointment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/cancel")
    public Appointment cancelAppointment(@PathVariable Long id, @RequestParam String userId) {
        return appointmentService.cancelAppointment(id, userId);
    }

    /** GET /api/appointments/shop/{shopId} — todas las citas del negocio (solo dueño) */
    @GetMapping("/shop/{shopId}")
    public ResponseEntity<?> getShopAppointments(@PathVariable String shopId) {
        try {
            String requesterId = getCurrentUserId();
            List<Appointment> appointments = appointmentService.getShopAppointments(shopId, requesterId);
            return ResponseEntity.ok(appointments);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/appointments/me/barber — citas asignadas al profesional autenticado
     * (desde hace 7 días hacia el futuro). Para el dashboard de empleado.
     */
    @GetMapping("/me/barber")
    public ResponseEntity<?> getMyBarberAppointments() {
        try {
            String userId = getCurrentUserId();
            List<Appointment> appointments = appointmentService.getBarberAppointments(userId);
            return ResponseEntity.ok(appointments);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** PUT /api/appointments/{id}/confirm — dueño/barbero confirma una cita pendiente */
    @PutMapping("/{id}/confirm")
    public ResponseEntity<?> confirmAppointment(@PathVariable Long id) {
        try {
            String requesterId = getCurrentUserId();
            Appointment updated = appointmentService.confirmAppointment(id, requesterId);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** PUT /api/appointments/{id}/complete — marcar cita como completada (dueño/barbero) */
    @PutMapping("/{id}/complete")
    public ResponseEntity<?> completeAppointment(@PathVariable Long id) {
        try {
            String requesterId = getCurrentUserId();
            Appointment updated = appointmentService.completeAppointment(id, requesterId);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** PUT /api/appointments/{id}/cancel-by-barber — dueño/barbero cancela la cita */
    @PutMapping("/{id}/cancel-by-barber")
    public ResponseEntity<?> cancelByBarber(@PathVariable Long id) {
        try {
            String requesterId = getCurrentUserId();
            Appointment updated = appointmentService.cancelByBarber(id, requesterId);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** PUT /api/appointments/{id}/no-show — cliente no se presentó */
    @PutMapping("/{id}/no-show")
    public ResponseEntity<?> noShowAppointment(@PathVariable Long id) {
        try {
            String requesterId = getCurrentUserId();
            Appointment updated = appointmentService.noShowAppointment(id, requesterId);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private String getCurrentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
