package barberiapp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {

    private Long id;
    private Long appointmentId;
    private Long orderId;
    private String reviewType;

    private String reviewerUserId;
    private String reviewerName;

    private Long targetBarberId;
    private String targetShopId;
    private String targetUserId;

    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}
