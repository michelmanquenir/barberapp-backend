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
        allowNullProductName();
        insertTransporteCategory();
    }

    /**
     * Elimina el unique constraint (barber_id, shop_id, day_of_week) de barber_schedules.
     * Antes del fix, solo se permitía un turno por día. Ahora se permiten múltiples turnos
     * (ej. 09:00-14:00 y 18:00-21:00 el mismo día), con validación de solapamiento en el servicio.
     */
    /**
     * Permite NULL en products.name para que los productos vinculados al catálogo global
     * puedan tener nombre nulo (se resuelve desde GlobalProduct en runtime).
     * ddl-auto=update no puede hacer columnas NOT NULL → nullable sin migración manual.
     */
    private void allowNullProductName() {
        try {
            jdbc.execute("ALTER TABLE products ALTER COLUMN name DROP NOT NULL");
            log.info("SchemaMigration: products.name ahora permite NULL (catálogo global)");
        } catch (Exception e) {
            log.debug("SchemaMigration: products.name ya permite NULL o no se pudo modificar — {}", e.getMessage());
        }
    }

    /**
     * Inserts the "Transporte" business category if it doesn't already exist.
     * Needed for existing DBs where DataInitializer already ran and won't re-seed.
     */
    private void insertTransporteCategory() {
        try {
            Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM business_categories WHERE slug = 'transporte'",
                Integer.class
            );
            if (count == null || count == 0) {
                String id = java.util.UUID.randomUUID().toString();
                jdbc.update(
                    "INSERT INTO business_categories (id, name, slug, icon, description, sort_order) VALUES (?, ?, ?, ?, ?, ?)",
                    id,
                    "Transporte",
                    "transporte",
                    "🚌",
                    "Servicios de transporte, traslados a eventos y logística de pasajeros.",
                    6
                );
                log.info("SchemaMigration: categoría 'Transporte' insertada correctamente");
            } else {
                log.debug("SchemaMigration: categoría 'Transporte' ya existe, se omite inserción");
            }
        } catch (Exception e) {
            log.warn("SchemaMigration: no se pudo insertar categoría 'Transporte' — {}", e.getMessage());
        }
    }

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
