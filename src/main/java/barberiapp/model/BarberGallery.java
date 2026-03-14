package barberiapp.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "barber_galleries")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BarberGallery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barber_id", nullable = false)
    private Barber barber;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "cover_url", columnDefinition = "TEXT")
    private String coverUrl;

    /**
     * Asociación opcional a una barbería (ID de BarberShop).
     * NULL = galería "general" (no asociada a ningún negocio específico).
     */
    @Column(name = "shop_id", length = 36)
    private String shopId;

    /**
     * Nombre de la barbería desnormalizado para evitar join en consultas públicas.
     */
    @Column(name = "shop_name", length = 150)
    private String shopName;

    /**
     * Si true, la galería está oculta: no aparece para los clientes.
     * columnDefinition incluye DEFAULT false para que filas existentes tengan valor al añadir la columna.
     */
    @Column(name = "hidden", columnDefinition = "boolean default false")
    private Boolean hidden = false;

    @OneToMany(mappedBy = "gallery", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt DESC")
    private List<BarberGalleryImage> images = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (hidden == null)    hidden    = false;
    }
}
