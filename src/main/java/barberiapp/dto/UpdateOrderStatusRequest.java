package barberiapp.dto;

import lombok.Data;

@Data
public class UpdateOrderStatusRequest {
    /** pending | confirmed | ready | delivered | cancelled */
    private String status;
}
