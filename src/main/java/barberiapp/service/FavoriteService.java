package barberiapp.service;

import barberiapp.model.Favorite;
import barberiapp.model.Barber;
import barberiapp.model.Profile;
import barberiapp.repository.FavoriteRepository;
import barberiapp.repository.BarberRepository;
import barberiapp.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final ProfileRepository profileRepository;
    private final BarberRepository barberRepository;

    public List<Favorite> getUserFavorites(String userId) {
        return favoriteRepository.findByUserId(userId);
    }

    public Favorite addFavorite(String userId, Long barberId) {
        // Chequear si ya existe
        Optional<Favorite> existing = favoriteRepository.findByUserIdAndBarberId(userId, barberId);
        if (existing.isPresent()) {
            return existing.get();
        }

        Profile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Barber barber = barberRepository.findById(barberId)
                .orElseThrow(() -> new RuntimeException("Barber not found"));

        Favorite favorite = new Favorite();
        favorite.setUser(profile);
        favorite.setBarber(barber);

        return favoriteRepository.save(favorite);
    }

    public void removeFavorite(Long favoriteId, String userId) {
        Favorite favorite = favoriteRepository.findById(favoriteId)
                .orElseThrow(() -> new RuntimeException("Favorite not found"));

        if (!favorite.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        favoriteRepository.delete(favorite);
    }
}
