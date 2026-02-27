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
    private List<Barber> barbers;
}
