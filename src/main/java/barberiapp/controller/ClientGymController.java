package barberiapp.controller;

import barberiapp.service.GymService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/gym")
@RequiredArgsConstructor
public class ClientGymController {

    private final GymService gymService;

    @GetMapping("/my-memberships")
    public ResponseEntity<?> getMyMemberships() {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            return ResponseEntity.ok(gymService.getMyGymMemberships(email));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
