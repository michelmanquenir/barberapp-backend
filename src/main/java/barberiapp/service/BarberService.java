package barberiapp.service;

import barberiapp.model.Barber;
import barberiapp.model.BarberShopMember;
import barberiapp.repository.BarberRepository;
import barberiapp.repository.BarberShopMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BarberService {

    private final BarberRepository barberRepository;
    private final BarberShopMemberRepository memberRepository;

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
}
