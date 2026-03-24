package barberiapp.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Catálogo global de productos.
 * Almacena la información pública y compartida de un producto
 * (nombre, imagen, código de barras, SKU, descripción, categoría).
 *
 * Múltiples tiendas pueden vincular sus propios registros de Product
 * a un mismo GlobalProduct, evitando duplicación de datos públicos.
 * Cada tienda mantiene sus propios precios y stock en la tabla products.
 */
@Entity
@Table(name = "global_products",
    uniqueConstraints = @UniqueConstraint(name = "uq_global_product_barcode", columnNames = "barcode"))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlobalProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 100)
    private String category;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    /** Código de barras EAN-13, UPC-A, etc. Único en el catálogo global. */
    @Column(length = 100)
    private String barcode;

    @Column(length = 50)
    private String sku;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
