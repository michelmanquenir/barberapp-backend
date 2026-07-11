package barberiapp.controller;

import barberiapp.dto.UserLookupResponse;
import barberiapp.model.AppUser;
import barberiapp.model.Profile;
import barberiapp.repository.AppUserRepository;
import barberiapp.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final AppUserRepository userRepo;
    private final ProfileRepository profileRepo;

    /**
     * Busca un usuario por email y retorna sus datos básicos de perfil.
     * Usado por el formulario de gym para detectar si el alumno ya existe en WeServ.
     */
    @GetMapping("/by-email")
    public ResponseEntity<UserLookupResponse> findByEmail(@RequestParam String email) {
        Optional<AppUser> userOpt = userRepo.findByEmailIgnoreCase(email.trim());
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        AppUser user = userOpt.get();
        Profile profile = profileRepo.findById(user.getId()).orElse(null);

        UserLookupResponse response = new UserLookupResponse(
                user.getId(),
                user.getEmail(),
                profile != null ? profile.getFullName() : null,
                profile != null ? profile.getPhone() : null,
                user.getRut(),
                profile != null ? profile.getBirthdate() : null
        );

        return ResponseEntity.ok(response);
    }
}
