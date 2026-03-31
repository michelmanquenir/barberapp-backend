package barberiapp.service;

import barberiapp.dto.FavoriteShopDto;
import barberiapp.model.BarberShop;
import barberiapp.model.FavoriteShop;
import barberiapp.model.Profile;
import barberiapp.repository.BarberShopRepository;
import barberiapp.repository.FavoriteShopRepository;
import barberiapp.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FavoriteShopService {

    private final FavoriteShopRepository favoriteShopRepository;
    private final ProfileRepository profileRepository;
    private final BarberShopRepository shopRepository;

    @Transactional(readOnly = true)
    public List<FavoriteShopDto> getUserFavoriteShops(String userId) {
        return favoriteShopRepository.findByUserId(userId)
                .stream()
                .map(FavoriteShopDto::from)
                .toList();
    }

    @Transactional
    public FavoriteShopDto addFavoriteShop(String userId, String shopId) {
        Optional<FavoriteShop> existing = favoriteShopRepository
                .findByUserIdAndShopId(userId, shopId);
        if (existing.isPresent()) return FavoriteShopDto.from(existing.get());

        Profile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        BarberShop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Negocio no encontrado"));

        FavoriteShop fav = new FavoriteShop();
        fav.setUser(profile);
        fav.setShop(shop);
        return FavoriteShopDto.from(favoriteShopRepository.save(fav));
    }

    @Transactional
    public void removeFavoriteShop(Long favoriteId, String userId) {
        FavoriteShop fav = favoriteShopRepository.findById(favoriteId)
                .orElseThrow(() -> new RuntimeException("Favorito no encontrado"));
        if (!fav.getUser().getId().equals(userId)) {
            throw new RuntimeException("No autorizado");
        }
        favoriteShopRepository.delete(fav);
    }
}
