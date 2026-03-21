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

        // ── Categorías raíz ───────────────────────────────────────────────────
        String idBarberia   = UUID.randomUUID().toString();
        String idBelleza    = UUID.randomUUID().toString();
        String idBazar      = UUID.randomUUID().toString();
        String idSalud      = UUID.randomUUID().toString();
        String idDeportes   = UUID.randomUUID().toString();
        String idOtros      = UUID.randomUUID().toString();

        List<ProductCategory> parents = List.of(
            pc(idBarberia, "Barbería y Cuidado Capilar", "✂️", null, 1),
            pc(idBelleza,  "Belleza y Maquillaje",       "💄", null, 2),
            pc(idBazar,    "Bazar / Abarrotes",           "🛒", null, 3),
            pc(idSalud,    "Salud y Bienestar",           "💊", null, 4),
            pc(idDeportes, "Deportes y Fitness",          "🏋️", null, 5),
            pc(idOtros,    "Otros",                       "📦", null, 99)
        );
        productCategoryRepository.saveAll(parents);

        // ── Subcategorías ─────────────────────────────────────────────────────
        List<ProductCategory> children = List.of(
            // Barbería y Cuidado Capilar
            pc(UUID.randomUUID().toString(), "Shampoos y acondicionadores", "🚿",  idBarberia, 1),
            pc(UUID.randomUUID().toString(), "Pomadas, ceras y fijadores",  "🪮",  idBarberia, 2),
            pc(UUID.randomUUID().toString(), "Tintes y coloración",         "🎨",  idBarberia, 3),
            pc(UUID.randomUUID().toString(), "Cuidado de la barba",         "🧔",  idBarberia, 4),
            pc(UUID.randomUUID().toString(), "Tratamientos capilares",      "✨",  idBarberia, 5),
            // Belleza y Maquillaje
            pc(UUID.randomUUID().toString(), "Maquillaje facial",           "💄",  idBelleza, 1),
            pc(UUID.randomUUID().toString(), "Cuidado de la piel",          "🧴",  idBelleza, 2),
            pc(UUID.randomUUID().toString(), "Uñas y esmaltes",             "💅",  idBelleza, 3),
            pc(UUID.randomUUID().toString(), "Perfumes y fragancias",       "🌸",  idBelleza, 4),
            // Bazar / Abarrotes
            pc(UUID.randomUUID().toString(), "Bebidas",                     "🥤",  idBazar, 1),
            pc(UUID.randomUUID().toString(), "Alimentos y abarrotes",       "🥫",  idBazar, 2),
            pc(UUID.randomUUID().toString(), "Snacks y golosinas",          "🍬",  idBazar, 3),
            pc(UUID.randomUUID().toString(), "Limpieza del hogar",          "🧹",  idBazar, 4),
            pc(UUID.randomUUID().toString(), "Higiene personal",            "🧼",  idBazar, 5),
            // Salud y Bienestar
            pc(UUID.randomUUID().toString(), "Suplementos y vitaminas",     "💊",  idSalud, 1),
            pc(UUID.randomUUID().toString(), "Medicamentos OTC",            "🩺",  idSalud, 2),
            pc(UUID.randomUUID().toString(), "Productos naturales",         "🌿",  idSalud, 3),
            // Deportes y Fitness
            pc(UUID.randomUUID().toString(), "Equipamiento deportivo",      "🥊",  idDeportes, 1),
            pc(UUID.randomUUID().toString(), "Ropa deportiva",              "👟",  idDeportes, 2),
            pc(UUID.randomUUID().toString(), "Nutrición deportiva",         "🥗",  idDeportes, 3),
            // Otros
            pc(UUID.randomUUID().toString(), "Accesorios",                  "🧲",  idOtros, 1),
            pc(UUID.randomUUID().toString(), "General",                     "📦",  idOtros, 99)
        );
        productCategoryRepository.saveAll(children);
    }

    /** Helper para construir ProductCategory sin repetir builder cada vez */
    private static ProductCategory pc(String id, String name, String icon, String parentId, int order) {
        return ProductCategory.builder()
                .id(id).name(name).icon(icon).parentId(parentId).sortOrder(order).build();
    }
}
