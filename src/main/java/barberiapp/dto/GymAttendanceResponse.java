package barberiapp.dto;

import barberiapp.model.GymAttendance;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class GymAttendanceResponse {
    private Long id;
    private Long memberId;
    private String memberName;
    private String shopId;
    private LocalDate attendanceDate;
    private LocalTime checkInTime;
    private String classType;
    private Boolean isTrialClass;
    private String notes;
    private LocalDateTime createdAt;

    public static GymAttendanceResponse from(GymAttendance a) {
        GymAttendanceResponse r = new GymAttendanceResponse();
        r.setId(a.getId());
        r.setMemberId(a.getMemberId());
        r.setShopId(a.getShopId());
        r.setAttendanceDate(a.getAttendanceDate());
        r.setCheckInTime(a.getCheckInTime());
        r.setClassType(a.getClassType());
        r.setIsTrialClass(a.getIsTrialClass());
        r.setNotes(a.getNotes());
        r.setCreatedAt(a.getCreatedAt());
        return r;
    }
}
