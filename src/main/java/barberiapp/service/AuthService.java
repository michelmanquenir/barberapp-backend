package barberiapp.service;

import barberiapp.dto.AuthResponse;
import barberiapp.dto.LoginRequest;
import barberiapp.dto.RegisterRequest;
import barberiapp.model.AppUser;
import barberiapp.model.Barber;
import barberiapp.model.Profile;
import barberiapp.model.UserRole;
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

    @PersistenceContext
    private EntityManager entityManager;

    public AuthService(AppUserRepository appUserRepository,
                       ProfileRepository profileRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.appUserRepository = appUserRepository;
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (appUserRepository.findByEmail(req.getEmail()).isPresent()) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        UserRole role = UserRole.valueOf(req.getRole().toUpperCase());
        String id = UUID.randomUUID().toString();

        AppUser user = AppUser.builder()
                .id(id)
                .email(req.getEmail())
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

        // Si se registra como BARBER, crear automáticamente su perfil de barbero
        if (role == UserRole.BARBER) {
            Barber barber = new Barber();
            barber.setName(req.getFullName());
            barber.setUserId(id);
            barber.setActive(true);
            entityManager.persist(barber);
        }

        String token = jwtUtil.generateToken(user);
        return new AuthResponse(token, id, user.getEmail(), role.name(), req.getFullName());
    }

    public AuthResponse login(LoginRequest req) {
        AppUser user = appUserRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Credenciales incorrectas"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Credenciales incorrectas");
        }

        String fullName = profileRepository.findById(user.getId())
                .map(Profile::getFullName)
                .orElse("");

        String token = jwtUtil.generateToken(user);
        return new AuthResponse(token, user.getId(), user.getEmail(), user.getRole().name(), fullName);
    }
}
