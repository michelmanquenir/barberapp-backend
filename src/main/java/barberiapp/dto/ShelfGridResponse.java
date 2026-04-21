package barberiapp.dto;

import lombok.Data;

import java.util.List;

@Data
public class ShelfGridResponse {

    private Long id;
    private String name;
    private String description;
    private int rows;
    private int columns;
    private List<ShelfSlotResponse> slots;
}
