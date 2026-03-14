package barberiapp.dto;

/**
 * Resultado de verificar si una nueva cita genera conflicto de horario
 * entre negocios para el mismo barbero.
 */
public record ScheduleConflictDto(
        boolean hasConflict,
        String conflictingShopName,   // nombre del otro negocio (si se conoce)
        String existingStart,          // hora inicio de la cita conflictiva (HH:mm)
        String existingEnd,            // hora fin de la cita conflictiva (HH:mm)
        String newStart,               // hora inicio de la nueva cita (HH:mm)
        String newEnd,                 // hora fin de la nueva cita (HH:mm)
        int gapMinutes,               // minutos de diferencia entre las dos citas
        String message                 // mensaje legible para el usuario
) {
    /** Resultado sin conflicto */
    public static ScheduleConflictDto none() {
        return new ScheduleConflictDto(false, null, null, null, null, null, 0, null);
    }
}
