package barberiapp.service;

import barberiapp.dto.*;
import barberiapp.model.*;
import barberiapp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GymService {

    private final GymMemberRepository memberRepo;
    private final GymMembershipRepository membershipRepo;
    private final GymAttendanceRepository attendanceRepo;
    private final GymProgressRepository progressRepo;
    private final GymPlanRepository planRepo;
    private final BarberShopRepository shopRepo;
    private final AppUserRepository userRepo;
    private final ProfileRepository profileRepo;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private static final String TEMP_PASSWORD_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789@#$!";
    private static final SecureRandom RANDOM = new SecureRandom();

    private String generateTempPassword() {
        StringBuilder sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            sb.append(TEMP_PASSWORD_CHARS.charAt(RANDOM.nextInt(TEMP_PASSWORD_CHARS.length())));
        }
        return sb.toString();
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private String requireShopId(String slug) {
        return shopRepo.findBySlug(slug)
                .map(BarberShop::getId)
                .orElseThrow(() -> new IllegalArgumentException("Negocio no encontrado"));
    }

    // ─── MIEMBROS ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<GymMemberResponse> getMembers(String shopId) {
        List<GymMember> members = memberRepo.findByShopIdOrderByNameAsc(shopId);
        // Cargar membresía activa y total asistencias en batch
        Map<Long, GymMembership> activeMemberships = membershipRepo
                .findByShopIdAndStatusOrderByEndDateAsc(shopId, "active")
                .stream().collect(Collectors.toMap(GymMembership::getMemberId, m -> m, (a, b) -> a));

        return members.stream().map(m -> {
            GymMemberResponse r = GymMemberResponse.from(m);
            GymMembership mem = activeMemberships.get(m.getId());
            if (mem != null) r.setActiveMembership(GymMembershipResponse.from(mem));
            r.setTotalAttendances(attendanceRepo.countByMemberId(m.getId()));
            return r;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GymMemberResponse getMember(Long memberId, String shopId) {
        GymMember m = memberRepo.findByIdAndShopId(memberId, shopId)
                .orElseThrow(() -> new IllegalArgumentException("Miembro no encontrado"));
        GymMemberResponse r = GymMemberResponse.from(m);
        membershipRepo.findFirstByMemberIdAndStatusOrderByEndDateDesc(memberId, "active")
                .ifPresent(mem -> r.setActiveMembership(GymMembershipResponse.from(mem)));
        r.setTotalAttendances(attendanceRepo.countByMemberId(memberId));
        return r;
    }

    @Transactional
    public GymMemberResponse createMember(String shopId, GymMemberRequest req) {
        if (req.getName() == null || req.getName().isBlank())
            throw new IllegalArgumentException("El nombre es obligatorio");

        LocalDate joinDate = req.getJoinDate() != null ? req.getJoinDate() : LocalDate.now();

        GymMember m = GymMember.builder()
                .shopId(shopId)
                .name(req.getName().trim())
                .email(req.getEmail())
                .phone(req.getPhone())
                .birthDate(req.getBirthDate())
                .rut(req.getRut())
                .emergencyContactName(req.getEmergencyContactName())
                .emergencyContactPhone(req.getEmergencyContactPhone())
                .medicalNotes(req.getMedicalNotes())
                .joinDate(joinDate)
                .status(req.getStatus() != null ? req.getStatus() : "active")
                .photoUrl(req.getPhotoUrl())
                .build();
        memberRepo.save(m);

        // Cargar negocio y datos del dueño una sola vez
        BarberShop shop = shopRepo.findById(shopId).orElse(null);
        String shopName  = shop != null ? shop.getName() : "WeServ";
        String joinDateStr = joinDate.format(DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", Locale.of("es", "ES")));

        // ── Crear cuenta en la app con contraseña provisional ─────────────────
        if (req.isCreateAppAccount()) {
            if (req.getEmail() == null || req.getEmail().isBlank())
                throw new IllegalArgumentException("El email es obligatorio para crear una cuenta en la app");

            String email = req.getEmail().trim().toLowerCase();

            if (userRepo.findByEmail(email).isPresent())
                throw new IllegalArgumentException("Ya existe una cuenta con el email " + email);

            String tempPassword = generateTempPassword();
            String userId = UUID.randomUUID().toString();

            AppUser newUser = AppUser.builder()
                    .id(userId)
                    .email(email)
                    .rut(req.getRut() != null ? req.getRut().trim().toUpperCase() : null)
                    .passwordHash(passwordEncoder.encode(tempPassword))
                    .role(UserRole.CLIENT)
                    .status(UserStatus.ACTIVE)
                    .mustChangePassword(true)
                    .build();
            userRepo.save(newUser);

            Profile profile = new Profile();
            profile.setId(userId);
            profile.setAppUser(newUser);
            profile.setFullName(req.getName().trim());
            profileRepo.save(profile);

            emailService.sendGymMemberWelcome(email, req.getName().trim(), shopName, tempPassword);

        } else if (req.getEmail() != null && !req.getEmail().isBlank()) {
            // Usuario existente: enviar confirmación de matrícula sin contraseña
            emailService.sendGymMemberEnrollment(req.getEmail().trim(), req.getName().trim(), shopName, joinDateStr);
        }

        // ── Notificar al dueño del gym ────────────────────────────────────────
        if (shop != null && shop.getOwner() != null) {
            String ownerEmail = shop.getOwner().getEmail();
            String ownerName  = profileRepo.findById(shop.getOwner().getId())
                    .map(Profile::getFullName)
                    .orElse(ownerEmail);
            emailService.sendNewGymMemberToOwner(ownerEmail, ownerName, req.getName().trim(),
                    req.getEmail(), shopName, joinDateStr);
        }

        return GymMemberResponse.from(m);
    }

    @Transactional
    public GymMemberResponse updateMember(Long memberId, String shopId, GymMemberRequest req) {
        GymMember m = memberRepo.findByIdAndShopId(memberId, shopId)
                .orElseThrow(() -> new IllegalArgumentException("Miembro no encontrado"));
        if (req.getName() != null) m.setName(req.getName().trim());
        if (req.getEmail() != null) m.setEmail(req.getEmail());
        if (req.getPhone() != null) m.setPhone(req.getPhone());
        if (req.getBirthDate() != null) m.setBirthDate(req.getBirthDate());
        if (req.getRut() != null) m.setRut(req.getRut());
        if (req.getEmergencyContactName() != null) m.setEmergencyContactName(req.getEmergencyContactName());
        if (req.getEmergencyContactPhone() != null) m.setEmergencyContactPhone(req.getEmergencyContactPhone());
        if (req.getMedicalNotes() != null) m.setMedicalNotes(req.getMedicalNotes());
        if (req.getJoinDate() != null) m.setJoinDate(req.getJoinDate());
        if (req.getStatus() != null) m.setStatus(req.getStatus());
        if (req.getPhotoUrl() != null) m.setPhotoUrl(req.getPhotoUrl());
        return GymMemberResponse.from(memberRepo.save(m));
    }

    @Transactional
    public void deleteMember(Long memberId, String shopId) {
        GymMember m = memberRepo.findByIdAndShopId(memberId, shopId)
                .orElseThrow(() -> new IllegalArgumentException("Miembro no encontrado"));
        memberRepo.delete(m);
    }

    // ─── MEMBRESÍAS ───────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<GymMembershipResponse> getMemberships(Long memberId, String shopId) {
        memberRepo.findByIdAndShopId(memberId, shopId)
                .orElseThrow(() -> new IllegalArgumentException("Miembro no encontrado"));
        return membershipRepo.findByMemberIdOrderByCreatedAtDesc(memberId)
                .stream().map(GymMembershipResponse::from).collect(Collectors.toList());
    }

    @Transactional
    public GymMembershipResponse createMembership(Long memberId, String shopId, GymMembershipRequest req) {
        memberRepo.findByIdAndShopId(memberId, shopId)
                .orElseThrow(() -> new IllegalArgumentException("Miembro no encontrado"));
        // Expirar membresías activas anteriores
        membershipRepo.findByShopIdAndStatusOrderByEndDateAsc(shopId, "active")
                .stream().filter(m -> m.getMemberId().equals(memberId))
                .forEach(m -> { m.setStatus("expired"); membershipRepo.save(m); });

        GymMembership mem = GymMembership.builder()
                .memberId(memberId)
                .shopId(shopId)
                .planName(req.getPlanName())
                .monthlyPrice(req.getMonthlyPrice())
                .visitsAllowed(req.getVisitsAllowed())
                .startDate(req.getStartDate() != null ? req.getStartDate() : LocalDate.now())
                .endDate(req.getEndDate() != null ? req.getEndDate() : LocalDate.now().plusMonths(1))
                .status(req.getStatus() != null ? req.getStatus() : "active")
                .paymentStatus(req.getPaymentStatus() != null ? req.getPaymentStatus() : "pending")
                .notes(req.getNotes())
                .build();
        return GymMembershipResponse.from(membershipRepo.save(mem));
    }

    @Transactional
    public GymMembershipResponse updateMembership(Long membershipId, String shopId, GymMembershipRequest req) {
        GymMembership mem = membershipRepo.findById(membershipId)
                .filter(m -> m.getShopId().equals(shopId))
                .orElseThrow(() -> new IllegalArgumentException("Membresía no encontrada"));
        if (req.getPlanName() != null) mem.setPlanName(req.getPlanName());
        if (req.getMonthlyPrice() != null) mem.setMonthlyPrice(req.getMonthlyPrice());
        if (req.getVisitsAllowed() != null) mem.setVisitsAllowed(req.getVisitsAllowed());
        if (req.getStartDate() != null) mem.setStartDate(req.getStartDate());
        if (req.getEndDate() != null) mem.setEndDate(req.getEndDate());
        if (req.getStatus() != null) mem.setStatus(req.getStatus());
        if (req.getPaymentStatus() != null) mem.setPaymentStatus(req.getPaymentStatus());
        if (req.getNotes() != null) mem.setNotes(req.getNotes());
        return GymMembershipResponse.from(membershipRepo.save(mem));
    }

    // ─── ASISTENCIA ───────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<GymAttendanceResponse> getMemberAttendance(Long memberId, String shopId) {
        memberRepo.findByIdAndShopId(memberId, shopId)
                .orElseThrow(() -> new IllegalArgumentException("Miembro no encontrado"));
        return attendanceRepo.findByMemberIdOrderByAttendanceDateDescCreatedAtDesc(memberId)
                .stream().map(GymAttendanceResponse::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<GymAttendanceResponse> getTodayAttendance(String shopId) {
        return attendanceRepo.findByShopIdAndAttendanceDateOrderByCheckInTimeAsc(shopId, LocalDate.now())
                .stream().map(a -> {
                    GymAttendanceResponse r = GymAttendanceResponse.from(a);
                    memberRepo.findById(a.getMemberId()).ifPresent(m -> r.setMemberName(m.getName()));
                    return r;
                }).collect(Collectors.toList());
    }

    @Transactional
    public GymAttendanceResponse checkIn(String shopId, GymAttendanceRequest req) {
        GymMember member = memberRepo.findByIdAndShopId(req.getMemberId(), shopId)
                .orElseThrow(() -> new IllegalArgumentException("Miembro no encontrado"));

        boolean isTrial = Boolean.TRUE.equals(req.getIsTrialClass());

        // Validar clase de prueba: solo 1 por miembro
        if (isTrial && Boolean.TRUE.equals(member.getHasTakenTrialClass())) {
            throw new IllegalArgumentException("Este miembro ya tomó su clase de prueba");
        }

        // Registrar asistencia
        GymAttendance att = GymAttendance.builder()
                .memberId(req.getMemberId())
                .shopId(shopId)
                .attendanceDate(req.getAttendanceDate() != null ? req.getAttendanceDate() : LocalDate.now())
                .checkInTime(req.getCheckInTime())
                .classType(req.getClassType())
                .isTrialClass(isTrial)
                .notes(req.getNotes())
                .build();
        att = attendanceRepo.save(att);

        // Marcar clase de prueba usada
        if (isTrial) {
            member.setHasTakenTrialClass(true);
            memberRepo.save(member);
        }

        // Descontar visita de la membresía activa (si aplica y no es trial)
        if (!isTrial) {
            membershipRepo.findFirstByMemberIdAndStatusOrderByEndDateDesc(req.getMemberId(), "active")
                    .ifPresent(mem -> {
                        if (mem.getVisitsAllowed() != null) {
                            int used = (mem.getVisitsUsed() != null ? mem.getVisitsUsed() : 0) + 1;
                            mem.setVisitsUsed(used);
                            if (used >= mem.getVisitsAllowed()) mem.setStatus("expired");
                            membershipRepo.save(mem);
                        }
                    });
        }

        GymAttendanceResponse r = GymAttendanceResponse.from(att);
        r.setMemberName(member.getName());
        return r;
    }

    @Transactional
    public void deleteAttendance(Long attendanceId, String shopId) {
        GymAttendance att = attendanceRepo.findById(attendanceId)
                .filter(a -> a.getShopId().equals(shopId))
                .orElseThrow(() -> new IllegalArgumentException("Asistencia no encontrada"));
        attendanceRepo.delete(att);
    }

    // ─── PROGRESO ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<GymProgressResponse> getProgress(Long memberId, String shopId) {
        memberRepo.findByIdAndShopId(memberId, shopId)
                .orElseThrow(() -> new IllegalArgumentException("Miembro no encontrado"));
        return progressRepo.findByMemberIdOrderByRecordDateDesc(memberId)
                .stream().map(GymProgressResponse::from).collect(Collectors.toList());
    }

    @Transactional
    public GymProgressResponse addProgress(Long memberId, String shopId, GymProgressRequest req) {
        memberRepo.findByIdAndShopId(memberId, shopId)
                .orElseThrow(() -> new IllegalArgumentException("Miembro no encontrado"));
        GymProgressRecord p = GymProgressRecord.builder()
                .memberId(memberId)
                .shopId(shopId)
                .recordDate(req.getRecordDate() != null ? req.getRecordDate() : LocalDate.now())
                .weightKg(req.getWeightKg())
                .heightCm(req.getHeightCm())
                .bodyFatPct(req.getBodyFatPct())
                .chestCm(req.getChestCm())
                .waistCm(req.getWaistCm())
                .hipsCm(req.getHipsCm())
                .bicepCm(req.getBicepCm())
                .thighCm(req.getThighCm())
                .notes(req.getNotes())
                .build();
        return GymProgressResponse.from(progressRepo.save(p));
    }

    @Transactional
    public GymProgressResponse updateProgress(Long progressId, Long memberId, String shopId, GymProgressRequest req) {
        GymProgressRecord p = progressRepo.findByIdAndMemberId(progressId, memberId)
                .filter(pr -> pr.getShopId().equals(shopId))
                .orElseThrow(() -> new IllegalArgumentException("Registro no encontrado"));
        if (req.getRecordDate() != null) p.setRecordDate(req.getRecordDate());
        if (req.getWeightKg() != null) p.setWeightKg(req.getWeightKg());
        if (req.getHeightCm() != null) p.setHeightCm(req.getHeightCm());
        if (req.getBodyFatPct() != null) p.setBodyFatPct(req.getBodyFatPct());
        if (req.getChestCm() != null) p.setChestCm(req.getChestCm());
        if (req.getWaistCm() != null) p.setWaistCm(req.getWaistCm());
        if (req.getHipsCm() != null) p.setHipsCm(req.getHipsCm());
        if (req.getBicepCm() != null) p.setBicepCm(req.getBicepCm());
        if (req.getThighCm() != null) p.setThighCm(req.getThighCm());
        if (req.getNotes() != null) p.setNotes(req.getNotes());
        return GymProgressResponse.from(progressRepo.save(p));
    }

    @Transactional
    public void deleteProgress(Long progressId, Long memberId, String shopId) {
        GymProgressRecord p = progressRepo.findByIdAndMemberId(progressId, memberId)
                .filter(pr -> pr.getShopId().equals(shopId))
                .orElseThrow(() -> new IllegalArgumentException("Registro no encontrado"));
        progressRepo.delete(p);
    }

    // ─── PLANES DEL GYM ──────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<GymPlanResponse> getPlans(String shopId) {
        return planRepo.findByShopIdOrderByPriceAsc(shopId)
                .stream().map(GymPlanResponse::from).collect(Collectors.toList());
    }

    @Transactional
    public GymPlanResponse createPlan(String shopId, GymPlanRequest req) {
        if (req.getName() == null || req.getName().isBlank())
            throw new IllegalArgumentException("El nombre del plan es obligatorio");
        if (req.getPrice() == null || req.getPrice() < 0)
            throw new IllegalArgumentException("El precio es obligatorio");
        if (req.getDurationMonths() == null || req.getDurationMonths() < 1)
            throw new IllegalArgumentException("La duración debe ser al menos 1 mes");

        GymPlan plan = GymPlan.builder()
                .shopId(shopId)
                .name(req.getName().trim())
                .description(req.getDescription())
                .price(req.getPrice())
                .durationMonths(req.getDurationMonths())
                .visitsAllowed(req.getVisitsAllowed())
                .active(req.getActive() != null ? req.getActive() : true)
                .build();
        return GymPlanResponse.from(planRepo.save(plan));
    }

    @Transactional
    public GymPlanResponse updatePlan(Long planId, String shopId, GymPlanRequest req) {
        GymPlan plan = planRepo.findByIdAndShopId(planId, shopId)
                .orElseThrow(() -> new IllegalArgumentException("Plan no encontrado"));
        if (req.getName()          != null) plan.setName(req.getName().trim());
        if (req.getDescription()   != null) plan.setDescription(req.getDescription());
        if (req.getPrice()         != null) plan.setPrice(req.getPrice());
        if (req.getDurationMonths() != null) plan.setDurationMonths(req.getDurationMonths());
        if (req.getVisitsAllowed() != null) plan.setVisitsAllowed(req.getVisitsAllowed());
        if (req.getActive()        != null) plan.setActive(req.getActive());
        return GymPlanResponse.from(planRepo.save(plan));
    }

    @Transactional
    public void deletePlan(Long planId, String shopId) {
        GymPlan plan = planRepo.findByIdAndShopId(planId, shopId)
                .orElseThrow(() -> new IllegalArgumentException("Plan no encontrado"));
        planRepo.delete(plan);
    }

    // ─── MIS MEMBRESÍAS (cliente) ─────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<MyGymMembershipResponse> getMyGymMemberships(String email) {
        List<GymMember> members = memberRepo.findByEmailIgnoreCase(email);
        return members.stream().map(m -> {
            BarberShop shop = shopRepo.findById(m.getShopId()).orElse(null);

            MyGymMembershipResponse r = new MyGymMembershipResponse();
            r.setMemberId(m.getId());
            r.setShopId(m.getShopId());
            r.setShopName(shop != null ? shop.getName() : "—");
            r.setShopSlug(shop != null ? shop.getSlug() : null);
            r.setMemberName(m.getName());
            r.setJoinDate(m.getJoinDate());
            r.setMemberStatus(m.getStatus());

            membershipRepo.findFirstByMemberIdAndStatusOrderByEndDateDesc(m.getId(), "active")
                    .ifPresent(mem -> {
                        r.setMembershipId(mem.getId());
                        r.setPlanName(mem.getPlanName());
                        r.setMonthlyPrice(mem.getMonthlyPrice());
                        r.setStartDate(mem.getStartDate());
                        r.setEndDate(mem.getEndDate());
                        r.setMembershipStatus(mem.getStatus());
                        r.setPaymentStatus(mem.getPaymentStatus());
                        r.setVisitsAllowed(mem.getVisitsAllowed());
                        r.setVisitsUsed(mem.getVisitsUsed());
                    });

            return r;
        }).collect(Collectors.toList());
    }

    // ─── STATS ────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Map<String, Object> getStats(String shopId) {
        long totalMembers   = memberRepo.countByShopId(shopId);
        long activeMembers  = memberRepo.countByShopIdAndStatus(shopId, "active");
        long activeMemships = membershipRepo.countByShopIdAndStatus(shopId, "active");
        long expiredMemships = membershipRepo.countByShopIdAndStatus(shopId, "expired");
        List<GymAttendanceResponse> todayAtt = getTodayAttendance(shopId);

        return Map.of(
            "totalMembers", totalMembers,
            "activeMembers", activeMembers,
            "inactiveMembers", totalMembers - activeMembers,
            "activeMemberships", activeMemships,
            "expiredMemberships", expiredMemships,
            "todayAttendances", todayAtt.size(),
            "todayList", todayAtt
        );
    }
}
