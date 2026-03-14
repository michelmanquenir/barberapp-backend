package barberiapp.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String email;
    private String password;
    private String fullName;
    private String role; // "CLIENT" or "BARBER"
    private String rut;  // RUT chileno (ej: 12345678-9), requerido y único
}
