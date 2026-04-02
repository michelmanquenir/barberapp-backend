package barberiapp.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
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
    private final ApplicationContext applicationContext;

    @Override
    public void run(ApplicationArguments args) {
        // Si el contexto ya está siendo destruido (shutdown en curso) no ejecutar migraciones
        if (applicationContext instanceof ConfigurableApplicationContext ctx && !ctx.isActive()) {
            log.warn("SchemaMigrationRunner: contexto inactivo, se omiten migraciones");
            return;
        }
        dropUniqueConstraintBarberScheduleDay();
        allowNullProductName();
        insertTransporteCategory();
        addTransportEventCoords();
        createShopGalleryImagesTable();
        addOrderIdToReviews();
        createGymTables();
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
            // Corregir filas existentes con active = NULL
            jdbc.update(
                "UPDATE business_categories SET active = true WHERE slug = 'transporte' AND active IS NULL"
            );
            if (count == null || count == 0) {
                String id = java.util.UUID.randomUUID().toString();
                jdbc.update(
                    "INSERT INTO business_categories (id, name, slug, icon, description, active, sort_order) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    id,
                    "Transporte",
                    "transporte",
                    "🚌",
                    "Servicios de transporte, traslados a eventos y logística de pasajeros.",
                    true,
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

    /**
     * Agrega columnas latitude/longitude a transport_events si no existen.
     * Necesario para el cálculo de tarifa de transporte sin geocoding.
     */
    private void addTransportEventCoords() {
        try {
            jdbc.execute("ALTER TABLE transport_events ADD COLUMN IF NOT EXISTS latitude DOUBLE PRECISION");
            jdbc.execute("ALTER TABLE transport_events ADD COLUMN IF NOT EXISTS longitude DOUBLE PRECISION");
            log.info("SchemaMigration: transport_events.latitude/longitude verificadas");
        } catch (Exception e) {
            log.debug("SchemaMigration: transport_events coords — {}", e.getMessage());
        }
    }

    /**
     * Crea la tabla shop_gallery_images si no existe.
     * Almacena las fotos del negocio (máx. 20 por negocio).
     */
    private void createShopGalleryImagesTable() {
        try {
            jdbc.execute("""
                CREATE TABLE IF NOT EXISTS shop_gallery_images (
                    id             BIGSERIAL PRIMARY KEY,
                    shop_id        VARCHAR(36)  NOT NULL,
                    image_url      TEXT         NOT NULL,
                    caption        VARCHAR(300),
                    display_order  INT          NOT NULL DEFAULT 0,
                    created_at     TIMESTAMP    NOT NULL DEFAULT NOW()
                )
                """);
            jdbc.execute(
                "CREATE INDEX IF NOT EXISTS idx_shop_gallery_shop_id ON shop_gallery_images(shop_id)"
            );
            log.info("SchemaMigration: tabla shop_gallery_images verificada");
        } catch (Exception e) {
            log.debug("SchemaMigration: shop_gallery_images — {}", e.getMessage());
        }
    }

    private void addOrderIdToReviews() {
        try {
            jdbc.execute(
                "ALTER TABLE reviews ADD COLUMN IF NOT EXISTS order_id BIGINT"
            );
            jdbc.execute(
                "ALTER TABLE reviews ALTER COLUMN appointment_id DROP NOT NULL"
            );
            log.info("SchemaMigration: reviews.order_id agregado, appointment_id ahora nullable");
        } catch (Exception e) {
            log.debug("SchemaMigration: addOrderIdToReviews — {}", e.getMessage());
        }
    }

    /**
     * Crea las 4 tablas del módulo Gym/Boxing si no existen.
     */
    private void createGymTables() {
        try {
            jdbc.execute("""
                CREATE TABLE IF NOT EXISTS gym_members (
                    id                      BIGSERIAL PRIMARY KEY,
                    shop_id                 VARCHAR(36)  NOT NULL,
                    name                    VARCHAR(100) NOT NULL,
                    email                   VARCHAR(200),
                    phone                   VARCHAR(30),
                    rut                     VARCHAR(20),
                    birth_date              DATE,
                    join_date               DATE,
                    status                  VARCHAR(20)  NOT NULL DEFAULT 'active',
                    photo_url               TEXT,
                    emergency_contact_name  VARCHAR(100),
                    emergency_contact_phone VARCHAR(30),
                    medical_notes           TEXT,
                    has_taken_trial_class   BOOLEAN      NOT NULL DEFAULT FALSE,
                    created_at              TIMESTAMP    NOT NULL DEFAULT NOW(),
                    updated_at              TIMESTAMP    NOT NULL DEFAULT NOW()
                )
                """);
            jdbc.execute(
                "CREATE INDEX IF NOT EXISTS idx_gym_members_shop_id ON gym_members(shop_id)"
            );

            jdbc.execute("""
                CREATE TABLE IF NOT EXISTS gym_memberships (
                    id              BIGSERIAL PRIMARY KEY,
                    member_id       BIGINT       NOT NULL,
                    shop_id         VARCHAR(36)  NOT NULL,
                    plan_name       VARCHAR(100),
                    monthly_price   INTEGER,
                    visits_allowed  INTEGER,
                    visits_used     INTEGER      NOT NULL DEFAULT 0,
                    start_date      DATE,
                    end_date        DATE,
                    status          VARCHAR(20)  NOT NULL DEFAULT 'active',
                    payment_status  VARCHAR(20)  NOT NULL DEFAULT 'pending',
                    notes           TEXT,
                    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
                    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW()
                )
                """);
            jdbc.execute(
                "CREATE INDEX IF NOT EXISTS idx_gym_memberships_member_id ON gym_memberships(member_id)"
            );
            jdbc.execute(
                "CREATE INDEX IF NOT EXISTS idx_gym_memberships_shop_id ON gym_memberships(shop_id)"
            );

            jdbc.execute("""
                CREATE TABLE IF NOT EXISTS gym_attendance (
                    id               BIGSERIAL PRIMARY KEY,
                    member_id        BIGINT       NOT NULL,
                    shop_id          VARCHAR(36)  NOT NULL,
                    attendance_date  DATE         NOT NULL,
                    check_in_time    TIME,
                    class_type       VARCHAR(100),
                    is_trial_class   BOOLEAN      NOT NULL DEFAULT FALSE,
                    notes            TEXT,
                    created_at       TIMESTAMP    NOT NULL DEFAULT NOW()
                )
                """);
            jdbc.execute(
                "CREATE INDEX IF NOT EXISTS idx_gym_attendance_member_id ON gym_attendance(member_id)"
            );
            jdbc.execute(
                "CREATE INDEX IF NOT EXISTS idx_gym_attendance_shop_date ON gym_attendance(shop_id, attendance_date)"
            );

            jdbc.execute("""
                CREATE TABLE IF NOT EXISTS gym_progress_records (
                    id           BIGSERIAL PRIMARY KEY,
                    member_id    BIGINT       NOT NULL,
                    shop_id      VARCHAR(36)  NOT NULL,
                    record_date  DATE         NOT NULL,
                    weight_kg    DOUBLE PRECISION,
                    height_cm    DOUBLE PRECISION,
                    body_fat_pct DOUBLE PRECISION,
                    chest_cm     DOUBLE PRECISION,
                    waist_cm     DOUBLE PRECISION,
                    hips_cm      DOUBLE PRECISION,
                    bicep_cm     DOUBLE PRECISION,
                    thigh_cm     DOUBLE PRECISION,
                    notes        TEXT,
                    created_at   TIMESTAMP    NOT NULL DEFAULT NOW()
                )
                """);
            jdbc.execute(
                "CREATE INDEX IF NOT EXISTS idx_gym_progress_member_id ON gym_progress_records(member_id)"
            );

            log.info("SchemaMigration: tablas gym verificadas/creadas correctamente");
        } catch (Exception e) {
            log.warn("SchemaMigration: createGymTables — {}", e.getMessage());
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
