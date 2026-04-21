package barberiapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "shelves")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Shelf {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ID del negocio al que pertenece la estantería */
    @Column(name = "shop_id", length = 36, nullable = false)
    private String shopId;

    /** Nombre descriptivo (ej: "Estantería Principal", "Bodega B") */
    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** Número de filas: 1 → fila A, 2 → A-B, 3 → A-C ... máx 26 */
    @Column(nullable = false)
    private int rows;

    /** Número de columnas por fila (1, 2, 3...) */
    @Column(nullable = false)
    private int columns;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
