package barberiapp.service;

import barberiapp.model.Appointment;
import barberiapp.model.Barber;
import barberiapp.model.Profile;
import barberiapp.model.ServiceEntity;
import barberiapp.repository.AppointmentRepository;
import barberiapp.repository.BarberRepository;
import barberiapp.repository.ProfileRepository;
import barberiapp.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final ProfileRepository profileRepository;
    private final BarberRepository barberRepository;
    private final ServiceRepository serviceRepository;

    public List<Appointment> getUserAppointments(String userId) {
        return appointmentRepository.findByUserIdOrderByDateDescTimeDesc(userId);
    }

    public Appointment createAppointment(Appointment appointmentReq, String userId, Long barberId, Long serviceId) {
        Profile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Barber barber = barberRepository.findById(barberId)
                .orElseThrow(() -> new RuntimeException("Barber not found"));
        ServiceEntity service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));

        appointmentReq.setUser(profile);
        appointmentReq.setBarber(barber);
        appointmentReq.setService(service);

        // El precio total normalmente lo definiriamos aqui para mayor seguridad:
        appointmentReq.setTotalPrice(service.getPrice()); // o permitirlo desde el request si aplica

        return appointmentRepository.save(appointmentReq);
    }

    public Appointment cancelAppointment(Long appointmentId, String userId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (!appointment.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        appointment.setStatus("cancelled");
        return appointmentRepository.save(appointment);
    }
}
