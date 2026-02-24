package barberiapp.controller;

import barberiapp.dto.AppointmentRequest;
import barberiapp.model.Appointment;
import barberiapp.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @GetMapping
    public List<Appointment> getUserAppointments(@RequestParam String userId) {
        return appointmentService.getUserAppointments(userId);
    }

    @PostMapping
    public Appointment createAppointment(@RequestBody AppointmentRequest request) {
        Appointment appointment = new Appointment();
        appointment.setDate(request.getDate());
        appointment.setTime(request.getTime());
        appointment.setLocation(request.getLocationType());
        appointment.setPaymentMethod(request.getPaymentMethod());

        return appointmentService.createAppointment(
                appointment,
                request.getUserId(),
                request.getBarberId(),
                request.getServiceId());
    }

    @PutMapping("/{id}/cancel")
    public Appointment cancelAppointment(@PathVariable Long id, @RequestParam String userId) {
        return appointmentService.cancelAppointment(id, userId);
    }
}
