package barberiapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLookupResponse {
    private String id;
    private String email;
    private String fullName;
    private String phone;
    private String rut;
}
