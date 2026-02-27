package barberiapp.service;

import barberiapp.dto.BarberShopResponse;
import barberiapp.dto.CreateShopRequest;
import barberiapp.model.*;
import barberiapp.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class BarberShopService {

    private final BarberShopRepository shopRepository;
    private final BarberShopMemberRepository memberRepository;
    private final AppUserRepository userRepository;
    private final BarberRepository barberRepository;

    public BarberShopService(BarberShopRepository shopRepository,
                             BarberShopMemberRepository memberRepository,
                             AppUserRepository userRepository,
                             BarberRepository barberRepository) {
        this.shopRepository = shopRepository;
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
        this.barberRepository = barberRepository;
    }

    @Transactional
    public BarberShopResponse createShop(String ownerId, CreateShopRequest req) {
        AppUser owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (shopRepository.findBySlug(req.getSlug()).isPresent()) {
            throw new IllegalArgumentException("El slug '" + req.getSlug() + "' ya está en uso");
        }

        BarberShop shop = BarberShop.builder()
                .id(UUID.randomUUID().toString())
                .name(req.getName())
                .description(req.getDescription())
                .owner(owner)
                .slug(req.getSlug())
                .active(true)
                .build();

        shopRepository.save(shop);
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
        if (memberRepository.existsByShopIdAndBarberId(shopId, barberId)) {
            throw new IllegalArgumentException("El barbero ya pertenece a este negocio");
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

    private BarberShopResponse toResponse(BarberShop shop, List<Barber> barbers) {
        return new BarberShopResponse(
                shop.getId(),
                shop.getName(),
                shop.getDescription(),
                shop.getSlug(),
                shop.getActive(),
                barbers
        );
    }
}
