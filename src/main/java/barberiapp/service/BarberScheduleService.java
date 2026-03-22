package barberiapp.service;

import barberiapp.dto.ScheduleConflictDto;
import barberiapp.model.Appointment;
import barberiapp.model.Barber;
import barberiapp.model.BarberSchedule;
import barberiapp.model.BarberShop;
import barberiapp.repository.AppointmentRepository;
import barberiapp.repository.BarberRepository;
import barberiapp.repository.BarberScheduleRepository;
import barberiapp.repository.BarberShopMemberRepository;
import barberiapp.repository.BarberShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BarberScheduleService {

    /** Mínimo de minutos de separación entre citas de distintos negocios */
    private static final int MIN_BUFFER_MINUTES = 30;

    private static final DateTimeFormatter HM = DateTimeFormatter.ofPattern("HH:mm");

    private final BarberScheduleRepository scheduleRepository;
    private final BarberRepository         barberRepository;
    private final BarberShopRepository     shopRepository;
    private final BarberShopMemberRepository memberRepository;
    private final AppointmentRepository    appointmentRepository;

    // ── Consultas ─────────────────────────────────────────────────────────────

    /** Todos los horarios activos de un barbero (en todos los negocios) */
    public List<BarberSchedule> getByBarber(Long barberId) {
        return scheduleRepository.findByBarberIdAndActiveTrue(barberId);
    }

    /** Horarios activos de un barbero en un negocio específico */
    public List<BarberSchedule> getByBarberAndShop(Long barberId, String shopId) {
        return scheduleRepository.findByBarberIdAndShopIdAndActiveTrue(barberId, shopId);
    }

    /** Todos los horarios de los barberos de un negocio */
    public List<BarberSchedule> getByShop(String shopId) {
        return scheduleRepository.findByShopIdAndActiveTrue(shopId);
    }

    // ── Crear / Eliminar ──────────────────────────────────────────────────────

    @Transactional
    public BarberSchedule create(Long barberId, String shopId, String dayOfWeek,
                                 LocalTime startTime, LocalTime endTime) {
        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("La hora de inicio debe ser anterior a la de fin");
        }

        Barber barber = barberRepository.findById(barberId)
                .orElseThrow(() -> new IllegalArgumentException("Barbero no encontrado"));
        BarberShop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new IllegalArgumentException("Negocio no encontrado"));

        // Verificar que el barbero pertenece al negocio
        if (!memberRepository.existsByShopIdAndBarberIdAndActiveTrue(shopId, barberId)) {
            throw new IllegalArgumentException("El barbero no pertenece a este negocio");
        }

        String day = dayOfWeek.toUpperCase();

        // Validar que el nuevo turno no se solape con alguno existente del mismo día
        List<BarberSchedule> sameDay = scheduleRepository
                .findByBarberIdAndShopIdAndActiveTrue(barberId, shopId)
                .stream()
                .filter(s -> s.getDayOfWeek().equals(day))
                .toList();

        for (BarberSchedule existing : sameDay) {
            // Dos rangos se solapan si uno empieza antes de que el otro termine
            boolean overlaps = startTime.isBefore(existing.getEndTime())
                    && endTime.isAfter(existing.getStartTime());
            if (overlaps) {
                throw new IllegalArgumentException(
                    String.format("El turno %s–%s se solapa con uno existente (%s–%s) ese día.",
                        startTime.format(HM), endTime.format(HM),
                        existing.getStartTime().format(HM), existing.getEndTime().format(HM))
                );
            }
        }

        BarberSchedule schedule = new BarberSchedule();
        schedule.setBarber(barber);
        schedule.setShop(shop);
        schedule.setDayOfWeek(day);
        schedule.setStartTime(startTime);
        schedule.setEndTime(endTime);

        return scheduleRepository.save(schedule);
    }

    @Transactional
    public void delete(Long scheduleId) {
        scheduleRepository.deleteById(scheduleId);
    }

    // ── Detección de conflictos ────────────────────────────────────────────────

    /**
     * Verifica si agendar una cita para {@code barberId} en {@code shopId}
     * el {@code date} a las {@code time} con duración {@code durationMinutes}
     * generaría un conflicto de tiempo con otras citas del barbero ese día.
     *
     * <p>Lógica:
     * <ul>
     *   <li>Se buscan TODAS las citas NO canceladas del barbero en esa fecha.</li>
     *   <li>Se excluyen las que son del mismo negocio (citas entre dos barberos
     *       del mismo local ya están controladas por el overlap check existente).</li>
     *   <li>Si alguna cita de otro negocio tiene menos de {@value MIN_BUFFER_MINUTES}
     *       minutos de separación con la nueva → conflicto.</li>
     * </ul>
     */
    public ScheduleConflictDto checkConflict(Long barberId, String shopId,
                                              LocalDate date, LocalTime time,
                                              int durationMinutes) {
        int dur = (durationMinutes > 0) ? durationMinutes : 30;
        LocalTime newStart = time;
        LocalTime newEnd   = time.plusMinutes(dur);

        // 1. Citas del barbero ese día (sin canceladas)
        List<Appointment> dayApts = appointmentRepository
                .findByBarberIdAndDateAndStatusNot(barberId, date, "cancelled");

        // 2. IDs de los barberos del negocio actual (para detectar si las citas son del mismo local)
        List<Long> sameShopBarberIds = memberRepository.findByShopIdAndActiveTrue(shopId)
                .stream()
                .map(m -> m.getBarber().getId())
                .toList();

        // 3. Buscar el nombre del negocio de cada cita conflictiva (a través de los miembros)
        for (Appointment apt : dayApts) {
            LocalTime aptStart = apt.getTime();
            int aptDur = (apt.getDurationMinutes() != null && apt.getDurationMinutes() > 0)
                    ? apt.getDurationMinutes() : 30;
            LocalTime aptEnd = aptStart.plusMinutes(aptDur);

            // ¿Overlap real (ya debería estar prevenido por el booked-barbers check)?
            // Los ignoramos — solo nos interesan citas cercanas sin solaparse.
            boolean overlaps = aptStart.isBefore(newEnd) && aptEnd.isAfter(newStart);
            if (overlaps) continue; // el solapamiento lo gestiona el flow existente

            // Calcular hueco entre las dos citas
            int gap;
            if (aptEnd.compareTo(newStart) <= 0) {
                // cita existente termina ANTES de la nueva
                gap = (int) Duration.between(aptEnd, newStart).toMinutes();
            } else {
                // cita existente empieza DESPUÉS de la nueva
                gap = (int) Duration.between(newEnd, aptStart).toMinutes();
            }

            if (gap < MIN_BUFFER_MINUTES) {
                // Intentar determinar el nombre del otro negocio
                String otherShopName = resolveShopName(apt.getBarber().getId(), shopId);

                String msg = gap == 0
                        ? String.format(
                            "El barbero pasa directamente de una cita a otra (0 min de margen). " +
                            "Se recomiendan al menos %d min entre citas de distintos negocios.", MIN_BUFFER_MINUTES)
                        : String.format(
                            "Solo hay %d min de margen entre esta cita y otra del barbero%s. " +
                            "Se recomiendan al menos %d min para desplazamientos entre negocios.",
                            gap,
                            otherShopName != null ? " en " + otherShopName : "",
                            MIN_BUFFER_MINUTES);

                return new ScheduleConflictDto(
                        true,
                        otherShopName,
                        aptStart.format(HM),
                        aptEnd.format(HM),
                        newStart.format(HM),
                        newEnd.format(HM),
                        gap,
                        msg
                );
            }
        }

        return ScheduleConflictDto.none();
    }

    /**
     * Intenta obtener el nombre del negocio donde el barbero tiene la cita conflictiva.
     * Busca entre los negocios donde está activo el barbero, excluyendo {@code excludeShopId}.
     */
    private String resolveShopName(Long barberId, String excludeShopId) {
        return memberRepository.findByBarberId(barberId).stream()
                .filter(m -> Boolean.TRUE.equals(m.getActive())
                        && !m.getShop().getId().equals(excludeShopId))
                .map(m -> m.getShop().getName())
                .findFirst()
                .orElse(null);
    }
}
