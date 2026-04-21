package barberiapp.dto;

import lombok.Data;

@Data
public class ShelfRequest {
    private String name;
    private String description;
    private int rows;
    private int columns;
}
