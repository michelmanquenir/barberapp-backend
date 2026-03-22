package barberiapp.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Ejecuta migraciones SQL manuales al iniciar la aplicación.
 * Se ejecuta ANTES de DataInitializer (Order 1 vs 2).
 *
 * Se usa cuando ddl-auto=update no puede eliminar constraints existentes.
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class SchemaMigrationRunner implements ApplicationRunner {

    private final JdbcTemplate jdbc;

    @Override
    public void run(ApplicationArguments args) {
        dropUniqueConstraintBarberScheduleDay();
    }

    /**
     * Elimina el unique constraint (barber_id, shop_id, day_of_week) de barber_schedules.
     * Antes del fix, solo se permitía un turno por día. Ahora se permiten múltiples turnos
     * (ej. 09:00-14:00 y 18:00-21:00 el mismo día), con validación de solapamiento en el servicio.
     */
    private void dropUniqueConstraintBarberScheduleDay() {
        try {
            jdbc.execute(
                "ALTER TABLE barber_schedules DROP CONSTRAINT IF EXISTS uq_barber_shop_day"
            );
            log.info("SchemaMigration: constraint uq_barber_shop_day eliminado (o ya no existía)");
        } catch (Exception e) {
            log.warn("SchemaMigration: no se pudo eliminar uq_barber_shop_day — {}", e.getMessage());
        }
    }
}
