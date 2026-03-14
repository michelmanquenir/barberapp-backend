package barberiapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class UserSummary {
    private String id;
    private String email;
    private String role;
    private String status;
    private String fullName;
    private String avatarUrl;
    private String dniUrl;
    private LocalDateTime createdAt;
}
