package barberiapp.dto;

import barberiapp.model.ShelfSlot;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ShelfSlotResponse {

    private Long id;
    private String code;
    private String label;

    /** Productos asignados a esta posición (vacía = slot libre). */
    private List<SlotProduct> products = new ArrayList<>();

    @Data
    public static class SlotProduct {
        private Long productId;
        private String productName;
        private String productImageUrl;
        private Integer productStock;
        private Integer productSalePrice;
    }

    public static ShelfSlotResponse from(ShelfSlot slot) {
        ShelfSlotResponse r = new ShelfSlotResponse();
        r.setId(slot.getId());
        r.setCode(slot.getCode());
        r.setLabel(slot.getLabel());
        return r;
    }
}
