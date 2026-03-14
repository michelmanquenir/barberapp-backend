package barberiapp.dto;

import barberiapp.model.UserSubscription;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class UserSubscriptionResponse {
    private Long id;
    private String userId;
    private String userName;
    private String shopId;
    private String shopName;
    private Long planId;
    private String planName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer cutsUsed;
    private Integer cutsAllowed;
    private Integer cutsRemaining;
    private Integer priceCharged;
    private String status;        // "active" | "expired" | "cancelled"
    private int daysRemaining;
    private LocalDateTime createdAt;

    public static UserSubscriptionResponse from(UserSubscription s) {
        UserSubscriptionResponse dto = new UserSubscriptionResponse();
        dto.setId(s.getId());
        dto.setUserId(s.getUserId());
        dto.setUserName(s.getUserName());
        dto.setShopId(s.getShopId());
        dto.setShopName(s.getShopName());
        dto.setPlanId(s.getPlan() != null ? s.getPlan().getId() : null);
        dto.setPlanName(s.getPlanName());
        dto.setStartDate(s.getStartDate());
        dto.setEndDate(s.getEndDate());
        dto.setCutsUsed(s.getCutsUsed());
        dto.setCutsAllowed(s.getCutsAllowed());
        dto.setCutsRemaining(Math.max(0, s.getCutsAllowed() - s.getCutsUsed()));
        dto.setPriceCharged(s.getPriceCharged());
        dto.setStatus(s.computedStatus());
        dto.setDaysRemaining(Math.max(0, (int) (s.getEndDate().toEpochDay() - LocalDate.now().toEpochDay())));
        dto.setCreatedAt(s.getCreatedAt());
        return dto;
    }
}
