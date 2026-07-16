package barberiapp.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class SelfCheckInResponse {
    private String    memberName;
    private String    shopName;
    private String    planName;
    private Integer   visitsAllowed;
    private Integer   visitsUsed;
    private LocalDate endDate;
    private LocalDate attendanceDate;
    /** true si esta asistencia agotó las visitas disponibles */
    private boolean   membershipJustExpired;
}
