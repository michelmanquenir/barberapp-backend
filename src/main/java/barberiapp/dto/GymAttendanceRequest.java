package barberiapp.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class GymAttendanceRequest {
    private Long memberId;
    private LocalDate attendanceDate;
    private LocalTime checkInTime;
    private String classType;
    private Boolean isTrialClass;
    private String notes;
}
