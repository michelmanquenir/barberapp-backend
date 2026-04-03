package barberiapp.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class GymMemberRequest {
    private String name;
    private String email;
    private String phone;
    private LocalDate birthDate;
    private String rut;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String medicalNotes;
    private LocalDate joinDate;
    private String status;
    private String photoUrl;

    /**
     * Si es true y el miembro tiene email, se crea una cuenta de usuario en la app
     * con contraseña provisional y se le envía un correo con las credenciales.
     */
    private boolean createAppAccount;
}
