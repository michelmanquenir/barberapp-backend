package barberiapp.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class GymMembershipRequest {
    private String planName;
    private Integer monthlyPrice;
    private Integer visitsAllowed; // null = unlimited
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private String paymentStatus;
    private String notes;
}
