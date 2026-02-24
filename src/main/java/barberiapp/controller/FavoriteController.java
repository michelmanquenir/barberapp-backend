package barberiapp.controller;

import barberiapp.model.Favorite;
import barberiapp.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @GetMapping
    public List<Favorite> getFavorites(@RequestParam String userId) {
        return favoriteService.getUserFavorites(userId);
    }

    @PostMapping("/{barberId}")
    public Favorite addFavorite(@RequestParam String userId, @PathVariable Long barberId) {
        return favoriteService.addFavorite(userId, barberId);
    }

    @DeleteMapping("/{id}")
    public void removeFavorite(@PathVariable Long id, @RequestParam String userId) {
        favoriteService.removeFavorite(id, userId);
    }
}
