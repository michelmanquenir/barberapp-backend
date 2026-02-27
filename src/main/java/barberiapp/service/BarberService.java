package barberiapp.service;

import barberiapp.model.Barber;
import barberiapp.model.BarberShopMember;
import barberiapp.repository.BarberRepository;
import barberiapp.repository.BarberShopMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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
