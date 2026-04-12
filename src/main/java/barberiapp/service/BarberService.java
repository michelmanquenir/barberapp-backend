package barberiapp.service;

import barberiapp.dto.BarberShopResponse;
import barberiapp.model.*;
import barberiapp.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BarberService {

    private final BarberRepository barberRepository;
    private final BarberShopMemberRepository memberRepository;
    private final BarberShopRepository shopRepository;
    private final AppUserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private static final String TEMP_PASSWORD_CHARS =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789@#$!";
    private static final SecureRandom RANDOM = new SecureRandom();

    private String generateTempPassword() {
        StringBuilder sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            sb.append(TEMP_PASSWORD_CHARS.charAt(RANDOM.nextInt(TEMP_PASSWORD_CHARS.length())));
        }
        return sb.toString();
    }

    // ─── Consultas básicas ────────────────────────────────────────────────────

    public List<Barber> getActiveBarbers() {
        return barberRepository.findByActiveTrue();
    }

    public List<Barber> getBarbersByShop(String shopId) {
        return memberRepository.findByShopIdAndActiveTrue(shopId).stream()
                .map(BarberShopMember::getBarber)
                .toList();
    }

    /** Obtiene el perfil de barbero vinculado al userId del login */
    public Optional<Barber> getMyProfile(String userId) {
        return barberRepository.findByUserId(userId);
    }

    /** Busca barberos registrados por nombre */
    public List<Barber> searchBarbers(String query) {
        if (query == null || query.isBlank()) {
            return barberRepository.findByActiveTrue();
        }
        return barberRepository.findByNameContainingIgnoreCaseAndActiveTrue(query);
    }

    /** Actualiza la imagen del barbero autenticado */
    public Optional<Barber> updateBarberImage(String userId, String imageUrl) {
        return barberRepository.findByUserId(userId).map(barber -> {
            barber.setImageUrl(imageUrl);
            return barberRepository.save(barber);
        });
    }

    public Barber createBarberProfile(String userId, String name, String bio, String imageUrl) {
        Barber barber = new Barber();
        barber.setName(name);
        barber.setBio(bio);
        barber.setImageUrl(imageUrl);
        barber.setUserId(userId);
        barber.setActive(true);
        return barberRepository.save(barber);
    }

    // ─── Gestión de cuentas de empleados ─────────────────────────────────────

    /**
     * Crea una cuenta en la app para un profesional que aún no tiene.
     * Solo el dueño del negocio puede invocar esto.
     * Se genera una contraseña temporal y se envía por email al profesional.
     */
    @Transactional
    public Barber createAccountForBarber(String shopId, Long barberId,
                                         String requesterId, String email, String rut) {
        // 1. Verificar que el negocio existe y el requester es el dueño
        BarberShop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new IllegalArgumentException("Negocio no encontrado"));
        if (!shop.getOwner().getId().equals(requesterId)) {
            throw new IllegalArgumentException("No tienes permiso para gestionar este negocio");
        }

        // 2. Verificar que el barbero existe y pertenece al negocio
        Barber barber = barberRepository.findById(barberId)
                .orElseThrow(() -> new IllegalArgumentException("Profesional no encontrado"));
        boolean inShop = memberRepository.existsByShopIdAndBarberIdAndActiveTrue(shopId, barberId);
        if (!inShop) {
            throw new IllegalArgumentException("El profesional no pertenece a este negocio");
        }

        // 3. Verificar que no tiene cuenta ya vinculada
        if (barber.getUserId() != null) {
            throw new IllegalArgumentException("Este profesional ya tiene una cuenta de app vinculada");
        }

        // 4. Verificar que el email no está en uso
        String normalizedEmail = email.trim().toLowerCase();
        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new IllegalArgumentException("Ya existe una cuenta registrada con el email " + normalizedEmail);
        }

        // Normalizar y validar RUT si se proporcionó
        String normalizedRut = null;
        if (rut != null && !rut.isBlank()) {
            normalizedRut = rut.trim().toUpperCase();
            if (userRepository.existsByRut(normalizedRut)) {
                throw new IllegalArgumentException("Ya existe una cuenta registrada con el RUT " + normalizedRut);
            }
        }

        // 5. Crear AppUser con contraseña temporal
        String tempPassword = generateTempPassword();
        String userId = UUID.randomUUID().toString();

        AppUser newUser = AppUser.builder()
                .id(userId)
                .email(normalizedEmail)
                .rut(normalizedRut)
                .passwordHash(passwordEncoder.encode(tempPassword))
                .role(UserRole.CLIENT)          // Rol CLIENT — el sistema detecta que es empleado via barber.userId
                .status(UserStatus.ACTIVE)      // Activo de inmediato — el dueño lo está registrando
                .mustChangePassword(true)
                .build();
        userRepository.save(newUser);

        // 6. Crear perfil con nombre del barbero
        Profile profile = new Profile();
        profile.setId(userId);
        profile.setAppUser(newUser);
        profile.setFullName(barber.getName());
        profileRepository.save(profile);

        // 7. Vincular userId al barbero
        barber.setUserId(userId);
        Barber saved = barberRepository.save(barber);

        // 8. Enviar email de bienvenida con credenciales provisionales
        emailService.sendBarberWelcome(normalizedEmail, barber.getName(), shop.getName(), tempPassword);

        log.info("Cuenta creada para profesional barberId={} email={} en shopId={}",
                barberId, normalizedEmail, shopId);
        return saved;
    }

    /**
     * Desvincula la cuenta de app de un profesional.
     * La cuenta de app NO se elimina — solo se rompe el vínculo con el perfil de barbero.
     * Solo el dueño del negocio puede invocar esto.
     */
    @Transactional
    public Barber unlinkAccountFromBarber(String shopId, Long barberId, String requesterId) {
        // 1. Verificar ownership
        BarberShop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new IllegalArgumentException("Negocio no encontrado"));
        if (!shop.getOwner().getId().equals(requesterId)) {
            throw new IllegalArgumentException("No tienes permiso para gestionar este negocio");
        }

        // 2. Verificar barbero
        Barber barber = barberRepository.findById(barberId)
                .orElseThrow(() -> new IllegalArgumentException("Profesional no encontrado"));
        boolean inShop = memberRepository.existsByShopIdAndBarberIdAndActiveTrue(shopId, barberId);
        if (!inShop) {
            throw new IllegalArgumentException("El profesional no pertenece a este negocio");
        }

        if (barber.getUserId() == null) {
            throw new IllegalArgumentException("Este profesional no tiene cuenta vinculada");
        }

        // 3. Desvincular (no eliminar la cuenta — el usuario podría tener historial)
        barber.setUserId(null);
        Barber saved = barberRepository.save(barber);

        log.info("Cuenta desvinculada de profesional barberId={} en shopId={}", barberId, shopId);
        return saved;
    }

    /**
     * Devuelve los negocios donde este usuario tiene un perfil de barbero activo.
     * Usado por el dashboard de empleado.
     */
    @Transactional(readOnly = true)
    public List<BarberShopResponse> getMyShops(String userId) {
        Barber barber = barberRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("No tienes perfil de profesional vinculado"));

        return memberRepository.findByBarberId(barber.getId()).stream()
                .filter(m -> Boolean.TRUE.equals(m.getActive()))
                .map(m -> {
                    BarberShop s = m.getShop();
                    BarberShopResponse r = new BarberShopResponse();
                    r.setId(s.getId());
                    r.setName(s.getName());
                    r.setDescription(s.getDescription());
                    r.setSlug(s.getSlug());
                    r.setActive(s.getActive());
                    r.setAddress(s.getAddress());
                    r.setLatitude(s.getLatitude());
                    r.setLongitude(s.getLongitude());
                    r.setCategoryId(s.getCategoryId());
                    r.setApprovalStatus(s.getApprovalStatus() != null
                            ? s.getApprovalStatus().name() : null);
                    r.setHomeServiceEnabled(s.getHomeServiceEnabled());
                    r.setPricePerKm(s.getPricePerKm());
                    return r;
                })
                .toList();
    }
}
