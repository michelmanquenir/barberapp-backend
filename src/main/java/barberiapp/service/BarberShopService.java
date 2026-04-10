package barberiapp.service;

import barberiapp.dto.BarberShopResponse;
import barberiapp.dto.CreateShopRequest;
import barberiapp.model.*;
import barberiapp.model.ApprovalStatus;
import barberiapp.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class BarberShopService {

    private final BarberShopRepository shopRepository;
    private final BarberShopMemberRepository memberRepository;
    private final AppUserRepository userRepository;
    private final BarberRepository barberRepository;
    private final ProfileRepository profileRepository;
    private final EmailService emailService;

    public BarberShopService(BarberShopRepository shopRepository,
                             BarberShopMemberRepository memberRepository,
                             AppUserRepository userRepository,
                             BarberRepository barberRepository,
                             ProfileRepository profileRepository,
                             EmailService emailService) {
        this.shopRepository = shopRepository;
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
        this.barberRepository = barberRepository;
        this.profileRepository = profileRepository;
        this.emailService = emailService;
    }

    @Transactional
    public BarberShopResponse createShop(String ownerId, CreateShopRequest req) {
        AppUser owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // El usuario debe estar aprobado antes de poder crear un negocio
        if (owner.getStatus() == UserStatus.PENDING) {
            throw new IllegalArgumentException("Tu cuenta está pendiente de aprobación. El administrador debe aprobarla antes de que puedas crear un negocio.");
        }
        if (owner.getStatus() == UserStatus.REJECTED) {
            throw new IllegalArgumentException("Tu cuenta ha sido rechazada. No puedes crear negocios.");
        }

        if (shopRepository.findBySlug(req.getSlug()).isPresent()) {
            throw new IllegalArgumentException("El slug '" + req.getSlug() + "' ya está en uso");
        }

        BarberShop shop = BarberShop.builder()
                .id(UUID.randomUUID().toString())
                .name(req.getName())
                .description(req.getDescription())
                .owner(owner)
                .slug(req.getSlug())
                .address(req.getAddress())
                .latitude(req.getLatitude())
                .longitude(req.getLongitude())
                .active(true)
                .homeServiceEnabled(Boolean.TRUE.equals(req.getHomeServiceEnabled()))
                .pricePerKm(req.getPricePerKm() != null ? req.getPricePerKm() : 0)
                .categoryId(req.getCategoryId())
                .build();

        shopRepository.save(shop);

        // Notificar al propietario que su negocio está en revisión (async)
        String ownerName = profileRepository.findById(ownerId)
                .map(p -> p.getFullName())
                .orElse(owner.getEmail());
        emailService.sendShopPending(owner.getEmail(), ownerName, shop.getName());

        return toResponse(shop, List.of());
    }

    public List<BarberShopResponse> getShopsByOwner(String ownerId) {
        return shopRepository.findByOwnerId(ownerId).stream()
                .map(shop -> {
                    List<Barber> barbers = getBarbersByShop(shop.getId());
                    return toResponse(shop, barbers);
                })
                .toList();
    }

    public BarberShopResponse getShopBySlug(String slug) {
        BarberShop shop = shopRepository.findBySlug(slug)
                .orElseThrow(() -> new IllegalArgumentException("Negocio no encontrado"));
        List<Barber> barbers = getBarbersByShop(shop.getId());
        return toResponse(shop, barbers);
    }

    public List<Barber> getBarbersByShop(String shopId) {
        return memberRepository.findByShopIdAndActiveTrue(shopId).stream()
                .map(BarberShopMember::getBarber)
                .toList();
    }

    @Transactional
    public void addBarberToShop(String shopId, Long barberId, String requesterId) {
        BarberShop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new IllegalArgumentException("Negocio no encontrado"));

        if (!shop.getOwner().getId().equals(requesterId)) {
            throw new IllegalArgumentException("No tienes permiso para modificar este negocio");
        }

        // Buscar si ya existe un registro activo o inactivo
        java.util.Optional<BarberShopMember> existing =
                memberRepository.findByShopIdAndBarberId(shopId, barberId);

        if (existing.isPresent()) {
            if (existing.get().getActive()) {
                throw new IllegalArgumentException("El barbero ya pertenece a este negocio");
            }
            // Registro inactivo (fue quitado antes): reactivar en vez de crear uno nuevo
            existing.get().setActive(true);
            memberRepository.save(existing.get());
            return;
        }

        Barber barber = barberRepository.findById(barberId)
                .orElseThrow(() -> new IllegalArgumentException("Barbero no encontrado"));

        BarberShopMember member = BarberShopMember.builder()
                .shop(shop)
                .barber(barber)
                .active(true)
                .build();
        memberRepository.save(member);
    }

    @Transactional
    public void removeBarberFromShop(String shopId, Long barberId, String requesterId) {
        BarberShop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new IllegalArgumentException("Negocio no encontrado"));

        if (!shop.getOwner().getId().equals(requesterId)) {
            throw new IllegalArgumentException("No tienes permiso para modificar este negocio");
        }

        memberRepository.findByShopIdAndActiveTrue(shopId).stream()
                .filter(m -> m.getBarber().getId().equals(barberId))
                .findFirst()
                .ifPresent(m -> {
                    m.setActive(false);
                    memberRepository.save(m);
                });
    }

    public BarberShopResponse getShopById(String shopId) {
        BarberShop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new IllegalArgumentException("Negocio no encontrado"));
        List<Barber> barbers = getBarbersByShop(shop.getId());
        return toResponse(shop, barbers);
    }

    @Transactional
    public BarberShopResponse updateShop(String shopId, String ownerId, CreateShopRequest req) {
        BarberShop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new IllegalArgumentException("Negocio no encontrado"));

        if (!shop.getOwner().getId().equals(ownerId)) {
            throw new IllegalArgumentException("No tienes permiso para modificar este negocio");
        }

        // Validar slug único (excluyendo el shop actual)
        shopRepository.findBySlug(req.getSlug()).ifPresent(existing -> {
            if (!existing.getId().equals(shopId)) {
                throw new IllegalArgumentException("El slug '" + req.getSlug() + "' ya está en uso");
            }
        });

        shop.setName(req.getName());
        shop.setDescription(req.getDescription());
        shop.setSlug(req.getSlug());
        shop.setAddress(req.getAddress());
        shop.setLatitude(req.getLatitude());
        shop.setLongitude(req.getLongitude());
        shop.setHomeServiceEnabled(Boolean.TRUE.equals(req.getHomeServiceEnabled()));
        shop.setPricePerKm(req.getPricePerKm() != null ? req.getPricePerKm() : 0);
        shop.setCategoryId(req.getCategoryId());

        shopRepository.save(shop);
        List<Barber> barbers = getBarbersByShop(shop.getId());
        return toResponse(shop, barbers);
    }

    /**
     * Lista pública de negocios — batch-load de barbers en 2 queries totales
     * (1 para shops + 1 para todos los members con barbers) en vez de 1+N.
     */
    @Transactional(readOnly = true)
    public List<BarberShopResponse> getAllActiveShops() {
        List<BarberShop> shops = shopRepository.findPublicApproved(ApprovalStatus.ACTIVE);
        if (shops.isEmpty()) return List.of();

        // Batch load: 1 sola query para TODOS los miembros activos con barber
        List<String> shopIds = shops.stream().map(BarberShop::getId).toList();
        Map<String, List<Barber>> barbersByShop = new HashMap<>();
        for (BarberShopMember m : memberRepository.findByShopIdInAndActiveTrueWithBarber(shopIds)) {
            barbersByShop.computeIfAbsent(m.getShop().getId(), k -> new ArrayList<>())
                         .add(m.getBarber());
        }

        return shops.stream()
                .map(shop -> toResponse(shop, barbersByShop.getOrDefault(shop.getId(), List.of())))
                .toList();
    }

    private BarberShopResponse toResponse(BarberShop shop, List<Barber> barbers) {
        return new BarberShopResponse(
                shop.getId(),
                shop.getName(),
                shop.getDescription(),
                shop.getSlug(),
                shop.getActive(),
                shop.getAddress(),
                shop.getLatitude(),
                shop.getLongitude(),
                barbers,
                shop.getHomeServiceEnabled(),
                shop.getPricePerKm(),
                shop.getApprovalStatus() != null ? shop.getApprovalStatus().name() : "ACTIVE",
                shop.getCategoryId()
        );
    }
}
