package barberiapp.config;

import barberiapp.model.BusinessCategory;
import barberiapp.repository.BusinessCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Siembra las categorías de negocio por defecto al iniciar la app,
 * solo si la tabla está vacía.
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final BusinessCategoryRepository categoryRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (categoryRepository.count() > 0) return;

        List<BusinessCategory> defaults = List.of(
            BusinessCategory.builder()
                .id(UUID.randomUUID().toString())
                .name("Barbería")
                .slug("barberia")
                .icon("✂️")
                .description("Cortes de cabello, barba y servicios de barbería clásica.")
                .sortOrder(1)
                .build(),
            BusinessCategory.builder()
                .id(UUID.randomUUID().toString())
                .name("Estilista / Peluquería")
                .slug("estilista")
                .icon("💇")
                .description("Salón de belleza, cortes, tintes, permanentes y tratamientos capilares.")
                .sortOrder(2)
                .build(),
            BusinessCategory.builder()
                .id(UUID.randomUUID().toString())
                .name("Lashes / Uñas")
                .slug("lashes")
                .icon("💅")
                .description("Extensiones de pestañas, lifting, manicure y pedicure.")
                .sortOrder(3)
                .build(),
            BusinessCategory.builder()
                .id(UUID.randomUUID().toString())
                .name("Bazar / Productos")
                .slug("bazar")
                .icon("🧴")
                .description("Tienda de productos de belleza, cuidado personal y cosmética.")
                .sortOrder(4)
                .build()
        );

        categoryRepository.saveAll(defaults);
    }
}
