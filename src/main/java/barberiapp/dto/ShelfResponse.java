package barberiapp.dto;

import barberiapp.model.Shelf;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ShelfResponse {

    private Long id;
    private String name;
    private String description;
    private int rows;
    private int columns;
    /** Total de posiciones = rows × columns */
    private int totalSlots;
    /** Posiciones con producto asignado */
    private int occupiedSlots;
    private LocalDateTime createdAt;

    public static ShelfResponse from(Shelf shelf) {
        ShelfResponse r = new ShelfResponse();
        r.setId(shelf.getId());
        r.setName(shelf.getName());
        r.setDescription(shelf.getDescription());
        r.setRows(shelf.getRows());
        r.setColumns(shelf.getColumns());
        r.setTotalSlots(shelf.getRows() * shelf.getColumns());
        r.setCreatedAt(shelf.getCreatedAt());
        return r;
    }
}
