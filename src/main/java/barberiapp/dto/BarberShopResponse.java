package barberiapp.dto;

import barberiapp.model.Barber;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BarberShopResponse {
    private String id;
    private String name;
    private String description;
    private String slug;
    private Boolean active;
    private String address;
    private Double latitude;
    private Double longitude;
    private List<Barber> barbers;
    private Boolean homeServiceEnabled;
    private Integer pricePerKm;
    private String approvalStatus;
    private String categoryId;
}
