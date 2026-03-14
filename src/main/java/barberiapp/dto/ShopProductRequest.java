package barberiapp.dto;

import lombok.Data;

@Data
public class ShopProductRequest {

    private String name;
    private String description;
    private String category;
    private String imageUrl;

    /** Precio de compra / costo */
    private Integer purchasePrice;

    /** Precio de venta al público */
    private Integer salePrice;

    /** Unidades en stock */
    private Integer stock;

    /** Si false, el producto no es visible para los clientes */
    private Boolean active;
}
