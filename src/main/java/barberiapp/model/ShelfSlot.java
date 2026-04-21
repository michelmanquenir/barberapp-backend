package barberiapp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "shelf_slots",
       uniqueConstraints = @UniqueConstraint(columnNames = {"shelf_id", "code"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShelfSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shelf_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Shelf shelf;

    /** Código de la posición: "A1", "B3", "C10" */
    @Column(nullable = false, length = 10)
    private String code;

    /** Etiqueta opcional libre (ej: "Lapiceras", "Cuadernos tamaño carta") */
    @Column(length = 120)
    private String label;
}
