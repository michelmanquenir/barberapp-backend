package barberiapp.controller;

import barberiapp.dto.ScheduleConflictDto;
import barberiapp.model.BarberSchedule;
import barberiapp.service.BarberScheduleService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/barber-schedules")
public class BarberScheduleController {

    private final BarberScheduleService scheduleService;

    public BarberScheduleController(BarberScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    /**
     * GET /api/barber-schedules?barberId=&shopId=
     * Retorna horarios filtrados. Al menos uno de los parámetros es requerido.
     */
    @GetMapping
    public List<BarberSchedule> getSchedules(
            @RequestParam(required = false) Long barberId,
            @RequestParam(required = false) String shopId) {

        if (barberId != null && shopId != null) {
            return scheduleService.getByBarberAndShop(barberId, shopId);
        }
        if (barberId != null) {
            return scheduleService.getByBarber(barberId);
        }
        if (shopId != null) {
            return scheduleService.getByShop(shopId);
        }
        return List.of();
    }

    /**
     * POST /api/barber-schedules
     * Crea (o reemplaza) un horario para un barbero en un negocio y día de semana.
     * Body: { barberId, shopId, dayOfWeek, startTime "HH:mm", endTime "HH:mm" }
     */
    @PostMapping
    public ResponseEntity<BarberSchedule> create(@RequestBody Map<String, String> body) {
        Long barberId    = Long.parseLong(body.get("barberId"));
        String shopId    = body.get("shopId");
        String dayOfWeek = body.get("dayOfWeek");
        LocalTime start  = LocalTime.parse(body.get("startTime"));
        LocalTime end    = LocalTime.parse(body.get("endTime"));

        BarberSchedule created = scheduleService.create(barberId, shopId, dayOfWeek, start, end);
        return ResponseEntity.ok(created);
    }

    /**
     * DELETE /api/barber-schedules/{id}
     * Elimina una entrada de horario.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        scheduleService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/barber-schedules/conflict-check
     * Verifica si agendar una cita generaría conflicto de horario entre negocios.
     *
     * Params: barberId, shopId, date (yyyy-MM-dd), time (HH:mm), durationMinutes
     */
    @GetMapping("/conflict-check")
    public ScheduleConflictDto checkConflict(
            @RequestParam Long barberId,
            @RequestParam String shopId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time,
            @RequestParam(defaultValue = "30") int durationMinutes) {

        return scheduleService.checkConflict(barberId, shopId, date, time, durationMinutes);
    }
}
