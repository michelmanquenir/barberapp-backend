package barberiapp.dto;

import barberiapp.model.GymMembership;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class GymMembershipResponse {
    private Long id;
    private Long memberId;
    private String shopId;
    private String planName;
    private Integer monthlyPrice;
    private Integer visitsAllowed;
    private Integer visitsUsed;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private String paymentStatus;
    private String notes;
    private LocalDateTime createdAt;
    // Computed
    private Integer visitsRemaining;
    private Long daysRemaining;

    public static GymMembershipResponse from(GymMembership m) {
        GymMembershipResponse r = new GymMembershipResponse();
        r.setId(m.getId());
        r.setMemberId(m.getMemberId());
        r.setShopId(m.getShopId());
        r.setPlanName(m.getPlanName());
        r.setMonthlyPrice(m.getMonthlyPrice());
        r.setVisitsAllowed(m.getVisitsAllowed());
        r.setVisitsUsed(m.getVisitsUsed() != null ? m.getVisitsUsed() : 0);
        r.setStartDate(m.getStartDate());
        r.setEndDate(m.getEndDate());
        r.setStatus(m.getStatus());
        r.setPaymentStatus(m.getPaymentStatus());
        r.setNotes(m.getNotes());
        r.setCreatedAt(m.getCreatedAt());
        if (m.getVisitsAllowed() != null) {
            r.setVisitsRemaining(Math.max(0, m.getVisitsAllowed() - (m.getVisitsUsed() != null ? m.getVisitsUsed() : 0)));
        }
        if (m.getEndDate() != null) {
            long days = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), m.getEndDate());
            r.setDaysRemaining(Math.max(0, days));
        }
        return r;
    }
}
