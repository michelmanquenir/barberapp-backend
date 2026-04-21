package barberiapp.dto;

import barberiapp.model.ShelfSlot;
import lombok.Data;

@Data
public class ShelfSlotResponse {

    private Long id;
    private String code;
    private String label;

    // Producto asignado a esta posición (null si está vacía)
    private Long productId;
    private String productName;
    private String productImageUrl;
    private Integer productStock;
    private Integer productSalePrice;

    public static ShelfSlotResponse from(ShelfSlot slot) {
        ShelfSlotResponse r = new ShelfSlotResponse();
        r.setId(slot.getId());
        r.setCode(slot.getCode());
        r.setLabel(slot.getLabel());
        return r;
    }
}
