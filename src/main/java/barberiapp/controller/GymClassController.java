package barberiapp.controller;

import barberiapp.model.GymClass;
import barberiapp.model.GymClassEnrollment;
import barberiapp.model.GymMember;
import barberiapp.repository.GymClassEnrollmentRepository;
import barberiapp.repository.GymClassRepository;
import barberiapp.repository.GymMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/gym/shops/{shopId}/classes")
@RequiredArgsConstructor
public class GymClassController {

    private final GymClassRepository classRepo;
    private final GymClassEnrollmentRepository enrollmentRepo;
    private final GymMemberRepository memberRepo;

    // ── List all classes ──────────────────────────────────────────────────────
    @GetMapping
    public List<Map<String, Object>> listClasses(@PathVariable String shopId) {
        List<GymClass> classes = classRepo.findByShopIdOrderByDayOfWeekAscStartTimeAsc(shopId);
        return classes.stream().map(c -> toDto(c, true)).collect(Collectors.toList());
    }

    // ── Create class ──────────────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<Map<String, Object>> createClass(
            @PathVariable String shopId,
            @RequestBody Map<String, Object> body) {
        GymClass gc = new GymClass();
        gc.setShopId(shopId);
        applyBody(gc, body);
        classRepo.save(gc);
        return ResponseEntity.ok(toDto(gc, false));
    }

    // ── Update class ──────────────────────────────────────────────────────────
    @PutMapping("/{classId}")
    public ResponseEntity<Map<String, Object>> updateClass(
            @PathVariable String shopId,
            @PathVariable Long classId,
            @RequestBody Map<String, Object> body) {
        GymClass gc = classRepo.findByIdAndShopId(classId, shopId)
                .orElseThrow(() -> new IllegalArgumentException("Clase no encontrada"));
        applyBody(gc, body);
        classRepo.save(gc);
        return ResponseEntity.ok(toDto(gc, false));
    }

    // ── Delete class ──────────────────────────────────────────────────────────
    @DeleteMapping("/{classId}")
    public ResponseEntity<Void> deleteClass(
            @PathVariable String shopId,
            @PathVariable Long classId) {
        GymClass gc = classRepo.findByIdAndShopId(classId, shopId)
                .orElseThrow(() -> new IllegalArgumentException("Clase no encontrada"));
        enrollmentRepo.deleteByClassId(classId);
        classRepo.delete(gc);
        return ResponseEntity.noContent().build();
    }

    // ── Get enrollments for a class ───────────────────────────────────────────
    @GetMapping("/{classId}/enrollments")
    public List<Map<String, Object>> getEnrollments(
            @PathVariable String shopId,
            @PathVariable Long classId) {
        List<GymClassEnrollment> enrollments = enrollmentRepo.findByClassIdOrderByEnrolledAtAsc(classId);
        List<Long> memberIds = enrollments.stream().map(GymClassEnrollment::getMemberId).collect(Collectors.toList());
        Map<Long, GymMember> memberMap = memberRepo.findAllById(memberIds)
                .stream().collect(Collectors.toMap(GymMember::getId, m -> m));

        return enrollments.stream().map(e -> {
            GymMember m = memberMap.get(e.getMemberId());
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("enrollmentId", e.getId());
            map.put("memberId", e.getMemberId());
            map.put("memberName", m != null ? m.getName() : "—");
            map.put("memberEmail", m != null && m.getEmail() != null ? m.getEmail() : "");
            map.put("memberPhone", m != null && m.getPhone() != null ? m.getPhone() : "");
            map.put("enrolledAt", e.getEnrolledAt() != null ? e.getEnrolledAt().toString() : "");
            return map;
        }).collect(Collectors.toList());
    }

    // ── Enroll member in class ────────────────────────────────────────────────
    @PostMapping("/{classId}/enrollments")
    public ResponseEntity<Map<String, Object>> enroll(
            @PathVariable String shopId,
            @PathVariable Long classId,
            @RequestBody Map<String, Object> body) {
        Long memberId = ((Number) body.get("memberId")).longValue();

        if (enrollmentRepo.existsByClassIdAndMemberId(classId, memberId)) {
            throw new IllegalArgumentException("El alumno ya está inscrito en esta clase");
        }

        GymClass gc = classRepo.findByIdAndShopId(classId, shopId)
                .orElseThrow(() -> new IllegalArgumentException("Clase no encontrada"));

        if (gc.getMaxCapacity() != null) {
            long count = enrollmentRepo.countByClassId(classId);
            if (count >= gc.getMaxCapacity()) {
                throw new IllegalArgumentException("La clase está llena (capacidad: " + gc.getMaxCapacity() + ")");
            }
        }

        GymMember member = memberRepo.findByIdAndShopId(memberId, shopId)
                .orElseThrow(() -> new IllegalArgumentException("Alumno no encontrado"));

        GymClassEnrollment enrollment = new GymClassEnrollment();
        enrollment.setClassId(classId);
        enrollment.setMemberId(memberId);
        enrollment.setShopId(shopId);
        enrollmentRepo.save(enrollment);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("enrollmentId", enrollment.getId());
        result.put("memberId", memberId);
        result.put("memberName", member.getName());
        result.put("enrolledAt", enrollment.getEnrolledAt() != null ? enrollment.getEnrolledAt().toString() : "");
        return ResponseEntity.ok(result);
    }

    // ── Remove enrollment ─────────────────────────────────────────────────────
    @DeleteMapping("/{classId}/enrollments/{enrollmentId}")
    public ResponseEntity<Void> unenroll(
            @PathVariable String shopId,
            @PathVariable Long classId,
            @PathVariable Long enrollmentId) {
        enrollmentRepo.deleteById(enrollmentId);
        return ResponseEntity.noContent().build();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private void applyBody(GymClass gc, Map<String, Object> body) {
        if (body.containsKey("name"))           gc.setName((String) body.get("name"));
        if (body.containsKey("classType"))      gc.setClassType((String) body.get("classType"));
        if (body.containsKey("instructorName")) gc.setInstructorName((String) body.get("instructorName"));
        if (body.containsKey("dayOfWeek"))      gc.setDayOfWeek((String) body.get("dayOfWeek"));
        if (body.containsKey("startTime"))      gc.setStartTime((String) body.get("startTime"));
        if (body.containsKey("endTime"))        gc.setEndTime((String) body.get("endTime"));
        if (body.containsKey("maxCapacity"))    gc.setMaxCapacity(body.get("maxCapacity") != null ? ((Number) body.get("maxCapacity")).intValue() : null);
        if (body.containsKey("description"))    gc.setDescription((String) body.get("description"));
        if (body.containsKey("color"))          gc.setColor((String) body.get("color"));
        if (body.containsKey("active"))         gc.setActive((Boolean) body.get("active"));
    }

    private Map<String, Object> toDto(GymClass gc, boolean includeEnrollmentCount) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id",             gc.getId());
        map.put("name",           gc.getName() != null ? gc.getName() : "");
        map.put("classType",      gc.getClassType() != null ? gc.getClassType() : "");
        map.put("instructorName", gc.getInstructorName() != null ? gc.getInstructorName() : "");
        map.put("dayOfWeek",      gc.getDayOfWeek() != null ? gc.getDayOfWeek() : "");
        map.put("startTime",      gc.getStartTime() != null ? gc.getStartTime() : "");
        map.put("endTime",        gc.getEndTime() != null ? gc.getEndTime() : "");
        map.put("maxCapacity",    gc.getMaxCapacity());
        map.put("description",    gc.getDescription() != null ? gc.getDescription() : "");
        map.put("color",          gc.getColor() != null ? gc.getColor() : "#6366f1");
        map.put("active",         gc.getActive() != null ? gc.getActive() : true);
        map.put("createdAt",      gc.getCreatedAt() != null ? gc.getCreatedAt().toString() : "");
        if (includeEnrollmentCount) {
            map.put("enrollmentCount", enrollmentRepo.countByClassId(gc.getId()));
        }
        return map;
    }
}
