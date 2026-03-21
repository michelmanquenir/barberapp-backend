package barberiapp.config;

import barberiapp.model.BusinessCategory;
import barberiapp.model.ProductCategory;
import barberiapp.repository.BusinessCategoryRepository;
import barberiapp.repository.ProductCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Siembra las categorías de negocio y de producto por defecto al iniciar la app,
 * solo si las tablas están vacías.
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final BusinessCategoryRepository categoryRepository;
    private final ProductCategoryRepository productCategoryRepository;

    @Override
    public void run(ApplicationArguments args) {
        seedBusinessCategories();
        seedProductCategories();
    }

    private void seedBusinessCategories() {
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
                .build(),
            BusinessCategory.builder()
                .id(UUID.randomUUID().toString())
                .name("Gimnasio de Boxeo")
                .slug("gimnasio-boxeo")
                .icon("🥊")
                .description("Clases de boxeo, entrenamientos personalizados y artes marciales.")
                .sortOrder(5)
                .build()
        );

        categoryRepository.saveAll(defaults);
    }

    private void seedProductCategories() {
        if (productCategoryRepository.count() > 0) return;

        List<ProductCategory> defaults = List.of(
            ProductCategory.builder().id(UUID.randomUUID().toString()).name("Cuidado del cabello").icon("💆").sortOrder(1).build(),
            ProductCategory.builder().id(UUID.randomUUID().toString()).name("Cuidado de la barba").icon("🧔").sortOrder(2).build(),
            ProductCategory.builder().id(UUID.randomUUID().toString()).name("Cuidado de la piel").icon("✨").sortOrder(3).build(),
            ProductCategory.builder().id(UUID.randomUUID().toString()).name("Maquillaje").icon("💄").sortOrder(4).build(),
            ProductCategory.builder().id(UUID.randomUUID().toString()).name("Perfumes y fragancias").icon("🌸").sortOrder(5).build(),
            ProductCategory.builder().id(UUID.randomUUID().toString()).name("Uñas y esmaltes").icon("💅").sortOrder(6).build(),
            ProductCategory.builder().id(UUID.randomUUID().toString()).name("Suplementos y vitaminas").icon("💊").sortOrder(7).build(),
            ProductCategory.builder().id(UUID.randomUUID().toString()).name("Accesorios").icon("🧴").sortOrder(8).build(),
            ProductCategory.builder().id(UUID.randomUUID().toString()).name("Equipamiento deportivo").icon("🥊").sortOrder(9).build(),
            ProductCategory.builder().id(UUID.randomUUID().toString()).name("Otros").icon("📦").sortOrder(99).build()
        );

        productCategoryRepository.saveAll(defaults);
    }
}
