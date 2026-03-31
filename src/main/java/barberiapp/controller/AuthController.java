package barberiapp.controller;

import barberiapp.dto.AuthResponse;
import barberiapp.dto.LoginRequest;
import barberiapp.dto.RegisterRequest;
import barberiapp.model.AppUser;
import barberiapp.model.PasswordResetToken;
import barberiapp.model.Profile;
import barberiapp.repository.AppUserRepository;
import barberiapp.repository.PasswordResetTokenRepository;
import barberiapp.repository.ProfileRepository;
import barberiapp.service.AuthService;
import barberiapp.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AppUserRepository userRepo;
    private final PasswordResetTokenRepository tokenRepo;
    private final ProfileRepository profileRepo;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        try {
            AuthResponse response = authService.register(req);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        try {
            AuthResponse response = authService.login(req);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Paso 1 — Solicitar código de recuperación.
     * Siempre responde OK (no revela si el email existe o no).
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank())
            return ResponseEntity.badRequest().body(Map.of("error", "El correo es requerido"));

        Optional<AppUser> userOpt = userRepo.findByEmail(email.trim().toLowerCase());
        if (userOpt.isPresent()) {
            AppUser user = userOpt.get();

            // Eliminar tokens previos del mismo email
            tokenRepo.deleteAllByEmail(email.trim().toLowerCase());

            // Generar código de 6 dígitos
            String code = String.format("%06d", new Random().nextInt(1_000_000));

            PasswordResetToken token = PasswordResetToken.builder()
                    .email(email.trim().toLowerCase())
                    .code(code)
                    .expiresAt(LocalDateTime.now().plusMinutes(15))
                    .used(false)
                    .build();
            tokenRepo.save(token);

            // Obtener nombre del perfil
            String name = profileRepo.findById(user.getId())
                    .map(Profile::getFullName)
                    .orElse("Usuario");

            emailService.sendPasswordResetCode(email.trim().toLowerCase(), name, code);
        }

        // Respuesta genérica — no revelar si el email existe
        return ResponseEntity.ok(Map.of("message", "Si el correo está registrado, recibirás un código en tu bandeja de entrada."));
    }

    /**
     * Paso 2 — Validar código y cambiar contraseña.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        String email       = body.get("email");
        String code        = body.get("code");
        String newPassword = body.get("newPassword");

        if (email == null || code == null || newPassword == null ||
            email.isBlank() || code.isBlank() || newPassword.isBlank())
            return ResponseEntity.badRequest().body(Map.of("error", "Todos los campos son requeridos"));

        if (newPassword.length() < 6)
            return ResponseEntity.badRequest().body(Map.of("error", "La contraseña debe tener al menos 6 caracteres"));

        String normalizedEmail = email.trim().toLowerCase();

        PasswordResetToken token = tokenRepo
                .findTopByEmailAndUsedFalseOrderByCreatedAtDesc(normalizedEmail)
                .orElse(null);

        if (token == null || !token.getCode().equals(code.trim()))
            return ResponseEntity.badRequest().body(Map.of("error", "Código incorrecto o no encontrado"));

        if (token.isExpired())
            return ResponseEntity.badRequest().body(Map.of("error", "El código ha expirado. Solicita uno nuevo."));

        // Actualizar contraseña
        AppUser user = userRepo.findByEmail(normalizedEmail).orElse(null);
        if (user == null)
            return ResponseEntity.badRequest().body(Map.of("error", "Usuario no encontrado"));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepo.save(user);

        // Marcar token como usado
        token.setUsed(true);
        tokenRepo.save(token);

        return ResponseEntity.ok(Map.of("message", "Contraseña actualizada correctamente"));
    }
}
