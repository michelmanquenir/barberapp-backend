package barberiapp.dto;

import lombok.Data;

@Data
public class GalleryRequest {
    private String name;
    private String description;
    private String coverUrl;
    /** ID de la barbería asociada (null = galería general) */
    private String shopId;
    /** Nombre de la barbería (para desnormalización) */
    private String shopName;
    /** true = oculta para clientes */
    private Boolean hidden;
}
