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

    /** Código de barras (EAN-13, UPC-A, Code128, etc.) */
    private String barcode;

    /** SKU / código interno del negocio */
    private String sku;

    /**
     * ID de un GlobalProduct del catálogo global.
     * Si se provee, los campos name/imageUrl/barcode/sku/description/category
     * se heredan del catálogo y los valores locales se ignoran.
     */
    private Long globalProductId;

    /**
     * ID del slot de estantería donde se almacena este producto.
     * null = sin ubicación asignada; -1 = quitar ubicación existente.
     */
    private Long shelfSlotId;
}
