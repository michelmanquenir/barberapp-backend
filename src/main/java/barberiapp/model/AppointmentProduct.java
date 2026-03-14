package barberiapp.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * Producto(s) añadidos a una cita.
 * Los campos productName y unitPrice son snapshots del momento de la compra,
 * para preservar el historial aunque el producto cambie de precio o se elimine.
 */
@Entity
@Table(name = "appointment_products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ID de la cita a la que pertenece este ítem */
    @Column(name = "appointment_id", nullable = false)
    private Long appointmentId;

    /**
     * ID del producto original (puede ser null si el producto fue eliminado
     * después de que se realizó la cita, pero el historial se conserva).
     */
    @Column(name = "product_id")
    private Long productId;

    /** Snapshot: nombre del producto en el momento de la compra */
    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;

    /** Snapshot: precio de venta en el momento de la compra */
    @Column(name = "unit_price", nullable = false)
    private Integer unitPrice;

    @Column(nullable = false)
    private Integer quantity;

    /** Subtotal = unitPrice × quantity */
    @Column(nullable = false)
    private Integer subtotal;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
