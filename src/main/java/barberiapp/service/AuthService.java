package barberiapp.service;

import barberiapp.dto.AuthResponse;
import barberiapp.dto.LoginRequest;
import barberiapp.dto.RegisterRequest;
import barberiapp.model.AppUser;
import barberiapp.model.Barber;
import barberiapp.model.Profile;
import barberiapp.model.UserRole;
import barberiapp.model.UserStatus;
import barberiapp.repository.AppUserRepository;
import barberiapp.repository.ProfileRepository;
import barberiapp.security.JwtUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    @PersistenceContext
    private EntityManager entityManager;

    public AuthService(AppUserRepository appUserRepository,
                       ProfileRepository profileRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       EmailService emailService) {
        this.appUserRepository = appUserRepository;
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        // ── Validaciones de unicidad ────────────────────────────────────────────
        if (appUserRepository.findByEmail(req.getEmail()).isPresent()) {
            throw new IllegalArgumentException("El correo electrónico ya está registrado");
        }

        // RUT: requerido y único
        if (req.getRut() == null || req.getRut().isBlank()) {
            throw new IllegalArgumentException("El RUT es requerido");
        }
        String normalizedRut = req.getRut().trim().toUpperCase();
        if (appUserRepository.existsByRut(normalizedRut)) {
            throw new IllegalArgumentException("El RUT ya está registrado en otra cuenta");
        }

        UserRole role = UserRole.valueOf(req.getRole().toUpperCase());
        String id = UUID.randomUUID().toString();

        AppUser user = AppUser.builder()
                .id(id)
                .email(req.getEmail())
                .rut(normalizedRut)
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .role(role)
                .build();

        // persist() en vez de save() para garantizar INSERT directo (no merge)
        entityManager.persist(user);
        entityManager.flush();

        Profile profile = new Profile();
        profile.setId(id);
        profile.setAppUser(user);
        profile.setFullName(req.getFullName());
        // persist() evita que Spring Data JPA llame a merge(), que rompe @MapsId en Hibernate 7
        entityManager.persist(profile);

        // Si se registra como BUSINESS_OWNER, crear automáticamente su perfil de barbero
        if (role == UserRole.BUSINESS_OWNER) {
            Barber barber = new Barber();
            barber.setName(req.getFullName());
            barber.setUserId(id);
            barber.setActive(true);
            entityManager.persist(barber);
        }

        String token = jwtUtil.generateToken(user);
        UserStatus status = user.getStatus() != null ? user.getStatus() : UserStatus.PENDING;

        // Enviar email de bienvenida al nuevo usuario (async, no bloquea)
        emailService.sendWelcomeEmail(user.getEmail(), req.getFullName(), role.name());

        // Notificar al super admin que hay un nuevo usuario pendiente de aprobación
        emailService.sendNewUserRegisteredToAdmin(req.getFullName(), user.getEmail(), role.name());

        return new AuthResponse(token, id, user.getEmail(), role.name(), req.getFullName(), null, status.name());
    }

    public AuthResponse login(LoginRequest req) {
        AppUser user = appUserRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Credenciales incorrectas"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Credenciales incorrectas");
        }

        Profile profile = profileRepository.findById(user.getId()).orElse(null);
        String fullName  = profile != null ? profile.getFullName()  : "";
        String avatarUrl = profile != null ? profile.getAvatarUrl() : null;
        UserStatus status = user.getStatus() != null ? user.getStatus() : UserStatus.PENDING;

        String token = jwtUtil.generateToken(user);
        return new AuthResponse(token, user.getId(), user.getEmail(), user.getRole().name(), fullName, avatarUrl, status.name());
    }
}
