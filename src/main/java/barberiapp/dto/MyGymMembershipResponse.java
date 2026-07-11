package barberiapp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class MyGymMembershipResponse {

    // Datos del miembro en el gym
    private Long   memberId;
    private String shopId;
    private String shopName;
    private String shopSlug;
    private String memberName;
    private LocalDate joinDate;
    private String memberStatus;

    // Membresía activa (null si no tiene ninguna)
    private Long      membershipId;
    private String    planName;
    private Integer   monthlyPrice;
    private LocalDate startDate;
    private LocalDate endDate;
    private String    membershipStatus;
    private String    paymentStatus;
    private Integer   visitsAllowed;
    private Integer   visitsUsed;
}
