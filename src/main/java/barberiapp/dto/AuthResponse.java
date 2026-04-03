package barberiapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String userId;
    private String email;
    private String role;
    private String fullName;
    private String avatarUrl;
    private String status;
    private Boolean mustChangePassword;
}
