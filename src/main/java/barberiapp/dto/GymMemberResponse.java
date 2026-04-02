package barberiapp.dto;

import barberiapp.model.GymMember;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class GymMemberResponse {
    private Long id;
    private String shopId;
    private String name;
    private String email;
    private String phone;
    private LocalDate birthDate;
    private String rut;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String medicalNotes;
    private LocalDate joinDate;
    private String status;
    private String photoUrl;
    private Boolean hasTakenTrialClass;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Computed fields loaded separately
    private GymMembershipResponse activeMembership;
    private Long totalAttendances;

    public static GymMemberResponse from(GymMember m) {
        GymMemberResponse r = new GymMemberResponse();
        r.setId(m.getId());
        r.setShopId(m.getShopId());
        r.setName(m.getName());
        r.setEmail(m.getEmail());
        r.setPhone(m.getPhone());
        r.setBirthDate(m.getBirthDate());
        r.setRut(m.getRut());
        r.setEmergencyContactName(m.getEmergencyContactName());
        r.setEmergencyContactPhone(m.getEmergencyContactPhone());
        r.setMedicalNotes(m.getMedicalNotes());
        r.setJoinDate(m.getJoinDate());
        r.setStatus(m.getStatus());
        r.setPhotoUrl(m.getPhotoUrl());
        r.setHasTakenTrialClass(m.getHasTakenTrialClass());
        r.setCreatedAt(m.getCreatedAt());
        r.setUpdatedAt(m.getUpdatedAt());
        return r;
    }
}
