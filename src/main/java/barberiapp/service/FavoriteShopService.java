package barberiapp.service;

import barberiapp.model.BarberShop;
import barberiapp.model.FavoriteShop;
import barberiapp.model.Profile;
import barberiapp.repository.BarberShopRepository;
import barberiapp.repository.FavoriteShopRepository;
import barberiapp.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FavoriteShopService {

    private final FavoriteShopRepository favoriteShopRepository;
    private final ProfileRepository profileRepository;
    private final BarberShopRepository shopRepository;

    public List<FavoriteShop> getUserFavoriteShops(String userId) {
        return favoriteShopRepository.findByUserId(userId);
    }

    public FavoriteShop addFavoriteShop(String userId, String shopId) {
        Optional<FavoriteShop> existing = favoriteShopRepository
                .findByUserIdAndShopId(userId, shopId);
        if (existing.isPresent()) return existing.get();

        Profile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        BarberShop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Negocio no encontrado"));

        FavoriteShop fav = new FavoriteShop();
        fav.setUser(profile);
        fav.setShop(shop);
        return favoriteShopRepository.save(fav);
    }

    public void removeFavoriteShop(Long favoriteId, String userId) {
        FavoriteShop fav = favoriteShopRepository.findById(favoriteId)
                .orElseThrow(() -> new RuntimeException("Favorito no encontrado"));
        if (!fav.getUser().getId().equals(userId)) {
            throw new RuntimeException("No autorizado");
        }
        favoriteShopRepository.delete(fav);
    }
}
