package barberiapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBarberAccountRequest {
    /** Email que se usará para la cuenta de la app del profesional */
    private String email;
}
